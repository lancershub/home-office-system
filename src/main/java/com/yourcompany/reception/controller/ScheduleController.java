package com.yourcompany.reception.controller;

import com.yourcompany.reception.entity.Schedule;
import com.yourcompany.reception.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/schedule")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    // 跳转到日历页面
    @GetMapping("/view")
    public String viewPage(HttpSession session) {
        // 【修改点1】暗号改成 visitorId
        if (session.getAttribute("visitorId") == null) {
            return "redirect:/hello"; // 如果没登录，踢回登录页
        }
        return "schedule";
    }

    @GetMapping("/data")
    @ResponseBody
    public List<Schedule> getJsonData(HttpSession session) {
        // 【修改点2】暗号改成 visitorId
        Integer userId = (Integer) session.getAttribute("visitorId");
        if (userId == null) return new ArrayList<>();
        return scheduleService.getAllSchedules(userId);
    }

    @PostMapping("/add")
    @ResponseBody
    public String add(Schedule schedule, HttpSession session) {
        // 【修改点3】暗号改成 visitorId
        Integer userId = (Integer) session.getAttribute("visitorId");
        if (userId == null) return "error";
        schedule.setUser_id(userId);
        return scheduleService.save(schedule) > 0 ? "success" : "fail";
    }

    @PostMapping("/delete")
    @ResponseBody
    public String delete(Integer id, HttpSession session) {
        // 【修改点4】暗号改成 visitorId
        Integer userId = (Integer) session.getAttribute("visitorId");
        if (userId == null) return "error";
        return scheduleService.delete(id, userId) > 0 ? "success" : "fail";
    }
}