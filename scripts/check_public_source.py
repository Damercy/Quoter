"""Check Git's index without printing credential values. Run before every public push."""
import re
import subprocess
import sys

paths = subprocess.check_output(["git", "ls-files", "-z"]).decode().split("\0")
blocked = re.compile(r"(^|/)(google-services\.json|keystore\.properties|\.env(?:\..*)?|local\.properties|tester-tracker\.csv)$|\.(jks|keystore|pem|p12|pfx|hprof|perfetto-trace)$", re.I)
patterns = {
    "private key": re.compile(rb"-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY"),
    "GitHub token": re.compile(rb"(?:gh[pousr]_[A-Za-z0-9]{20,}|github_pat_[A-Za-z0-9_]{30,})"),
    "Google API key": re.compile(rb"AIza[0-9A-Za-z_-]{30,}"),
    "OpenAI key": re.compile(rb"sk-(?:proj-|svcacct-)?[A-Za-z0-9_-]{30,}"),
    "personal Windows path": re.compile(rb"C:[\\/]Users[\\/](?!Public|<|USER|username|your-user)[^\\/\s]+", re.I),
    "personal email": re.compile(rb"[A-Za-z0-9._%+-]+@(?:gmail|outlook|hotmail|yahoo)\.com", re.I),
}
failures = []
for path in filter(None, paths):
    if path.split("/")[-1] == "AGENTS.md" or any(part in {"docs", "audit", ".scratch", "poc"} for part in path.lower().split("/")[:-1]):
        failures.append(f"{path}: documentation directory must remain private")
    if blocked.search(path) and not path.endswith(".env.example"):
        failures.append(f"{path}: excluded configuration or private artifact")
    content = subprocess.check_output(["git", "show", f":{path}"])
    if b"\0" in content[:8192]:
        continue
    for label, pattern in patterns.items():
        if pattern.search(content):
            failures.append(f"{path}: {label}")
if failures:
    print("Public source check failed (values withheld):\n" + "\n".join(failures))
    sys.exit(1)
print(f"Public source check passed: {len(list(filter(None, paths)))} tracked files.")
