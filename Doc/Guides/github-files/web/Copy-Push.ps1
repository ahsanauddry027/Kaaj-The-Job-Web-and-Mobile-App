<#
.SYNOPSIS
  Copies the files of one push (1, 2 or 3) from this project into your clone of the CSE471 group repository.

.DESCRIPTION
  push-1-setup-and-module-1.txt  shared setup + Module 1 (employer profile and verification; post and manage jobs)
  push-2-module-2.txt            Module 2 (scam screening and moderation)
  push-3-module-3.txt            Module 3 (AI job-post assistant)

  A few shared files were changed again by a later module. For those, the file as it was at that point is kept in
  versions\push-1\ and versions\push-2\, and this script copies that version, so every push builds on its own and
  each pull request shows only its own module's changes. Nothing in studentapi is changed.

.EXAMPLE
  cd C:\Users\UseR\Desktop\Projects\studentapi
  powershell -ExecutionPolicy Bypass -File .\Doc\Guides\github-files\web\Copy-Push.ps1 -Push 1 -To ..\cse471-kaaj
#>
param(
    [Parameter(Mandatory = $true)][ValidateSet('1', '2', '3')][string]$Push,
    [Parameter(Mandatory = $true)][string]$To
)
$ErrorActionPreference = 'Stop'

$here = $PSScriptRoot                                                   # ...\studentapi\Doc\Guides\github-files\web
$project = (Resolve-Path (Join-Path $here '..\..\..\..')).Path          # ...\studentapi
if (-not (Test-Path $To)) { throw "Folder not found: $To. Clone the group repository first (guide 05, step W1)." }
$target = (Resolve-Path $To).Path
if ($target.TrimEnd('\') -eq $project.TrimEnd('\')) { throw 'Copy into your clone of the GROUP repository, not into studentapi itself.' }
if (-not (Test-Path (Join-Path $target '.git'))) { throw "$target is not a Git repository. Clone the group repository first (guide 05, step W1)." }

$listFile = Get-ChildItem -Path $here -Filter "push-$Push-*.txt" | Select-Object -First 1
if (-not $listFile) { throw "List for push $Push not found in $here" }
$paths = Get-Content -Path $listFile.FullName -Encoding UTF8 | Where-Object { $_.Trim() -and -not $_.StartsWith('#') }

$copied = 0
$earlier = 0
$missing = @()
foreach ($rel in $paths) {
    $winRel = $rel.Trim() -replace '/', '\'
    $source = Join-Path $here ("versions\push-$Push\" + $winRel)
    if (Test-Path -LiteralPath $source) {
        $earlier++
    } else {
        $source = Join-Path $project $winRel
    }
    if (-not (Test-Path -LiteralPath $source)) {
        $missing += $rel
        continue
    }
    $dest = Join-Path $target $winRel
    $folder = Split-Path -Parent $dest
    if (-not (Test-Path -LiteralPath $folder)) { New-Item -ItemType Directory -Force -Path $folder | Out-Null }
    Copy-Item -LiteralPath $source -Destination $dest -Force
    $copied++
}

Write-Host ("Push {0}: copied {1} files into {2} ({3} of them as they were at that point)." -f $Push, $copied, $target, $earlier)
if ($missing.Count -gt 0) {
    Write-Warning ("{0} files were not found in studentapi:" -f $missing.Count)
    $missing | ForEach-Object { Write-Host "  $_" }
}
Write-Host ("Next:  cd `"{0}`"   then   git status" -f $target)
