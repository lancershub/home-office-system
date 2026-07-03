package com.yourcompany.reception.task;

import com.yourcompany.reception.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReminderTask {

    @Autowired
    private ScheduleService scheduleService;

    // Cron 表达式：每天早上 8:00 执行一次
    // 格式：秒 分 时 日 月 周
    @Scheduled(cron = "0 0 8 * * ?")
    public void checkTodaySchedules() {
        System.out.println("系统正在扫描今日日程并准备提醒...");
        // 这里可以调用 service 查询今日待办，并标记为“待提醒”或发送邮件
    }
}