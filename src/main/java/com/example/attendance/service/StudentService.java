package com.example.attendance.service;

import com.example.attendance.entity.Student;
import java.util.List;

public interface StudentService {
    void addStudent(Student student);
    List<Student> getAllStudents();
    Student getStudentById(Long id);
    void updateStudent(Student student);
    void deleteStudent(Long id);
}