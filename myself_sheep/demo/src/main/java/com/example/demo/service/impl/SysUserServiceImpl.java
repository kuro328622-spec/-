package com.example.demo.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.entity.SysRole;
import com.example.demo.entity.SysUser;
import com.example.demo.repository.SysUserRepository;
import com.example.demo.service.SysUserService;

/**
 * 用户业务逻辑实现类
 */
@Service
public class SysUserServiceImpl implements SysUserService {

    // 添加日志记录器
    private static final Logger log = LoggerFactory.getLogger(SysUserServiceImpl.class);

    @Autowired
    private SysUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public SysUser register(SysUser sysUser) {
        Optional<SysUser> existingUser = userRepository.findByUsername(sysUser.getUsername());
        if (existingUser.isPresent()) {
            System.out.println("用户名已存在！");
            return null;
        }
        String encodedPassword = passwordEncoder.encode(sysUser.getPassword());
        sysUser.setPassword(encodedPassword);
        SysRole defaultRole = new SysRole();
        defaultRole.setId(2L); // 假设默认角色ID为2
        sysUser.setRole(defaultRole);
        return userRepository.save(sysUser);
    }

    @Override
    public SysUser findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    @Override
    public SysUser login(String username, String password) {
        // 【优化】直接使用 repository 查询，避免在 findByUsername 中再次查询
        Optional<SysUser> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            SysUser user = userOptional.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return user;
            }
        }
        return null;
    }

    @Override
    public void updateProfile(String username, String nickname, String avatarUrl) {
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (nickname != null && !nickname.isEmpty()) {
            user.setNickname(nickname);
        }
        if (avatarUrl != null) {
            user.setAvatarUrl(avatarUrl);
        }

        userRepository.save(user);
    }

    @Override
    public void updatePassword(String username, String newPassword) {
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        String encodedNewPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedNewPassword);
        userRepository.save(user);
    }

    // 【修复】修正了方法签名中的语法错误
    @Override
    public void updateProfileWithPassword(String username, String newNickname, String newPassword) {
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (newNickname != null && !newNickname.isEmpty()) {
            user.setNickname(newNickname);
        }
        if (newPassword != null && !newPassword.isEmpty()) {
            String encodedNewPassword = passwordEncoder.encode(newPassword);
            user.setPassword(encodedNewPassword);
        }

        userRepository.save(user);
    }

    @Override
    public void updateAvatar(String username, String newAvatarUrl) {
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        user.setAvatarUrl(newAvatarUrl);
        userRepository.save(user);
    }

    @Override
    public List<String> findRoleNamesByUserId(Long userId) {
        Optional<SysUser> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            SysUser user = userOptional.get();
            SysRole role = user.getRole();
            if (role != null && role.getRoleName() != null) {
                List<String> roleNames = new ArrayList<>();
                roleNames.add(role.getRoleName());
                return roleNames;
            }
        }
        return new ArrayList<>();
    }

    @Override
    public List<String> findRoleCodesByUserId(Long userId) {
        log.info("--- 开始执行 findRoleCodesByUserId，用户ID: {}", userId);

        Optional<SysUser> userOptional = userRepository.findById(userId);

        if (userOptional.isPresent()) {
            SysUser user = userOptional.get();
            SysRole role = user.getRole();

            log.info("从 user 对象中获取的 role 对象: {}", role);

            if (role != null) {
                log.info("role 对象不为 null，其 ID: {}, Name: {}, Code: {}", role.getId(), role.getRoleName(), role.getRoleCode());

                if (role.getRoleCode() != null) {
                    List<String> roleCodes = new ArrayList<>();
                    roleCodes.add(role.getRoleCode());
                    log.info("成功获取 roleCode: {}，准备返回", roleCodes);
                    return roleCodes;
                } else {
                    log.error("!!! 错误：role 对象不为 null，但其 roleCode 为 null！");
                }
            } else {
                log.error("!!! 错误：从 user 对象中获取的 role 为 null！");
            }
        } else {
            log.error("!!! 错误：根据用户ID {} 未找到用户！", userId);
        }

        log.warn("方法执行结束，返回空的 roleCodes 列表。");
        return new ArrayList<>();
    }
}
