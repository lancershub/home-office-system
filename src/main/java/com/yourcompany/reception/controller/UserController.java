package com.yourcompany.reception.controller;

import com.yourcompany.reception.service.VisitorService;
import com.yourcompany.reception.service.ChatMessageService;
import com.yourcompany.reception.entity.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.yourcompany.reception.util.Json;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private VisitorService visitorService;

    @Autowired
    private ChatMessageService chatMessageService;

    /**
     * 1. 页面跳转：返回人员调配看板页面
     */
    @GetMapping("/assignPage")
    public String assignPage(HttpSession session) {
        // 【关键修复】：人员调配是管理员特权，必须检查 adminUser
        if (session.getAttribute("adminUser") == null) {
            return "redirect:/hello";
        }
        return "employee_dept";
    }

    /**
     * 2. 获取数据：返回所有注册员工的列表
     */
    @GetMapping(value = "/list", produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getUserList(HttpSession session) {

        if (session.getAttribute("adminUser") == null) {
            return "[]"; // 没登录就返回空数组字符串
        }

        // 1. 查出那 13 个人
        List<Map<String, Object>> list = visitorService.getAllVisitorsWithDept();

        // 2. 强行手动翻译！把 Java 集合变成 JSON 字符串
        String jsonResult = Json.encode(list);

        // 3. 在控制台打印出来看看长什么样

        // 4. 直接把这段纯文本丢给前端！
        return jsonResult;
    }
    // 进入在线聊天室
    @GetMapping("/chatPage")
    public String chatPage(javax.servlet.http.HttpSession session) {
        // 门卫检查：强制必须登录后才能进聊天室！
        if (session.getAttribute("adminUser") == null && session.getAttribute("visitorId") == null) {
            return "redirect:/hello"; // 这里的 /hello 请改成你实际的登录页路径
        }
        return "chat_test"; // 跳转到 WEB-INF/views/.../chat_test.jsp
    }
    /**
     * 3. 核心功能：为员工分配部门
     */
    /**
     * 4. 获取聊天历史记录
     */
    @GetMapping(value = "/chatHistory", produces = "application/json;charset=utf-8")
    @ResponseBody
    public String chatHistory(String withUserId, Integer beforeId, HttpSession session) {
        String myUserId = null;
        if (session.getAttribute("adminUser") != null) {
            myUserId = "admin";
        } else if (session.getAttribute("visitorId") != null) {
            myUserId = session.getAttribute("visitorId").toString();
        }
        if (myUserId == null || withUserId == null) {
            return "[]";
        }
        List<ChatMessage> list = chatMessageService.getHistory(myUserId, withUserId, beforeId);
        return Json.encode(list);
    }

    @PostMapping("/assignDept")
    @ResponseBody
    public String assignDept(Integer userId, Integer deptId, HttpSession session) {
        // 【关键修复】：只有管理员才能改别人的部门
        if (session.getAttribute("adminUser") == null) return "error";

        boolean success = visitorService.updateEmployeeDept(userId, deptId);
        return success ? "success" : "fail";
    }
}