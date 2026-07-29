# Repository State Baseline

> Generated: 2026-07-29T14:43:17.011399700+03:30
> Task ID: P00-PROGRAM-DISC-001

## 1. Resolved Git Roots

| Component | Path |
|---|---|
| KMP Client / WordPress | `D:\Android\AndroidStudioProjects\kmp-shop` |
| Spring Boot Server | `D:\Android\AndroidStudioProjects\ShopServer\Shop` |

## 2. Main Repository (kmp-shop)

- **Branch**: `develop`
- **HEAD**: `659c711dbcd73a33f1db8ee56fa8a55b8f9602dc`
- **Last Commit Message**: `Update platform network config and add comprehensive project documentation`
- **Git Status**:
```text
On branch develop
Your branch is up to date with 'origin/develop'.

nothing to commit, working tree clean
```

### Top-level Modules (Gradle)
- `:composeApp`
- `:core:designSystem`
- `:core:domain`
- `:core:data`
- `:core:network`
- `:core:navigation`
- `:core:common`
- `:feature:auth`
- `:feature:main`
- `:feature:cart`
- `:feature:catalog`
- `:feature:blog`
- `:feature:settings`
- `:feature:profile`
- `:feature:orders`
- `:feature:details`
- `:feature:support`
- `:feature:academy`
- `:feature:clinic`
- `:feature:psychtest`
- `:feature:comparison`
- `:feature:admin:products`
- `:feature:admin:orders`
- `:feature:admin:options`
- `:feature:admin:wallet`
- `:feature:admin:blog`
- `:feature:admin:academy`
- `:feature:admin:clinic`
- `:feature:admin:psychtest`

### Workflows (.github/workflows)
- `build-all.yml`
- `deploy-web.yml`
- `design-compare.yml`
- `screenshot-url.yml`
- `serve-live-wp.yml`
- `serve-live.yml`
- `wordpress-package.yml`

## 3. Server Repository (ShopServer/Shop)

- **Branch**: `develop`
- **HEAD**: `bde7769fa64aaae42f02e8d63ce973d408d32590`
- **Last Commit Message**: `Merge pull request #8 from puriakazemieh/claude/wordpress-plugin-theme-plan-ruouji`
- **Git Status**:
```text
On branch develop
Your branch is up to date with 'origin/develop'.

nothing to commit, working tree clean
```

### Workflows (.github/workflows)
- `build-jar.yml`
- `run-server.yml`

## 4. Instruction Files Audit

| File | Status | Notes |
|---|---|---|
| `AGENTS.md` | PRESENT | Added AI agent instructions |
| `RTK.md` | PRESENT | Added technical guardrails |

## 5. Unknowns & Questions

1. **Instruction Files**: `AGENTS.md` and `RTK.md` are now present in the root directory.
2. **Release Artifacts**: Are there any manually managed release artifacts (ZIPs, AABs) that are not tracked in Git but should be part of the baseline?
3. **Target Store Accounts**: Which Google Play / Apple Developer accounts are intended for the final release?
4. **PHP Environment**: Docker is available, but the daemon is currently unreachable. A `tools/test-env/docker-compose.yml` has been added for future use.

---

## Baseline Verification
- [x] `git status` recorded (clean for both)
- [x] Branch and HEAD recorded
- [x] Module inventory complete
- [x] Instruction files audited
