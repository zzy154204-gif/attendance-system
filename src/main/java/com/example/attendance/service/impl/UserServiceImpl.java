package com.example.attendance.service.impl;

import com.example.attendance.dao.UserDao;
import com.example.attendance.entity.User;
import com.example.attendance.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List; // 记得这里也要导入 java.util.List

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;

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
        return userDao.getById(id);
    }

    // 4. 更新用户
    @Override
    public List<Object> update(User user) {
        userDao.update(user);
        return null;
    }

    // 5. 删除用户
    @Override
    public void delete(Integer id) {
        userDao.deleteById(id);
    }
}