package com.cyh.mallportal.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.base-path:./uploads}")
    private String basePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String spuLocation = new File(basePath, "images/spu").toURI().toString();
        registry.addResourceHandler("/uploads/images/spu/**")
                .addResourceLocations(spuLocation);

        String skuLocation = new File(basePath, "images/sku").toURI().toString();
        registry.addResourceHandler("/uploads/images/sku/**")
                .addResourceLocations(skuLocation);

        String brandsLocation = new File(basePath, "images/brands").toURI().toString();
        registry.addResourceHandler("/uploads/images/brands/**")
                .addResourceLocations(brandsLocation);

        String storesLocation = new File(basePath, "images/stores").toURI().toString();
        registry.addResourceHandler("/uploads/images/stores/**")
                .addResourceLocations(storesLocation);

        String avatarsLocation = new File(basePath, "images/avatars").toURI().toString();
        registry.addResourceHandler("/uploads/images/avatars/**")
                .addResourceLocations(avatarsLocation);

        String bannersLocation = new File(basePath, "images/banners").toURI().toString();
        registry.addResourceHandler("/uploads/images/banners/**")
                .addResourceLocations(bannersLocation);
    }
}