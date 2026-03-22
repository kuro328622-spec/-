package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 项目主启动类 整合了JPA启用配置和静态资源（头像）访问配置
 */
@SpringBootApplication
// 修复：明确指定JPA Repository接口的扫描路径
// "com.example.demo" 是所有Repository接口所在包的根路径
@EnableJpaRepositories(basePackages = "com.example.demo")
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    /**
     * 配置静态资源访问规则：允许前端通过URL访问服务器本地的头像文件
     */
    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                // 获取项目根目录，拼接头像上传路径
                String avatarUploadPath = System.getProperty("user.dir") + "/uploads/avatars/";
                // 配置URL前缀与本地文件路径的映射
                registry.addResourceHandler("/api/avatars/**")
                        .addResourceLocations("file:" + avatarUploadPath);
            }
        };
    }
}
