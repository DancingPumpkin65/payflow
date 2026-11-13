param(
    [string]$Text,
    [string]$Description,
    [string]$Capture,
    [switch]$Dump
)

$ErrorActionPreference = 'Stop'
$adbPath = Join-Path $env:LOCALAPPDATA 'PayflowTools\platform-tools\adb.exe'
& $adbPath shell uiautomator dump /sdcard/payflow-review.xml | Out-Null
[xml]$screen = (& $adbPath shell cat /sdcard/payflow-review.xml)
if (-not $screen.SelectSingleNode('//node[@package="com.dancingpumpkin.payflow"]')) { throw 'Payflow must be visible before review input.' }
if ($Text -or $Description) {
    $matches = @($screen.SelectNodes('//node') | Where-Object {
        if ($Text) { $_.GetAttribute('text') -eq $Text } else { $_.GetAttribute('content-desc') -eq $Description }
    })
    if ($matches.Count -ne 1) { throw "Expected one control, found $($matches.Count)." }
    $bounds = [regex]::Matches($matches[0].GetAttribute('bounds'), '\d+') | ForEach-Object { [int]$_.Value }
    & $adbPath shell input tap ([int](($bounds[0] + $bounds[2]) / 2)) ([int](($bounds[1] + $bounds[3]) / 2))
    & $adbPath shell uiautomator dump /sdcard/payflow-review.xml | Out-Null
    [xml]$screen = (& $adbPath shell cat /sdcard/payflow-review.xml)
}
if ($Dump) {
    $screen.SelectNodes('//node[@text!="" or @content-desc!="" or @class="android.widget.EditText"]') | ForEach-Object {
        '{0} | {1} | {2}' -f $_.GetAttribute('text'), $_.GetAttribute('content-desc'), $_.GetAttribute('bounds')
    }
}
if ($Capture) {
    & $adbPath shell screencap -p /sdcard/payflow-review.png
    & $adbPath pull /sdcard/payflow-review.png $Capture
}
