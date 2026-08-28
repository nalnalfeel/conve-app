$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$targetDir = Join-Path $projectRoot "target"

if (-not (Test-Path $targetDir)) {
    Write-Error "Folder target belum ada. Jalankan: .\mvnw.cmd -DskipTests package"
    exit 1
}

$jarFile = Get-ChildItem $targetDir -Filter "*.jar" |
    Where-Object { $_.Name -notmatch 'original-' } |
    Sort-Object LastWriteTimeUtc |
    Select-Object -Last 1

if (-not $jarFile) {
    Write-Error "Tidak menemukan JAR hasil build. Jalankan: .\mvnw.cmd -DskipTests package"
    exit 1
}

if (-not $env:JAVA_HOME) {
    Write-Error "JAVA_HOME belum diatur. Pastikan JDK 21 aktif."
    exit 1
}

$jpackage = Join-Path $env:JAVA_HOME "bin\jpackage.exe"
if (-not (Test-Path $jpackage)) {
    Write-Error "jpackage.exe tidak ditemukan di JAVA_HOME. Pastikan JDK 21 terinstall dengan tool jpackage."
    exit 1
}

$wix = Get-Command light.exe -ErrorAction SilentlyContinue
if (-not $wix) {
    Write-Error "WiX Toolset belum terinstall. Install WiX 3.x atau lebih dari https://wixtoolset.org lalu jalankan script ini lagi."
    exit 1
}

$outputDir = Join-Path $targetDir "Hasil-EXE"
if (Test-Path $outputDir) { Remove-Item $outputDir -Recurse -Force }
New-Item -ItemType Directory -Force -Path $outputDir | Out-Null

& $jpackage `
  --type exe `
  --name "Conve App" `
  --app-version "1.0.1" `
  --input $targetDir `
  --main-jar $jarFile.Name `
  --main-class "com.cvt.conveapp.Launcher" `
  --dest $outputDir `
  --win-shortcut `
  --win-menu

Write-Host "Package selesai. Hasil ada di: $outputDir"
