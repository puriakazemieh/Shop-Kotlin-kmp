# Captures local Codex session token metadata; NEVER commit its output.
[CmdletBinding()]
param(
    [ValidateSet('baseline', 'after')][string]$Label = 'baseline',
    [string]$OutputDirectory
)
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
if (-not $OutputDirectory) {
    $OutputDirectory = Join-Path $repoRoot '.artifacts\codex-usage'
}
if (-not (Get-Command npx -ErrorAction SilentlyContinue)) {
    throw 'npx not found. Install Node.js first.'
}
New-Item -ItemType Directory -Force -Path $OutputDirectory | Out-Null
$stamp = (Get-Date).ToUniversalTime().ToString('yyyyMMddTHHmmssZ')
$filePath = Join-Path $OutputDirectory ("{0}-{1}-sessions.json" -f $stamp, $Label)

Write-Host "Reading existing Codex session logs through ccusage..."
$lines = & npx --yes ccusage@latest codex session --json --no-cost
if ($LASTEXITCODE -ne 0) {
    throw "ccusage failed with exit code $LASTEXITCODE. No baseline was recorded."
}
$payload = [string]::Join([Environment]::NewLine, [string[]]$lines)
if ([string]::IsNullOrWhiteSpace($payload)) {
    throw 'ccusage returned no JSON; cannot record a measurement.'
}
$null = $payload | ConvertFrom-Json -ErrorAction Stop
[System.IO.File]::WriteAllText($filePath, $payload, (New-Object System.Text.UTF8Encoding($false)))

Write-Host "Saved local-only session report: $filePath"
Write-Warning 'This is a snapshot of locally retained Codex CLI sessions, not a repo-scoped or account-wide token baseline. Compare matched NEW sessions after setup; do not subtract entire cumulative reports as if they were per-task values.'
