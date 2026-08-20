package com.cyh.malluser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.cyh.malluser", "com.cyh.mallcommon"})
@EnableDiscoveryClient
public class MallUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(MallUserApplication.class, args);
    }
}