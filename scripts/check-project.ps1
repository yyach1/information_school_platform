$ErrorActionPreference = "Stop"
Write-Host "== 学院平台项目快速检查 ==" -ForegroundColor Cyan

if (-not (Test-Path "member-c/pom.xml")) {
  throw "请在仓库根目录执行本脚本：scripts/check-project.ps1"
}

Write-Host "`n[1/4] Git 状态" -ForegroundColor Yellow
git status --short

Write-Host "`n[2/4] 后端 Maven 版本" -ForegroundColor Yellow
try { mvn -v | Select-Object -First 3 } catch { Write-Warning "未检测到 Maven，请安装 Maven 后再运行后端。" }

Write-Host "`n[3/4] Node/NPM 版本" -ForegroundColor Yellow
try { node -v; npm -v } catch { Write-Warning "未检测到 Node 或 npm，请安装 Node.js 后再运行管理端。" }

Write-Host "`n[4/4] 关键目录检查" -ForegroundColor Yellow
$paths = @("db/kingbase", "docs", "member-c/src/main/java", "member-c/admin-web/src", "miniprogram")
foreach ($p in $paths) {
  if (Test-Path $p) { Write-Host "OK  $p" -ForegroundColor Green } else { Write-Host "MISS $p" -ForegroundColor Red }
}

Write-Host "`n检查完成。" -ForegroundColor Cyan
