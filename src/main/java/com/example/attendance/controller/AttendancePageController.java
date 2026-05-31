package com.example.attendance.controller;

import com.example.attendance.dto.CourseOption;
import com.example.attendance.dto.RegisterForm;
import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.Student;
import com.example.attendance.service.AttendanceService;
import com.example.attendance.service.StudentService;
import com.example.attendance.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.io.InputStream;
import com.example.attendance.service.ExcelHelper;

/**
 * 页面跳转控制器：处理登录、注册和仪表盘页面渲染。
 */
@Controller
public class AttendancePageController {

    @Autowired
    private UserService userService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private AttendanceService attendanceService;

    /**
     * 登录页。
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    /**
     * 注册页。
     */
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    /**
     * 注册提交：校验与保存后跳转到登录页。
     */
    @PostMapping("/register")
    public String register(RegisterForm form, Model model) {
        try {
            userService.registerWithConfirm(form.username(), form.password(), form.confirmPassword(), form.role());
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMsg", ex.getMessage());
            model.addAttribute("form", form);
            return "register";
        } catch (DataIntegrityViolationException ex) {
            // 注册失败兜底：处理数据库约束错误，避免直接 500。
            model.addAttribute("errorMsg", "注册失败：用户名已存在或数据不合法");
            model.addAttribute("form", form);
            return "register";
        } catch (Exception ex) {
            // 兜底异常：提供友好提示并保留表单输入。
            model.addAttribute("errorMsg", "注册失败，请稍后再试");
            model.addAttribute("form", form);
            return "register";
        }
    }

    /**
     * 系统首页/仪表盘。
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal, Authentication authentication) {
        String name = principal == null ? "" : principal.getName();
        String message = name.isBlank() ? "欢迎进入系统" : "欢迎，" + name;
        model.addAttribute("welcomeMsg", message);
        model.addAttribute("roleLabel", isTeacher(authentication) ? "教师" : "学生");
        return "dashboard";
    }

    /**
     * 教师端首页。
     */
    @GetMapping("/teacher/dashboard")
    public String teacherDashboard(Model model, Principal principal) {
        String name = principal == null ? "" : principal.getName();
        model.addAttribute("welcomeMsg", name.isBlank() ? "教师端" : "教师端，" + name);
        return "teacher-dashboard";
    }

    /**
     * 学生端首页。
     */
    @GetMapping("/student/dashboard")
    public String studentDashboard(Model model, Principal principal) {
        String name = principal == null ? "" : principal.getName();
        model.addAttribute("welcomeMsg", name.isBlank() ? "学生端" : "学生端，" + name);
        return "student-dashboard";
    }

    /**
     * 考勤打卡页面。
     */
    @GetMapping("/attendance/checkIn")
    public String checkInPage(
            Model model,
            @RequestParam(required = false) String successMsg,
            @RequestParam(required = false) String errorMsg
    ) {
        model.addAttribute("courses", buildCourses());
        model.addAttribute("successMsg", successMsg);
        model.addAttribute("errorMsg", errorMsg);
        return "attendance-check-in";
    }

    /**
     * 提交打卡：检查时间窗口并写入考勤记录。
     */
    @PostMapping("/attendance/checkIn")
    public String checkIn(
            @RequestParam Integer courseId,
            @RequestParam(required = false) String remark,
            Principal principal,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        if (isTeacher(authentication)) {
            redirectAttributes.addAttribute("errorMsg", "教师账号无需打卡。");
            return "redirect:/attendance/checkIn";
        }
        String studentNumber = principal == null ? "" : principal.getName();
        Student student = studentService.getStudentByStudentNumber(studentNumber);
        if (student == null) {
            redirectAttributes.addAttribute("errorMsg", "未找到学生信息，请先维护学生档案。");
            return "redirect:/attendance/checkIn";
        }

        CourseOption course = findCourse(courseId);
        if (course == null) {
            redirectAttributes.addAttribute("errorMsg", "课程不存在，请重新选择。 ");
            return "redirect:/attendance/checkIn";
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime classStart = LocalDate.now().atTime(course.startTime());
        LocalDateTime windowStart = classStart.minusMinutes(15);
        LocalDateTime windowEnd = classStart.plusMinutes(30);

        // 时间窗口限制：课程开始前 15 分钟到开始后 30 分钟内允许打卡。
        if (now.isBefore(windowStart) || now.isAfter(windowEnd)) {
            redirectAttributes.addAttribute("errorMsg", "当前不在打卡时间窗口内。");
            return "redirect:/attendance/checkIn";
        }

        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setCourseId(course.id());
        attendance.setCourseName(course.name());
        attendance.setCheckInTime(now);
        attendance.setStatus(now.toLocalTime().isAfter(course.startTime()) ? "LATE" : "NORMAL");
        attendance.setRemark(remark);
        attendance.setCreateTime(LocalDateTime.now());
        attendanceService.saveAttendance(attendance);

        redirectAttributes.addAttribute("successMsg", "打卡成功，状态：" + attendance.getStatus());
        return "redirect:/attendance/list";
    }

    /**
     * 考勤记录列表（分页+筛选）。
     */
    @GetMapping("/attendance/list")
    public String attendanceList(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) String range,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model,
            Principal principal,
            Authentication authentication
    ) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
        LocalDate start = parseDate(startDate, formatter);
        LocalDate end = parseDate(endDate, formatter);

        if (range != null && !range.isBlank()) {
            LocalDate today = LocalDate.now();
            switch (range) {
                case "today" -> {
                    start = today;
                    end = today;
                }
                case "week" -> {
                    start = today.with(DayOfWeek.MONDAY);
                    end = today.with(DayOfWeek.SUNDAY);
                }
                case "month" -> {
                    start = today.withDayOfMonth(1);
                    end = today.withDayOfMonth(today.lengthOfMonth());
                }
                default -> {
                }
            }
        }

        LocalDateTime startTime = start == null ? null : start.atStartOfDay();
        LocalDateTime endTime = end == null ? null : end.plusDays(1).atStartOfDay().minusNanos(1);

        String filterStudentNumber = null;
        if (!isTeacher(authentication)) {
            String studentNumber = principal == null ? "" : principal.getName();
            Student student = studentService.getStudentByStudentNumber(studentNumber);
            filterStudentNumber = student == null ? null : student.getStudentNumber();
        }

        PageRequest pageable = PageRequest.of(Math.max(page - 1, 0), size, Sort.by(Sort.Direction.DESC, "checkInTime"));
        Page<Attendance> attendancePage = attendanceService.findAttendancePage(
                filterStudentNumber,
                status,
                courseId,
                startTime,
                endTime,
                pageable
        );

        model.addAttribute("records", attendancePage.getContent());
        model.addAttribute("isTeacher", isTeacher(authentication));
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", attendancePage.getTotalPages());
        model.addAttribute("startDate", start == null ? "" : start.format(formatter));
        model.addAttribute("endDate", end == null ? "" : end.format(formatter));
        model.addAttribute("status", status == null ? "" : status);
        model.addAttribute("courseId", courseId);
        model.addAttribute("courses", buildCourses());
        return "attendance-list";
    }

    /**
     * 导出考勤记录（CSV）。
     */
    @GetMapping("/attendance/export")
    public void exportAttendance(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer courseId,
            Principal principal,
            Authentication authentication,
            HttpServletResponse response
    ) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
        LocalDate start = parseDate(startDate, formatter);
        LocalDate end = parseDate(endDate, formatter);
        LocalDateTime startTime = start == null ? null : start.atStartOfDay();
        LocalDateTime endTime = end == null ? null : end.plusDays(1).atStartOfDay().minusNanos(1);

        String filterStudentNumber = null;
        if (!isTeacher(authentication)) {
            String studentNumber = principal == null ? "" : principal.getName();
            Student student = studentService.getStudentByStudentNumber(studentNumber);
            filterStudentNumber = student == null ? null : student.getStudentNumber();
        }

        List<Attendance> records = attendanceService.findAttendanceList(
                filterStudentNumber,
                status,
                courseId,
                startTime,
                endTime
        );

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=attendance.csv");

        StringBuilder builder = new StringBuilder();
        builder.append("日期,课程,打卡时间,状态,备注\n");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        for (Attendance record : records) {
            String date = record.getCheckInTime() == null ? "" : record.getCheckInTime().format(dateFormatter);
            String time = record.getCheckInTime() == null ? "" : record.getCheckInTime().format(timeFormatter);
            builder.append(date).append(',')
                    .append(safeCsv(record.getCourseName())).append(',')
                    .append(time).append(',')
                    .append(safeCsv(record.getStatus())).append(',')
                    .append(safeCsv(record.getRemark()))
                    .append("\n");
        }
        response.getWriter().write(builder.toString());
        response.getWriter().flush();
    }

    private LocalDate parseDate(String value, DateTimeFormatter formatter) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value, formatter);
    }

    private String safeCsv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\r")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    private List<CourseOption> buildCourses() {
        return List.of(
                new CourseOption(1, "Java程序设计", LocalTime.of(8, 0)),
                new CourseOption(2, "数据库原理", LocalTime.of(10, 0)),
                new CourseOption(3, "Java EE开发", LocalTime.of(14, 0))
        );
    }

    private CourseOption findCourse(Integer courseId) {
        if (courseId == null) {
            return null;
        }
        return buildCourses().stream()
                .filter(course -> course.id().equals(courseId))
                .findFirst()
                .orElse(null);
    }

    private boolean isTeacher(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_TEACHER".equals(authority.getAuthority()));
    }

    /**
     * 批量导入考勤页面。
     */
    @GetMapping("/attendance/import")
    public String importPage() {
        return "attendance-import";
    }

    /**
     * 批量导入考勤数据。
     */
    @PostMapping("/attendance/import")
    public String importAttendance(@RequestParam("file") MultipartFile file,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "请选择要上传的文件");
            return "redirect:/attendance/import";
        }
        try {
            String studentNumber = principal == null ? null : principal.getName();
            Student student = studentService.getStudentByStudentNumber(studentNumber);
            if (student == null) {
                redirectAttributes.addFlashAttribute("error", "未找到学生信息，请先维护学生档案");
                return "redirect:/attendance/import";
            }
            // 这里假设课程ID和名称由前端或模板指定，实际可根据需求调整
            Integer courseId = 1;
            String courseName = "Java程序设计";
            InputStream is = file.getInputStream();
            var records = ExcelHelper.parseExcel(is, student, courseId, courseName);
            for (var att : records) {
                attendanceService.saveAttendance(att);
            }
            redirectAttributes.addFlashAttribute("success", "导入成功，共 " + records.size() + " 条记录");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "导入失败：" + e.getMessage());
        }
        return "redirect:/attendance/import";
    }
}
