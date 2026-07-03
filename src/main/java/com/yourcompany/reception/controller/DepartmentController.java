package com.yourcompany.reception.controller;

import com.yourcompany.reception.entity.Department;
import com.yourcompany.reception.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpSession;
import java.util.List;
import com.alibaba.fastjson.JSON;

@Controller
@RequestMapping("/dept")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    // 1. 页面跳转：返回部门管理页面
    @RequestMapping("/view")
    public String viewPage(HttpSession session, Model model) {
        if (session.getAttribute("adminUser") == null) {
            return "redirect:/hello";
        }
        model.addAttribute("institutions", departmentService.getAllInstitutions());
        return "dept";
    }

    // 2. 获取数据：返回给前端的 JSON 列表
    @RequestMapping(value = "/list", produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getList(HttpSession session) {
        if (session.getAttribute("adminUser") == null) {
            return "[]";
        }
        List<Department> depts = departmentService.getAllDepartments();
        return JSON.toJSONString(depts);
    }

    // 3. 保存或更新部门
    @RequestMapping("/save")
    @ResponseBody
    public String save(Department department, HttpSession session) {
        if (session.getAttribute("adminUser") == null) return "error";
        boolean success = departmentService.saveOrUpdate(department);
        return success ? "success" : "fail";
    }

    // 4. 删除部门
    @RequestMapping("/delete")
    @ResponseBody
    public String delete(Integer id, HttpSession session) {
        if (session.getAttribute("adminUser") == null) return "error";
        boolean success = departmentService.deleteDepartment(id);
        return success ? "success" : "fail";
    }
}