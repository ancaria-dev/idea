<#
.SYNOPSIS
    Prints or sets pluginVersion, the plugin's own version.

.DESCRIPTION
    gradle.properties is the source of truth. CHANGELOG.md is deliberately
    left alone: renaming its heading would rewrite a past release's notes
    once this plugin has actually shipped one. Add a new heading for the new
    version by hand instead of expecting this script to do it.

    gradle/libs.versions.toml's `coderpack` entry is a different number: the
    dev.ancaria.coderpack:templates version this plugin resolves, not its own
    version. This script does not touch it.

.EXAMPLE
    pwsh tools/version.ps1
    pwsh tools/version.ps1 0.99.1
#>
param(
    [string]$Version
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$propsPath = Join-Path $root 'gradle.properties'

$match = Select-String -Path $propsPath -Pattern '^pluginVersion=(.+)$'
if (-not $match) { throw "No pluginVersion= line in $propsPath" }
$current = $match.Matches[0].Groups[1].Value

if (-not $Version) {
    Write-Host $current
    return
}

$text = Get-Content -Path $propsPath -Raw
$new = $text -replace "(?m)^pluginVersion=$([regex]::Escape($current))$", "pluginVersion=$Version"
if ($new -eq $text) { throw "pluginVersion=$current not found in $propsPath" }
Set-Content -Path $propsPath -Value $new -NoNewline

Write-Host "$current -> $Version"
Write-Host 'Add a new CHANGELOG.md heading by hand -- this script leaves release notes alone.'
