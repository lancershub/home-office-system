package com.yourcompany.reception.controller;

import org.springframework.ui.Model;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

@Controller
public class ReceptionController {

    // 【关键】：这里注入了 jdbcTemplate，所有数据库操作都靠它
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ================== 1. 门户与登录相关 ==================

    @RequestMapping("/hello")
    public String sayHello() {
        return "index"; // 访问首页
    }

    @PostMapping("/login")
    public String login(String username, String password, HttpSession session, Model model) {
        // 角色 A：超级管理员登录
        if ("admin".equals(username) && "123456".equals(password)) {
            session.setAttribute("adminUser", username);
            return "redirect:main";
        }

        // 角色 B：普通访客登录
        try {
            int visitorId = Integer.parseInt(username);
            String sql = "SELECT * FROM visitor_record WHERE id = ? AND password = ?";
            List<Map<String, Object>> users = jdbcTemplate.queryForList(sql, visitorId, password);

            if (!users.isEmpty()) {
                Map<String, Object> user = users.get(0);
                session.setAttribute("visitorName", user.get("visitor_name"));
                session.setAttribute("visitorId", user.get("id"));
                return "redirect:visitorIndex";
            }
        } catch (NumberFormatException e) {
            // 输入的不是数字ID，忽略报错
        }

        model.addAttribute("loginError", "账号或密码错误，请检查！");
        return "index";
    }

    @RequestMapping("/logout.action")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:hello";
    }

    // ================== 2. 访客注册 (获取自增 ID) ==================

    @PostMapping("/register")
    public String register(String visitorName, String phone, String purpose, String password) {
        String sql = "INSERT INTO visitor_record (visitor_name, phone, purpose, password) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        // 【修改点】：改用原生的匿名内部类，完美兼容所有 Java 版本，不再报“不支持”的错误
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, visitorName);
                ps.setString(2, phone);
                ps.setString(3, purpose);
                ps.setString(4, password);
                return ps;
            }
        }, keyHolder);

        int newId = keyHolder.getKey().intValue();
        return "redirect:hello?regSuccess=true&newId=" + newId;
    }

    // ================== 3. 管理员后台功能 (增删改查) ==================

    @RequestMapping("/main")
    public String showMain(HttpSession session) {
        if (session.getAttribute("adminUser") == null) {
            return "redirect:hello";
        }
        return "main";
    }

    @RequestMapping("/list")
    public String showList(Model model) {
        String sql = "SELECT * FROM visitor_record ORDER BY id DESC";
        List<Map<String, Object>> visitorList = jdbcTemplate.queryForList(sql);
        model.addAttribute("visitors", visitorList);
        System.out.println("==== 管理员查询列表，实际查到的数据条数是：" + visitorList.size() + " ====");
        return "list";
    }

    @PostMapping("/addVisitor")
    public String addVisitor(String visitorName, String phone, String purpose) {
        // 后台管理员手动新增，默认给个空密码或者初始密码
        String sql = "INSERT INTO visitor_record (visitor_name, phone, purpose, password) VALUES (?, ?, ?, '123456')";
        jdbcTemplate.update(sql, visitorName, phone, purpose);
        return "redirect:list";
    }

    @RequestMapping("/deleteVisitor")
    public String deleteVisitor(Integer id) {
        String sql = "DELETE FROM visitor_record WHERE id = ?";
        jdbcTemplate.update(sql, id);
        return "redirect:list";
    }

    @PostMapping("/updateVisitor")
    public String updateVisitor(Integer id, String visitorName, String phone, String purpose) {
        String sql = "UPDATE visitor_record SET visitor_name = ?, phone = ?, purpose = ? WHERE id = ?";
        jdbcTemplate.update(sql, visitorName, phone, purpose, id);
        return "redirect:list";
    }

    // ================== 4. 访客个人专属后台 ==================

    // 4. 访客个人专属后台 (新增查询今日考勤逻辑)
    @RequestMapping("/visitorIndex")
    public String visitorIndex(HttpSession session, Model model) {
        Integer visitorId = (Integer) session.getAttribute("visitorId");
        if (visitorId == null) {
            return "redirect:hello";
        }

        model.addAttribute("msg", "尊敬的访客 " + session.getAttribute("visitorName") + "，欢迎登录！");

        // === 新增：查询当前用户今天的打卡记录 ===
        // 使用 CURDATE() 获取今天日期，使用 DATE_FORMAT 格式化时间让页面显示更美观
        String sql = "SELECT id, " +
                "DATE_FORMAT(clock_in_time, '%H:%i') AS clock_in_time, " +
                "DATE_FORMAT(clock_out_time, '%H:%i') AS clock_out_time " +
                "FROM attendance_record " +
                "WHERE user_id = ? AND work_date = CURDATE()";

        List<Map<String, Object>> records = jdbcTemplate.queryForList(sql, visitorId);

        if (!records.isEmpty()) {
            // 如果查到了数据，说明今天打过卡，把记录传给前端
            model.addAttribute("todayRecord", records.get(0));
        }

        return "visitor_main";
    }
}