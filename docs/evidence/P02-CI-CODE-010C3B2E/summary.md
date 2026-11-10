# P02-CI-CODE-010C3B2E — bundled core DB triage

Date: 2026-11-10. The pinned shipped Plugin Check report SHA-256 is `50f0c694447c4b9aab40b1d66c4091cecdad0614ba18c8f70208c3f2b36fd0bd`. `node docs/evidence/P02-CI-CODE-010C3B2E/build-ledger.cjs` produced 12 exact findings: 11 importer queries/updates use fixed-prefix registry identifiers and `wpdb->prepare`, `wpdb->insert`, or `wpdb->delete`; one wallet insert has a genuine payment integrity risk. The prior WPCS root did not scan bundled core, so there are zero overlapping WPCS entries. No absence is treated as approval.

The wallet's `wpdb->insert` itself formats values, but `deduct_funds` performs non-atomic balance read/modify/write and ignores the later ledger insert result. The WooCommerce gateway is registered by `WooCompat` and its enabled setting defaults to `yes`; checkout can report success after an unlogged or concurrent incorrect debit. This is assigned to P0 `P02-SEC-CODE-010C3B2E1` to disable the gateway pending a full atomic wallet design. This review does not mark wallet safe or silently defer it to a later phase.

Checks: ledger rebuild printed `findings=12 importerContext=11 walletFix=1 priorWpcs=0` (exit 0); PHP lint passed for both source files. No product code changed in this review.
