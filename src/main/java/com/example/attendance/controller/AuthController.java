package com.example.attendance.controller;

import com.example.attendance.dto.AuthResponse;
import com.example.attendance.dto.LoginRequest;
import com.example.attendance.dto.RegisterRequest;
import com.example.attendance.entity.User;
import com.example.attendance.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录与注册接口。
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 注册接口：创建新账号并保存加密后的密码。
     */
    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request);
        return new AuthResponse("注册成功", user.getId(), user.getUsername(), user.getRealName(), user.getRole());
    }

    /**
     * 登录接口：校验用户名与密码。
     */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        User user = userService.login(request);
        return new AuthResponse("登录成功", user.getId(), user.getUsername(), user.getRealName(), user.getRole());
    }
}

