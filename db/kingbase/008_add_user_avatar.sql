-- 用户表新增头像字段
ALTER TABLE "user" ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(512);
