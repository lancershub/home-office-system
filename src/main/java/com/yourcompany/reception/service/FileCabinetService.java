package com.yourcompany.reception.service;
import com.yourcompany.reception.entity.FileCabinet;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class FileCabinetService {
    private final JdbcTemplate jdbc;
    public FileCabinetService(JdbcTemplate jdbc) { this.jdbc=jdbc; }
    @Transactional
    public void beginUpload(Integer user, String original, String stored, long size) {
        jdbc.queryForObject("SELECT id FROM visitor_record WHERE id = ? FOR UPDATE", Integer.class, user);
        Long used=jdbc.queryForObject("SELECT COALESCE(SUM(file_size),0) FROM file_cabinet WHERE user_id = ?", Long.class,user);
        if (used + size > 1024L*1024*1024) throw new IllegalArgumentException("个人文件空间超过 1GB，请清理回收站");
        jdbc.update("INSERT INTO file_cabinet (user_id, original_name, file_name, file_size, file_type, storage_status) VALUES (?, ?, ?, ?, 'application/octet-stream', 'UPLOADING')", user,original,stored,size);
    }
    public boolean activate(String stored) {
        return jdbc.update("UPDATE file_cabinet SET storage_status = 'ACTIVE' WHERE file_name = ? AND storage_status = 'UPLOADING'",stored)==1;
    }
    public void failUpload(String stored) {
        jdbc.update("UPDATE file_cabinet SET storage_status = 'PENDING_DELETE', is_deleted = 1, delete_time = NOW() WHERE file_name = ? AND storage_status = 'UPLOADING'",stored);
    }
    public List<FileCabinet> listActive(Integer user) { return query("SELECT * FROM file_cabinet WHERE user_id = ? AND is_deleted = 0 AND storage_status = 'ACTIVE' ORDER BY upload_time DESC",user); }
    public List<FileCabinet> listDeleted(Integer user) { return query("SELECT * FROM file_cabinet WHERE user_id = ? AND is_deleted = 1 AND storage_status IN ('ACTIVE','PENDING_DELETE') ORDER BY delete_time DESC",user); }
    public FileCabinet owned(Integer id,Integer user) {
        List<FileCabinet> rows=query("SELECT * FROM file_cabinet WHERE id = ? AND user_id = ?",id,user);
        return rows.isEmpty()?null:rows.get(0);
    }
    public boolean softDelete(Integer id,Integer user) { return jdbc.update("UPDATE file_cabinet SET is_deleted = 1, delete_time = NOW() WHERE id = ? AND user_id = ? AND is_deleted = 0 AND storage_status = 'ACTIVE'",id,user)==1; }
    public boolean restore(Integer id,Integer user) { return jdbc.update("UPDATE file_cabinet SET is_deleted = 0, delete_time = NULL WHERE id = ? AND user_id = ? AND is_deleted = 1 AND storage_status = 'ACTIVE'",id,user)==1; }
    public FileCabinet requestDeletion(Integer id,Integer user) {
        jdbc.update("UPDATE file_cabinet SET storage_status = 'PENDING_DELETE' WHERE id = ? AND user_id = ? AND is_deleted = 1 AND storage_status = 'ACTIVE'",id,user);
        FileCabinet meta=owned(id,user);
        return meta!=null && "PENDING_DELETE".equals(meta.getStorage_status())?meta:null;
    }
    public List<FileCabinet> pendingCleanup() {
        jdbc.update("UPDATE file_cabinet SET storage_status = 'PENDING_DELETE', is_deleted = 1, delete_time = NOW() WHERE storage_status = 'UPLOADING' AND upload_time < DATE_SUB(NOW(), INTERVAL 1 HOUR)");
        return query("SELECT * FROM file_cabinet WHERE storage_status = 'PENDING_DELETE' ORDER BY id LIMIT 100");
    }
    public void finishDeletion(Integer id) { jdbc.update("DELETE FROM file_cabinet WHERE id = ? AND storage_status = 'PENDING_DELETE'",id); }
    private List<FileCabinet> query(String sql,Object...args) { return jdbc.query(sql,args,new BeanPropertyRowMapper<>(FileCabinet.class)); }
}
