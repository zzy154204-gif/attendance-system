package com.example.attendance.entity;

import jakarta.persistence.*; // 注意：如果是Spring Boot 3.x，使用jakarta
import lombok.Data;

import java.util.List;


@Data
@Entity
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String studentNumber;

    @Column(length = 50)
    private String name;

    private String clazz;
    // 在 Student 类中添加
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    private List<Attendance> attendances;
}