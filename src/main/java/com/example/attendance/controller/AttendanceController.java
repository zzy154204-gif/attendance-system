package com.example.attendance.controller;

import com.example.attendance.common.Result;
import com.example.attendance.entity.Attendance;
import com.example.attendance.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    // 签到：POST /attendance/checkin   body: {"studentId":1,"status":"正常"}
    @PostMapping("/checkin")
    public Result<Attendance> checkIn(@RequestBody Map<String, Object> body) {
        Long studentId = Long.valueOf(body.get("studentId").toString());
        String status = (String) body.getOrDefault("status", "正常");
        Attendance record = attendanceService.checkIn(studentId, status);
        return Result.success("签到成功", record);
    }

    // 查询所有考勤：GET /attendance/all
    @GetMapping("/all")
    public Result<List<Attendance>> getAll() {
        return Result.success(attendanceService.getAll());
    }

    // 按学生查询：GET /attendance/student/{studentId}
    @GetMapping("/student/{studentId}")
    public Result<List<Attendance>> getByStudent(@PathVariable Long studentId) {
        return Result.success(attendanceService.getByStudentId(studentId));
    }

    // 按状态筛选：GET /attendance/status/{status}  （如"迟到"、"缺勤"）
    @GetMapping("/status/{status}")
    public Result<List<Attendance>> getByStatus(@PathVariable String status) {
        return Result.success(attendanceService.getByStatus(status));
    }

    // 删除考勤记录：DELETE /attendance/{id}
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        attendanceService.delete(id);
        return Result.success("删除成功", null);
    }
}
