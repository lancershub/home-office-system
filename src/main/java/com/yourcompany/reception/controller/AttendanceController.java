package com.yourcompany.reception.controller;
import com.yourcompany.reception.service.AttendanceService;
import com.yourcompany.reception.util.Csv;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

@Controller
public class AttendanceController {
    private final JdbcTemplate jdbc;
    private final AttendanceService attendance;
    public AttendanceController(JdbcTemplate jdbc, AttendanceService attendance) { this.jdbc = jdbc; this.attendance = attendance; }
    @PostMapping("/clockIn")
    public String clockIn(HttpSession session) {
        attendance.clockIn((Integer) session.getAttribute("visitorId"), (String) session.getAttribute("visitorName"));
        return "redirect:visitorIndex";
    }
    @PostMapping("/clockOut")
    public String clockOut(HttpSession session) { attendance.clockOut((Integer) session.getAttribute("visitorId")); return "redirect:visitorIndex"; }
    @GetMapping("/attendanceList")
    public String list(Model model, @RequestParam(defaultValue="1") int page) {
        if (page < 1 || page > 100000) throw new IllegalArgumentException("页码不正确");
        model.addAttribute("records", jdbc.queryForList("SELECT id, user_name, DATE_FORMAT(work_date, '%Y-%m-%d') AS work_date, DATE_FORMAT(clock_in_time, '%H:%i:%s') AS clock_in_time, DATE_FORMAT(clock_out_time, '%H:%i:%s') AS clock_out_time FROM attendance_record ORDER BY work_date DESC, id DESC LIMIT 100 OFFSET ?", (page-1)*100));
        model.addAttribute("page", page);
        return "attendance_list";
    }
    @GetMapping("/exportAttendance")
    public void export(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=attendance_report.csv");
        PrintWriter writer = response.getWriter();
        writer.write('\uFEFF'); writer.println("员工姓名,考勤日期,上班打卡,下班打卡,打卡完整性");
        // Keyset batches bound memory without keeping a long-running transaction.
        long lastId = 0;
        Long upper = jdbc.queryForObject("SELECT COALESCE(MAX(id),0) FROM attendance_record", Long.class);
        while (lastId < upper) {
            List<Map<String,Object>> rows = jdbc.queryForList("SELECT id, user_name, work_date, clock_in_time, clock_out_time FROM attendance_record WHERE id > ? AND id <= ? ORDER BY id LIMIT 500", lastId, upper);
            if (rows.isEmpty()) break;
            for (Map<String,Object> r : rows) {
                writer.println(Csv.cell(r.get("user_name")) + "," + Csv.cell(r.get("work_date")) + "," + Csv.cell(r.get("clock_in_time")) + "," + Csv.cell(r.get("clock_out_time")) + "," + Csv.cell(r.get("clock_in_time") != null && r.get("clock_out_time") != null ? "上下班均已打卡" : "打卡不完整"));
                lastId = ((Number)r.get("id")).longValue();
            }
            writer.flush();
            if (writer.checkError()) break;
        }
    }
}
