#!/usr/bin/env python3
"""Compare an exact, version-pinned WPCS style baseline with a PHPCS JSON report."""

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


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--report", type=pathlib.Path, required=True)
    parser.add_argument("--baseline", type=pathlib.Path, required=True)
    parser.add_argument("--write-baseline", action="store_true")
    parser.add_argument("--mode", choices=("style", "strict"), default="strict")
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
    print(
        f"Style current={sum(style.values())} baseline={sum(known.values())} "
        f"new={sum(added.values())} resolved={sum(removed.values())}; "
        f"Security/DB unresolved={sum(security_db.values())}; mode={args.mode}"
    )
    for finding, count in sorted(added.items())[:20]:
        print(f"NEW {count}x {finding[0]}:{finding[1]}:{finding[2]} {finding[3]} {finding[4]}")
    if len(added) > 20:
        print(f"... {len(added) - 20} additional distinct new style findings")
    if added or (args.mode == "strict" and security_db):
        return 1
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (KeyError, TypeError, ValueError, json.JSONDecodeError) as error:
        print(f"Invalid WPCS input: {error}", file=sys.stderr)
        sys.exit(2)
