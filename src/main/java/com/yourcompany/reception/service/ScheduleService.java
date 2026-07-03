package com.yourcompany.reception.service; // 对应你的包名

import com.yourcompany.reception.entity.Schedule; // 直接导入 entity 包下的类
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScheduleService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Schedule> getAllSchedules(Integer userId) {
        String sql = "SELECT * FROM schedule_record WHERE user_id = ? ORDER BY schedule_date ASC";
        return jdbcTemplate.query(sql, new Object[]{userId}, new BeanPropertyRowMapper<>(Schedule.class));
    }

    public List<Schedule> getTodaySchedules(Integer userId) {
        String sql = "SELECT * FROM schedule_record WHERE user_id = ? AND schedule_date = CURDATE() AND status = 0";
        return jdbcTemplate.query(sql, new Object[]{userId}, new BeanPropertyRowMapper<>(Schedule.class));
    }

    public int save(Schedule s) {
        String sql = "INSERT INTO schedule_record (user_id, title, content, schedule_date, status) VALUES (?, ?, ?, ?, 0)";
        return jdbcTemplate.update(sql, s.getUser_id(), s.getTitle(), s.getContent(), s.getSchedule_date());
    }

    public int delete(Integer id, Integer userId) {
        String sql = "DELETE FROM schedule_record WHERE id = ? AND user_id = ?";
        return jdbcTemplate.update(sql, id, userId);
    }
}