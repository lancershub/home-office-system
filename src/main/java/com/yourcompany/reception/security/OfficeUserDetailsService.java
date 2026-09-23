package com.yourcompany.reception.security;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class OfficeUserDetailsService implements UserDetailsService {
    private final JdbcTemplate jdbc;
    public OfficeUserDetailsService(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    @Override public UserDetails loadUserByUsername(String name) {
        boolean admin = "admin".equals(name);
        if (!admin && (name == null || !name.matches("[1-9][0-9]{0,8}"))) throw new UsernameNotFoundException("Unknown account");
        List<Map<String, Object>> rows = admin
            ? jdbc.queryForList("SELECT password_hash AS password FROM admin_account WHERE username = ?", name)
            : jdbc.queryForList("SELECT password FROM visitor_record WHERE id = ?", Integer.valueOf(name));
        if (rows.isEmpty()) throw new UsernameNotFoundException("Unknown account");
        String hash = String.valueOf(rows.get(0).get("password"));
        // Never accept plaintext passwords, including pre-migration records.
        if (!hash.matches("\\$2[aby]\\$[0-9]{2}\\$[./A-Za-z0-9]{53}")) throw new UsernameNotFoundException("Password reset required");
        return User.withUsername(name).password(hash).roles(admin ? "ADMIN" : "EMPLOYEE").build();
    }
    public String displayName(String id) {
        return jdbc.queryForObject("SELECT visitor_name FROM visitor_record WHERE id = ?", String.class, Integer.valueOf(id));
    }
}
