[CmdletBinding()]
param(
    [string]$RepositoryRoot,
    [switch]$Overwrite
)

$ErrorActionPreference = 'Stop'
if ([string]::IsNullOrWhiteSpace($RepositoryRoot)) {
    $RepositoryRoot = Split-Path -Parent $PSScriptRoot
}
$utf8 = New-Object System.Text.UTF8Encoding($false)
$tick = [char]96
$fence = [string]$tick * 3

function Write-Utf8([string]$Path, [string]$Content) {
    [System.IO.File]::WriteAllText($Path, $Content, $utf8)
}

function Persian-Number([string]$Text) {
    $result = $Text
    $digits = '۰۱۲۳۴۵۶۷۸۹'
    for ($i = 0; $i -lt 10; $i++) { $result = $result.Replace($digits[$i], [string]$i) }
    return [int]$result
}

function Allowed-Paths([string]$Area) {
    switch ($Area) {
        'WPPLUGIN' { return @('wordpress/carmilla-bridge/**', 'wordpress/**/tests/**', 'docs/**') }
        'WPTHEME' { return @('wordpress/carmilla-theme/**', 'wordpress/**/tests/**', 'docs/**') }
        'SPRING' { return @('D:\Android\AndroidStudioProjects\ShopServer\Shop\**', 'docs/**') }
        'ANDROID' { return @('composeApp/**', 'core/**', 'feature/**', 'docs/**') }
        'PWA' { return @('composeApp/**', 'core/**', 'feature/**', 'docs/**') }
        'MANIFEST' { return @('composeApp/**', 'core/**', 'feature/**', 'wordpress/carmilla-bridge/**', 'docs/**') }
        'PAYMENT' { return @('wordpress/carmilla-bridge/**', 'composeApp/**', 'core/**', 'feature/**', 'docs/**') }
        'MESSAGE' { return @('wordpress/carmilla-bridge/**', 'docs/**') }
        'SEED' { return @('wordpress/carmilla-bridge/**', 'docs/**') }
        'MIGRATION' { return @('wordpress/carmilla-bridge/**', 'docs/**') }
        'LMS' { return @('wordpress/carmilla-bridge/**', 'composeApp/**', 'core/**', 'feature/**', 'docs/**') }
        'CLINIC' { return @('wordpress/carmilla-bridge/**', 'composeApp/**', 'core/**', 'feature/**', 'docs/**') }
        'PSYCH' { return @('wordpress/carmilla-bridge/**', 'composeApp/**', 'core/**', 'feature/**', 'docs/**') }
        'IOS' { return @('iosApp/**', 'composeApp/**', 'core/**', 'feature/**', 'docs/**') }
        'DESKTOP' { return @('composeApp/**', 'core/**', 'feature/**', 'docs/**') }
        { $_ -in @('CORE','ARCH','SECURITY','QA','OBSERVABILITY') } { return @('composeApp/**', 'core/**', 'feature/**', 'wordpress/**', 'docs/**') }
        { $_ -in @('CI','BUILDER') } { return @('.github/**', 'gradle/**', 'build-logic/**', 'tools/**', 'docs/**') }
        default { return @('docs/**') }
    }
}

function Auto-Test-Lines($Task) {
    if ($Task.Type -in @('BIZ','LEGAL','DISC','ADR','DOC','EXPERIMENT','REVIEW','SAFETY')) {
        return @('- تست خودکار لازم نیست؛ reviewer انسانی باید صحت Evidence و خروجی را بررسی کند.', ('- معیار اختصاصی: ' + $Task.Validation))
    }
    if ($Task.Area -in @('WPPLUGIN','WPTHEME','SEED','MIGRATION','MESSAGE')) {
        return @('- Command: در محیط WordPress CI/container، lint و test محدود به Scope را اجرا کن.', '- Expected: activation/install و تست مرتبط exit code 0؛ نبود PHP محلی مجوز تیک‌زدن نیست.', ('- معیار اختصاصی: ' + $Task.Validation))
    }
    if ($Task.Area -eq 'SPRING') {
        return @('- Command: در D:\Android\AndroidStudioProjects\ShopServer\Shop، taskهای Gradle را کشف و test محدود به Scope را اجرا کن.', '- Expected: test profile مستقل از PostgreSQL محلی و exit code 0.', ('- معیار اختصاصی: ' + $Task.Validation))
    }
    return @('- Command baseline: .\gradlew.bat :composeApp:compileKotlinJvm و سپس task هدفی که پس از discovery مشخص می‌شود.', '- Command وب در صورت تغییر: .\gradlew.bat :composeApp:compileKotlinJs', '- Expected: commandهای محدود به Scope exit code 0 و report ذخیره‌شده داشته باشند.', ('- معیار اختصاصی: ' + $Task.Validation))
}

function Manual-Test-Lines($Task) {
    $human = $Task.Owner -match 'HUMAN|EXTERNAL' -or $Task.Type -in @('MANUAL','BIZ','LEGAL','SAFETY','REVIEW')
    $lines = New-Object System.Collections.Generic.List[string]
    if ($human) {
        $lines.Add('- این Task نیازمند اقدام یا تأیید انسانی/خارجی است.')
        $lines.Add('- AI باید در پاسخ نهایی مراحل دقیق،محیط،داده و نتیجه مورد انتظار را به کاربر بگوید و Status را AWAITING_MANUAL_QA یا BLOCKED بگذارد.')
    } else {
        $lines.Add('- اگر تغییر UI/network/migration دارد، انسان happy path،خطا و accessibility مرتبط را اجرا می‌کند؛ در غیر این صورت N/A را مستند کن.')
    }
    $lines.Add('- Environment/device/browser و داده synthetic را ثبت کن.')
    $lines.Add(('- انتظار: ' + $Task.Validation))
    $lines.Add('- Tester،تاریخ،build fingerprint،نتیجه و Evidence الزامی است.')
    return @($lines)
}

$master = Join-Path $RepositoryRoot 'docs\MASTER_IMPLEMENTATION_CHECKLIST_FA.md'
$tasksDirectory = Join-Path $RepositoryRoot 'docs\tasks'
$indexPath = Join-Path $RepositoryRoot 'docs\tasks.md'
$summaryPath = Join-Path $RepositoryRoot 'docs\PHASE_SUMMARY_FA.md'
if (-not (Test-Path -LiteralPath $master)) { throw "Master checklist not found: $master" }
if (((Test-Path -LiteralPath $tasksDirectory) -or (Test-Path -LiteralPath $indexPath) -or (Test-Path -LiteralPath $summaryPath)) -and -not $Overwrite) {
    throw 'Generated task files already exist. Do not overwrite active Status/Evidence; use -Overwrite only before execution begins.'
}

$lines = Get-Content -LiteralPath $master -Encoding UTF8
$records = New-Object System.Collections.Generic.List[object]
$phases = @{}
$currentPhase = $null
$currentTitle = $null

for ($lineNumber = 0; $lineNumber -lt $lines.Count; $lineNumber++) {
    $line = $lines[$lineNumber]
    if ($line -match '^##\s+[۰-۹]+\.\s+فاز\s+([۰-۹]+)\s+—\s+(.+)$') {
        $currentPhase = 'P{0:D2}' -f (Persian-Number $Matches[1])
        $currentTitle = $Matches[2].Trim()
        $phases[$currentPhase] = [pscustomobject]@{ Key=$currentPhase; Title=$currentTitle; Header=$lineNumber; Goal='' }
        continue
    }
    if (-not $line.StartsWith('| [ ] | ')) { continue }
    $parts = @($line.Split('|') | ForEach-Object { $_.Trim() })
    if ($parts.Count -lt 5) { continue }
    $id = $parts[2].Trim([char]96)
    if ($id -notmatch '^(P\d{2}|CT|CB)-[A-Z0-9]+-[A-Z0-9]+-\d{3}$') { continue }
    $idParts = $id.Split('-')
    $kind, $area, $type = $idParts[0], $idParts[1], $idParts[2]
    $cells = @($parts[3..($parts.Count - 2)])
    if ($kind -match '^P\d{2}$') {
        $owner, $priority, $work, $validation = $cells[0], $cells[1], $cells[2], $cells[3]
    } elseif ($kind -eq 'CB') {
        $owner, $priority, $work, $validation = $cells[0], 'P1/MEDIUM', $cells[1], $cells[2]
    } else {
        $owner, $priority, $work, $validation = 'BOTH', 'P1/MEDIUM', ('ایجاد و نگه‌داری artifact: ' + $cells[0]), $cells[1]
    }
    $records.Add([pscustomobject]@{
        Id=$id; Kind=$kind; Area=$area; Type=$type; Owner=$owner; Priority=$priority;
        Work=(($work -replace '\s+',' ').Trim()); Validation=(($validation -replace '\s+',' ').Trim());
        Phase=if ($kind -match '^P\d{2}$') { $kind } else { 'CONTROL' };
        PhaseTitle=if ($kind -match '^P\d{2}$') { $currentTitle } else { 'کنترل دائمی' };
        Number=[int]$idParts[3]; Status='TODO'; Depends=@(); Blocks=@()
    })
}

foreach ($phase in $phases.Values) {
    $found = $false
    $goal = New-Object System.Collections.Generic.List[string]
    for ($i = $phase.Header + 1; $i -lt $lines.Count; $i++) {
        $candidate = $lines[$i].Trim()
        if ($candidate -match '^##\s+' -or $candidate -eq '---') { break }
        if ($candidate -eq '### هدف') { $found = $true; continue }
        if ($found -and $candidate -match '^###\s+') { break }
        if ($found -and $candidate) { $goal.Add($candidate) }
    }
    $phase.Goal = (($goal -join ' ') -replace '\s+',' ').Trim()
}

$firstDependencies = @{
    P00=@(); P01=@('P00-PROGRAM-GATE-019'); P02=@('P01-SECURITY-GATE-023'); P03=@('P02-CORE-GATE-018');
    P04=@('P03-MANIFEST-GATE-022'); P05=@('P04-WPPLUGIN-GATE-024'); P06=@('P05-PAYMENT-GATE-024');
    P07=@('P06-MESSAGE-GATE-016'); P08=@('P07-SEED-GATE-026'); P09=@('P08-PWA-GATE-019');
    P10=@('P09-QA-GATE-018'); P11=@('P10-PROGRAM-OPS-019'); P12=@('P11-ANDROID-GATE-021');
    P13=@('P12-BUILDER-GATE-019'); P14=@('P13-LMS-GATE-027'); P15=@('P14-CLINIC-GATE-032');
    P16=@('P15-SPRING-GATE-029'); P17=@('P16-IOS-GATE-022')
}
foreach ($group in ($records | Where-Object { $_.Kind -match '^P\d{2}$' } | Group-Object Phase)) {
    $ordered = @($group.Group | Sort-Object Number)
    for ($i = 0; $i -lt $ordered.Count; $i++) {
        if ($i -eq 0) { $ordered[$i].Depends = @($firstDependencies[$group.Name]) }
        else { $ordered[$i].Depends = @($ordered[$i - 1].Id); $ordered[$i - 1].Blocks = @($ordered[$i].Id) }
    }
}
($records | Where-Object { $_.Id -eq 'P00-PROGRAM-DISC-001' } | Select-Object -First 1).Status = 'READY'

if (Test-Path -LiteralPath $tasksDirectory) { Get-ChildItem -LiteralPath $tasksDirectory -Filter '*.md' -File | Remove-Item -Force }
else { New-Item -ItemType Directory -Path $tasksDirectory | Out-Null }

foreach ($task in $records) {
    $title = $task.Work
    if ($title.Length -gt 150) { $title = $title.Substring(0,147) + '...' }
    $depends = if ($task.Depends.Count) { $task.Depends -join ', ' } else { 'ندارد؛ اولین Task صف یا Control مستقل است.' }
    $blocks = if ($task.Blocks.Count) { $task.Blocks -join ', ' } else { 'طبق Gate و نقشه وابستگی Master checklist.' }
    $authority = if ($task.Owner -match 'HUMAN|EXTERNAL' -or $task.Area -in @('PAYMENT','SECURITY','CLINIC','PSYCH','MIGRATION')) { 'BOTH یا HUMAN طبق Evidence' } else { 'BOTH' }
    $card = New-Object System.Text.StringBuilder
    [void]$card.AppendLine('# ' + $task.Id + ' — ' + $title)
    [void]$card.AppendLine()
    [void]$card.AppendLine('## Prompt اجرای همین Task')
    [void]$card.AppendLine()
    [void]$card.AppendLine($fence + 'text')
    [void]$card.AppendLine('نقش تو Implementer و Verifier فقط همین Task است.')
    [void]$card.AppendLine()
    [void]$card.AppendLine('Repository:')
    [void]$card.AppendLine('D:\Android\AndroidStudioProjects\kmp-shop')
    [void]$card.AppendLine()
    [void]$card.AppendLine('Master checklist:')
    [void]$card.AppendLine('D:\Android\AndroidStudioProjects\kmp-shop\docs\MASTER_IMPLEMENTATION_CHECKLIST_FA.md')
    [void]$card.AppendLine()
    [void]$card.AppendLine('Source audit:')
    [void]$card.AppendLine('D:\Android\AndroidStudioProjects\kmp-shop\docs\PROJECT_AUDIT_AND_PUBLICATION_PLAN_FA.md')
    [void]$card.AppendLine()
    [void]$card.AppendLine('Task ID:')
    [void]$card.AppendLine($task.Id)
    [void]$card.AppendLine()
    [void]$card.AppendLine('قبل از تغییر:')
    [void]$card.AppendLine('1. AGENTS.md و هر دستور ارجاع‌شده‌ای که واقعاً وجود دارد را بخوان.')
    [void]$card.AppendLine('2. Task، dependency، scope، acceptance و source reference را کامل بخوان.')
    [void]$card.AppendLine('3. git status را بررسی و تغییرات موجود کاربر را حفظ کن.')
    [void]$card.AppendLine('4. baseline test مشخص‌شده را اجرا کن.')
    [void]$card.AppendLine('5. اگر Task بزرگ‌تر از M یا مبهم است، اجرا نکن؛ آن را به Taskهای کوچک‌تر پیشنهاد بده.')
    [void]$card.AppendLine()
    [void]$card.AppendLine('قواعد:')
    foreach ($rule in @('فقط همین Task را انجام بده.','کمترین diff لازم را بساز.','خارج از Allowed scope تغییر نده.','dependency upgrade،refactor جانبی یا تغییر API contract انجام نده.','secret یا داده واقعی ایجاد/ثبت نکن.','deploy/publish/production/payment واقعی انجام نده مگر Task صریح و تأییدشده باشد.','ابتدا تست شکست یا characterization مناسب را اضافه کن.','همه commandهای verification را واقعاً اجرا کن.','تست دستی اجرا‌نشده را تیک نزن و وضعیت را AWAITING_MANUAL_QA بگذار.','بدون Evidence Task را DONE نکن.','فقط checkbox/status/evidence همین Task را به‌روزرسانی کن.','به Task بعدی نرو.')) { [void]$card.AppendLine('- ' + $rule) }
    [void]$card.AppendLine()
    [void]$card.AppendLine('شرایط توقف:')
    foreach ($stop in @('تداخل با تغییرات حل‌نشده کاربر','نبود credential/contract/تصمیم ضروری','نیاز به عملیات مخرب یا Production','baseline failure مرتبط','نیاز به تغییر contract خارج از Scope')) { [void]$card.AppendLine('- ' + $stop) }
    [void]$card.AppendLine()
    [void]$card.AppendLine('پاسخ نهایی: Outcome،Changed files،Automated tests،Manual test status،Acceptance Criteria،Evidence paths،Checklist status change،Remaining risks/blockers و Rollback instructions.')
    [void]$card.AppendLine($fence)
    [void]$card.AppendLine()
    [void]$card.AppendLine('- Status: ' + $task.Status)
    [void]$card.AppendLine('- Phase/Area/Type: ' + $task.Phase + ' / ' + $task.Area + ' / ' + $task.Type)
    [void]$card.AppendLine('- Priority/Risk/Size: ' + $task.Priority + ' / UNASSESSED (قبل از READY تعیین شود)')
    [void]$card.AppendLine('- Owner: ' + $task.Owner)
    [void]$card.AppendLine('- Completion authority: ' + $authority)
    [void]$card.AppendLine('- Depends on: ' + $depends)
    [void]$card.AppendLine('- Blocks: ' + $blocks)
    [void]$card.AppendLine('- Requirement source: Master checklist row ' + $task.Id + ' و Source audit بخش ' + $task.Area)
    [void]$card.AppendLine()
    [void]$card.AppendLine('## هدف قابل اندازه‌گیری')
    [void]$card.AppendLine($task.Work)
    [void]$card.AppendLine()
    [void]$card.AppendLine('## خروجی مورد انتظار')
    [void]$card.AppendLine($task.Validation)
    [void]$card.AppendLine()
    [void]$card.AppendLine('## خارج از محدوده')
    [void]$card.AppendLine('- هر Feature،provider،platform یا refactor خارج از همین Task ID.')
    [void]$card.AppendLine('- deploy/publish،پرداخت واقعی،تغییر Production و تغییر داده مشتری.')
    [void]$card.AppendLine()
    [void]$card.AppendLine('## Preconditions')
    [void]$card.AppendLine('- Status باید READY باشد؛ TODO مجوز اجرا نیست.')
    [void]$card.AppendLine('- Dependencyها: ' + $depends)
    [void]$card.AppendLine('- git status و baseline پیش از تغییر ثبت شوند.')
    [void]$card.AppendLine()
    [void]$card.AppendLine('## Allowed files/directories')
    foreach ($path in (Allowed-Paths $task.Area)) { [void]$card.AppendLine('- ' + $path) }
    [void]$card.AppendLine('- اگر مسیر لازم خارج از این فهرست بود،Task را BLOCKED کن و Scope بخواه.')
    [void]$card.AppendLine()
    [void]$card.AppendLine('## Forbidden actions')
    [void]$card.AppendLine('- حذف/overwrite تغییرات کاربر،git reset/checkout،ارتقای dependency یا تغییر contract خارج Scope.')
    [void]$card.AppendLine('- ثبت credential،داده واقعی مشتری یا داده سلامت در repo/Evidence.')
    [void]$card.AppendLine('- عملیات Production یا migration تخریبی.')
    [void]$card.AppendLine()
    [void]$card.AppendLine('## مراحل پیاده‌سازی')
    [void]$card.AppendLine('1. بخش ' + $task.Phase + ' در Master checklist و Source audit مرتبط را بخوان.')
    [void]$card.AppendLine('2. وضعیت موجود و baseline محدود به Scope را کشف و ثبت کن.')
    [void]$card.AppendLine('3. Size را تعیین کن؛ اگر بزرگ‌تر از M است child Task پیشنهاد بده و متوقف شو.')
    [void]$card.AppendLine('4. characterization/test منفی لازم را اضافه کن یا دلیل مستند نبود آن را ثبت کن.')
    [void]$card.AppendLine('5. فقط تغییر لازم برای هدف را پیاده‌سازی کن.')
    [void]$card.AppendLine('6. validation و تست‌ها را اجرا،Evidence را ذخیره و Status صحیح را ثبت کن.')
    [void]$card.AppendLine()
    [void]$card.AppendLine('## Automated tests با command و expected result')
    foreach ($item in (Auto-Test-Lines $task)) { [void]$card.AppendLine($item) }
    [void]$card.AppendLine()
    [void]$card.AppendLine('## Manual tests با environment/data/steps/expected')
    foreach ($item in (Manual-Test-Lines $task)) { [void]$card.AppendLine($item) }
    [void]$card.AppendLine()
    [void]$card.AppendLine('## Acceptance Criteria')
    foreach ($item in @('خروجی با هدف و validation این کارت منطبق است.','Scope خارج از Allowed files/directories گسترش نیافته است.','تست خودکار/بازبینی لازم واقعاً اجرا و نتیجه ثبت شده است.','اگر تست دستی لازم است،Evidence انسانی ثبت شده یا Status برابر AWAITING_MANUAL_QA است.')) { [void]$card.AppendLine('- [ ] ' + $item) }
    [void]$card.AppendLine()
    [void]$card.AppendLine('## Security/Privacy/Migration checks')
    foreach ($item in @('Secret،Token،PII،PHI یا داده مشتری در source،log و Evidence ثبت نشود.','برای API/write path،authorization و ownership بررسی شود.','برای migration،forward fix/rollback و backup بررسی شود.')) { [void]$card.AppendLine('- ' + $item) }
    [void]$card.AppendLine()
    [void]$card.AppendLine('## Evidence')
    [void]$card.AppendLine('- مسیر: docs/evidence/' + $task.Id + '/')
    [void]$card.AppendLine('- baseline commit/build،command/cwd/exit code،test report،screenshot redacted و reviewer را ثبت کن.')
    [void]$card.AppendLine()
    [void]$card.AppendLine('## Rollback')
    [void]$card.AppendLine('- روش بازگشت کم‌خطر یا forward-fix پیش از تغییر ثبت شود.')
    [void]$card.AppendLine('- Migration/Payment/Secret/Health بدون backup و تأیید انسانی DONE نمی‌شود.')
    [void]$card.AppendLine()
    [void]$card.AppendLine('## Completion record')
    foreach ($field in @('Started at:','Completed at:','Changed files:','Commands and exit codes:','Manual tester/date/result:','Evidence paths:','Remaining risks/blockers:','Final status: TODO | CODE_COMPLETE | AWAITING_MANUAL_QA | IN_REVIEW | DONE | BLOCKED')) { [void]$card.AppendLine('- ' + $field) }
    Write-Utf8 (Join-Path $tasksDirectory ($task.Id + '.md')) $card.ToString()
}

$index = New-Object System.Text.StringBuilder
[void]$index.AppendLine('# صف اجرای Taskهای Carmilla')
[void]$index.AppendLine()
[void]$index.AppendLine('## Prompt اولیه برای AI')
[void]$index.AppendLine()
[void]$index.AppendLine($fence + 'text')
[void]$index.AppendLine('این فایل فقط صف اجرای Taskها است. دقیقاً یک Task را در هر نوبت انجام بده.')
[void]$index.AppendLine('1. اولین ردیف READY را پیدا کن.')
[void]$index.AppendLine('2. اگر READY وجود ندارد،Task دلخواه از TODO انتخاب نکن؛dependencyها و آخرین Task را بررسی و Blocker را گزارش کن.')
[void]$index.AppendLine('3. فایل docs/tasks/<TASK-ID>.md را کامل بخوان؛Prompt ابتدای همان کارت مرجع اجرا است.')
[void]$index.AppendLine('4. فقط همان کارت را اجرا کن. بدون Evidence یا تست دستی انسانی،Master checklist را DONE نکن.')
[void]$index.AppendLine('5. اگر اقدام انسانی/خارجی لازم است،مراحل دقیق،محیط،داده و انتظار را به کاربر بگو و Status را AWAITING_MANUAL_QA یا BLOCKED بگذار.')
[void]$index.AppendLine('6. پس از تکمیل واقعی،فقط row همین Task و Completion record کارت را به‌روزرسانی کن؛سپس اگر dependency پاس است،Task بعدی را READY کن.')
[void]$index.AppendLine('7. به Task بعدی نرو و تغییرات کاربر را حفظ کن.')
[void]$index.AppendLine($fence)
[void]$index.AppendLine()
[void]$index.AppendLine('## وضعیت‌ها')
[void]$index.AppendLine()
[void]$index.AppendLine('- READY: تنها Task مجاز برای شروع AI.')
[void]$index.AppendLine('- TODO: dependency یا تصمیم لازم هنوز آماده نیست.')
[void]$index.AppendLine('- CODE_COMPLETE: کد/تست خودکار انجام شده،اما تست دستی یا Review باقی است.')
[void]$index.AppendLine('- AWAITING_MANUAL_QA: AI باید مراحل دستی را به کاربر بدهد.')
[void]$index.AppendLine('- DONE: فقط پس از Evidence و authority لازم؛آن‌وقت checkbox Master هم تیک می‌خورد.')
[void]$index.AppendLine('- BLOCKED: دلیل،Evidence و اقدام لازم برای رفع blocker ثبت شود.')
[void]$index.AppendLine()
[void]$index.AppendLine('## Taskهای فازی')
[void]$index.AppendLine()
foreach ($phase in ($phases.Values | Sort-Object Key)) {
    $phaseTasks = @($records | Where-Object { $_.Phase -eq $phase.Key } | Sort-Object Number)
    if ($phaseTasks.Count -eq 0) { continue }
    [void]$index.AppendLine('### ' + $phase.Key + ' — ' + $phase.Title)
    [void]$index.AppendLine()
    [void]$index.AppendLine('| Status | Task ID | Owner | Depends on | کارت |')
    [void]$index.AppendLine('|---|---|---|---|---|')
    foreach ($task in $phaseTasks) {
        $depends = if ($task.Depends.Count) { $task.Depends -join ', ' } else { '—' }
        [void]$index.AppendLine('| ' + $task.Status + ' | ' + $task.Id + ' | ' + $task.Owner + ' | ' + $depends + ' | [' + $task.Id + '](tasks/' + $task.Id + '.md) |')
    }
    [void]$index.AppendLine()
}
[void]$index.AppendLine('## کنترل‌های دائمی CT و CB')
[void]$index.AppendLine()
[void]$index.AppendLine('این‌ها فقط وقتی READY می‌شوند که Gate mapping در Master checklist آن‌ها را لازم کرده یا کاربر صریحاً انتخابشان کند.')
[void]$index.AppendLine()
foreach ($task in ($records | Where-Object { $_.Kind -in @('CT','CB') } | Sort-Object Id)) {
    [void]$index.AppendLine('- TODO — [' + $task.Id + '](tasks/' + $task.Id + '.md) — ' + $task.Work)
}
Write-Utf8 $indexPath $index.ToString()

$summary = New-Object System.Text.StringBuilder
[void]$summary.AppendLine('# خلاصه ساده فازهای Carmilla')
[void]$summary.AppendLine()
[void]$summary.AppendLine('برای اجرای جزئیات به docs/tasks.md و کارت هر Task مراجعه کن.')
[void]$summary.AppendLine()
foreach ($phase in ($phases.Values | Sort-Object Key)) {
    $phaseTasks = @($records | Where-Object { $_.Phase -eq $phase.Key } | Sort-Object Number)
    [void]$summary.AppendLine('## ' + $phase.Key + ' — ' + $phase.Title)
    [void]$summary.AppendLine()
    [void]$summary.AppendLine('- هدف: ' + $phase.Goal)
    [void]$summary.AppendLine('- Taskها:')
    foreach ($task in $phaseTasks) { [void]$summary.AppendLine('  - [' + $task.Id + '](tasks/' + $task.Id + '.md) — ' + $task.Work) }
    [void]$summary.AppendLine()
}
Write-Utf8 $summaryPath $summary.ToString()

Write-Host ('Generated {0} task cards.' -f $records.Count)
