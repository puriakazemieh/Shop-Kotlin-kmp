#!/usr/bin/env python3
"""Exercise the WPCS baseline gate on a real report and a synthetic new finding."""

import json
import pathlib
import subprocess
import sys
import tempfile


def main():
    report_path = pathlib.Path(sys.argv[1])
    baseline_path = pathlib.Path(sys.argv[2])
    checker = pathlib.Path(__file__).with_name("check_wpcs_baseline.py")
    report = json.loads(report_path.read_text(encoding="utf-8"))

    def run(path, mode):
        return subprocess.run(
            [sys.executable, str(checker), "--report", str(path), "--baseline", str(baseline_path), "--mode", mode],
            capture_output=True, text=True, check=False,
        )

    clean = run(report_path, "style")
    strict = run(report_path, "strict")
    first_file = next(iter(report["files"].values()))
    first_file["messages"].append({
        "line": 1, "column": 1, "source": "Generic.Files.LineLength.TooLong",
        "message": "Synthetic new style violation", "type": "ERROR",
    })
    with tempfile.TemporaryDirectory() as temporary:
        mutated = pathlib.Path(temporary) / "synthetic-report.json"
        mutated.write_text(json.dumps(report), encoding="utf-8")
        violation = run(mutated, "style")

    checks = (
        ("existing_style_baseline_clean", clean.returncode == 0 and "new=0" in clean.stdout),
        ("security_db_remains_blocking", strict.returncode == 1 and "Security/DB unresolved=" in strict.stdout),
        ("synthetic_new_style_fails", violation.returncode == 1 and "Synthetic new style violation" in violation.stdout),
    )
    for name, ok in checks:
        print(("PASS " if ok else "FAIL ") + name)
    print(clean.stdout.strip())
    print(strict.stdout.strip())
    print(violation.stdout.strip().splitlines()[0])
    return 0 if all(ok for _, ok in checks) else 1


if __name__ == "__main__":
    sys.exit(main())
