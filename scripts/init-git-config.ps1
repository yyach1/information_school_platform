$ErrorActionPreference = "Stop"
git config core.autocrlf false
git config core.eol lf
git config commit.template .gitmessage
Write-Host "已设置本仓库 Git 换行与提交模板。" -ForegroundColor Green
