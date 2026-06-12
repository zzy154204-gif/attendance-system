package com.example.attendance.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*; // 注意：如果是Spring Boot 3.x，使用jakarta
import jakarta.validation.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;
import lombok.Data;

import java.util.List;


@Data
@Entity
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "studentNumber 不能为空")
    private String studentNumber;

    @Column(length = 50)
    @NotBlank(message = "name 不能为空")
    private String name;

    // 兼容前端传 class 字段，实体仍然用 clazz 避开 Java 关键字冲突。
    @JsonAlias("class")
    private String clazz;

    @Column(length = 10)
    private String gender;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private java.time.LocalDate birthDate;

    @Column(length = 50)
    private String contact;
    // 在 Student 类中添加
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Attendance> attendances;
}