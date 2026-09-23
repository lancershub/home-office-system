package com.yourcompany.reception.controller;

import com.yourcompany.reception.util.Json;
import com.yourcompany.reception.entity.Institution;
import com.yourcompany.reception.service.InstitutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/inst")
public class InstitutionController {

    @Autowired
    private InstitutionService institutionService;

    @GetMapping("/view")
    public String viewPage(HttpSession session) {
        if (session.getAttribute("adminUser") == null) return "redirect:/hello";
        return "institution";
    }

    @GetMapping(value = "/list", produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getList(HttpSession session) {
        if (session.getAttribute("adminUser") == null) return "[]";
        List<Institution> list = institutionService.getAllInstitutions();
        return Json.encode(list);
    }

    @PostMapping("/save")
    @ResponseBody
    public String save(Institution institution, HttpSession session) {
        if (session.getAttribute("adminUser") == null) return "error";
        boolean success = institutionService.saveOrUpdate(institution);
        return success ? "success" : "fail";
    }

    @PostMapping("/delete")
    @ResponseBody
    public String delete(Integer id, HttpSession session) {
        if (session.getAttribute("adminUser") == null) return "error";
        boolean success = institutionService.deleteInstitution(id);
        return success ? "success" : "fail";
    }
}