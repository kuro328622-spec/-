package com.example.demo.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.LoginRequest;
import com.example.demo.entity.SysUser;
import com.example.demo.service.SysUserService;
import com.example.demo.util.JwtUtil;

import jakarta.validation.Valid;

/**
 * 用户控制器，负责处理与用户相关的HTTP请求 - 注册 - 根据用户名查询 - 登录 - 获取/更新个人资料 (包括昵称和密码) - 上传头像
 * (仅上传文件) - 更新头像 (仅更新数据库)
 */
@RestController
@RequestMapping("/api/users")
public class SysUserController {

    // 【修复 1: 修正了 @Autowired 的语法错误】
    @Autowired
    private SysUserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SysUser sysUser) {
        SysUser registeredUser = userService.register(sysUser);
        if (registeredUser != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "注册成功！");
            response.put("data", registeredUser);
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "注册失败，用户名已存在。");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        SysUser user = userService.findByUsername(username);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        SysUser user = userService.login(loginRequest.getUsername(), loginRequest.getPassword());
        if (user == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "登录失败，用户名或密码错误。");
            return ResponseEntity.status(401).body(response);
        }

        String token = jwtUtil.generateToken(user.getUsername());
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "登录成功！");

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("avatarUrl", user.getAvatarUrl());
        userInfo.put("roleId", user.getRole().getId());

        // 【核心修复 2: 添加 roles 字段】
        List<String> roles = new ArrayList<>();
        // 增加健壮性判断，防止 user.getRole() 为 null
        if (user.getRole() != null && user.getRole().getRoleCode() != null) {
            roles.add(user.getRole().getRoleCode()); // 例如："ROLE_ADMIN"
        }
        userInfo.put("roles", roles);

        userInfo.put("createTime", user.getCreateTime());

        response.put("data", userInfo);
        response.put("token", token);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUserProfile(Authentication authentication) {
        String username = authentication.getName();
        SysUser user = userService.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(404).body("用户不存在");
        }

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("nickname", user.getNickname());
        profile.put("avatarUrl", user.getAvatarUrl());

        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(@RequestBody Map<String, String> updates, Authentication authentication) {
        String username = authentication.getName();
        String newNickname = updates.get("nickname");
        String newPassword = updates.get("password");
        userService.updateProfileWithPassword(username, newNickname, newPassword);
        return ResponseEntity.ok("个人资料更新成功");
    }

    @PutMapping("/password")
    public ResponseEntity<?> updateUserPassword(@RequestBody Map<String, String> passwords, Authentication authentication) {
        String username = authentication.getName();
        String newPassword = passwords.get("newPassword");
        if (newPassword == null || newPassword.isEmpty()) {
            return ResponseEntity.badRequest().body("新密码不能为空");
        }
        userService.updatePassword(username, newPassword);
        return ResponseEntity.ok("密码修改成功");
    }

    @PostMapping("/upload-avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file, Authentication authentication) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("上传的文件不能为空");
        }

        String uploadDir = System.getProperty("user.dir") + "/uploads/avatars/";
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString() + extension;
        File dest = new File(uploadDir + newFileName);

        try {
            file.transferTo(dest);
            String avatarUrl = "/api/avatars/" + newFileName;

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "头像上传成功，请点击保存以生效。");
            response.put("avatarUrl", avatarUrl);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("文件上传失败: " + e.getMessage());
        }
    }

    @PutMapping("/avatar")
    public ResponseEntity<?> updateUserAvatar(@RequestBody Map<String, String> request, Authentication authentication) {
        String username = authentication.getName();
        String newAvatarUrl = request.get("avatarUrl");

        if (newAvatarUrl == null || newAvatarUrl.isEmpty()) {
            return ResponseEntity.badRequest().body("头像URL不能为空");
        }

        userService.updateAvatar(username, newAvatarUrl);

        return ResponseEntity.ok("头像更新成功");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "输入信息有误，请检查！");
        response.put("errors", errors);
        return ResponseEntity.badRequest().body(response);
    }
}
