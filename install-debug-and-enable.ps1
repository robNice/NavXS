$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$service = "de.robnice.navxs/de.robnice.navxs.accessibility.NavigationAccessibilityService"
$adbCandidates = @(
    "adb",
    "C:\Users\cornice\AppData\Local\Android\Sdk\platform-tools\adb.exe"
)

function Get-AdbPath {
    foreach ($candidate in $adbCandidates) {
        try {
            if ($candidate -eq "adb") {
                $cmd = Get-Command adb -ErrorAction Stop
                return $cmd.Source
            }
            if (Test-Path $candidate) {
                return $candidate
            }
        } catch {
        }
    }
    throw "adb not found."
}

$adb = Get-AdbPath

Push-Location $projectRoot
try {
    & "$projectRoot\gradlew.bat" installDebug
    & $adb devices | Out-Null
    & $adb shell settings put secure accessibility_enabled 1
    & $adb shell settings put secure enabled_accessibility_services $service
    & $adb shell am start -n de.robnice.navxs/.ui.MainActivity | Out-Null
    Write-Host "NavXS installed, accessibility service enabled, and app launched."
} finally {
    Pop-Location
}
