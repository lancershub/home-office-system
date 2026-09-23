package com.yourcompany.reception;
import com.yourcompany.reception.service.*;
import com.yourcompany.reception.entity.*;
import com.yourcompany.reception.task.FileCleanupTask;
import com.yourcompany.reception.util.Csv;
import org.junit.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.sql.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PersistenceTest {
    private JdbcTemplate jdbc;
    private DriverManagerDataSource ds;
    private String schema;
    private Path directory;
    @Before public void setup() throws Exception {
        schema="office_security_test_"+UUID.randomUUID().toString().replace("-","");
        String base=System.getenv("OFFICE_TEST_MYSQL_URL");
        if(base!=null){
            if(!base.equals("jdbc:mysql://127.0.0.1:13307/")) throw new IllegalArgumentException("Only isolated test MySQL on port 13307 is allowed");
            try(Connection c=DriverManager.getConnection(base,"root","");Statement s=c.createStatement()){s.execute("CREATE DATABASE "+schema);}
            ds=new DriverManagerDataSource(base+schema+"?serverTimezone=Asia/Shanghai","root","");
            try(Connection c=ds.getConnection()){ScriptUtils.executeSqlScript(c,new ClassPathResource("db/schema.sql"));}
        }else{
            ds=new DriverManagerDataSource("jdbc:h2:mem:"+schema+";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1","sa","");
            jdbc=new JdbcTemplate(ds);
            jdbc.execute("CREATE TABLE visitor_record(id INT PRIMARY KEY AUTO_INCREMENT,visitor_name VARCHAR(100),phone VARCHAR(32),purpose VARCHAR(500),password VARCHAR(255),dept_id INT)");
            jdbc.execute("CREATE TABLE admin_account(username VARCHAR(64) PRIMARY KEY,password_hash VARCHAR(255))");
            jdbc.execute("CREATE TABLE attendance_record(id INT PRIMARY KEY AUTO_INCREMENT,user_id INT NOT NULL,user_name VARCHAR(100),work_date DATE NOT NULL,clock_in_time TIMESTAMP,clock_out_time TIMESTAMP,UNIQUE(user_id,work_date))");
            jdbc.execute("CREATE TABLE message_record(id INT PRIMARY KEY AUTO_INCREMENT,sender_id INT,receiver_id INT,content TEXT,send_time TIMESTAMP,is_read INT,client_message_id VARCHAR(64),UNIQUE(sender_id,client_message_id))");
            jdbc.execute("CREATE TABLE file_cabinet(id INT PRIMARY KEY AUTO_INCREMENT,user_id INT,file_name VARCHAR(255) UNIQUE,original_name VARCHAR(255),file_size BIGINT,file_type VARCHAR(100),upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,is_deleted INT DEFAULT 0,delete_time TIMESTAMP,storage_status VARCHAR(24) DEFAULT 'ACTIVE')");
        }
        jdbc=new JdbcTemplate(ds);
        jdbc.update("INSERT INTO visitor_record(id,visitor_name,phone,password) VALUES(1,'One','1','disabled'),(2,'Two','2','disabled')");
        jdbc.update("INSERT INTO admin_account VALUES('admin','disabled')");
        directory=Files.createTempDirectory("office-files-test-");
    }
    @After public void cleanup() throws Exception {
        if(directory!=null)try(java.util.stream.Stream<Path> paths=Files.walk(directory)){paths.sorted(Comparator.reverseOrder()).forEach(p->{try{Files.delete(p);}catch(Exception ex){throw new RuntimeException(ex);}});}
        if(ds!=null){
            if(System.getenv("OFFICE_TEST_MYSQL_URL")!=null && schema.matches("office_security_test_[a-f0-9]{32}"))
                try(Connection c=DriverManager.getConnection("jdbc:mysql://127.0.0.1:13307/","root","");Statement s=c.createStatement()){s.execute("DROP DATABASE "+schema);}
            else jdbc.execute("SHUTDOWN");
        }
    }
    @Test public void concurrentClockInAndRepeatedClockOutPreserveFirstTimestamps() throws Exception {
        AttendanceService service=new AttendanceService(jdbc);
        ExecutorService pool=Executors.newFixedThreadPool(8);
        try{
            List<Future<?>> jobs=new ArrayList<>();
            for(int i=0;i<20;i++)jobs.add(pool.submit(()->service.clockIn(1,"One")));
            for(Future<?> job:jobs)job.get(15,TimeUnit.SECONDS);
        }finally{pool.shutdownNow();}
        assertEquals(Integer.valueOf(1),jdbc.queryForObject("SELECT COUNT(*) FROM attendance_record",Integer.class));
        Timestamp first=jdbc.queryForObject("SELECT clock_in_time FROM attendance_record",Timestamp.class);
        service.clockIn(1,"One");
        assertEquals(first,jdbc.queryForObject("SELECT clock_in_time FROM attendance_record",Timestamp.class));
        service.clockOut(1);
        Timestamp out=jdbc.queryForObject("SELECT clock_out_time FROM attendance_record",Timestamp.class);
        service.clockOut(1);
        assertEquals(out,jdbc.queryForObject("SELECT clock_out_time FROM attendance_record",Timestamp.class));
    }
    @Test public void clockOutWithoutClockInIsRejected(){
        assertThrows(IllegalArgumentException.class,()->new AttendanceService(jdbc).clockOut(1));
        assertEquals(Integer.valueOf(0),jdbc.queryForObject("SELECT COUNT(*) FROM attendance_record",Integer.class));
    }
    @Test public void messageRetryIsIdempotentAndPayloadCannotChange(){
        ChatMessageService service=new ChatMessageService(jdbc);
        String key=UUID.randomUUID().toString(),content="quote \"\n<img src=x onerror=alert(1)>";
        Map<String,Object> first=service.save("1","2",content,key);
        assertEquals(first.get("id"),service.save("1","2",content,key).get("id"));
        assertEquals(Integer.valueOf(1),jdbc.queryForObject("SELECT COUNT(*) FROM message_record",Integer.class));
        assertThrows(IllegalArgumentException.class,()->service.save("1","admin","different",key));
        assertEquals(content,service.getHistory("1","2",null).get(0).getContent());
        assertTrue(service.getHistory("admin","2",null).isEmpty());
    }
    @Test public void historyIsBoundedAndCanPageBackwards(){
        ChatMessageService service=new ChatMessageService(jdbc);
        for(int i=0;i<105;i++)service.save("1","2","message "+i,UUID.randomUUID().toString());
        List<ChatMessage> latest=service.getHistory("1","2",null);
        assertEquals(100,latest.size());
        assertEquals(5,service.getHistory("1","2",latest.get(0).getId()).size());
    }
    @Test public void registrationStoresOnlySaltedHash(){
        AccountService accounts=new AccountService(jdbc,new BCryptPasswordEncoder(4));
        String password="A-test-password-2026";
        int id=accounts.register("User","123","purpose",password);
        String stored=jdbc.queryForObject("SELECT password FROM visitor_record WHERE id=?",String.class,id);
        assertNotEquals(password,stored);
        assertTrue(new BCryptPasswordEncoder().matches(password,stored));
        assertThrows(IllegalArgumentException.class,()->accounts.register("User","123","","123456"));
    }
    @Test public void fileOwnershipAndDeletionTombstoneSurviveStorageFailure() throws Exception {
        FileCabinetService service=new FileCabinetService(jdbc);
        new TransactionTemplate(new DataSourceTransactionManager(ds)).execute(status->{service.beginUpload(1,"notes.txt","key",4);return null;});
        assertTrue(service.activate("key"));
        FileCabinet meta=service.listActive(1).get(0);
        assertNull(service.owned(meta.getId(),2));
        assertFalse(service.softDelete(meta.getId(),2));
        assertTrue(service.softDelete(meta.getId(),1));
        assertNotNull(service.requestDeletion(meta.getId(),1));
        assertFalse(service.restore(meta.getId(),1));
        FileStorage broken=mock(FileStorage.class);
        doThrow(new java.io.IOException("disk offline")).when(broken).delete("key");
        FileCleanupTask cleanup=new FileCleanupTask(service,broken);
        assertThrows(java.io.IOException.class,()->cleanup.purge(meta));
        assertEquals("PENDING_DELETE",service.owned(meta.getId(),1).getStorage_status());
        doNothing().when(broken).delete("key");
        cleanup.purge(meta);
        assertNull(service.owned(meta.getId(),1));
    }
    @Test public void failedUploadRemainsDiscoverableAndCanBeCleaned() throws Exception {
        FileCabinetService service=new FileCabinetService(jdbc);
        service.beginUpload(1,"notes.txt","failed-upload",4);
        FileStorage storage=new FileStorage(directory.toString());
        Files.write(storage.path("failed-upload.part"),new byte[]{1});
        service.failUpload("failed-upload");
        assertTrue(service.listActive(1).isEmpty());
        FileCabinet meta=service.listDeleted(1).get(0);
        new FileCleanupTask(service,storage).purge(meta);
        assertFalse(Files.exists(storage.path("failed-upload.part")));
        assertNull(service.owned(meta.getId(),1));
    }
    @Test public void storageRejectsTraversalAndWritesOnlyGeneratedKeys() throws Exception {
        FileStorage storage=new FileStorage(directory.toString());
        assertThrows(IllegalArgumentException.class,()->storage.path("../escape"));
        assertThrows(IllegalArgumentException.class,()->storage.validate(new MockMultipartFile("file","../x.txt","text/plain",new byte[]{1})));
        assertThrows(IllegalArgumentException.class,()->storage.validate(new MockMultipartFile("file","x.jsp","text/plain",new byte[]{1})));
        MockMultipartFile upload=new MockMultipartFile("file","x.txt","text/plain","hello".getBytes("UTF-8"));
        storage.write("generated-key",upload);
        assertEquals("hello",new String(Files.readAllBytes(storage.path("generated-key")),"UTF-8"));
        storage.delete("generated-key");storage.delete("generated-key");
    }
    @Test public void schemaGuardAcceptsMigratedDatabase() throws Exception {
        new SchemaGuard(ds).verify();
    }
    @Test public void schemaGuardRejectsMissingAttendanceConstraint() throws Exception {
        jdbc.execute("DROP TABLE attendance_record");
        jdbc.execute("CREATE TABLE attendance_record(id INT PRIMARY KEY AUTO_INCREMENT,user_id INT,work_date DATE)");
        assertThrows(SQLException.class,()->new SchemaGuard(ds).verify());
    }
    @Test public void csvEscapesQuotesLinesAndSpreadsheetFormulas(){
        assertEquals("\"' =1+1\"",Csv.cell(" =1+1"));
        assertEquals("\"a,\"\"b\"\"\nnext\"",Csv.cell("a,\"b\"\nnext"));
        assertEquals("\"'@SUM(A1)\"",Csv.cell("@SUM(A1)"));
    }
}
