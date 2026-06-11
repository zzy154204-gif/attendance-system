package com.example.attendance.service.impl;

import com.example.attendance.dao.CourseRepository;
import com.example.attendance.dto.CourseOption;
import com.example.attendance.entity.Course;
import com.example.attendance.exception.BusinessException;
import com.example.attendance.service.CourseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    public CourseServiceImpl(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    @Transactional
    public Course addCourse(Course course) {
        syncCourseId(course);
        if (course.getCode() != null && !course.getCode().isBlank()) {
            Course existing = courseRepository.findByCode(course.getCode());
            if (existing != null) {
                throw new BusinessException("课程代码已存在：" + course.getCode());
            }
        }
        return courseRepository.save(course);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Course> findPage(Pageable pageable) {
        return courseRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Course getById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("404", "课程不存在"));
    }

    @Override
    @Transactional
    public Course updateCourse(Course course) {
        syncCourseId(course);
        Course existing = getById(course.getId());
        if (course.getCode() != null && !course.getCode().isBlank()
                && !course.getCode().equals(existing.getCode())) {
            Course conflict = courseRepository.findByCode(course.getCode());
            if (conflict != null && !conflict.getId().equals(course.getId())) {
                throw new BusinessException("课程代码已存在：" + course.getCode());
            }
        }
        return courseRepository.save(course);
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new BusinessException("404", "课程不存在");
        }
        courseRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> findByTeacher(String teacherName) {
        return courseRepository.findByTeacherNameContaining(teacherName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Course> getCourseOptions() {
        return courseRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseOption> buildCourseOptions() {
        List<Course> courses = courseRepository.findAll();
        if (!courses.isEmpty()) {
            return courses.stream()
                    .map(c -> new CourseOption(c.getId(), c.getName(),
                            c.getStartTime() != null ? c.getStartTime() : LocalTime.of(8, 0)))
                    .toList();
        }
        // 数据库无课程时返回默认课程
        return List.of(
                new CourseOption(1L, "Java程序设计", LocalTime.of(8, 0)),
                new CourseOption(2L, "数据库原理", LocalTime.of(10, 0)),
                new CourseOption(3L, "Java EE开发", LocalTime.of(14, 0))
        );
    }

    private void syncCourseId(Course course) {
        if (course == null) {
            return;
        }
        if (course.getCode() != null && !course.getCode().isBlank()) {
            course.setCourseId(course.getCode());
        } else if (course.getCourseId() == null || course.getCourseId().isBlank()) {
            course.setCourseId(null);
        }
    }
}
