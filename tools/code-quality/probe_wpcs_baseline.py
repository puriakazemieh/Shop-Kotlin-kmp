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
    ledger_path = pathlib.Path(sys.argv[3])
    checker = pathlib.Path(__file__).with_name("check_wpcs_baseline.py")
    report = json.loads(report_path.read_text(encoding="utf-8"))

    def run(path, mode, ledger=ledger_path):
        return subprocess.run(
            [sys.executable, str(checker), "--report", str(path), "--baseline", str(baseline_path),
             "--mode", mode, "--security-ledger", str(ledger)],
            capture_output=True, text=True, check=False,
        )

    clean = run(report_path, "style")
    strict = run(report_path, "strict")
    phase = run(report_path, "phase")
    first_file = next(iter(report["files"].values()))
    first_file["messages"].append({
        "line": 1, "column": 1, "source": "Generic.Files.LineLength.TooLong",
        "message": "Synthetic new style violation", "type": "ERROR",
    })
    with tempfile.TemporaryDirectory() as temporary:
        mutated = pathlib.Path(temporary) / "synthetic-report.json"
        mutated.write_text(json.dumps(report), encoding="utf-8")
        violation = run(mutated, "phase")
        first_file["messages"].pop()
        first_file["messages"].append({
            "line": 2, "column": 1, "source": "WordPress.Security.NonceVerification.Missing",
            "message": "Synthetic new Security violation", "type": "ERROR",
        })
        mutated.write_text(json.dumps(report), encoding="utf-8")
        security_violation = run(mutated, "phase")
        ledger = json.loads(ledger_path.read_text(encoding="utf-8"))
        ledger["securityDbFindings"][0]["evidence"] = "docs/evidence/missing-review.md"
        invalid_ledger = pathlib.Path(temporary) / "invalid-ledger.json"
        invalid_ledger.write_text(json.dumps(ledger), encoding="utf-8")
        invalid = run(report_path, "phase", invalid_ledger)

    checks = (
        ("existing_style_baseline_clean", clean.returncode == 0 and "new=0" in clean.stdout),
        ("security_db_remains_blocking", strict.returncode == 1 and "Security/DB current=" in strict.stdout),
        ("reviewed_phase_passes", phase.returncode == 0 and "unreviewed=0" in phase.stdout),
        ("synthetic_new_style_fails", violation.returncode == 1 and "Synthetic new style violation" in violation.stdout),
        ("synthetic_new_security_fails", security_violation.returncode == 1 and "Synthetic new Security violation" in security_violation.stdout),
        ("missing_review_evidence_fails", invalid.returncode == 2 and "missing evidence" in invalid.stderr),
    )
    for name, ok in checks:
        print(("PASS " if ok else "FAIL ") + name)
    print(clean.stdout.strip())
    print(strict.stdout.strip())
    print(phase.stdout.strip())
    print(violation.stdout.strip().splitlines()[0])
    print(security_violation.stdout.strip().splitlines()[0])
    return 0 if all(ok for _, ok in checks) else 1


if __name__ == "__main__":
    sys.exit(main())
