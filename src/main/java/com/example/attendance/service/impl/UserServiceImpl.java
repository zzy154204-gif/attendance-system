package com.example.attendance.service.impl;

import com.example.attendance.dao.UserDao;
import com.example.attendance.dto.LoginRequest;
import com.example.attendance.dto.RegisterRequest;
import com.example.attendance.entity.User;
import com.example.attendance.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List; // 记得这里也要导入 java.util.List

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 1. 根据用户名查询（之前写的）
    @Override
    public User getByUsername(String username) {
        return userDao.findByUsername(username);
    }

    // 2. 查询所有用户
    @Override
    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    // 3. 根据 ID 查询
    @Override
    public User getById(Integer id) {
        return userDao.findById(id).orElse(null);
    }

    // 4. 更新用户
    @Override
    public void update(User user) {
        userDao.save(user);
    }

    // 5. 删除用户
    @Override
    public void delete(Integer id) {
        userDao.deleteById(id);
    }

    @Override
    public User register(RegisterRequest request) {
        // 注册功能添加：先校验用户名是否重复，再加密密码后入库。
        User existing = userDao.findByUsername(request.username());
        if (existing != null) {
            throw new IllegalArgumentException("用户名已存在");
        }

        String normalizedRealName = request.realName();
        if (normalizedRealName == null || normalizedRealName.isBlank()) {
            normalizedRealName = request.username();
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRealName(normalizedRealName);
        user.setRole(normalizeRole(request.role()));

        return userDao.save(user);
    }

    @Override
    public User registerWithConfirm(String username, String password, String confirmPassword) {
        // 注册页面校验添加：确认两次密码一致后再调用注册逻辑。
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("两次输入的密码不一致");
        }
        // 页面注册未填写姓名时，用用户名作为默认展示名。
        RegisterRequest request = new RegisterRequest(username, password, username, "STUDENT");
        return register(request);
    }

    @Override
    public User login(LoginRequest request) {
        // 登录功能添加：按用户名查询并校验密码是否匹配。
        User user = userDao.findByUsername(request.username());
        if (user == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        return user;
    }

    private String normalizeRole(String role) {
        // 角色标准化：空值默认学生角色，避免后续权限判断空指针。
        if (role == null || role.isBlank()) {
            return "STUDENT";
        }
        return role.trim().toUpperCase();
    }
}