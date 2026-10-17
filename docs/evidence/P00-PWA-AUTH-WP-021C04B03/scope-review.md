# P00-PWA-AUTH-WP-021C04B03 — scope review

Date: 2026-10-17. No implementation or database change was made in this review. The B02 contract selects a research candidate, not a demonstrated safe production design.

The complete isolated proof requires atomic same-owner multi-reference management and conflict/failure handling across user INSERT, UPDATE and DELETE, plus separate usermeta INSERT, UPDATE and DELETE, coupled with real WordPress and direct-SQL race testing. The implementation also needs to reason about reassignments and rollback while preserving existing user records. Bundling all of that into one M card would obscure independent failure modes and acceptance evidence.

The proposed sequence is B03A (synthetic user INSERT only), B03B (synthetic user UPDATE/DELETE), B03C (synthetic `cb_mobile` operations), and B03D (combined proof and feasibility gate). All use synthetic fixtures only. The ASCII/binary experiment is not an approved real-world canonicalization; shared hosting, existing extensions, multisite, trigger privileges, deployment trust and owner migration QA remain unresolved. B03, registration, C08 and the broader P00 gate remain incomplete.
