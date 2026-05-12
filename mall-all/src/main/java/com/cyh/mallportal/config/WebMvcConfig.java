package com.cyh.mallportal.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.images-path:./uploads/images}")
    private String imagesPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 /images/** 映射到文件系统中的图片目录
        //访问格式：http://localhost:8888/api/images/{dateDir}/{fileName}
        String imageLocation = new File(imagesPath).toURI().toString();
        registry.addResourceHandler("/images/**")
                .addResourceLocations(imageLocation);
    }
}
