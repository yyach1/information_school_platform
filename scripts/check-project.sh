#!/usr/bin/env bash
set -e

echo "== 学院平台项目快速检查 =="
if [ ! -f "member-c/pom.xml" ]; then
  echo "请在仓库根目录执行：bash scripts/check-project.sh"
  exit 1
fi

echo
echo "[1/4] Git 状态"
git status --short || true

echo
echo "[2/4] 后端 Maven 版本"
if command -v mvn >/dev/null 2>&1; then
  mvn -v | head -n 3
else
  echo "未检测到 Maven，请安装 Maven 后再运行后端。"
fi

echo
echo "[3/4] Node/NPM 版本"
if command -v node >/dev/null 2>&1 && command -v npm >/dev/null 2>&1; then
  node -v
  npm -v
else
  echo "未检测到 Node 或 npm，请安装 Node.js 后再运行管理端。"
fi

echo
echo "[4/4] 关键目录检查"
for p in db/kingbase docs member-c/src/main/java member-c/admin-web/src miniprogram; do
  if [ -e "$p" ]; then echo "OK  $p"; else echo "MISS $p"; fi
done
