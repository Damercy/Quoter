import importlib.util
import json
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch

spec = importlib.util.spec_from_file_location('release', Path(__file__).with_name('release.py'))
release = importlib.util.module_from_spec(spec)
spec.loader.exec_module(release)


class ReleaseTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.notes = self.root / 'CHANGELOG.md'
        self.notes.write_text('Reviewed release notes')
        self.manifest = self.root / 'release.json'
        self.manifest.write_text('{}')
        self.plan = {'repo': release.REPO, 'head': 'old', 'remoteHead': 'remote',
                     'tag': 'v4.00.00', 'sources': {'CHANGELOG.md': release.file_hash(self.notes)},
                     'removeTrackedKeys': [], 'assets': {},
                     'manifest': str(self.manifest), 'manifestHash': release.file_hash(self.manifest),
                     'notes': str(self.notes), 'notesHash': release.file_hash(self.notes)}
        self.planpath = self.root / 'plan.json'
        self.planpath.write_text(json.dumps(self.plan))
        self.args = type('Args', (), {'plan': self.planpath, 'approval': release.digest(self.plan)})()
        self.calls = []
        self.head = 'old'
        self.fail_play = False
        self.root_patch = patch.object(release, 'ROOT', self.root)
        self.root_patch.start()
        self.addCleanup(self.root_patch.stop)

    def fake_run(self, *args, **kwargs):
        args = tuple(str(x) for x in args)
        self.calls.append(args)
        if args[:3] == ('git', 'rev-parse', 'HEAD'):
            return self.head
        if args[:2] == ('git', 'commit'):
            self.head = 'new'
        if len(args) > 1 and 'play-store.py' in args[1]:
            if self.fail_play:
                raise RuntimeError('Play request interrupted')
        return ''

    def invoke(self):
        not_found = type('Result', (), {'returncode': 1, 'stdout': ''})()
        with patch.object(release, 'run', side_effect=self.fake_run), \
             patch.object(release, 'check_connection'), \
             patch.object(release, 'remote_head', return_value='remote'), \
             patch.object(release.subprocess, 'run', return_value=not_found):
            release.execute(self.args)

    def test_wrong_approval_performs_no_mutation(self):
        self.args.approval = 'not-approved'
        with self.assertRaisesRegex(ValueError, 'Approval digest'):
            self.invoke()
        self.assertEqual([], self.calls)

    def test_changed_reviewed_source_performs_no_mutation(self):
        self.notes.write_text('An unreviewed change')
        with self.assertRaisesRegex(ValueError, 'source changed'):
            self.invoke()
        self.assertEqual([], self.calls)

    def test_play_submission_precedes_github_publication(self):
        self.invoke()
        play_index = next(i for i, call in enumerate(self.calls) if 'play-store.py' in ' '.join(call))
        publish_index = next(i for i, call in enumerate(self.calls) if '--draft=false' in call)
        self.assertLess(play_index, publish_index)
        push = next(call for call in self.calls if call[:2] == ('git', 'push'))
        self.assertIn('--atomic', push)
        self.assertNotIn('--force', push)

    def test_uncertain_play_stops_publication_and_repeat_upload(self):
        self.fail_play = True
        with self.assertRaisesRegex(RuntimeError, 'interrupted'):
            self.invoke()
        self.assertFalse(any('--draft=false' in call for call in self.calls))
        self.calls.clear()
        with self.assertRaisesRegex(ValueError, 'uncertain outcome'):
            self.invoke()
        self.assertFalse(any('play-store.py' in ' '.join(call) for call in self.calls))

    def test_changed_bundle_prevents_commit(self):
        bundle = self.root / 'release.aab'
        bundle.write_bytes(b'old')
        self.plan['assets'][str(bundle)] = release.file_hash(bundle)
        self.planpath.write_text(json.dumps(self.plan))
        self.args.approval = release.digest(self.plan)
        bundle.write_bytes(b'changed')
        with self.assertRaisesRegex(ValueError, 'asset changed'):
            self.invoke()
        self.assertEqual([], self.calls)

    def test_github_only_never_invokes_play(self):
        self.plan['destinations'] = ['github']
        self.planpath.write_text(json.dumps(self.plan))
        self.args.approval = release.digest(self.plan)
        self.invoke()
        self.assertTrue(any('--draft=false' in call for call in self.calls))
        self.assertFalse(any('play-store.py' in ' '.join(call) for call in self.calls))


if __name__ == '__main__':
    unittest.main()
