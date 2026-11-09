#!/usr/bin/env python3
"""Compare exact WPCS style and reviewed Security/DB findings with a PHPCS report."""

import argparse
import collections
import json
import pathlib
import sys


def entries(report):
    style = collections.Counter()
    security_db = collections.Counter()
    for raw_path, file_report in report["files"].items():
        path = raw_path.replace("\\", "/")
        marker = "/wordpress/carmilla-bridge/"
        if marker not in path:
            raise ValueError(f"Unexpected PHPCS path: {raw_path}")
        path = "wordpress/carmilla-bridge/" + path.split(marker, 1)[1]
        for message in file_report["messages"]:
            finding = (
                path,
                int(message["line"]),
                int(message["column"]),
                message["source"],
                message["message"],
                message["type"],
            )
            target = security_db if finding[3].startswith(("WordPress.Security.", "WordPress.DB.")) else style
            target[finding] += 1
    return style, security_db


def to_records(counter):
    return [
        dict(zip(("path", "line", "column", "source", "message", "type"), key), count=count)
        for key, count in sorted(counter.items())
    ]


def from_records(records):
    result = collections.Counter()
    for row in records:
        key = tuple(row[field] for field in ("path", "line", "column", "source", "message", "type"))
        if key[3].startswith(("WordPress.Security.", "WordPress.DB.")):
            raise ValueError("Security/DB finding must not enter the style baseline")
        if not isinstance(row["count"], int) or row["count"] < 1:
            raise ValueError("Invalid baseline count")
        result[key] += row["count"]
    return result


def reviewed_security_records(ledger, repository_root):
    if (ledger.get("schema"), ledger.get("standard"), ledger.get("wpcsVersion"), ledger.get("phpcsVersion")) != (1, "WordPress", "3.4.1", "3.13.6"):
        raise ValueError("Unsupported Security/DB review metadata")
    reviewed = collections.Counter()
    for row in ledger["securityDbFindings"]:
        key = tuple(row[field] for field in ("path", "line", "column", "source", "message", "type"))
        if not key[3].startswith(("WordPress.Security.", "WordPress.DB.")):
            raise ValueError("Non-Security/DB finding in review ledger")
        if row.get("count") != 1 or reviewed[key]:
            raise ValueError("Duplicate or invalid Security/DB review entry")
        if row.get("disposition") not in ("context_justified", "deferred") or len(row.get("reason", "")) < 30:
            raise ValueError("Security/DB entry lacks a reviewed disposition and reason")
        evidence = row.get("evidence", "")
        if not evidence.startswith("docs/evidence/") or not (repository_root / evidence).is_file():
            raise ValueError(f"Security/DB entry has missing evidence: {evidence}")
        target = row.get("targetTask")
        if row["disposition"] == "deferred":
            if not isinstance(target, str) or not target.startswith(("P03-", "P04-", "P05-", "P06-", "P07-", "P08-", "P09-", "P10-", "P11-", "P12-", "P13-", "P14-", "P15-", "P16-", "P17-", "P18-")) or not (repository_root / "docs/tasks" / f"{target}.md").is_file():
                raise ValueError(f"Deferred Security/DB entry lacks a future-phase task: {target}")
        elif target is not None:
            raise ValueError("Context-justified Security/DB entry must not have a target task")
        reviewed[key] = 1
    return reviewed


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--report", type=pathlib.Path, required=True)
    parser.add_argument("--baseline", type=pathlib.Path, required=True)
    parser.add_argument("--write-baseline", action="store_true")
    parser.add_argument("--mode", choices=("style", "strict", "phase"), default="strict")
    parser.add_argument("--security-ledger", type=pathlib.Path, help="Exact reviewed Security/DB dispositions for phase mode")
    parser.add_argument("--revision", help="Git source revision when creating a baseline")
    args = parser.parse_args()

    report = json.loads(args.report.read_text(encoding="utf-8"))
    style, security_db = entries(report)
    if args.write_baseline:
        if not args.revision:
            parser.error("--revision is required with --write-baseline")
        baseline = {
            "schema": 1,
            "sourceRevision": args.revision,
            "standard": "WordPress",
            "wpcsVersion": "3.4.1",
            "phpcsVersion": "3.13.6",
            "scanRoot": "wordpress/carmilla-bridge",
            "ignored": ["*/vendor/*", "*/node_modules/*", "*/tests/*"],
            "styleFindings": to_records(style),
        }
        args.baseline.parent.mkdir(parents=True, exist_ok=True)
        args.baseline.write_text(json.dumps(baseline, ensure_ascii=False, separators=(",", ":")) + "\n", encoding="utf-8")
        print(f"Wrote {sum(style.values())} exact style findings; Security/DB excluded: {sum(security_db.values())}")
        return 0

    baseline = json.loads(args.baseline.read_text(encoding="utf-8"))
    if baseline.get("schema") != 1 or baseline.get("standard") != "WordPress":
        raise ValueError("Unsupported WPCS baseline metadata")
    known = from_records(baseline["styleFindings"])
    added = style - known
    removed = known - style
    unreviewed = collections.Counter()
    resolved_security = collections.Counter()
    if args.mode == "phase":
        if not args.security_ledger:
            parser.error("--security-ledger is required in phase mode")
        repository_root = pathlib.Path(__file__).resolve().parents[2]
        ledger = json.loads(args.security_ledger.read_text(encoding="utf-8"))
        reviewed = reviewed_security_records(ledger, repository_root)
        unreviewed = security_db - reviewed
        resolved_security = reviewed - security_db
    print(
        f"Style current={sum(style.values())} baseline={sum(known.values())} "
        f"new={sum(added.values())} resolved={sum(removed.values())}; "
        f"Security/DB current={sum(security_db.values())} unreviewed={sum(unreviewed.values())} "
        f"resolved={sum(resolved_security.values())}; mode={args.mode}"
    )
    for finding, count in sorted(added.items())[:20]:
        print(f"NEW {count}x {finding[0]}:{finding[1]}:{finding[2]} {finding[3]} {finding[4]}")
    if len(added) > 20:
        print(f"... {len(added) - 20} additional distinct new style findings")
    for finding, count in sorted(unreviewed.items())[:20]:
        print(f"UNREVIEWED {count}x {finding[0]}:{finding[1]}:{finding[2]} {finding[3]} {finding[4]}")
    if len(unreviewed) > 20:
        print(f"... {len(unreviewed) - 20} additional distinct unreviewed Security/DB findings")
    if added or unreviewed or (args.mode == "strict" and security_db):
        return 1
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (KeyError, TypeError, ValueError, json.JSONDecodeError) as error:
        print(f"Invalid WPCS input: {error}", file=sys.stderr)
        sys.exit(2)
