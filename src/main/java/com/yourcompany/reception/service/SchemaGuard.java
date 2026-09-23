package com.yourcompany.reception.service;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

/** Do not start the app with missing consistency constraints. Migration is an explicit deployment step. */
@Component
public class SchemaGuard {
    private final DataSource source;
    public SchemaGuard(DataSource source){this.source=source;}
    @PostConstruct public void verify() throws SQLException {
        try(Connection c=source.getConnection()){
            DatabaseMetaData meta=c.getMetaData();
            requireUnique(meta,c.getCatalog(),"attendance_record",new HashSet<>(Arrays.asList("user_id","work_date")));
            requireUnique(meta,c.getCatalog(),"message_record",new HashSet<>(Arrays.asList("sender_id","client_message_id")));
            requireUnique(meta,c.getCatalog(),"file_cabinet",Collections.singleton("file_name"));
            try(Statement s=c.createStatement()){
                s.executeQuery("SELECT storage_status FROM file_cabinet WHERE 1=0").close();
                s.executeQuery("SELECT password_hash FROM admin_account WHERE 1=0").close();
            }
        }catch(SQLException ex){throw new SQLException("Database schema is not ready. Apply db/schema.sql for a new database or db/migrations/V1__security_and_consistency.sql for an existing database before startup.",ex);}
    }
    private void requireUnique(DatabaseMetaData metadata,String catalog,String table,Set<String> columns)throws SQLException{
        Map<String,Set<String>> indexes=new HashMap<>();
        try(ResultSet rs=metadata.getIndexInfo(catalog,null,table,true,false)){
            while(rs.next()){
                String name=rs.getString("INDEX_NAME"),column=rs.getString("COLUMN_NAME");
                if(name!=null&&column!=null)indexes.computeIfAbsent(name,k->new HashSet<>()).add(column.toLowerCase(Locale.ROOT));
            }
        }
        if(!indexes.containsValue(columns))throw new SQLException("Missing unique constraint: "+table+columns);
    }
}
