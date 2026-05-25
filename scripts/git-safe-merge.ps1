param(
  [Parameter(Mandatory=$true)] [string] $SourceBranch,
  [string] $TargetBranch = "main"
)
$ErrorActionPreference = "Stop"

Write-Host "切换到 $TargetBranch 并更新远端..." -ForegroundColor Cyan
git checkout $TargetBranch
git pull origin $TargetBranch

Write-Host "准备合并 $SourceBranch。先执行 --no-commit，便于人工检查。" -ForegroundColor Cyan
git merge --no-commit --no-ff $SourceBranch

Write-Host "合并已暂存但未提交。请检查冲突和修改后执行：" -ForegroundColor Yellow
Write-Host "  git status"
Write-Host "  git commit -m \"merge: $SourceBranch into $TargetBranch\""
Write-Host "  git push origin $TargetBranch"
