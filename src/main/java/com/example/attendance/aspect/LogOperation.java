package com.example.attendance.aspect;

import java.lang.annotation.*;

/**
 * 标注在方法上，自动记录操作日志。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogOperation {

    /** 操作类型：LOGIN, CHECK_IN, IMPORT, EXPORT, DELETE, CREATE, UPDATE, APPROVE 等 */
    String operation();

    /** 操作目标：Attendance, Student, Leave, Course 等 */
    String target() default "";

    /** 操作详情（支持 SpEL 表达式，如 #result.id） */
    String detail() default "";
}
