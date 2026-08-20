package com.cyh.mallstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * mall-store 店铺服务启动类
 * 提供店铺管理、轮播图管理、文件上传等功能
 * 注册到 Nacos 服务中心，通过网关路由对外提供服务
 */
@SpringBootApplication(scanBasePackages = {"com.cyh.mallstore", "com.cyh.mallcommon"})
@EnableDiscoveryClient
public class MallStoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(MallStoreApplication.class, args);
    }
}