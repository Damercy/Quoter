"""Check release guards without uploading or committing anything to Play."""
import contextlib
import importlib.util
import io
import json
import tempfile
import unittest
import zipfile
from pathlib import Path
from unittest.mock import MagicMock, patch

spec = importlib.util.spec_from_file_location("play_store", Path(__file__).with_name("play-store.py"))
play = importlib.util.module_from_spec(spec)
spec.loader.exec_module(play)


class ReleaseGuards(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.manifest = Path(self.temp.name) / "release.json"
        self.data = {"packageName": play.PACKAGE, "listings": {"en-GB": {"title": "Quoter"}}}

    def write(self):
        self.manifest.write_text(json.dumps(self.data), encoding="utf-8")

    def test_other_package_rejected(self):
        self.data["packageName"] = "com.stackapp.stack"
        self.write()
        with self.assertRaises(ValueError):
            play.load_plan(self.manifest)

    def test_unsigned_bundle_rejected(self):
        with zipfile.ZipFile(self.manifest.parent / "unsigned.aab", "w") as bundle:
            bundle.writestr("BundleConfig.pb", b"test")
        self.data.update(bundle="unsigned.aab", release={"track": "internal", "status": "draft", "expectedVersionCode": 40000})
        self.write()
        with self.assertRaisesRegex(ValueError, "unsigned"):
            play.load_plan(self.manifest)

    def run_command(self, commit=False, fail=False):
        self.write()
        service = MagicMock()
        edits = service.edits.return_value
        edits.insert.return_value.execute.return_value = {"id": "temporary-edit"}
        argv = ["play-store.py", "apply", "--manifest", str(self.manifest)]
        if commit:
            argv.append("--commit")
        with patch.object(play, "client", return_value=service), patch.object(play, "export"), patch.object(
            play, "apply", side_effect=ValueError("Validation failed") if fail else None
        ), patch("sys.argv", argv), contextlib.redirect_stdout(io.StringIO()):
            if fail:
                with self.assertRaises(ValueError):
                    play.main()
            else:
                play.main()
        return edits

    def test_default_discards_edit_without_commit(self):
        edits = self.run_command()
        edits.commit.assert_not_called()
        edits.delete.assert_called_once_with(packageName=play.PACKAGE, editId="temporary-edit")

    def test_failure_discards_edit_even_when_commit_requested(self):
        edits = self.run_command(commit=True, fail=True)
        edits.commit.assert_not_called()
        edits.delete.assert_called_once()

    def test_commit_does_not_cancel_existing_review(self):
        edits = self.run_command(commit=True)
        edits.commit.assert_called_once_with(packageName=play.PACKAGE, editId="temporary-edit", changesInReviewBehavior="ERROR_IF_IN_REVIEW")
        edits.delete.assert_not_called()

    def test_resumable_upload_returns_final_server_response(self):
        request = MagicMock()
        status = MagicMock()
        status.progress.return_value = .5
        request.next_chunk.side_effect = [(status, None), (None, {'versionCode': 40000})]
        with contextlib.redirect_stdout(io.StringIO()):
            self.assertEqual(play.upload_chunks(request, 'Bundle'), {'versionCode': 40000})
        self.assertEqual(request.next_chunk.call_count, 2)
        request.next_chunk.assert_called_with(num_retries=3)


if __name__ == "__main__":
    unittest.main()
