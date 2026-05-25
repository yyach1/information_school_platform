$ErrorActionPreference = "Stop"
Set-Location "$PSScriptRoot/../member-c/admin-web"
if (-not (Test-Path "node_modules")) {
  npm install
}
npm run dev
