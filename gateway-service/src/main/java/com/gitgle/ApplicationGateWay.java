package com.gitgle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@SpringBootApplication
public class ApplicationGateWay {
    public static void main(String[] args) {
        SpringApplication.run(ApplicationGateWay.class, args);
    }
}