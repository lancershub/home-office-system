package com.yourcompany.reception.service;
import com.yourcompany.reception.util.Input;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.PreparedStatement;
import java.sql.Statement;

@Service
public class AccountService {
    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwords;
    public AccountService(JdbcTemplate jdbc, PasswordEncoder passwords) { this.jdbc = jdbc; this.passwords = passwords; }
    public int register(String name, String phone, String purpose, String password) {
        String n = Input.text(name, "姓名", 100, true), p = Input.text(phone, "电话", 32, true), reason = Input.text(purpose, "说明", 500, false);
        String hash = passwords.encode(Input.password(password));
        KeyHolder key = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO visitor_record (visitor_name, phone, purpose, password) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, n); ps.setString(2, p); ps.setString(3, reason); ps.setString(4, hash); return ps;
        }, key);
        return key.getKey().intValue();
    }
    public void update(Integer id, String name, String phone, String purpose) {
        jdbc.update("UPDATE visitor_record SET visitor_name = ?, phone = ?, purpose = ? WHERE id = ?",
            Input.text(name, "姓名", 100, true), Input.text(phone, "电话", 32, true), Input.text(purpose, "说明", 500, false), Input.id(id));
    }
    @Transactional
    public void delete(Integer id) {
        Input.id(id);
        // Preserve business records: accounts with dependent data cannot be physically deleted.
        jdbc.queryForObject("SELECT id FROM visitor_record WHERE id = ? FOR UPDATE", Integer.class, id);
        for (String table : new String[]{"attendance_record", "schedule_record", "file_cabinet"}) {
            if (jdbc.queryForObject("SELECT COUNT(*) FROM " + table + " WHERE user_id = ?", Integer.class, id) > 0)
                throw new IllegalArgumentException("该员工存在业务记录，不能直接删除");
        }
        if (jdbc.queryForObject("SELECT COUNT(*) FROM message_record WHERE sender_id = ? OR receiver_id = ?", Integer.class, id, id) > 0)
            throw new IllegalArgumentException("该员工存在聊天记录，不能直接删除");
        jdbc.update("DELETE FROM visitor_record WHERE id = ?", id);
    }
}
