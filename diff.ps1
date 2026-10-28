$tasks = Get-Content docs/tasks.md | Select-String "\| (TODO|DONE|AWAITING_MANUAL_QA|BLOCKED|IN_REVIEW|CODE_COMPLETE) \| (P\d+-[A-Z0-9-]+)" -AllMatches | % { $_.Matches } | % { $_.Groups }
$tasksMap = @{}
for ($i=0; $i -lt $tasks.Count; $i+=3) {
    $status = $tasks[$i+1].Value
    $id = $tasks[$i+2].Value
    $tasksMap[$id] = $status
}
$master = Get-Content docs/MASTER_IMPLEMENTATION_CHECKLIST_FA.md | Select-String "\| \[([x /])\] \| \`(P\d+-[A-Z0-9-]+)\`" -AllMatches | % { $_.Matches } | % { $_.Groups }
$masterMap = @{}
for ($i=0; $i -lt $master.Count; $i+=3) {
    $mark = $master[$i+1].Value
    $id = $master[$i+2].Value
    $status = if ($mark -eq "x") { "DONE" } elseif ($mark -eq " ") { "TODO" } elseif ($mark -eq "/") { "IN_PROGRESS" } else { "UNKNOWN" }
    $masterMap[$id] = $status
}
$diffs = @()
foreach ($id in $tasksMap.Keys) {
    if ($masterMap.ContainsKey($id)) {
        $tStat = $tasksMap[$id]
        $mStat = $masterMap[$id]
        if (($tStat -eq "DONE" -and $mStat -ne "DONE") -or ($tStat -ne "DONE" -and $mStat -eq "DONE")) {
            $diffs += "$id -> tasks: $tStat vs master: $mStat"
        }
    }
}
$diffs | Out-String
