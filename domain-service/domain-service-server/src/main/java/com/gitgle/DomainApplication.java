package com.gitgle;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
@MapperScan(basePackages = "com.gitgle.mapper")
public class DomainApplication {
    public static void main(String[] args) {
        SpringApplication.run(DomainApplication.class, args);
    }
}