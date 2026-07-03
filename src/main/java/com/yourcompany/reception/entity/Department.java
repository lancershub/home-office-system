package com.yourcompany.reception.entity;

import java.util.Date;

public class Department {
    private Integer id;
    private String dept_name;
    private String dept_desc;
    private Integer manager_id;
    private Integer inst_id;
    private String inst_name;   // JOIN 查询展示用，非数据库字段
    private Date create_time;

    public Department() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getDept_name() { return dept_name; }
    public void setDept_name(String dept_name) { this.dept_name = dept_name; }

    public String getDept_desc() { return dept_desc; }
    public void setDept_desc(String dept_desc) { this.dept_desc = dept_desc; }

    public Integer getManager_id() { return manager_id; }
    public void setManager_id(Integer manager_id) { this.manager_id = manager_id; }

    public Integer getInst_id() { return inst_id; }
    public void setInst_id(Integer inst_id) { this.inst_id = inst_id; }

    public String getInst_name() { return inst_name; }
    public void setInst_name(String inst_name) { this.inst_name = inst_name; }

    public Date getCreate_time() { return create_time; }
    public void setCreate_time(Date create_time) { this.create_time = create_time; }
}