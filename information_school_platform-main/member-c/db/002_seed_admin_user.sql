-- Seed default ADMIN user
-- Password: admin123 (BCrypt encoded)
INSERT INTO "user" (username, password_hash, real_name, role, status)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'ADMIN', 'ACTIVE')
ON CONFLICT (username) DO NOTHING;
