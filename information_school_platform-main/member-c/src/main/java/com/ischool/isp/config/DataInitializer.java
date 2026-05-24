package com.ischool.isp.config;

import com.ischool.isp.entity.User;
import com.ischool.isp.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        User admin = userMapper.selectById(1L);
        if (admin != null) {
            String newHash = passwordEncoder.encode("admin123");
            if (!passwordEncoder.matches("admin123", admin.getPasswordHash())) {
                admin.setPasswordHash(newHash);
                userMapper.updateById(admin);
                log.info("已修正 admin 用户密码哈希");
            } else {
                log.info("admin 密码正常");
            }
        }
    }
}
