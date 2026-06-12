package com.example.attendance.service.impl;

import com.example.attendance.dao.StudentDao;
import com.example.attendance.dao.UserDao;
import com.example.attendance.dto.LoginRequest;
import com.example.attendance.dto.RegisterRequest;
import com.example.attendance.entity.Student;
import com.example.attendance.entity.User;
import com.example.attendance.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private StudentDao studentDao;

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
        // 1. 校验用户名是否重复
        User existing = userDao.findByUsername(request.username());
        if (existing != null) {
            throw new IllegalArgumentException("用户名已存在");
        }

        // 2. 姓名默认用用户名
        String realName = request.realName();
        if (realName == null || realName.isBlank()) {
            realName = request.username();
        }

        // 3. 保存 User
        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRealName(realName);
        user.setRole(normalizeRole(request.role()));
        User savedUser = userDao.save(user);

        // 4. 学生角色自动创建 Student 记录（失败不影响注册）
        if ("STUDENT".equals(savedUser.getRole())) {
            try {
                Student studentExisting = studentDao.findByStudentNumber(savedUser.getUsername());
                if (studentExisting == null) {
                    Student student = new Student();
                    student.setStudentNumber(savedUser.getUsername());
                    student.setName(realName);
                    student.setGender("");
                    student.setContact("");
                    student.setClazz("");
                    student.setBirthDate(java.time.LocalDate.of(2000, 1, 1));
                    studentDao.save(student);
                    log.info("注册时自动创建 Student 成功：{}", savedUser.getUsername());
                }
            } catch (Exception e) {
                // Student 创建失败不阻断注册，后续打卡时会兜底创建
                log.error("自动创建 Student 失败（不影响注册）：username={}, error={}",
                        savedUser.getUsername(), e.getMessage());
            }
        }

        return savedUser;
    }

    @Override
    public User registerWithConfirm(String username, String password, String confirmPassword, String role) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("两次输入的密码不一致");
        }
        RegisterRequest request = new RegisterRequest(username, password, username, normalizeRole(role));
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