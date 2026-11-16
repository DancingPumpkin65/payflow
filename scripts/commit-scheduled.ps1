param(
    [string]$Message,
    [switch]$DryRun
)
$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path $PSScriptRoot -Parent
$currentRoot = git rev-parse --show-toplevel
if ($LASTEXITCODE -ne 0 -or [IO.Path]::GetFullPath($currentRoot) -ne [IO.Path]::GetFullPath($repoRoot)) {
    throw 'Run this helper inside the Payflow repository.'
}
$schedule = Import-Csv (Join-Path $PSScriptRoot 'commit-schedule.csv')
$history = @(git log --all --format='%H|%aI|%cI')
if ($LASTEXITCODE -ne 0) { throw 'Cannot read Git history.' }
$counts = @{}
foreach ($line in $history) {
    $parts = $line.Split('|')
    $date = $parts[1].Substring(0, 10)
    if ($parts[1] -eq $parts[2] -and $parts[1].EndsWith('+01:00')) {
        if (-not $counts.ContainsKey($date)) { $counts[$date] = 0 }
        $counts[$date]++
    }
}
$previous = ''
foreach ($row in $schedule) {
    $parsed = [datetime]::ParseExact($row.date, 'yyyy-MM-dd', [cultureinfo]::InvariantCulture)
    if ($row.date -le $previous -or [int]$row.count -lt 1) { throw 'Schedule must contain ordered, unique dates and positive counts.' }
    $previous = $row.date
    if ($counts.ContainsKey($row.date) -and $counts[$row.date] -gt [int]$row.count) { throw "Existing commits exceed the target on $($row.date); history was not changed." }
}
$next = $schedule | Where-Object { [int]$counts[$_.date] -lt [int]$_.count } | Select-Object -First 1
if (-not $next) { throw 'Schedule is full. Ask the user for the next dates.' }
$used = [int]$counts[$next.date]
$assigned = [datetime]::ParseExact($next.date, 'yyyy-MM-dd', [cultureinfo]::InvariantCulture).AddHours(9).AddMinutes($used * 20)
$stamp = $assigned.ToString("yyyy-MM-dd'T'HH:mm:ss", [cultureinfo]::InvariantCulture) + '+01:00'
Write-Output "Scheduled: $stamp (slot $($used + 1)/$($next.count))"
if ($DryRun) { return }
if ([string]::IsNullOrWhiteSpace($Message)) { throw 'A commit message is required.' }
git diff --cached --quiet
if ($LASTEXITCODE -eq 0) { throw 'Nothing staged; empty commits are not created.' }
if ($LASTEXITCODE -ne 1) { throw 'Cannot inspect staged changes.' }
$authorBefore = $env:GIT_AUTHOR_DATE
$committerBefore = $env:GIT_COMMITTER_DATE
try {
    $env:GIT_AUTHOR_DATE = $stamp
    $env:GIT_COMMITTER_DATE = $stamp
    git commit -m $Message
    if ($LASTEXITCODE -ne 0) { throw 'Commit failed.' }
    $verified = git log -1 --format='%aI|%cI'
    if ($verified -ne "$stamp|$stamp") { throw 'Commit date verification failed.' }
} finally {
    $env:GIT_AUTHOR_DATE = $authorBefore
    $env:GIT_COMMITTER_DATE = $committerBefore
}
