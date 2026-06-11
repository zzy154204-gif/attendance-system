package com.example.attendance.controller;

import com.example.attendance.aspect.LogOperation;
import com.example.attendance.dto.CourseOption;
import com.example.attendance.dto.RegisterForm;
import com.example.attendance.dto.StudentDashboardStats;
import com.example.attendance.dto.TeacherDashboardStats;
import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.Course;
import com.example.attendance.entity.Student;
import com.example.attendance.service.AttendanceService;
import com.example.attendance.service.CourseService;
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
import java.io.InputStream;
import java.util.List;

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
    private CourseService courseService;

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
     * 系统首页/仪表盘：根据角色自动跳转。
     */
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        if (isAdmin(authentication)) {
            return "redirect:/admin/dashboard";
        }
        if (isTeacher(authentication)) {
            return "redirect:/teacher/dashboard";
        }
        return "redirect:/student/dashboard";
    }

    /**
     * 管理员 Dashboard：可查看全局统计，也能访问所有功能。
     */
    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model, Principal principal, Authentication authentication) {
        String name = principal == null ? "管理员" : principal.getName();
        model.addAttribute("welcomeMsg", "管理员控制台 — " + name);

        // 教师端统计数据
        TeacherDashboardStats stats = attendanceService.getTeacherStats();
        stats = new TeacherDashboardStats(
                studentService.countStudents(),
                stats.todayCheckIns(), stats.todayDistinctStudents(),
                stats.monthTotal(), stats.monthNormal(), stats.monthLate(),
                stats.monthRate(), stats.courseStats()
        );
        model.addAttribute("stats", stats);
        model.addAttribute("isAdmin", true);
        return "admin-dashboard";
    }

    /**
     * 教师端首页。
     */
    @GetMapping("/teacher/dashboard")
    public String teacherDashboard(Model model, Principal principal) {
        String name = principal == null ? "教师" : principal.getName();
        model.addAttribute("welcomeMsg", name);

        TeacherDashboardStats stats = attendanceService.getTeacherStats();
        stats = new TeacherDashboardStats(
                studentService.countStudents(),
                stats.todayCheckIns(), stats.todayDistinctStudents(),
                stats.monthTotal(), stats.monthNormal(), stats.monthLate(),
                stats.monthRate(), stats.courseStats()
        );
        model.addAttribute("stats", stats);
        model.addAttribute("isAdmin", false);
        return "teacher-dashboard";
    }

    /**
     * 学生端首页。
     */
    @GetMapping("/student/dashboard")
    public String studentDashboard(Model model, Principal principal, Authentication authentication) {
        String name = principal == null ? "" : principal.getName();
        model.addAttribute("welcomeMsg", name);

        // 获取当前学生
        Student student = studentService.getStudentByStudentNumber(name);
        if (student != null) {
            StudentDashboardStats stats = attendanceService.getStudentStats(student.getId());
            // 今日已打卡课程数
            LocalDateTime todayStart = LocalDate.now().atStartOfDay();
            LocalDateTime todayEnd = LocalDateTime.now();
            long todayChecked = attendanceService.findAttendanceList(
                    student.getStudentNumber(), null, null, todayStart, todayEnd).size();
            stats = new StudentDashboardStats(
                    stats.monthTotal(), stats.monthNormal(), stats.monthLate(),
                    stats.monthRate(), stats.todayCourses(), (int) todayChecked);
            model.addAttribute("stats", stats);
        }
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
    @LogOperation(operation = "CHECK_IN", target = "Attendance")
    @PostMapping("/attendance/checkIn")
    public String checkIn(
            @RequestParam Long courseId,
            @RequestParam(required = false) String remark,
            Principal principal,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            if (isTeacher(authentication)) {
                redirectAttributes.addFlashAttribute("errorMsg", "教师账号无需打卡。");
                return "redirect:/attendance/checkIn";
            }
            String studentNumber = principal == null ? "" : principal.getName();
            Student student = studentService.getStudentByStudentNumber(studentNumber);
            if (student == null) {
                // 兜底：用户已注册但没有 Student 记录的，自动创建
                student = new Student();
                student.setStudentNumber(studentNumber);
                student.setName(studentNumber);
                studentService.addStudent(student);
            }

            Course course;
            try {
                course = courseService.getById(courseId);
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMsg", "课程不存在，请重新选择。");
                return "redirect:/attendance/checkIn";
            }

            LocalDateTime dayStart = LocalDate.now().atStartOfDay();
            LocalDateTime dayEnd = LocalDate.now().plusDays(1).atStartOfDay();
            List<Attendance> todayAttendances = attendanceService.findAttendanceList(
                    studentNumber, null, courseId, dayStart, dayEnd);
            if (!todayAttendances.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMsg", "今天这门课已经打过卡了，请勿重复提交。");
                return "redirect:/attendance/checkIn";
            }

            LocalDateTime now = LocalDateTime.now();
            LocalTime startTime = course.getStartTime() != null ? course.getStartTime() : LocalTime.of(8, 0);
            LocalTime endTime = course.getEndTime() != null ? course.getEndTime() : startTime.plusHours(2);
            LocalDateTime windowStart = LocalDate.now().atTime(startTime);
            LocalDateTime windowEnd = LocalDate.now().atTime(endTime);

            if (now.isBefore(windowStart) || now.isAfter(windowEnd)) {
                String windowInfo = String.format("打卡窗口：%s ~ %s（课程 %s）",
                        windowStart.toLocalTime(), windowEnd.toLocalTime(),
                        course.getName());
                redirectAttributes.addFlashAttribute("errorMsg", "当前不在课程时间范围内。" + windowInfo);
                return "redirect:/attendance/checkIn";
            }

            Attendance attendance = new Attendance();
            attendance.setStudent(student);
            attendance.setCourse(course);
            attendance.setCheckInTime(now);
            attendance.setStatus(now.toLocalTime().isAfter(startTime) ? "LATE" : "NORMAL");
            attendance.setRemark(remark);
            attendance.setCreateTime(LocalDateTime.now());
            attendanceService.saveAttendance(attendance);

            redirectAttributes.addFlashAttribute("successMsg", "打卡成功，状态：" + attendance.getStatus());
            return "redirect:/student/dashboard";
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("errorMsg", "打卡失败：记录冲突，请刷新页面后重试。");
            return "redirect:/attendance/checkIn";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "打卡失败：" + e.getMessage());
            return "redirect:/attendance/checkIn";
        }
    }

    /**
     * 考勤记录列表（分页+筛选）。
     */
    @GetMapping("/attendance/list")
    public String attendanceList(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long courseId,
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
        if (!isTeacherOrAdmin(authentication)) {
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
        model.addAttribute("isTeacher", isTeacherOrAdmin(authentication));
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
            @RequestParam(required = false) Long courseId,
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
        if (!isTeacherOrAdmin(authentication)) {
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
        builder.append("学号,姓名,日期,课程,打卡时间,状态,备注\n");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        for (Attendance record : records) {
            String studentNumber = record.getStudent() == null ? "" : record.getStudent().getStudentNumber();
            String studentName = record.getStudent() == null ? "" : record.getStudent().getName();
            String date = record.getCheckInTime() == null ? "" : record.getCheckInTime().format(dateFormatter);
            String time = record.getCheckInTime() == null ? "" : record.getCheckInTime().format(timeFormatter);
            builder.append(safeCsv(studentNumber)).append(',')
                    .append(safeCsv(studentName)).append(',')
                    .append(date).append(',')
                    .append(safeCsv(record.getCourseName())).append(',')
                    .append(time).append(',')
                    .append(safeCsv(record.getStatus())).append(',')
                    .append(safeCsv(record.getRemark()))
                    .append("\n");
        }
        response.getWriter().write(builder.toString());
        response.getWriter().flush();
    }

    /**
     * 导出考勤记录（Excel .xlsx）。
     */
    @GetMapping("/attendance/exportExcel")
    public void exportAttendanceExcel(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long courseId,
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
        if (!isTeacherOrAdmin(authentication)) {
            String studentNumber = principal == null ? "" : principal.getName();
            Student student = studentService.getStudentByStudentNumber(studentNumber);
            filterStudentNumber = student == null ? null : student.getStudentNumber();
        }

        List<Attendance> records = attendanceService.findAttendanceList(
                filterStudentNumber, status, courseId, startTime, endTime);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=attendance.xlsx");

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("考勤记录");

            // 表头样式
            org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // 表头行
            String[] headers = {"学号", "姓名", "日期", "课程", "打卡时间", "状态", "备注"};
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 数据行
            int rowIdx = 1;
            for (Attendance record : records) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(record.getStudent() == null ? "" : record.getStudent().getStudentNumber());
                row.createCell(1).setCellValue(record.getStudent() == null ? "" : record.getStudent().getName());
                row.createCell(2).setCellValue(record.getCheckInTime() == null ? "" : record.getCheckInTime().format(dateFormatter));
                row.createCell(3).setCellValue(record.getCourseName() == null ? "" : record.getCourseName());
                row.createCell(4).setCellValue(record.getCheckInTime() == null ? "" : record.getCheckInTime().format(timeFormatter));
                row.createCell(5).setCellValue(record.getStatus() == null ? "" : record.getStatus());
                row.createCell(6).setCellValue(record.getRemark() == null ? "" : record.getRemark());
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();
        }
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
        return courseService.buildCourseOptions();
    }

    private CourseOption findCourse(Long courseId) {
        if (courseId == null) return null;
        return buildCourses().stream()
                .filter(c -> c.id().equals(courseId))
                .findFirst().orElse(null);
    }

    private boolean isTeacher(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_TEACHER".equals(authority.getAuthority()));
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    private boolean isTeacherOrAdmin(Authentication authentication) {
        return isTeacher(authentication) || isAdmin(authentication);
    }

    /**
     * 批量导入考勤页面。
     */
    @GetMapping("/attendance/import")
    public String importPage(Model model) {
        model.addAttribute("courses", buildCourses());
        return "attendance-import";
    }

    /**
     * 批量导入考勤数据。
     */
    @LogOperation(operation = "IMPORT", target = "Attendance")
    @PostMapping("/attendance/import")
    public String importAttendance(@RequestParam("file") MultipartFile file,
                                   @RequestParam("courseId") Long courseId,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "请选择要上传的文件");
            return "redirect:/attendance/import";
        }
        Course course = courseService.getById(courseId);
        if (course == null) {
            redirectAttributes.addFlashAttribute("error", "课程不存在，请重新选择");
            return "redirect:/attendance/import";
        }
        try {
            String studentNumber = principal == null ? null : principal.getName();
            Student student = studentService.getStudentByStudentNumber(studentNumber);
            if (student == null) {
                redirectAttributes.addFlashAttribute("error", "未找到学生信息，请先维护学生档案");
                return "redirect:/attendance/import";
            }
            InputStream is = file.getInputStream();
            var records = ExcelHelper.parseExcel(is, student, course);
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
