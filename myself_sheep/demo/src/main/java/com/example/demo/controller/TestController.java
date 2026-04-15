package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    // 受保护的测试接口
    @GetMapping("/hello")
    public String hello() {
        return "恭喜！您已成功通过JWT认证，访问了受保护的资源！";
    }
}
