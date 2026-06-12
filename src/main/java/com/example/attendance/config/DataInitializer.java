package com.example.attendance.config;

import com.example.attendance.dao.CourseRepository;
import com.example.attendance.entity.Course;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

/**
 * 系统初始化：首次启动时创建默认课程。
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final CourseRepository courseRepository;

    public DataInitializer(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    public void run(String... args) {
        if (courseRepository.count() == 0) {
            log.info("课程表为空，创建默认课程...");

            Course c = new Course();
            c.setCourseId("EE301");
            c.setCode("CS301");
            c.setName("Java EE开发");
            c.setStartTime(LocalTime.of(14, 0));
            c.setEndTime(LocalTime.of(16, 0));
            c.setClassroom("实验楼201");
            c.setWeekDay("WEDNESDAY");
            courseRepository.save(c);

            log.info("默认课程创建完成");
        }
    }
}
