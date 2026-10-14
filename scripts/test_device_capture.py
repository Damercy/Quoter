import contextlib
import io
from pathlib import Path
import sys
import tempfile
import unittest

from capture_device_tests import capture
from verify_device_tests import parse_results, validate


class DeviceCaptureTest(unittest.TestCase):
    def run_child(self, script, timeout=5):
        with tempfile.TemporaryDirectory() as folder, contextlib.redirect_stdout(io.StringIO()):
            result = Path(folder) / 'instrumentation.txt'
            status = capture([sys.executable, '-c', script], result, timeout, completion_grace=.1)
            return status, result.read_text()

    def test_completed_runner_does_not_wait_on_lingering_client(self):
        status, text = self.run_child("import time; print('INSTRUMENTATION_CODE: -1', flush=True); time.sleep(60)")
        self.assertEqual(0, status)
        self.assertIn('INSTRUMENTATION_CODE: -1', text)

    def test_zero_exit_without_native_completion_is_rejected(self):
        status, _ = self.run_child("print('runner disconnected')")
        self.assertEqual(1, status)

    def test_silent_client_is_bounded(self):
        status, _ = self.run_child('import time; time.sleep(60)', timeout=.5)
        self.assertEqual(1, status)

    def test_completion_without_cases_still_fails_result_validation(self):
        status, text = self.run_child("print('INSTRUMENTATION_CODE: -1')")
        self.assertEqual(0, status)
        with self.assertRaises(ValueError):
            validate(text, parse_results(text), 1)
