package com.ischool.isp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class IspApplication {
    public static void main(String[] args) {
        SpringApplication.run(IspApplication.class, args);
    }
}
