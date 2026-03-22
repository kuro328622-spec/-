package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.SysUser;

/**
 * 用户数据访问接口 Spring Data JPA会自动为这个接口提供实现
 */
@Repository // 声明这是一个Repository Bean，让Spring容器管理
public interface SysUserRepository extends JpaRepository<SysUser, Long> {

    /**
     * 根据用户名查询用户 Spring Data JPA会根据方法名自动生成对应的SQL：SELECT * FROM sys_user WHERE
     * username = ?
     *
     * @param username 用户名
     * @return
     * 返回一个Optional<SysUser>对象。Optional可以优雅地处理用户不存在的情况，避免NullPointerException。
     */
    Optional<SysUser> findByUsername(String username);
}
