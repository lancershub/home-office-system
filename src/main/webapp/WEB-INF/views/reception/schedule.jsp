<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>我的日程管理</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css">

    <style>
        body { background-color: #f8f9fa; padding: 15px; }
        #calendar {
            background: white;
            padding: 20px;
            border-radius: 12px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.08);
            min-height: 600px;
        }
    </style>
</head>
<body>

<div class="container-fluid">
    <div class="card border-0 shadow-sm mb-3">
        <div class="card-body d-flex justify-content-between align-items-center">
            <h4 class="mb-0 fw-bold text-primary"><i class="bi bi-calendar3 me-2"></i>个人日程工作台</h4>
            <button class="btn btn-sm btn-outline-secondary" onclick="location.reload()"><i class="bi bi-arrow-clockwise"></i> 刷新</button>
        </div>
    </div>
    <div id="calendar">
        <div class="text-center text-muted py-5">
            <div class="spinner-border text-primary mb-3" role="status"></div>
            <p>日历加载中，请稍候...</p>
        </div>
    </div>
</div>

<div class="modal fade" id="addScheduleModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow">
            <div class="modal-header bg-primary text-white">
                <h5 class="modal-title">新增日程安排</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body p-4">
                <form id="scheduleForm">
                    <div class="mb-3">
                        <label class="form-label fw-bold">选定日期</label>
                        <input type="text" class="form-control bg-light" id="schedule_date" name="schedule_date" readonly>
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-bold">日程标题</label>
                        <input type="text" class="form-control" id="title" name="title" required placeholder="请输入任务名称">
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-bold">备注内容</label>
                        <textarea class="form-control" id="content" name="content" rows="3"></textarea>
                    </div>
                </form>
            </div>
            <div class="modal-footer border-0">
                <button type="button" class="btn btn-light" data-bs-dismiss="modal">取消</button>
                <button type="button" class="btn btn-primary px-4" onclick="saveSchedule()">确认保存</button>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/jquery@3.6.0/dist/jquery.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<!-- FullCalendar v5 稳定版，兼容性好 -->
<link href="https://cdn.jsdelivr.net/npm/fullcalendar@5.11.5/main.min.css" rel="stylesheet">
<script src="https://cdn.jsdelivr.net/npm/fullcalendar@5.11.5/main.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/fullcalendar@5.11.5/locales/zh-cn.js"></script>

<script>
    var ctx = "${pageContext.request.contextPath}";
    var calendar;

    $(document).ready(function() {
        var calendarEl = document.getElementById('calendar');
        if (!calendarEl) return;

        // 检查 FullCalendar 是否加载成功
        if (typeof FullCalendar === 'undefined') {
            $('#calendar').html('<div class="text-center text-danger py-5"><p>日历组件加载失败，请检查网络连接后刷新页面</p></div>');
            return;
        }

        calendar = new FullCalendar.Calendar(calendarEl, {
            initialView: 'dayGridMonth',
            locale: 'zh-cn',
            buttonText: { today: '今天', month: '月', week: '周', list: '列表' },
            headerToolbar: {
                left: 'prev,next today',
                center: 'title',
                right: 'dayGridMonth,listWeek'
            },
            events: function(fetchInfo, successCallback, failureCallback) {
                $.ajax({
                    url: ctx + '/schedule/data',
                    type: 'GET',
                    dataType: 'json',
                    success: function(res) {
                        var events = res.map(function(item) {
                            return {
                                id: item.id,
                                title: item.title,
                                start: item.schedule_date,
                                color: item.status === 1 ? '#198754' : '#0d6efd'
                            };
                        });
                        successCallback(events);
                    },
                    error: function(xhr) {
                        console.error("数据加载失败:", xhr);
                        failureCallback();
                    }
                });
            },
            dateClick: function(info) {
                $("#scheduleForm")[0].reset();
                $("#schedule_date").val(info.dateStr);
                var modal = new bootstrap.Modal(document.getElementById('addScheduleModal'));
                modal.show();
            },
            eventClick: function(info) {
                if (confirm('确认删除该日程吗？')) {
                    $.ajax({
                        url: ctx + '/schedule/delete',
                        type: 'POST',
                        data: { id: info.event.id },
                        success: function(res) {
                            if (res === 'success') info.event.remove();
                        }
                    });
                }
            }
        });

        calendar.render();
    });

    function saveSchedule() {
        var title = $("#title").val();
        if (!title) return alert("请填写标题");

        $.ajax({
            url: ctx + '/schedule/add',
            type: 'POST',
            data: $("#scheduleForm").serialize(),
            success: function(res) {
                if (res === 'success') {
                    var modal = bootstrap.Modal.getInstance(document.getElementById('addScheduleModal'));
                    if (modal) modal.hide();
                    calendar.refetchEvents();
                } else {
                    alert("保存失败，请检查登录状态");
                }
            },
            error: function() { alert("服务器连接失败"); }
        });
    }
</script>
</body>
</html>