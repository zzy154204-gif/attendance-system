package com.example.attendance.controller;

import com.example.attendance.dto.ApiResponse;
import com.example.attendance.entity.User;
import com.example.attendance.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户信息管理 REST API 接口。
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 根据用户名查询用户信息。
     */
    @GetMapping("/info/{name}")
    public ApiResponse<User> getUserInfo(@PathVariable("name") String name) {
        return ApiResponse.success(userService.getByUsername(name));
    }

    /**
     * 查询全部用户。
     */
    @GetMapping("/all")
    public ApiResponse<List<User>> getAll() {
        return ApiResponse.success(userService.getAllUsers());
    }

    /**
     * 按主键 ID 查询用户。
     */
    @GetMapping("/id/{id}")
    public ApiResponse<User> getById(@PathVariable("id") Integer id) {
        return ApiResponse.success(userService.getById(id));
    }

    /**
     * 更新用户信息。
     */
    @PutMapping("/update")
    public ApiResponse<User> update(@RequestBody User user) {
        userService.update(user);
        return ApiResponse.success("更新成功", user);
    }

    /**
     * 删除用户。
     */
    @DeleteMapping("/delete/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Integer id) {
        userService.delete(id);
        return ApiResponse.success("删除成功");
    }
}
