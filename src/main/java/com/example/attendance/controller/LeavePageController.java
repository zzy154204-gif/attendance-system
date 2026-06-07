package com.example.attendance.controller;

import com.example.attendance.aspect.LogOperation;
import com.example.attendance.dto.CourseOption;
import com.example.attendance.entity.Course;
import com.example.attendance.entity.LeaveApplication;
import com.example.attendance.entity.Student;
import com.example.attendance.service.CourseService;
import com.example.attendance.service.LeaveApplicationService;
import com.example.attendance.service.StudentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalTime;
import java.util.List;

/**
 * 请假申请页面控制器。
 */
@Controller
public class LeavePageController {

    private final LeaveApplicationService leaveApplicationService;
    private final StudentService studentService;
    private final CourseService courseService;

    public LeavePageController(LeaveApplicationService leaveApplicationService,
                               StudentService studentService,
                               CourseService courseService) {
        this.leaveApplicationService = leaveApplicationService;
        this.studentService = studentService;
        this.courseService = courseService;
    }

    /**
     * 请假申请页面。
     */
    @GetMapping("/leave/apply")
    public String applyPage(Model model) {
        model.addAttribute("courses", buildCourses());
        return "leave-apply";
    }

    /**
     * 提交请假申请。
     */
    @LogOperation(operation = "APPLY_LEAVE", target = "Leave")
    @PostMapping("/leave/apply")
    public String apply(
            @RequestParam Integer courseId,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam String reason,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        String username = principal == null ? "" : principal.getName();
        Student student = studentService.getStudentByStudentNumber(username);
        if (student == null) {
            redirectAttributes.addFlashAttribute("errorMsg", "未找到学生信息，请先维护学生档案");
            return "redirect:/leave/apply";
        }

        CourseOption course = findCourse(courseId);
        if (course == null) {
            redirectAttributes.addFlashAttribute("errorMsg", "课程不存在");
            return "redirect:/leave/apply";
        }

        try {
            LeaveApplication application = new LeaveApplication();
            application.setStudent(student);
            application.setCourseId(course.id());
            application.setCourseName(course.name());
            application.setStartTime(java.time.LocalDateTime.parse(startTime));
            application.setEndTime(java.time.LocalDateTime.parse(endTime));
            application.setReason(reason);

            leaveApplicationService.apply(application);
            redirectAttributes.addFlashAttribute("successMsg", "请假申请已提交，请等待审批");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/leave/list";
    }

    /**
     * 请假记录列表。
     */
    @GetMapping("/leave/list")
    public String list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            Principal principal,
            Model model,
            Authentication authentication
    ) {
        PageRequest pageable = PageRequest.of(Math.max(page - 1, 0), size, Sort.by("applyTime").descending());
        Page<LeaveApplication> leavePage;

        boolean isTeacherOrAdmin = isTeacherOrAdmin(authentication);
        if (isTeacherOrAdmin) {
            if (status != null && !status.isBlank()) {
                leavePage = leaveApplicationService.findByStatus(status, pageable);
            } else {
                leavePage = leaveApplicationService.findAll(pageable);
            }
        } else {
            String username = principal == null ? "" : principal.getName();
            Student student = studentService.getStudentByStudentNumber(username);
            if (student != null) {
                leavePage = leaveApplicationService.findByStudent(student.getId(), pageable);
            } else {
                leavePage = Page.empty(pageable);
            }
        }

        model.addAttribute("records", leavePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", leavePage.getTotalPages());
        model.addAttribute("isTeacher", isTeacherOrAdmin);
        model.addAttribute("status", status == null ? "" : status);
        return "leave-list";
    }

    /**
     * 审批请假（教师/管理员）。
     */
    @LogOperation(operation = "APPROVE_LEAVE", target = "Leave")
    @PostMapping("/leave/approve/{id}")
    public String approve(
            @PathVariable Long id,
            @RequestParam boolean approved,
            @RequestParam(required = false) String remark,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        try {
            String approver = principal == null ? "系统" : principal.getName();
            leaveApplicationService.approve(id, approved, remark, approver);
            redirectAttributes.addFlashAttribute("successMsg", approved ? "已批准" : "已拒绝");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/leave/list";
    }

    private List<CourseOption> buildCourses() {
        // 优先从数据库加载课程，若无则使用默认课程
        try {
            List<Course> courses = courseService.getAllCourses();
            if (courses != null && !courses.isEmpty()) {
                return courses.stream()
                        .map(c -> new CourseOption(c.getId().intValue(), c.getName(),
                                c.getStartTime() != null ? c.getStartTime() : LocalTime.of(8, 0)))
                        .toList();
            }
        } catch (Exception ignored) {
            // CourseService 不可用时回退到硬编码课程
        }
        return List.of(
                new CourseOption(1, "Java程序设计", LocalTime.of(8, 0)),
                new CourseOption(2, "数据库原理", LocalTime.of(10, 0)),
                new CourseOption(3, "Java EE开发", LocalTime.of(14, 0))
        );
    }

    private CourseOption findCourse(Integer courseId) {
        if (courseId == null) return null;
        return buildCourses().stream()
                .filter(c -> c.id().equals(courseId))
                .findFirst().orElse(null);
    }

    private boolean isTeacherOrAdmin(Authentication auth) {
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_TEACHER".equals(a.getAuthority()) || "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
