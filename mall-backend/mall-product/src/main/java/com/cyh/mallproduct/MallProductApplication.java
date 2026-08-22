package com.cyh.mallproduct;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * mall-product 商品服务启动类
 * 提供 SPU/SKU 管理、品牌、分类、属性、商品搜索（ES）等功能
 * 注册到 Nacos 服务中心，通过网关路由对外提供服务
 */
@SpringBootApplication(scanBasePackages = {"com.cyh.mallproduct", "com.cyh.mallcommon"})
@EnableDiscoveryClient
@EnableFeignClients
public class MallProductApplication {
    public static void main(String[] args) {
        SpringApplication.run(MallProductApplication.class, args);
    }
}