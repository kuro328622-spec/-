package com.example.demo.service.impl;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.demo.entity.SysUser;
import com.example.demo.service.SysUserService;

/**
 * 自定义UserDetailsService，用于从数据库加载用户信息和权限
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private SysUserService userService;

    /**
     * 根据用户名从数据库中加载用户信息及其权限
     *
     * @param username 用户名
     * @return UserDetails对象，包含用户信息和权限
     * @throws UsernameNotFoundException 如果用户不存在
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 调用我们自己的service方法查询用户
        SysUser sysUser = userService.findByUsername(username);

        // 2. 如果用户不存在，抛出异常
        if (sysUser == null) {
            throw new UsernameNotFoundException("用户 '" + username + "' 不存在");
        }

        // 3. 【核心修改】根据用户ID查询其所有角色的CODE
        //    注意：这里调用的方法名变了，从 findRoleNamesByUserId 改为 findRoleCodesByUserId
        List<String> roleCodes = userService.findRoleCodesByUserId(sysUser.getId());

        // 4. 将角色CODE列表转换为Spring Security需要的GrantedAuthority集合
        //    【重要修改】因为 role_code 已经包含 "ROLE_" 前缀，所以这里不再手动添加
        Collection<GrantedAuthority> authorities = roleCodes.stream()
                .map(roleCode -> new SimpleGrantedAuthority(roleCode)) // 直接使用 roleCode
                .collect(Collectors.toList());

        // 5. 返回包含用户名、密码和权限信息的UserDetails对象
        return new User(sysUser.getUsername(), sysUser.getPassword(), authorities);
    }
}
