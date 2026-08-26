package com.cyh.mallgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 网关服务启动类
 *
 * @author cyh
 * @since 2024-01-01
 */
@SpringBootApplication
@EnableDiscoveryClient
public class MallGatewayApplication {

    public static void main(String[] args) {
        String workDir = System.getProperty("user.dir");
        System.out.println("-----------------当前工作目录: " + workDir);
        SpringApplication.run(MallGatewayApplication.class, args);
    }
}
