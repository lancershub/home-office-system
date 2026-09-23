package com.yourcompany.reception.util;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.sql.*;
import java.util.Arrays;

/** Offline admin bootstrap / password reset. Password is read from console or a temporary environment variable. */
public final class AccountPasswordTool {
    public static void main(String[] args) throws Exception {
        if(args.length!=1 || !("admin".equals(args[0]) || args[0].matches("[1-9][0-9]{0,8}"))) throw new IllegalArgumentException("Usage: AccountPasswordTool admin|employeeId");
        char[] secret=System.console()==null ? required("OFFICE_ACCOUNT_PASSWORD").toCharArray() : System.console().readPassword("New password (12+ characters): ");
        String hash;
        try { hash=new BCryptPasswordEncoder(12).encode(Input.password(new String(secret))); } finally {Arrays.fill(secret,'\0');}
        try(Connection db=DriverManager.getConnection(required("OFFICE_DB_URL"),required("OFFICE_DB_USER"),required("OFFICE_DB_PASSWORD"))) {
            if("admin".equals(args[0])) {
                try(PreparedStatement ps=db.prepareStatement("INSERT INTO admin_account(username,password_hash) VALUES ('admin',?) ON DUPLICATE KEY UPDATE password_hash=VALUES(password_hash)")) {ps.setString(1,hash);ps.executeUpdate();}
            }else{
                try(PreparedStatement ps=db.prepareStatement("UPDATE visitor_record SET password=? WHERE id=?")) {
                    ps.setString(1,hash);ps.setInt(2,Integer.parseInt(args[0]));if(ps.executeUpdate()!=1)throw new IllegalArgumentException("Account not found");
                }
            }
        }
        System.out.println("Password reset completed; restart the application to invalidate existing sessions.");
    }
    private static String required(String name) {
        String value=System.getenv(name);
        if(value==null || value.isEmpty())throw new IllegalArgumentException("Missing "+name);
        return value;
    }
}
