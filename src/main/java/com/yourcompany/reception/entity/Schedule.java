package com.yourcompany.reception.entity; // 对应你的包名

/**
 * 日程管理实体类
 */
public class Schedule {
    private Integer id;
    private Integer user_id;
    private String title;
    private String content;
    private String schedule_date;
    private Integer status;
    private String create_time;

    public Schedule() {}

    // Getter & Setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUser_id() { return user_id; }
    public void setUser_id(Integer user_id) { this.user_id = user_id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSchedule_date() { return schedule_date; }
    public void setSchedule_date(String schedule_date) { this.schedule_date = schedule_date; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getCreate_time() { return create_time; }
    public void setCreate_time(String create_time) { this.create_time = create_time; }
}