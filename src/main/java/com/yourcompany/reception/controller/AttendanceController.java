package com.yourcompany.reception.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@Controller
public class AttendanceController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ================== 访客端打卡功能 ==================

    // 1. 上班打卡
    @PostMapping("/clockIn")
    public String clockIn(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("visitorId");
        String userName = (String) session.getAttribute("visitorName");

        if (userId != null) {
            String sql = "INSERT INTO attendance_record (user_id, user_name, work_date, clock_in_time) VALUES (?, ?, CURDATE(), NOW())";
            jdbcTemplate.update(sql, userId, userName);
        }
        return "redirect:visitorIndex";
    }

    // 2. 下班打卡
    @PostMapping("/clockOut")
    public String clockOut(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("visitorId");

        if (userId != null) {
            String sql = "UPDATE attendance_record SET clock_out_time = NOW() WHERE user_id = ? AND work_date = CURDATE()";
            jdbcTemplate.update(sql, userId);
        }
        return "redirect:visitorIndex";
    }

    // ================== 管理员端功能 ==================

    // 3. 管理员查看所有考勤记录
    @RequestMapping("/attendanceList")
    public String attendanceList(Model model) {
        String sql = "SELECT id, user_name, " +
                "DATE_FORMAT(work_date, '%Y-%m-%d') AS work_date, " +
                "DATE_FORMAT(clock_in_time, '%H:%i:%s') AS clock_in_time, " +
                "DATE_FORMAT(clock_out_time, '%H:%i:%s') AS clock_out_time, " +
                "status " +
                "FROM attendance_record " +
                "ORDER BY work_date DESC, clock_in_time DESC";

        List<Map<String, Object>> records = jdbcTemplate.queryForList(sql);
        model.addAttribute("records", records);

        return "attendance_list";
    }

    // 4. 导出考勤报表为 CSV
    @RequestMapping("/exportAttendance")
    public String exportAttendance(HttpServletResponse response) throws IOException {
        String sql = "SELECT user_name, " +
                "DATE_FORMAT(work_date, '%Y-%m-%d') AS work_date, " +
                "DATE_FORMAT(clock_in_time, '%H:%i:%s') AS clock_in_time, " +
                "DATE_FORMAT(clock_out_time, '%H:%i:%s') AS clock_out_time " +
                "FROM attendance_record " +
                "ORDER BY work_date DESC, clock_in_time DESC";

        List<Map<String, Object>> records = jdbcTemplate.queryForList(sql);

        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=attendance_report.csv");

        PrintWriter writer = response.getWriter();
        writer.write('﻿'); // UTF-8 BOM，确保 Excel 打开不乱码
        writer.println("员工姓名,考勤日期,上班打卡,下班打卡,考勤状态");

        for (Map<String, Object> r : records) {
            String clockIn = r.get("clock_in_time") != null ? r.get("clock_in_time").toString() : "异常未打卡";
            String clockOut = r.get("clock_out_time") != null ? r.get("clock_out_time").toString() : "未打卡/办公中";
            String status = (r.get("clock_in_time") != null && r.get("clock_out_time") != null) ? "全勤" : "状态待定";

            writer.println(r.get("user_name") + "," +
                    r.get("work_date") + "," +
                    clockIn + "," +
                    clockOut + "," +
                    status);
        }
        writer.flush();
        return null;
    }
}