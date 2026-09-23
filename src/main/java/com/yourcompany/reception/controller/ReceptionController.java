package com.yourcompany.reception.controller;
import com.yourcompany.reception.service.AccountService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Controller
public class ReceptionController {
    private final JdbcTemplate jdbc;
    private final AccountService accounts;
    public ReceptionController(JdbcTemplate jdbc, AccountService accounts) { this.jdbc = jdbc; this.accounts = accounts; }
    @GetMapping("/hello") public String hello() { return "index"; }
    @GetMapping("/") public String home() { return "redirect:/hello"; }
    // Login and logout are exclusively handled by Spring Security.
    @PostMapping("/register")
    public String register(String visitorName, String phone, String purpose, String password) {
        int id = accounts.register(visitorName, phone, purpose, password);
        return "redirect:hello?regSuccess=true&newId=" + id;
    }
    @GetMapping("/main") public String main() { return "main"; }
    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("visitors", jdbc.queryForList("SELECT id, visitor_name, phone, purpose FROM visitor_record ORDER BY id DESC"));
        return "list";
    }
    @PostMapping("/addVisitor")
    public String add(String visitorName, String phone, String purpose, String password) {
        accounts.register(visitorName, phone, purpose, password);
        return "redirect:list";
    }
    @PostMapping("/deleteVisitor")
    public String delete(Integer id) { accounts.delete(id); return "redirect:list"; }
    @PostMapping("/updateVisitor")
    public String update(Integer id, String visitorName, String phone, String purpose) {
        accounts.update(id, visitorName, phone, purpose); return "redirect:list";
    }
    @GetMapping("/visitorIndex")
    public String visitorIndex(HttpSession session, Model model) {
        Integer id = (Integer) session.getAttribute("visitorId");
        model.addAttribute("msg", "欢迎，" + session.getAttribute("visitorName"));
        List<Map<String,Object>> rows = jdbc.queryForList("SELECT id, DATE_FORMAT(clock_in_time, '%H:%i') AS clock_in_time, DATE_FORMAT(clock_out_time, '%H:%i') AS clock_out_time FROM attendance_record WHERE user_id = ? AND work_date = CURDATE()", id);
        if (!rows.isEmpty()) model.addAttribute("todayRecord", rows.get(0));
        return "visitor_main";
    }
}
