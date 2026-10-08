"""Path-routing regressions: cheap edits skip Android, runtime/build edits do not."""
import importlib.util
import json
from pathlib import Path
import unittest

spec = importlib.util.spec_from_file_location('changes', Path(__file__).with_name('ci-changes.py'))
changes = importlib.util.module_from_spec(spec)
spec.loader.exec_module(changes)
config = json.loads((Path(__file__).resolve().parents[1] / '.github/ci-paths.json').read_text())


class Routing(unittest.TestCase):
    def test_documentation_media_and_publishing_do_not_build_android(self):
        self.assertFalse(changes.android_changed(['README.md', 'AGENTS.md', 'audit/DESIGN-DIRECTION.md',
            'play-store/listing.next.json', 'screenshots/frames/phone-01.svg', 'scripts/release.py'], config))

    def test_app_and_toolchain_changes_build_android(self):
        for path in ['app/src/main/AndroidManifest.xml', 'app/src/main/java/example/Reader.kt',
                     'app/src/main/res/drawable/vec_share.xml', 'app/src/main/assets/quotes.json',
                     'app/src/androidTest/Test.kt', 'gradle/libs.versions.toml',
                     '.github/workflows/android.yml', 'scripts/ci-changes.py']:
            with self.subTest(path=path):
                self.assertTrue(changes.android_changed([path], config))

    def test_icon_sources_skip_until_generated_resources_change(self):
        self.assertFalse(changes.android_changed(['app/src/main/assets/icons/share.svg'], config))
        self.assertTrue(changes.android_changed(['app/src/main/assets/icons/share.svg',
            'app/src/main/res/drawable/vec_share.xml'], config))
