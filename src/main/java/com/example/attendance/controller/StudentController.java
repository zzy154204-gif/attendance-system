
package com.example.attendance.controller;

import com.example.attendance.entity.Student;
import jakarta.validation.Valid;
import com.example.attendance.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 学生管理与示例任务接口。
 * <p>包含学生 CRUD，以及课程作业中要求的基础查询/打卡接口。</p>
 */
@RestController
public class StudentController {
    @Autowired
    private StudentService studentService;

    /**
     * 新增学生。
     *
     * @param student 学生信息
     * @return 操作结果
     */
    @PostMapping({"/add", "/students"})
    public String add(@Valid @RequestBody Student student) {
        studentService.addStudent(student);
        return "成功";
    }

    /**
     * 查询全部学生。
     *
     * @return 学生列表
     */
    @GetMapping("/students")
    public List<Student> getAllStudents() {
        return studentService.getAllStudents();
    }

    /**
     * 按主键 ID 查询学生。
     *
     * @param id 学生主键
     * @return 学生信息
     */
    @GetMapping("/students/{id}")
    public Student getStudentById(@PathVariable("id") Long id) {
        return studentService.getStudentById(id);
    }

    /**
     * 更新学生。
     *
     * @param id 学生主键
     * @param student 待更新的数据
     * @return 操作结果
     */
    @PutMapping("/students/{id}")
    public String updateStudent(@PathVariable("id") Long id, @Valid @RequestBody Student student) {
        student.setId(id);
        studentService.updateStudent(student);
        return "更新成功";
    }

    /**
     * 删除学生。
     *
     * @param id 学生主键
     * @return 操作结果
     */
    @DeleteMapping("/students/{id}")
    public String deleteStudent(@PathVariable("id") Long id) {
        studentService.deleteStudent(id);
        return "删除成功";
    }

    /**
     * 任务一：返回固定的学生信息示例。
     *
     * @return 示例信息（姓名、学号、班级）
     */
    @GetMapping("/student/info")
    public Map<String, String> getStudentInfo() {
        return Map.of(
                "name", "张宗元",
                "studentId", "42411134",
                "class", "计算机2班"
        );
    }

    /**
     * 任务二：模拟学生打卡。
     *
     * @param body 请求体，需包含 studentId
     * @return 打卡结果
     */
    @PostMapping("/student/attendance")
    public String attendance(@RequestBody Map<String, String> body) {
        String studentId = body.get("studentId");
        return "学号为 " + studentId + " 的学生打卡成功！";
    }

    /**
     * 任务三：返回课程列表示例。
     *
     * @return 课程名称列表
     */
    @GetMapping("/student/courses")
    public List<String> getCourses() {
        return Arrays.asList("机器学习", "数据库原理", "Java EE开发", "计算机组成原理");
    }
}
