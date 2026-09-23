package com.yourcompany.reception.controller;
import com.yourcompany.reception.service.*;
import com.yourcompany.reception.task.FileCleanupTask;
import com.yourcompany.reception.entity.FileCabinet;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ContentDisposition;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Controller
@RequestMapping("/file")
public class FileCabinetController {
    private final FileCabinetService files;
    private final FileStorage storage;
    private final FileCleanupTask cleanup;
    public FileCabinetController(FileCabinetService files,FileStorage storage,FileCleanupTask cleanup){this.files=files;this.storage=storage;this.cleanup=cleanup;}
    @GetMapping("/view") public String view(){return "file_cabinet";}
    @PostMapping("/upload") @ResponseBody
    public String upload(@RequestParam("file") MultipartFile file,HttpSession session) throws IOException {
        String original=storage.validate(file),stored=UUID.randomUUID().toString();
        files.beginUpload((Integer)session.getAttribute("visitorId"),original,stored,file.getSize());
        try {
            storage.write(stored,file);
            if(!files.activate(stored)) {
                storage.delete(stored);
                throw new IOException("Upload expired; retry");
            }
        } catch(IOException | RuntimeException ex) {
            try { files.failUpload(stored); } catch(RuntimeException secondary) { ex.addSuppressed(secondary); }
            // The cleanup task owns compensation, also after process termination or a DB outage.
            throw ex;
        }
        return "success";
    }
    @GetMapping("/list") @ResponseBody public List<FileCabinet> list(HttpSession s){return files.listActive((Integer)s.getAttribute("visitorId"));}
    @GetMapping("/trash") @ResponseBody public List<FileCabinet> trash(HttpSession s){return files.listDeleted((Integer)s.getAttribute("visitorId"));}
    @GetMapping("/download/{id}")
    public void download(@PathVariable Integer id,HttpSession session,HttpServletResponse response) throws IOException {
        FileCabinet meta=files.owned(id,(Integer)session.getAttribute("visitorId"));
        if(meta==null || !Integer.valueOf(0).equals(meta.getIs_deleted()) || !"ACTIVE".equals(meta.getStorage_status())){response.sendError(404);return;}
        Path file=storage.path(meta.getFile_name());
        if(!Files.isRegularFile(file,LinkOption.NOFOLLOW_LINKS)){response.sendError(404);return;}
        response.setContentType("application/octet-stream");
        response.setHeader("X-Content-Type-Options","nosniff");
        response.setHeader("Content-Disposition",ContentDisposition.attachment().filename(meta.getOriginal_name(),StandardCharsets.UTF_8).build().toString());
        response.setContentLengthLong(Files.size(file));
        Files.copy(file,response.getOutputStream());
    }
    @PostMapping("/delete/{id}") @ResponseBody public String delete(@PathVariable Integer id,HttpSession s){return files.softDelete(id,(Integer)s.getAttribute("visitorId"))?"success":"fail";}
    @PostMapping("/restore/{id}") @ResponseBody public String restore(@PathVariable Integer id,HttpSession s){return files.restore(id,(Integer)s.getAttribute("visitorId"))?"success":"fail";}
    @PostMapping("/permanentDelete/{id}") @ResponseBody
    public String permanentDelete(@PathVariable Integer id,HttpSession s) throws IOException {
        FileCabinet meta=files.requestDeletion(id,(Integer)s.getAttribute("visitorId"));
        if(meta==null)return "fail";
        cleanup.purge(meta);
        return "success";
    }
}
