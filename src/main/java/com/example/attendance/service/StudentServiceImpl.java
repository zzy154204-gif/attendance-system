package com.example.attendance.service;

import com.example.attendance.dao.StudentDao;
import com.example.attendance.pojo.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudentServiceImpl implements StudentService {

    @Autowired
    private StudentDao studentDao;

    @Override
    public void addStudent(Student student) {
        studentDao.insert(student);
    }
}