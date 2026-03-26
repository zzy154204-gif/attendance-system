package com.example.attendance.dao;

import com.example.attendance.pojo.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.JdbcTemplate;
@Repository
public class StudentDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void insert(Student student) {
        String sql = "insert into student(name, age) values (?, ?)";
        jdbcTemplate.update(sql,
                student.getName(),
                student.getAge());
    }
}