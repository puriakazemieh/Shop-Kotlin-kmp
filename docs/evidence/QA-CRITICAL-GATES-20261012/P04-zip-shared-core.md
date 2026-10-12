# P04 — rebuilt Theme and Bridge ZIPs

Date: 2026-10-12

## Build

The checked-in Bash package scripts initially could not run in this Windows environment because the WSL runtime did not provide the `zip` executable. Their temporary `packages` directories were moved outside the repository to `D:\Carmilla-QA\failed-zip-staging-20261012`.

On 2026-10-12, `zip` and `unzip` were installed in the existing Ubuntu WSL environment. Both artifacts were then rebuilt successfully by the checked-in `wordpress/build-theme-zip.sh` and `wordpress/build-bridge-zip.sh` scripts.

## Verification

| Artifact | Entries | Shared Core files | SHA-256 |
| --- | ---: | ---: | --- |
| `wordpress/carmilla-theme.zip` | 137 | 20/20, exact source hash match | `8AD7DE8EAEA85A15492549F9B3BCB7A456056D238F18D9F47EAB030500669127` |
| `wordpress/carmilla-bridge.zip` | 106 | 20/20, exact source hash match | `2F1BBD2108BF6BCCBE48F2F7642286D7C0F2AD258B06A6AEBD4DD6E6C91F310E` |

The validator confirmed that both archives contain `packages/carmilla-core/Carmilla_Kernel.php`, no Shared Core file is missing, changed, or unexpected, and no `.git`, `.env`, log, `node_modules`, or `vendor` entry is present.

## Related validation

Docker PHP 8.1 lint passed for every PHP file below `wordpress/` before packaging.

## Limitation

This evidence verifies artifact contents and syntax. Independent clean-install and co-install behavior remain separate P04 manual/Gate checks.
