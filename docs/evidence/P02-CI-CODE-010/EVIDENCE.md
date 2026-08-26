# Evidence for P02-CI-CODE-010

## Actions
- Created `.github/workflows/code-quality.yml` configured to trigger on `pull_request` and `push` to `develop`/`main`.
- Implemented ktlint via `ScaCap/action-ktlint`.
- Implemented detekt via `alauda/action-detekt`.
- Implemented WPCS via `reviewdog/action-phpcs` for WordPress Coding Standards.
- Implemented WordPress Plugin Check via `wordpress/plugin-check-action`.
- Configured all actions to use `reviewdog` with `reporter: github-pr-review` to strictly enforce "baseline debt جدا؛ کد جدید violation اضافه نکند". This ensures only changes introduced in the PR are checked and blocked, effectively ignoring old violations without needing physical baseline files to be maintained locally.

## Final Status
DONE
