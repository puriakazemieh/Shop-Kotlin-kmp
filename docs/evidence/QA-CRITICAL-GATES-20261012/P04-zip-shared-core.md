# P04 — rebuilt Theme and Bridge ZIPs

Date: 2026-10-12

## Build

The checked-in Bash package scripts could not run in this Windows environment because the available Bash runtime does not provide the `zip` executable. The scripts created no ZIP and their temporary `packages` directories were moved outside the repository to `D:\Carmilla-QA\failed-zip-staging-20261012`.

Both artifacts were then rebuilt from the current repository source with the native Windows ZIP runtime. For each artifact, the build staging copy received the current `wordpress/packages/carmilla-core` directory at `packages/carmilla-core`; the repository source directories themselves remained unchanged.

## Verification

| Artifact | Entries | Shared Core files | SHA-256 |
| --- | ---: | ---: | --- |
| `wordpress/carmilla-theme.zip` | 107 | 20/20, exact source hash match | `54AA4807FAC2C919680DB515F39BD606713C6A4813B8D612320F2788BB9B7ADC` |
| `wordpress/carmilla-bridge.zip` | 85 | 20/20, exact source hash match | `37EB6D5946BC3C0C2904FF6A2F245C8401433B6E24F4C30FEF14E1C1ADD87D0C` |

The validator confirmed that both archives contain `packages/carmilla-core/Carmilla_Kernel.php`, no Shared Core file is missing, changed, or unexpected, and no `.git`, `.env`, log, `node_modules`, or `vendor` entry is present.

## Related validation

Docker PHP 8.1 lint passed for every PHP file below `wordpress/` before packaging.

## Limitation

This evidence verifies artifact contents and syntax. Independent clean-install and co-install behavior remain separate P04 manual/Gate checks.
