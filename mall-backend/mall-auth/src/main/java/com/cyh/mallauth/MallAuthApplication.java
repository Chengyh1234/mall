package com.cyh.mallauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.cyh.mallauth", "com.cyh.mallcommon"})
@EnableDiscoveryClient
@EnableFeignClients
public class MallAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(MallAuthApplication.class, args);
    }
}