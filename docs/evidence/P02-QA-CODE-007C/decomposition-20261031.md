# P02-QA-CODE-007C size gate — 2026-10-31

The card was labelled M but combines 19 WordPress/Woo smokes and four DB-only smokes from the 007A inventory. They use different WordPress roots (`/var/www/html`, `/usr/src/wordpress`), database names, opt-in guards and mail/OTP or migration side effects. One implementation and one CI result cannot safely characterize all of those boundaries as a single M task.

The owner authorized ordered splitting and continuation on 2026-10-31. C1 covers two WP CLI commerce tests; C2 covers four installed-root login tests; C3 covers six identity/incident tests; C4 covers four shared-root login/route tests; C5 covers three token/adapter tests; C6 covers four DB-only tests. Each child has a bounded fixture, acceptance, CI evidence and cleanup obligation. C1 is first READY, each successor depends on its predecessor, and 007C remains TODO until the six children are proven. No smoke test is claimed passed by this decomposition.

The original parent 007 still depends on 007C. Its phase gate and later cards are unchanged. Existing user-owned working-tree files are excluded from commits.
