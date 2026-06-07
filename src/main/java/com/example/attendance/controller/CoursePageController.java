package com.example.attendance.controller;

import com.example.attendance.aspect.LogOperation;
import com.example.attendance.entity.Course;
import com.example.attendance.service.CourseService;
import jakarta.validation.Valid;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 课程管理页面控制器。
 */
@Controller
@RequestMapping("/course")
public class CoursePageController {

    private final CourseService courseService;

    public CoursePageController(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * 课程列表页。
     */
    @GetMapping("/list")
    public String list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        PageRequest pageable = PageRequest.of(Math.max(page - 1, 0), size, Sort.by("weekDay").ascending());
        Page<Course> coursePage = courseService.findPage(pageable);

        model.addAttribute("courses", coursePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", coursePage.getTotalPages());
        return "course-list";
    }

    /**
     * 新增课程页。
     */
    @GetMapping("/add")
    public String addPage(Model model) {
        model.addAttribute("course", new Course());
        model.addAttribute("formAction", "/course/save");
        model.addAttribute("formTitle", "新增课程");
        return "course-form";
    }

    /**
     * 编辑课程页。
     */
    @GetMapping("/edit/{id}")
    public String editPage(@PathVariable Long id, Model model) {
        Course course = courseService.getById(id);
        model.addAttribute("course", course);
        model.addAttribute("formAction", "/course/update");
        model.addAttribute("formTitle", "编辑课程");
        return "course-form";
    }

    /**
     * 保存新增课程。
     */
    @LogOperation(operation = "CREATE", target = "Course")
    @PostMapping("/save")
    public String save(@Valid Course course, BindingResult result,
                       Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("formAction", "/course/save");
            model.addAttribute("formTitle", "新增课程");
            return "course-form";
        }
        try {
            courseService.addCourse(course);
            redirectAttributes.addFlashAttribute("successMsg", "课程添加成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/course/list";
    }

    /**
     * 保存编辑课程。
     */
    @LogOperation(operation = "UPDATE", target = "Course")
    @PostMapping("/update")
    public String update(@Valid Course course, BindingResult result,
                         Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("formAction", "/course/update");
            model.addAttribute("formTitle", "编辑课程");
            return "course-form";
        }
        try {
            courseService.updateCourse(course);
            redirectAttributes.addFlashAttribute("successMsg", "课程更新成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/course/list";
    }

    /**
     * 删除课程。
     */
    @LogOperation(operation = "DELETE", target = "Course")
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            courseService.deleteCourse(id);
            redirectAttributes.addFlashAttribute("successMsg", "课程已删除");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/course/list";
    }
}
