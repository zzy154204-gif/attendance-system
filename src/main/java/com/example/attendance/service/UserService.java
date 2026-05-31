package com.example.attendance.service;

import com.example.attendance.dto.LoginRequest;
import com.example.attendance.dto.RegisterRequest;
import com.example.attendance.entity.User;

import java.util.List;

/**
 * 用户服务接口。
 */
public interface UserService {
    /**
     * 根据用户名查询用户。
     *
     * @param username 用户名
     * @return 用户实体
     */
    User getByUsername(String username);

    /**
     * 查询全部用户。
     *
     * @return 用户列表
     */
    List<User> getAllUsers();

    /**
     * 按主键查询用户。
     *
     * @param id 用户主键
     * @return 用户实体
     */
    User getById(Integer id);

    /**
     * 更新用户。
     *
     * @param user 用户实体
     */
    void update(User user);

    /**
     * 删除用户。
     *
     * @param id 用户主键
     */
    void delete(Integer id);

    /**
     * 注册新用户并返回用户信息。
     *
     * @param request 注册请求
     * @return 用户实体
     */
    User register(RegisterRequest request);

    /**
     * 登录校验并返回用户信息。
     *
     * @param request 登录请求
     * @return 用户实体
     */
    User login(LoginRequest request);

    /**
     * 注册页面提交：校验两次密码一致后注册。
     *
     * @param username 用户名
     * @param password 密码
     * @param confirmPassword 确认密码
     * @return 用户实体
     */
    User registerWithConfirm(String username, String password, String confirmPassword, String role);
}