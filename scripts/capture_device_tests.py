"""Capture native results without waiting on a legacy adb shell after completion."""
import argparse
from pathlib import Path
import subprocess
import threading


def capture(command, result, timeout_seconds, completion_grace=5):
    result.parent.mkdir(parents=True, exist_ok=True)
    process = subprocess.Popen(command, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                               text=True, encoding='utf-8', errors='replace', bufsize=1)
    expired = threading.Event()

    def expire():
        expired.set()
        if process.poll() is None:
            process.kill()

    deadline = threading.Timer(timeout_seconds, expire)
    deadline.daemon = True
    deadline.start()
    completed = False
    try:
        with result.open('w', encoding='utf-8') as output:
            for line in process.stdout:
                output.write(line)
                output.flush()
                print(line, end='', flush=True)
                if line.strip() == 'INSTRUMENTATION_CODE: -1':
                    completed = True
                    deadline.cancel()
                    break
        try:
            process.wait(timeout=completion_grace)
        except subprocess.TimeoutExpired:
            # Stop only the host client; the native runner has already reported its finish.
            process.terminate()
            try:
                process.wait(timeout=5)
            except subprocess.TimeoutExpired:
                process.kill()
                process.wait()
        # Completion alone is insufficient: verify_device_tests.py checks every case and summary.
        return 0 if completed and not expired.is_set() else 1
    finally:
        deadline.cancel()
        if process.poll() is None:
            process.kill()
            process.wait()
        process.stdout.close()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--result', type=Path, required=True)
    parser.add_argument('--timeout', type=float, default=480)
    args = parser.parse_args()
    return capture(['adb', 'shell', 'am', 'instrument', '-r', '-w',
                    'com.dayaonweb.quoter.test/androidx.test.runner.AndroidJUnitRunner'],
                   args.result, args.timeout)


if __name__ == '__main__':
    raise SystemExit(main())
