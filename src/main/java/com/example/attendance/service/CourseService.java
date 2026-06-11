package com.example.attendance.service;

import com.example.attendance.dto.CourseOption;
import com.example.attendance.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 课程信息服务接口。
 */
public interface CourseService {

    Course addCourse(Course course);

    List<Course> getAllCourses();

    Page<Course> findPage(Pageable pageable);

    Course getById(Long id);

    Course updateCourse(Course course);

    void deleteCourse(Long id);

    List<Course> findByTeacher(String teacherName);

    List<Course> getCourseOptions();

    /**
     * 获取课程下拉选项列表（含课程名和上课时间）。
     * 优先从数据库加载，若无数据则返回默认课程。
     */
    List<CourseOption> buildCourseOptions();
}
