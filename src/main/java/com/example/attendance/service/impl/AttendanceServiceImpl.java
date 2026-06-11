package com.example.attendance.service.impl;

import com.example.attendance.dao.AttendanceRepository;
import com.example.attendance.dto.AttendancePageResponse;
import com.example.attendance.dto.AttendanceView;
import com.example.attendance.dto.StudentDashboardStats;
import com.example.attendance.dto.TeacherDashboardStats;
import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.Student;
import com.example.attendance.service.AttendanceService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 考勤服务实现。
 * <p>基于 JPA Specification 实现动态条件 + 分页排序查询。</p>
 */
@Service
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    /**
     * 查询考勤并转换为页面展示 DTO。
     */
    @Override
    @Transactional(readOnly = true)
    public AttendancePageResponse queryAttendances(
            String studentNumber,
            String status,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int page,
            int size,
            String sortBy,
            String direction
    ) {
        // 分页功能添加：把 page/size/sort 参数封装为 Pageable，交给 JPA 自动分页。
        Pageable pageable = PageRequest.of(page, size, buildSort(sortBy, direction));

        // 多条件查询功能添加：动态拼接 where 条件，未传参数就不参与过滤。
        Specification<Attendance> specification = buildSpecification(studentNumber, status, null, startTime, endTime);

        Page<AttendanceView> result = attendanceRepository.findAll(specification, pageable)
                .map(this::toView);

        return new AttendancePageResponse(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isFirst(),
                result.isLast()
        );
    }

    @Override
    @Transactional
    public Attendance saveAttendance(Attendance attendance) {
        return attendanceRepository.save(attendance);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Attendance> findAttendancePage(
            String studentNumber,
            String status,
            Long courseId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Pageable pageable
    ) {
        return attendanceRepository.findAll(buildSpecification(studentNumber, status, courseId, startTime, endTime), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Attendance> findAttendanceList(
            String studentNumber,
            String status,
            Long courseId,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        return attendanceRepository.findAll(buildSpecification(studentNumber, status, courseId, startTime, endTime),
                Sort.by(Sort.Direction.DESC, "checkInTime"));
    }

    @Override
    @Transactional(readOnly = true)
    public StudentDashboardStats getStudentStats(Long studentId) {
        LocalDateTime monthStart = LocalDateTime.now()
                .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime monthEnd = LocalDateTime.now();

        long total = attendanceRepository.countByStudent_IdAndCheckInTimeBetween(studentId, monthStart, monthEnd);
        long normal = attendanceRepository.countByStudent_IdAndStatusAndCheckInTimeBetween(studentId, "NORMAL", monthStart, monthEnd);
        long late = attendanceRepository.countByStudent_IdAndStatusAndCheckInTimeBetween(studentId, "LATE", monthStart, monthEnd);
        double rate = total > 0 ? Math.round(normal * 10000.0 / total) / 100.0 : 100.0;

        return new StudentDashboardStats(total, normal, late, rate, 3, 0); // 课程数暂时固定3，已打卡数由Controller计算
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherDashboardStats getTeacherStats() {
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime todayEnd = LocalDateTime.now();
        LocalDateTime monthStart = LocalDateTime.now()
                .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime monthEnd = LocalDateTime.now();

        long todayCheckIns = attendanceRepository.countByCheckInTimeBetween(todayStart, todayEnd);
        long todayDistinct = attendanceRepository.countDistinctStudentByCheckInTimeBetween(todayStart, todayEnd);
        long monthTotal = attendanceRepository.countByCheckInTimeBetween(monthStart, monthEnd);
        long monthNormal = attendanceRepository.countByStatusAndCheckInTimeBetween("NORMAL", monthStart, monthEnd);
        long monthLate = attendanceRepository.countByStatusAndCheckInTimeBetween("LATE", monthStart, monthEnd);
        double monthRate = monthTotal > 0 ? Math.round(monthNormal * 10000.0 / monthTotal) / 100.0 : 100.0;

        List<TeacherDashboardStats.CourseStat> courseStats =
                attendanceRepository.countByCourseBetween(monthStart, monthEnd).stream()
                        .map(row -> new TeacherDashboardStats.CourseStat(
                                (String) row[0],
                                (Long) row[1]
                        ))
                        .toList();

        return new TeacherDashboardStats(0, todayCheckIns, todayDistinct,
                monthTotal, monthNormal, monthLate, monthRate, courseStats);
    }

    /**
     * 将实体对象转换为页面展示对象。
     */
    private AttendanceView toView(Attendance attendance) {
        Student student = attendance.getStudent();
        Long studentId = student == null ? null : student.getId();
        String studentNumber = student == null ? null : student.getStudentNumber();
        String studentName = student == null ? null : student.getName();

        return new AttendanceView(
                attendance.getId(),
                attendance.getCheckInTime(),
                attendance.getStatus(),
                studentId,
                studentNumber,
                studentName
        );
    }

    private Specification<Attendance> buildSpecification(
            String studentNumber,
            String status,
            Long courseId,
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (studentNumber != null && !studentNumber.isBlank()) {
                predicates.add(cb.equal(root.get("student").get("studentNumber"), studentNumber));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (courseId != null) {
                predicates.add(cb.equal(root.get("course").get("id"), courseId));
            }
            if (startTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("checkInTime"), startTime));
            }
            if (endTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("checkInTime"), endTime));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * 构建排序规则并限制可排序字段。
     */
    private Sort buildSort(String sortBy, String direction) {
        // 排序功能添加：限制可排序字段，避免前端传错字段导致 SQL 异常。
        String normalized = sortBy == null ? "checkInTime" : sortBy.trim();
        String mappedProperty = switch (normalized) {
            case "id", "status", "checkInTime" -> normalized;
            case "studentNumber" -> "student.studentNumber";
            default -> "checkInTime";
        };

        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return Sort.by(sortDirection, mappedProperty);
    }
}

