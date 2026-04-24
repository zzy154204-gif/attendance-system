package com.example.attendance.controller;

import com.example.attendance.common.Result;
import com.example.attendance.entity.User;
import com.example.attendance.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController // 告诉 Spring 这是一个返回 JSON 数据的接口控制器
@RequestMapping("/user") // 这一组接口都以 /user 开头
public class UserController {

    @Autowired
    private UserService userService;

    // 登录：POST /user/login   body: {"username":"xxx","password":"xxx"}
    // TODO: 后续改用 BCrypt 加密存储密码（见 README 第一优先级）
    @PostMapping("/login")
    public Result<User> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        User user = userService.getByUsername(username);
        if (user == null || !user.getPassword().equals(password)) {
            return Result.error(401, "用户名或密码错误");
        }
        return Result.success("登录成功", user);
    }

    // 路径参数查询，访问地址：localhost:8080/user/info/zy_student
    @GetMapping("/info/{name}")
    public User getUserInfo(@PathVariable("name") String name) {
        return userService.getByUsername(name);
    }
    // 查詢所有：GET http://localhost:8080/user/all
    @GetMapping("/all")
    public List<User> getAll() {
        return userService.getAllUsers();
    }

    // 根據 ID 查詢：GET http://localhost:8080/user/id/1
    @GetMapping("/id/{id}")
    public User getById(@PathVariable("id") Integer id) {
        return userService.getById(id);
    }

    // 更新：PUT http://localhost:8080/user/update
    @PutMapping("/update")
    public String update(@RequestBody User user) {
        userService.update(user);
        return "更新成功！";
    }

    // 刪除：DELETE http://localhost:8080/user/delete/1
    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id) {
        userService.delete(id);
        return "刪除成功！";
    }
}