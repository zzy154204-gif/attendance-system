package com.example.attendance.service;

import com.example.attendance.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 课程信息服务接口。
 */
public interface CourseService {

    /** 新增课程 */
    Course addCourse(Course course);

    /** 查询全部课程 */
    List<Course> getAllCourses();

    /** 分页查询课程 */
    Page<Course> findPage(Pageable pageable);

    /** 按 ID 查询 */
    Course getById(Long id);

    /** 更新课程 */
    Course updateCourse(Course course);

    /** 删除课程 */
    void deleteCourse(Long id);

    /** 按教师查询 */
    List<Course> findByTeacher(String teacherName);

    /** 获取课程选项列表（用于下拉选择） */
    List<Course> getCourseOptions();
}
