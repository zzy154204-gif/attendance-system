package com.example.attendance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class AttendanceSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(AttendanceSystemApplication.class, args);
    }

    @GetMapping("/hello")
    public String hello() {
        return "欢迎来到班级考勤管理系统!";
    }

    @GetMapping("/")
    public String index() {
        return "项目已启动，请访问 /hello、/about 或 /user/all";
    }

    @GetMapping("/about")
    public String about(){
        return "姓名：张宗元,专业：计算机科学与技术";
    }
}