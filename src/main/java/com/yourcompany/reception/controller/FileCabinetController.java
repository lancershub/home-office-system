package com.yourcompany.reception.controller;

import com.alibaba.fastjson.JSON;
import com.yourcompany.reception.entity.FileCabinet;
import com.yourcompany.reception.service.FileCabinetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/file")
public class FileCabinetController {

    @Autowired
    private FileCabinetService fileCabinetService;

    private static final String UPLOAD_DIR = System.getProperty("user.home") + "/file-cabinet/";

    @RequestMapping("/view")
    public String viewPage(HttpSession session) {
        if (session.getAttribute("visitorId") == null) return "redirect:/hello";
        return "file_cabinet";
    }

    @RequestMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("visitorId");
        if (userId == null || file.isEmpty()) return "redirect:/hello";

        try {
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) dir.mkdirs();

            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String storedName = UUID.randomUUID().toString() + ext;

            file.transferTo(new File(dir, storedName));

            String fileType = file.getContentType();
            fileCabinetService.saveMeta(userId, originalName, storedName, file.getSize(), fileType);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "redirect:/file/view";
    }

    @RequestMapping(value = "/list", produces = "application/json;charset=utf-8")
    @ResponseBody
    public String list(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("visitorId");
        if (userId == null) return "[]";
        List<FileCabinet> list = fileCabinetService.listActive(userId);
        return JSON.toJSONString(list);
    }

    @RequestMapping(value = "/trash", produces = "application/json;charset=utf-8")
    @ResponseBody
    public String trash(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("visitorId");
        if (userId == null) return "[]";
        List<FileCabinet> list = fileCabinetService.listDeleted(userId);
        return JSON.toJSONString(list);
    }

    @RequestMapping("/download/{id}")
    public void download(@PathVariable Integer id, HttpSession session, HttpServletResponse response) {
        Integer userId = (Integer) session.getAttribute("visitorId");
        if (userId == null) return;

        FileCabinet meta = fileCabinetService.getById(id);
        if (meta == null || !meta.getUser_id().equals(userId)) return;

        File file = new File(UPLOAD_DIR + meta.getFile_name());
        if (!file.exists()) return;

        try {
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" +
                    new String(meta.getOriginal_name().getBytes("UTF-8"), "ISO-8859-1") + "\"");
            response.setContentLengthLong(file.length());

            OutputStream os = response.getOutputStream();
            Files.copy(file.toPath(), os);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("/delete/{id}")
    @ResponseBody
    public String softDelete(@PathVariable Integer id, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("visitorId");
        if (userId == null) return "error";
        return fileCabinetService.softDelete(id, userId) ? "success" : "fail";
    }

    @RequestMapping("/restore/{id}")
    @ResponseBody
    public String restore(@PathVariable Integer id, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("visitorId");
        if (userId == null) return "error";
        return fileCabinetService.restore(id, userId) ? "success" : "fail";
    }

    @RequestMapping("/permanentDelete/{id}")
    @ResponseBody
    public String permanentDelete(@PathVariable Integer id, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("visitorId");
        if (userId == null) return "error";

        FileCabinet meta = fileCabinetService.getById(id);
        if (meta == null || !meta.getUser_id().equals(userId)) return "fail";

        if (fileCabinetService.permanentDelete(id, userId)) {
            File file = new File(UPLOAD_DIR + meta.getFile_name());
            if (file.exists()) file.delete();
            return "success";
        }
        return "fail";
    }
}
