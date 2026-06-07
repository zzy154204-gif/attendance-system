package com.example.attendance.dao;

import com.example.attendance.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    /** 按课程代码查询 */
    Course findByCode(String code);

    /** 按授课教师查询 */
    List<Course> findByTeacherNameContaining(String teacherName);

    /** 按学期查询 */
    List<Course> findBySemester(Integer semester);

    /** 按名称模糊查询 */
    List<Course> findByNameContainingIgnoreCase(String name);
}
