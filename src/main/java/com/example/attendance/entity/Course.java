package com.example.attendance.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "course")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 课程编码/业务编号（映射到数据库 course_id 列） */
    @Column(name = "course_id", nullable = false, length = 50)
    private String courseId;

    /** 课程名称（映射到数据库 course_name 列） */
    @Column(name = "course_name", nullable = false, length = 100)
    private String name;

    /** 课程代码 */
    @Column(unique = true, length = 50)
    private String code;

    /** 授课教师 */
    @Column(name = "teacher_name", length = 50)
    private String teacherName;

    /** 教师ID（数据库要求 NOT NULL，默认 0） */
    @Column(name = "teacher_id", nullable = false)
    private Long teacherId = 0L;

    /** 教室 */
    @Column(length = 50)
    private String classroom;

    /** 上课时间 */
    @Column(name = "start_time")
    private LocalTime startTime;

    /** 下课时间 */
    @Column(name = "end_time")
    private LocalTime endTime;

    /** 星期几（存储字符串：MONDAY, TUESDAY 等） */
    @Column(name = "week_day", length = 10)
    private String weekDay;

    /** 班级名称（如：软件工程1班） */
    @Column(name = "class_name", length = 50)
    private String className = "";

    /** 学期 */
    private Integer semester;

    // ===== 便捷方法：Java DayOfWeek 与 String 互转 =====

    /** 获取 DayOfWeek 枚举 */
    @Transient
    public DayOfWeek getWeekDayEnum() {
        if (weekDay == null || weekDay.isBlank()) return null;
        try {
            return DayOfWeek.valueOf(weekDay);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** 设置 DayOfWeek 枚举 */
    public void setWeekDayEnum(DayOfWeek dayOfWeek) {
        this.weekDay = dayOfWeek == null ? null : dayOfWeek.name();
    }
}
