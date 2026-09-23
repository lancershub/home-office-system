package com.yourcompany.reception.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import com.yourcompany.reception.util.Input;

@Service
public class FileStorage {
    public static final long MAX_SIZE=20L*1024*1024;
    private final Path root;
    public FileStorage(@Value("${OFFICE_UPLOAD_DIR}") String directory) {
        if (directory==null || directory.trim().isEmpty() || !Paths.get(directory).isAbsolute()) throw new IllegalArgumentException("OFFICE_UPLOAD_DIR must be absolute");
        root=Paths.get(directory).toAbsolutePath().normalize();
    }
    public String validate(MultipartFile file) {
        if(file.isEmpty() || file.getSize()>MAX_SIZE) throw new IllegalArgumentException("文件必须非空且不超过 20MB");
        String name=Input.text(file.getOriginalFilename(),"文件名",200,true);
        if(name.contains("/") || name.contains("\\") || name.chars().anyMatch(c -> c < 32 || c == 127)) throw new IllegalArgumentException("文件名无效");
        String ext=name.contains(".")?name.substring(name.lastIndexOf('.')+1).toLowerCase(Locale.ROOT):"";
        if(!Arrays.asList("pdf","txt","csv","doc","docx","xls","xlsx","ppt","pptx","png","jpg","jpeg","zip").contains(ext)) throw new IllegalArgumentException("不支持该文件类型");
        return name;
    }
    public Path path(String name) {
        // Generated storage keys have no user-controlled extension or directory.
        if(name==null || name.contains("/") || name.contains("\\") || name.equals(".") || name.equals("..")) throw new IllegalArgumentException("Invalid storage key");
        Path path=root.resolve(name).normalize();
        if(!path.getParent().equals(root)) throw new IllegalArgumentException("Invalid storage key");
        return path;
    }
    public void write(String stored,MultipartFile file) throws IOException {
        Files.createDirectories(root);
        Path partial=path(stored+".part"),target=path(stored);
        try(java.io.InputStream in=file.getInputStream(); java.io.OutputStream out=Files.newOutputStream(partial,StandardOpenOption.CREATE_NEW)) {
            byte[] buffer=new byte[8192]; int n; long total=0;
            while((n=in.read(buffer))!=-1) { total+=n; if(total>MAX_SIZE) throw new IOException("File exceeds size limit"); out.write(buffer,0,n); }
            if(total!=file.getSize()) throw new IOException("Incomplete upload");
        }
        try { Files.move(partial,target,StandardCopyOption.ATOMIC_MOVE); }
        catch(AtomicMoveNotSupportedException ex) { Files.move(partial,target); }
    }
    public void delete(String stored) throws IOException {
        Files.deleteIfExists(path(stored+".part"));
        Files.deleteIfExists(path(stored));
    }
}
