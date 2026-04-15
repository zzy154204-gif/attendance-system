package com.example.attendance.service;

import com.example.attendance.dao.StudentDao;
import com.example.attendance.entity.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {

    @Autowired
    private StudentDao studentDao;

    @Override
    public void addStudent(Student student) {
        studentDao.save(student);
    }

    @Override
    public List<Student> getAllStudents() {
        return studentDao.findAll();
    }

    @Override
    public Student getStudentById(Long id) {
        return studentDao.findById(id).orElse(null);
    }

    @Override
    public void updateStudent(Student student) {
        studentDao.save(student);
    }

    @Override
    public void deleteStudent(Long id) {
        studentDao.deleteById(id);
    }
}