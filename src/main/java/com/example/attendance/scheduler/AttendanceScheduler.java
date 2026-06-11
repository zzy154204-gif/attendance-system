package com.example.attendance.scheduler;

import com.example.attendance.dao.AttendanceRepository;
import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.Course;
import com.example.attendance.entity.Student;
import com.example.attendance.service.AttendanceService;
import com.example.attendance.service.CourseService;
import com.example.attendance.service.StudentService;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 定时任务：在课程结束后，自动将未打卡的学生标记为缺勤。
 */
@Component
public class AttendanceScheduler {

    private static final Logger log = LoggerFactory.getLogger(AttendanceScheduler.class);

    private final CourseService courseService;
    private final StudentService studentService;
    private final AttendanceService attendanceService;
    private final AttendanceRepository attendanceRepository;

    public AttendanceScheduler(CourseService courseService, StudentService studentService,
                               AttendanceService attendanceService, AttendanceRepository attendanceRepository) {
        this.courseService = courseService;
        this.studentService = studentService;
        this.attendanceService = attendanceService;
        this.attendanceRepository = attendanceRepository;
    }

    /**
     * 每个工作日 16:00 执行，标记当天未打卡学生为缺勤。
     * 也每隔 60 分钟检查一次，以便及时标记（但会跳过已标记的记录）。
     */
    @Scheduled(cron = "0 0 16 * * MON-FRI")
    @Scheduled(fixedRate = 3600000)
    public void markAbsentStudents() {
        LocalDate today = LocalDate.now();
        DayOfWeek todayDayOfWeek = today.getDayOfWeek();

        // 周末不处理
        if (todayDayOfWeek == DayOfWeek.SATURDAY || todayDayOfWeek == DayOfWeek.SUNDAY) {
            return;
        }

        List<Course> courses = courseService.getAllCourses();
        List<Student> students = studentService.getAllStudents();

        if (courses.isEmpty() || students.isEmpty()) {
            return;
        }

        int markedCount = 0;

        for (Course course : courses) {
            // 只处理今天有课的课程
            DayOfWeek courseDay = course.getWeekDayEnum();
            if (courseDay == null || courseDay != todayDayOfWeek) {
                continue;
            }

            LocalTime startTime = course.getStartTime();
            LocalTime endTime = course.getEndTime();
            if (startTime == null) {
                continue;
            }
            // 默认下课时间为上课时间 + 2 小时
            if (endTime == null) {
                endTime = startTime.plusHours(2);
            }

            // 打卡窗口在课程结束后才关闭，只有窗口关闭后才标记缺勤
            LocalDateTime windowClose = LocalDateTime.of(today, endTime);
            if (LocalDateTime.now().isBefore(windowClose)) {
                continue;
            }

            LocalDateTime dayStart = today.atStartOfDay();
            LocalDateTime dayEnd = today.plusDays(1).atStartOfDay();

            for (Student student : students) {
                try {
                    // 检查该学生今天是否已有该课程的打卡记录
                    long count = attendanceRepository.count((root, query, cb) -> {
                        List<Predicate> predicates = new ArrayList<>();
                        predicates.add(cb.equal(root.get("student").get("id"), student.getId()));
                        predicates.add(cb.equal(root.get("course").get("id"), course.getId()));
                        predicates.add(cb.between(root.get("checkInTime"), dayStart, dayEnd));
                        return cb.and(predicates.toArray(new Predicate[0]));
                    });

                    if (count == 0) {
                        Attendance absent = new Attendance();
                        absent.setStudent(student);
                        absent.setCourse(course);
                        absent.setCheckInTime(LocalDateTime.of(today, startTime));
                        absent.setStatus("ABSENT");
                        absent.setCreateTime(LocalDateTime.now());
                        absent.setRemark("系统自动标记缺勤");
                        attendanceService.saveAttendance(absent);
                        markedCount++;
                    }
                } catch (Exception e) {
                    log.warn("标记缺勤失败 student={} course={}: {}", student.getId(), course.getId(), e.getMessage());
                }
            }
        }

        if (markedCount > 0) {
            log.info("自动标记缺勤完成，共标记 {} 条记录", markedCount);
        }
    }
}
