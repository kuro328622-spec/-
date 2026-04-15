package com.example.demo.service;

import com.example.demo.entity.SysUser;
import java.util.List; // 确保导入了 List

/**
 * 用户业务逻辑接口
 */
public interface SysUserService {

    /**
     * 用户注册
     *
     * @param sysUser 前端传来的用户信息（包含用户名、密码等）
     * @return 注册成功后，从数据库返回的完整用户信息（包含ID和创建时间）
     */
    SysUser register(SysUser sysUser);

    /**
     * 根据用户名查找用户
     *
     * @param username 用户名
     * @return 找到的用户对象，如果找不到则返回null
     */
    SysUser findByUsername(String username);

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 明文密码
     * @return 如果登录成功，返回用户对象；如果用户名或密码错误，返回null
     */
    SysUser login(String username, String password);

    /**
     * 更新用户的个人资料（昵称和头像）
     *
     * @param username 用户名
     * @param nickname 新的昵称
     * @param avatarUrl 新的头像URL
     */
    void updateProfile(String username, String nickname, String avatarUrl);

    /**
     * 修改用户密码
     *
     * @param username 用户名
     * @param newPassword 新的明文密码
     */
    void updatePassword(String username, String newPassword);

    /**
     * 更新用户的个人资料（包括昵称和密码）
     *
     * @param username 用户名
     * @param newNickname 新的昵称
     * @param newPassword 新的明文密码
     */
    void updateProfileWithPassword(String username, String newNickname, String newPassword);

    /**
     * 专门用于更新用户的头像URL
     *
     * @param username 用户名
     * @param newAvatarUrl 新的头像URL
     */
    void updateAvatar(String username, String newAvatarUrl);

    /**
     * 【保留】根据用户ID查询该用户的所有角色名称
     *
     * @param userId 用户ID
     * @return 角色名称列表，例如 ["超级管理员"]
     */
    List<String> findRoleNamesByUserId(Long userId);

    /**
     * 【新增方法】根据用户ID查询该用户的所有角色CODE
     *
     * @param userId 用户ID
     * @return 角色CODE列表，例如 ["ROLE_ADMIN"]
     */
    List<String> findRoleCodesByUserId(Long userId);
}
