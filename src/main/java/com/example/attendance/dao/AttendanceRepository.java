package com.example.attendance.dao;

import com.example.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long>, JpaSpecificationExecutor<Attendance> {

    // 任务三要求的“自定义查询”：根据学生 ID 查询该生的所有考勤记录
    List<Attendance> findByStudent_Id(Long studentId);

    // 你甚至可以根据状态查询
    List<Attendance> findByStatus(String status);
}