package com.yourcompany.reception.service;

import com.yourcompany.reception.entity.FileCabinet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FileCabinetService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void saveMeta(Integer userId, String originalName, String storedName, Long fileSize, String fileType) {
        String sql = "INSERT INTO file_cabinet (user_id, file_name, original_name, file_size, file_type) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, userId, storedName, originalName, fileSize, fileType);
    }

    public List<FileCabinet> listActive(Integer userId) {
        String sql = "SELECT * FROM file_cabinet WHERE user_id = ? AND is_deleted = 0 ORDER BY upload_time DESC";
        return jdbcTemplate.query(sql, new Object[]{userId}, new BeanPropertyRowMapper<>(FileCabinet.class));
    }

    public List<FileCabinet> listDeleted(Integer userId) {
        String sql = "SELECT * FROM file_cabinet WHERE user_id = ? AND is_deleted = 1 ORDER BY delete_time DESC";
        return jdbcTemplate.query(sql, new Object[]{userId}, new BeanPropertyRowMapper<>(FileCabinet.class));
    }

    public FileCabinet getById(Integer id) {
        String sql = "SELECT * FROM file_cabinet WHERE id = ?";
        List<FileCabinet> list = jdbcTemplate.query(sql, new Object[]{id}, new BeanPropertyRowMapper<>(FileCabinet.class));
        return list.isEmpty() ? null : list.get(0);
    }

    public boolean softDelete(Integer id, Integer userId) {
        String sql = "UPDATE file_cabinet SET is_deleted = 1, delete_time = NOW() WHERE id = ? AND user_id = ? AND is_deleted = 0";
        return jdbcTemplate.update(sql, id, userId) > 0;
    }

    public boolean restore(Integer id, Integer userId) {
        String sql = "UPDATE file_cabinet SET is_deleted = 0, delete_time = NULL WHERE id = ? AND user_id = ? AND is_deleted = 1";
        return jdbcTemplate.update(sql, id, userId) > 0;
    }

    public boolean permanentDelete(Integer id, Integer userId) {
        String sql = "DELETE FROM file_cabinet WHERE id = ? AND user_id = ? AND is_deleted = 1";
        return jdbcTemplate.update(sql, id, userId) > 0;
    }
}
