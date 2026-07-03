package com.yourcompany.reception.entity;

import java.util.Date;

public class Institution {
    private Integer id;
    private String inst_name;
    private String inst_desc;
    private Date create_time;

    public Institution() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getInst_name() { return inst_name; }
    public void setInst_name(String inst_name) { this.inst_name = inst_name; }

    public String getInst_desc() { return inst_desc; }
    public void setInst_desc(String inst_desc) { this.inst_desc = inst_desc; }

    public Date getCreate_time() { return create_time; }
    public void setCreate_time(Date create_time) { this.create_time = create_time; }
}