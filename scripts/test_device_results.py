import unittest
from verify_device_tests import parse_results, validate


def result(codes, summary='OK (2 tests)', terminated=True):
    lines = []
    for index, code in enumerate(codes):
        for status in (1, code):
            lines.extend([f'INSTRUMENTATION_STATUS: class=example.ReaderTest',
                          f'INSTRUMENTATION_STATUS: test=case{index}', f'INSTRUMENTATION_STATUS_CODE: {status}'])
    lines.append(summary)
    if terminated:
        lines.append('INSTRUMENTATION_CODE: -1')
    return '\n'.join(lines)


class DeviceResultsTest(unittest.TestCase):
    def test_accepts_completed_run_and_declared_window_skip(self):
        text = result([0, -4])
        validate(text, parse_results(text), 2)

    def test_rejects_empty_success_report(self):
        text = result([], 'OK (0 tests)')
        with self.assertRaises(ValueError):
            validate(text, parse_results(text), 2)

    def test_rejects_partial_discovery(self):
        text = result([0], 'OK (1 test)')
        with self.assertRaises(ValueError):
            validate(text, parse_results(text), 2)

    def test_rejects_failure_even_with_success_summary(self):
        text = result([0, -2])
        with self.assertRaises(ValueError):
            validate(text, parse_results(text), 2)

    def test_rejects_crashed_runner_after_cases(self):
        text = result([0, 0], terminated=False)
        with self.assertRaises(ValueError):
            validate(text, parse_results(text), 2)

    def test_rejects_failure_summary(self):
        text = result([0, 0], 'FAILURES!!!')
        with self.assertRaises(ValueError):
            validate(text, parse_results(text), 2)
