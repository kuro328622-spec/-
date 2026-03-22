package com.example.demo.handler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

// 全局异常处理器：只捕获业务异常，返回友好提示
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 捕获所有RuntimeException（包含我们抛的业务异常）
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(RuntimeException e) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 400); // 错误码
        result.put("message", e.getMessage()); // 友好提示（比如“入库数量必须大于0”）
        result.put("data", null);
        return ResponseEntity.badRequest().body(result);
    }
}
