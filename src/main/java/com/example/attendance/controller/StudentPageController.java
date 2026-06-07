package com.example.attendance.controller;

import com.example.attendance.aspect.LogOperation;
import com.example.attendance.entity.Student;
import com.example.attendance.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 学生表单与列表页面控制器。
 */
@Controller
@RequestMapping("/student")
public class StudentPageController {

    @Autowired
    private StudentService studentService;

    /**
     * 学生列表页（分页 + 搜索）。
     */
    @GetMapping("/list")
    public String list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            Model model
    ) {
        PageRequest pageable = PageRequest.of(Math.max(page - 1, 0), size, Sort.by("studentNumber").ascending());
        Page<Student> studentPage = studentService.findPage(keyword, pageable);

        model.addAttribute("students", studentPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", studentPage.getTotalPages());
        model.addAttribute("totalElements", studentPage.getTotalElements());
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        return "student-list";
    }

    /**
     * 新增学生页。
     */
    @GetMapping("/add")
    public String addPage(Model model) {
        model.addAttribute("student", new Student());
        model.addAttribute("formAction", "/student/save");
        model.addAttribute("formTitle", "新增学生");
        return "student-form";
    }

    /**
     * 编辑学生页（数据回显）。
     */
    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable Long id, Model model) {
        Student student = studentService.getStudentById(id);
        if (student == null) {
            return "redirect:/student/list";
        }
        model.addAttribute("student", student);
        model.addAttribute("formAction", "/student/update");
        model.addAttribute("formTitle", "编辑学生");
        return "student-form";
    }

    /**
     * 保存新增学生。
     */
    @PostMapping("/save")
    public String save(@Valid Student student, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formAction", "/student/save");
            model.addAttribute("formTitle", "新增学生");
            return "student-form";
        }
        studentService.addStudent(student);
        return "redirect:/student/list";
    }

    /**
     * 保存编辑学生。
     */
    @PostMapping("/update")
    public String update(@Valid Student student, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formAction", "/student/update");
            model.addAttribute("formTitle", "编辑学生");
            return "student-form";
        }
        studentService.updateStudent(student);
        return "redirect:/student/list";
    }

    /**
     * 删除学生。注意：必须使用 POST，GET 请求不应执行写操作。
     */
    @LogOperation(operation = "DELETE", target = "Student")
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return "redirect:/student/list";
    }
}


