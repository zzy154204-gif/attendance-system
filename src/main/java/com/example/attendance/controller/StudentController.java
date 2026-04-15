
package com.example.attendance.controller;

import com.example.attendance.entity.Student;
import com.example.attendance.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
public class StudentController {
    @Autowired
    private StudentService studentService;

    // 兼容旧路径 /add，同时支持 REST 风格 /students
    @PostMapping({"/add", "/students"})
    public String add(@RequestBody Student student) {
        studentService.addStudent(student);
        return "成功";
    }

    // 查询所有学生
    @GetMapping("/students")
    public List<Student> getAllStudents() {
        return studentService.getAllStudents();
    }

    // 根据 ID 查询学生
    @GetMapping("/students/{id}")
    public Student getStudentById(@PathVariable("id") Long id) {
        return studentService.getStudentById(id);
    }

    // 更新学生信息
    @PutMapping("/students/{id}")
    public String updateStudent(@PathVariable("id") Long id, @RequestBody Student student) {
        student.setId(id);
        studentService.updateStudent(student);
        return "更新成功";
    }

    // 删除学生
    @DeleteMapping("/students/{id}")
    public String deleteStudent(@PathVariable("id") Long id) {
        studentService.deleteStudent(id);
        return "删除成功";
    }

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
