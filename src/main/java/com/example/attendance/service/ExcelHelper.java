package com.example.attendance.service;

import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.Course;
import com.example.attendance.entity.Student;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExcelHelper {
    public static List<Attendance> parseExcel(InputStream is, Student student, Course course) throws Exception {
        List<Attendance> attendances = new ArrayList<>();
        Workbook workbook = new XSSFWorkbook(is);
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rows = sheet.iterator();
        int rowNumber = 0;
        while (rows.hasNext()) {
            Row currentRow = rows.next();
            if (rowNumber++ == 0) continue;
            Cell dateCell = currentRow.getCell(0);
            Cell timeCell = currentRow.getCell(1);
            Cell statusCell = currentRow.getCell(2);
            Cell remarkCell = currentRow.getCell(3);
            if (dateCell == null || timeCell == null) continue;
            LocalDate date = dateCell.getCellType() == CellType.NUMERIC ? dateCell.getLocalDateTimeCellValue().toLocalDate() : LocalDate.parse(dateCell.getStringCellValue());
            LocalTime time = timeCell.getCellType() == CellType.NUMERIC ? timeCell.getLocalDateTimeCellValue().toLocalTime() : LocalTime.parse(timeCell.getStringCellValue());
            String status = statusCell != null ? statusCell.getStringCellValue() : "NORMAL";
            String remark = remarkCell != null ? remarkCell.getStringCellValue() : null;
            Attendance att = new Attendance();
            att.setStudent(student);
            att.setCourse(course);
            att.setCheckInTime(LocalDateTime.of(date, time));
            att.setStatus(status);
            att.setRemark(remark);
            att.setCreateTime(LocalDateTime.now());
            attendances.add(att);
        }
        workbook.close();
        return attendances;
    }
}
