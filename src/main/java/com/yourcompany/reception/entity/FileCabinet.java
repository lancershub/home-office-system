package com.yourcompany.reception.entity;

import java.util.Date;

public class FileCabinet {
    private String storage_status;
    public String getStorage_status() { return storage_status; }
    public void setStorage_status(String value) { storage_status = value; }
    private Integer id;
    private Integer user_id;
    private String file_name;
    private String original_name;
    private Long file_size;
    private String file_type;
    private Date upload_time;
    private Integer is_deleted;
    private Date delete_time;

    public FileCabinet() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUser_id() { return user_id; }
    public void setUser_id(Integer user_id) { this.user_id = user_id; }

    public String getFile_name() { return file_name; }
    public void setFile_name(String file_name) { this.file_name = file_name; }

    public String getOriginal_name() { return original_name; }
    public void setOriginal_name(String original_name) { this.original_name = original_name; }

    public Long getFile_size() { return file_size; }
    public void setFile_size(Long file_size) { this.file_size = file_size; }

    public String getFile_type() { return file_type; }
    public void setFile_type(String file_type) { this.file_type = file_type; }

    public Date getUpload_time() { return upload_time; }
    public void setUpload_time(Date upload_time) { this.upload_time = upload_time; }

    public Integer getIs_deleted() { return is_deleted; }
    public void setIs_deleted(Integer is_deleted) { this.is_deleted = is_deleted; }

    public Date getDelete_time() { return delete_time; }
    public void setDelete_time(Date delete_time) { this.delete_time = delete_time; }
}
