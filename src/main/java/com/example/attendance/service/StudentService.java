package com.example.attendance.service;

import com.example.attendance.entity.Student;
import java.util.List;

/**
 * 学生服务接口。
 */
public interface StudentService {
    /**
     * 新增学生。
     *
     * @param student 学生实体
     */
    void addStudent(Student student);

    /**
     * 查询全部学生。
     *
     * @return 学生列表
     */
    List<Student> getAllStudents();

    /**
     * 分页查询学生列表（支持关键字过滤）。
     *
     * @param keyword 关键字（姓名或学号）
     * @param pageable 分页参数
     * @return 分页结果
     */
    org.springframework.data.domain.Page<Student> findPage(String keyword, org.springframework.data.domain.Pageable pageable);

    /**
     * 按主键查询学生。
     *
     * @param id 学生主键
     * @return 学生实体
     */
    Student getStudentById(Long id);

    /**
     * 更新学生。
     *
     * @param student 学生实体
     */
    void updateStudent(Student student);

    /**
     * 删除学生。
     *
     * @param id 学生主键
     */
    void deleteStudent(Long id);

    /**
     * 按学号查询学生。
     *
     * @param studentNumber 学号
     * @return 学生实体
     */
    Student getStudentByStudentNumber(String studentNumber);
}