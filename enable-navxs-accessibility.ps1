$ErrorActionPreference = "Stop"

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

function Get-AdbTarget {
    param(
        [string]$AdbPath
    )

    $deviceLines = & $AdbPath devices | Select-Object -Skip 1 | Where-Object {
        $_.Trim() -and $_ -match "\sdevice$"
    }

    $serials = @($deviceLines | ForEach-Object {
        ($_ -split "\s+")[0]
    })

    if ($serials.Count -eq 0) {
        throw "No adb device/emulator connected."
    }

    $emulators = @($serials | Where-Object { $_ -like "emulator-*" })
    if ($emulators.Count -eq 1) {
        return $emulators[0]
    }

    if ($serials.Count -eq 1) {
        return $serials[0]
    }

    throw "Multiple adb targets connected: $($serials -join ', '). Re-run with only one target connected, or add explicit -s handling."
}

$adb = Get-AdbPath
$target = Get-AdbTarget -AdbPath $adb

& $adb -s $target shell settings put secure accessibility_enabled 1
& $adb -s $target shell settings put secure enabled_accessibility_services $service
& $adb -s $target shell am start -n de.robnice.navxs/.ui.MainActivity | Out-Null

Write-Host "NavXS accessibility service enabled and app launched on $target."
