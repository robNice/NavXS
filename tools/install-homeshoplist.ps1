[CmdletBinding()]
param(
    [string]$Serial
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Split-Path -Parent $scriptDir
$apkPath = Join-Path $projectRoot "tools\apks\HomeShopList.apk"
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
    throw "adb nicht gefunden."
}

function Get-OnlineDevices {
    param(
        [string]$AdbPath
    )

    $deviceLines = & $AdbPath devices |
        Select-Object -Skip 1 |
        Where-Object { $_.Trim() -and $_ -match "\sdevice$" }

    return @($deviceLines | ForEach-Object {
        $parts = $_ -split "\s+"
        [pscustomobject]@{
            Serial = $parts[0]
            State = $parts[1]
        }
    })
}

$adbPath = Get-AdbPath

if (-not (Test-Path $apkPath)) {
    throw "APK nicht gefunden: $apkPath"
}

$onlineDevices = Get-OnlineDevices -AdbPath $adbPath

if ($onlineDevices.Count -eq 0) {
    throw "Kein Online-Gerät gefunden."
}

if ([string]::IsNullOrWhiteSpace($Serial)) {
    if ($onlineDevices.Count -eq 1) {
        $Serial = $onlineDevices[0].Serial
    } else {
        Write-Host "Online devices:"
        $onlineDevices | ForEach-Object { Write-Host "  $($_.Serial)  $($_.State)" }
        throw "Bitte Zielgerät angeben: .\tools\install-homeshoplist.ps1 -Serial emulator-5554"
    }
}

$selectedDevice = $onlineDevices | Where-Object { $_.Serial -eq $Serial }
if (-not $selectedDevice) {
    Write-Host "Online devices:"
    $onlineDevices | ForEach-Object { Write-Host "  $($_.Serial)  $($_.State)" }
    throw "Ausgewähltes Gerät ist nicht online: $Serial"
}

Write-Host "Installiere HomeShopList auf $Serial ..."
& $adbPath -s $Serial install -r $apkPath

if ($LASTEXITCODE -ne 0) {
    throw "adb install fehlgeschlagen"
}

Write-Host "Fertig: $Serial"
