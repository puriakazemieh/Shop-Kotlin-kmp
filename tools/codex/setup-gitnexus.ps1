# Index two separate repositories and configure a single global Codex MCP.
[CmdletBinding()]
param(
    [string]$ServerRepoPath = 'D:\Android\AndroidStudioProjects\ShopServer\Shop',
    [switch]$SkipCodexSetup
)
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$clientRepoPath = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
foreach ($cmd in @('git', 'node', 'npx')) {
    if (-not (Get-Command $cmd -ErrorAction SilentlyContinue)) {
        throw "$cmd is not installed or not on PATH."
    }
}
$nodeVersionText = (& node --version).Trim()
if ($LASTEXITCODE -ne 0 -or $nodeVersionText -notmatch '^v(\d+)\.(\d+)\.') {
    throw "Unable to check Node.js version: $nodeVersionText"
}
$major = [int]$Matches[1]
$minor = [int]$Matches[2]
if (-not (($major -eq 22 -and $minor -ge 18) -or ($major -eq 24 -and $minor -ge 11) -or ($major -gt 24))) {
    throw 'GitNexus requires Node.js 22.18+ (22.x) or 24.11+.'
}
$repositories = @($clientRepoPath, $ServerRepoPath)
foreach ($path in $repositories) {
    if (-not (Test-Path -LiteralPath $path -PathType Container)) {
        throw "Repository directory not found: $path ; pass -ServerRepoPath to override its old recorded location."
    }
    $root = (& git -C $path rev-parse --show-toplevel)
    if ($LASTEXITCODE -ne 0 -or -not $root) {
        throw "Not a Git repository: $path"
    }
}

foreach ($path in $repositories) {
    Write-Host "Indexing $path (index-only: do not rewrite project AGENTS.md)..."
    Push-Location -LiteralPath $path
    try {
        & npx --yes gitnexus@latest analyze --index-only
        if ($LASTEXITCODE -ne 0) { throw "GitNexus indexing failed in $path" }
        & npx --yes gitnexus@latest status
        if ($LASTEXITCODE -ne 0) { throw "GitNexus status failed in $path" }
    }
    finally { Pop-Location }
}

if (-not $SkipCodexSetup) {
    Write-Host 'Configuring Codex GitNexus MCP, skills and hooks once (global user config)...'
    Push-Location -LiteralPath $clientRepoPath
    try {
        & npx --yes gitnexus@latest setup -c codex
        if ($LASTEXITCODE -ne 0) { throw 'GitNexus Codex integration setup failed.' }
    }
    finally { Pop-Location }
    if (Get-Command codex -ErrorAction SilentlyContinue) {
        & codex mcp list
        if ($LASTEXITCODE -ne 0) { Write-Warning 'Could not verify Codex MCP list; check it manually.' }
    }
    else {
        Write-Warning 'Codex CLI not on PATH. Verify MCP in your Codex app once installed.'
    }
}
Write-Host 'Repository indexes requested. Reopen Codex and approve the new GitNexus hooks via /hooks if prompted.'
