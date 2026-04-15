package com.example.attendance.dao;

import com.example.attendance.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentDao extends JpaRepository<Student, Long> {
    // JpaRepository 已经提供了基础的 CRUD 方法
    // save(), findById(), findAll(), delete() 等
}