package com.yourcompany.reception.controller;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.logging.*;
@ControllerAdvice
public class ApiExceptionHandler {
    private static final Logger LOG = Logger.getLogger(ApiExceptionHandler.class.getName());
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String,String>> badInput(IllegalArgumentException ex) { return error(400, ex.getMessage()); }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String,String>> conflict(DataIntegrityViolationException ex) { return error(409, "数据冲突或仍被引用，请刷新后重试"); }
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String,String>> tooLarge(MaxUploadSizeExceededException ex) { return error(413, "文件超过大小限制"); }
    @ExceptionHandler({DataAccessException.class, IOException.class})
    public ResponseEntity<Map<String,String>> unavailable(Exception ex) {
        LOG.log(Level.SEVERE, "Persistence or storage operation failed", ex);
        return error(503, "暂时无法完成操作，请稍后重试");
    }
    private ResponseEntity<Map<String,String>> error(int status, String message) { return ResponseEntity.status(status).body(Collections.singletonMap("message", message)); }
}
