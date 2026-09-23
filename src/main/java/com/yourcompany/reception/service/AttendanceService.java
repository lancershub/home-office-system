package com.yourcompany.reception.service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Service
public class AttendanceService {
    private final JdbcTemplate jdbc;
    public AttendanceService(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public void clockIn(Integer id, String name) {
        try {
            jdbc.update("INSERT INTO attendance_record (user_id, user_name, work_date, clock_in_time) VALUES (?, ?, CURDATE(), NOW())", id, name);
        } catch (DuplicateKeyException ex) {
            // Requires uk_attendance_user_date. A repeat is successful without changing the first timestamp.
            if (jdbc.queryForObject("SELECT COUNT(*) FROM attendance_record WHERE user_id = ? AND work_date = CURDATE()", Integer.class, id) != 1) throw ex;
        }
    }
    public void clockOut(Integer id) {
        int rows = jdbc.update("UPDATE attendance_record SET clock_out_time = NOW() WHERE user_id = ? AND work_date = CURDATE() AND clock_in_time IS NOT NULL AND clock_out_time IS NULL", id);
        if (rows == 0 && jdbc.queryForObject("SELECT COUNT(*) FROM attendance_record WHERE user_id = ? AND work_date = CURDATE() AND clock_in_time IS NOT NULL AND clock_out_time IS NOT NULL", Integer.class, id) == 0)
            throw new IllegalArgumentException("请先完成上班打卡");
    }
}
