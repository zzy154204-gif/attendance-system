package com.example.attendance.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime checkInTime;

    private String status;

    /** 关联课程（外键） */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(length = 200)
    private String remark;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    /** 关联学生（外键） */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id")
    private Student student;

    /** 便捷方法：获取课程名称，避免模板/导出空指针 */
    public String getCourseName() {
        return course != null ? course.getName() : null;
    }

    /** 便捷方法：获取课程 ID */
    public Long getCourseId() {
        return course != null ? course.getId() : null;
    }
}
