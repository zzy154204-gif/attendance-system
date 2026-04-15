package com.example.attendance.service;

import com.example.attendance.entity.User;

import java.util.List;

public interface UserService {
    // 根据用户名获取用户对象
    User getByUsername(String username);
    List<User> getAllUsers();
    User getById(Integer id);
    void update(User user);
    void delete(Integer id);
}