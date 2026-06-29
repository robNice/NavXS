param(
    [string]$Target = "emulator"
)

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

function Get-AdbSerials {
    param(
        [string]$AdbPath
    )

    $deviceLines = & $AdbPath devices | Select-Object -Skip 1 | Where-Object {
        $_.Trim() -and $_ -match "\sdevice$"
    }

    return @($deviceLines | ForEach-Object {
        ($_ -split "\s+")[0]
    })
}

function Resolve-AdbTarget {
    param(
        [string]$AdbPath,
        [string]$RequestedTarget
    )

    $serials = Get-AdbSerials -AdbPath $AdbPath
    if ($serials.Count -eq 0) {
        throw "No adb device/emulator connected."
    }

    $emulators = @($serials | Where-Object { $_ -like "emulator-*" })
    $devices = @($serials | Where-Object { $_ -notlike "emulator-*" })

    switch ($RequestedTarget.ToLowerInvariant()) {
        "emulator" {
            if ($emulators.Count -eq 1) { return $emulators[0] }
            if ($emulators.Count -eq 0) { throw "No emulator connected." }
            throw "Multiple emulators connected: $($emulators -join ', ')"
        }
        "device" {
            if ($devices.Count -eq 1) { return $devices[0] }
            if ($devices.Count -eq 0) { throw "No physical device connected." }
            throw "Multiple physical devices connected: $($devices -join ', ')"
        }
        default {
            if ($serials -contains $RequestedTarget) { return $RequestedTarget }
            throw "Requested target '$RequestedTarget' not found. Connected: $($serials -join ', ')"
        }
    }
}

$adb = Get-AdbPath
$targetSerial = Resolve-AdbTarget -AdbPath $adb -RequestedTarget $Target

Push-Location $projectRoot
try {
    & "$projectRoot\gradlew.bat" installDebug
    & $adb -s $targetSerial shell settings put secure accessibility_enabled 1
    & $adb -s $targetSerial shell settings put secure enabled_accessibility_services $service
    & $adb -s $targetSerial shell am start -n de.robnice.navxs/.ui.MainActivity | Out-Null
    Write-Host "NavXS installed, accessibility service enabled, and app launched on $targetSerial."
} finally {
    Pop-Location
}
