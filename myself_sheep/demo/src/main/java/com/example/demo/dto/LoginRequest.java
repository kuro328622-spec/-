package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求的数据传输对象 (DTO) 专门用于接收前端传来的登录信息。
 *
 * 【最终修复版】: 移除了 Lombok 的 @Data 注解， 手动添加了所有必需的 getter 和 setter 方法，
 * 确保代码在任何环境下都能正常编译。
 */
public class LoginRequest {

    /**
     * 账号 (用户名)
     *
     * @NotBlank: 验证此字段不能为空。
     */
    @NotBlank(message = "账号不能为空")
    private String username;

    /**
     * 密码
     *
     * @NotBlank: 验证此字段不能为空。
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    // --- 手动添加的 Getter 和 Setter 方法 ---
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
