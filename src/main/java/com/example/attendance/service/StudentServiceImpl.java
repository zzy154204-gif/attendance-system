package com.example.attendance.service;

import com.example.attendance.dao.StudentDao;
import com.example.attendance.entity.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
    public Page<Student> findPage(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return studentDao.findAll(pageable);
        }
        String trimmed = keyword.trim();
        return studentDao.findByNameContainingIgnoreCaseOrStudentNumberContainingIgnoreCase(trimmed, trimmed, pageable);
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

    @Override
    public Student getStudentByStudentNumber(String studentNumber) {
        if (studentNumber == null || studentNumber.isBlank()) {
            return null;
        }
        return studentDao.findByStudentNumber(studentNumber.trim());
    }
}