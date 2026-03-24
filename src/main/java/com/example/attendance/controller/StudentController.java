
package com.example.attendance.controller;

import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
public class StudentController {

    // 任务一：GET /student/info
    @GetMapping("/student/info")
    public Map<String, String> getStudentInfo() {
        return Map.of(
                "name", "张宗元",
                "studentId", "42411134",
                "class", "计算机2班"
        );
    }

    // 任务二：POST /student/attendance
    @PostMapping("/student/attendance")
    public String attendance(@RequestBody Map<String, String> body) {
        String studentId = body.get("studentId");
        return "学号为 " + studentId + " 的学生打卡成功！";
    }

    // 任务三：GET /student/courses
    @GetMapping("/student/courses")
    public List<String> getCourses() {
        return Arrays.asList("机器学习", "数据库原理", "Java EE开发", "计算机组成原理");
    }
}