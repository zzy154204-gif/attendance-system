package com.example.attendance.controller;

import com.example.attendance.entity.User;
import com.example.attendance.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户信息管理接口。
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 根据用户名查询用户信息。
     *
     * @param name 用户名
     * @return 用户信息
     */
    @GetMapping("/info/{name}")
    public User getUserInfo(@PathVariable("name") String name) {
        return userService.getByUsername(name);
    }

    /**
     * 查询全部用户。
     *
     * @return 用户列表
     */
    @GetMapping("/all")
    public List<User> getAll() {
        return userService.getAllUsers();
    }

    /**
     * 按主键 ID 查询用户。
     *
     * @param id 用户主键
     * @return 用户信息
     */
    @GetMapping("/id/{id}")
    public User getById(@PathVariable("id") Integer id) {
        return userService.getById(id);
    }

    /**
     * 更新用户信息。
     *
     * @param user 用户数据
     * @return 操作结果
     */
    @PutMapping("/update")
    public String update(@RequestBody User user) {
        userService.update(user);
        return "更新成功！";
    }

    /**
     * 删除用户。
     *
     * @param id 用户主键
     * @return 操作结果
     */
    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id) {
        userService.delete(id);
        return "刪除成功！";
    }
}