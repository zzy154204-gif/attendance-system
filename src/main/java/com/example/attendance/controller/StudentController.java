package com.example.attendance.controller;

import com.example.attendance.dto.ApiResponse;
import com.example.attendance.entity.Student;
import jakarta.validation.Valid;
import com.example.attendance.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生管理 REST API 接口。
 */
@RestController
public class StudentController {
    @Autowired
    private StudentService studentService;

    /**
     * 新增学生。
     */
    @PostMapping({"/add", "/students"})
    public ApiResponse<Student> add(@Valid @RequestBody Student student) {
        studentService.addStudent(student);
        return ApiResponse.success("新增成功", student);
    }

    /**
     * 查询全部学生。
     */
    @GetMapping("/students")
    public ApiResponse<List<Student>> getAllStudents() {
        return ApiResponse.success(studentService.getAllStudents());
    }

    /**
     * 按主键 ID 查询学生。
     */
    @GetMapping("/students/{id}")
    public ApiResponse<Student> getStudentById(@PathVariable("id") Long id) {
        return ApiResponse.success(studentService.getStudentById(id));
    }

    /**
     * 更新学生。
     */
    @PutMapping("/students/{id}")
    public ApiResponse<Student> updateStudent(@PathVariable("id") Long id, @Valid @RequestBody Student student) {
        student.setId(id);
        studentService.updateStudent(student);
        return ApiResponse.success("更新成功", student);
    }

    /**
     * 删除学生。
     */
    @DeleteMapping("/students/{id}")
    public ApiResponse<Void> deleteStudent(@PathVariable("id") Long id) {
        studentService.deleteStudent(id);
        return ApiResponse.success("删除成功");
    }
}
