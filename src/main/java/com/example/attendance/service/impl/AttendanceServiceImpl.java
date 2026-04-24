package com.example.attendance.service.impl;

import com.example.attendance.dao.AttendanceRepository;
import com.example.attendance.dao.StudentDao;
import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.Student;
import com.example.attendance.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private StudentDao studentDao;

    @Override
    public Attendance checkIn(Long studentId, String status) {
        Student student = studentDao.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("学生不存在，ID=" + studentId));
        Attendance record = new Attendance();
        record.setStudent(student);
        record.setCheckInTime(LocalDateTime.now());
        record.setStatus(status != null ? status : "正常");
        return attendanceRepository.save(record);
    }

    @Override
    public List<Attendance> getByStudentId(Long studentId) {
        return attendanceRepository.findByStudentId(studentId);
    }

    @Override
    public List<Attendance> getAll() {
        return attendanceRepository.findAll();
    }

    @Override
    public List<Attendance> getByStatus(String status) {
        return attendanceRepository.findByStatus(status);
    }

    @Override
    public void delete(Long id) {
        if (!attendanceRepository.existsById(id)) {
            throw new IllegalArgumentException("考勤记录不存在，ID=" + id);
        }
        attendanceRepository.deleteById(id);
    }
}
