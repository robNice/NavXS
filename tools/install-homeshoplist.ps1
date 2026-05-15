param(
    [string]$Serial
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Split-Path -Parent $scriptDir
$apkPath = Join-Path $projectRoot "tools\apks\HomeShopList.apk"
$adbPath = "C:\Users\cornice\AppData\Local\Android\Sdk\platform-tools\adb.exe"

if (-not (Test-Path $adbPath)) {
    throw "adb nicht gefunden: $adbPath"
}

if (-not (Test-Path $apkPath)) {
    throw "APK nicht gefunden: $apkPath"
}

$deviceLines = & $adbPath devices |
    Select-Object -Skip 1 |
    Where-Object { $_ -match "\S" } |
    ForEach-Object { ($_ -split "\s+")[0,1] -join "`t" }

$onlineDevices = $deviceLines |
    Where-Object { $_ -match "`tdevice$" } |
    ForEach-Object { ($_ -split "`t")[0] }

if ([string]::IsNullOrWhiteSpace($Serial)) {
    if ($onlineDevices.Count -eq 1) {
        $Serial = $onlineDevices[0]
    } else {
        Write-Host "Online devices:"
        $deviceLines | ForEach-Object { Write-Host "  $_" }
        throw "Bitte Zielgerät angeben: .\tools\install-homeshoplist.ps1 -Serial emulator-5554"
    }
}

if ($Serial -notin $onlineDevices) {
    Write-Host "Online devices:"
    $deviceLines | ForEach-Object { Write-Host "  $_" }
    throw "Ausgewähltes Gerät ist nicht online: $Serial"
}

Write-Host "Installiere HomeShopList auf $Serial ..."
& $adbPath -s $Serial install -r $apkPath

if ($LASTEXITCODE -ne 0) {
    throw "adb install fehlgeschlagen"
}

Write-Host "Fertig: $Serial"
