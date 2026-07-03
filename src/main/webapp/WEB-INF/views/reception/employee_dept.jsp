<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>员工部门分配</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css">
    <style>
        body { background-color: #f8f9fa; padding: 20px; }
        .card { border-radius: 12px; border: none; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
        .dept-select { border-radius: 20px; font-size: 14px; border-color: #dee2e6; }
        .dept-select:focus { box-shadow: none; border-color: #0d6efd; }
    </style>
</head>
<body>

<div class="container-fluid">
    <div class="card">
        <div class="card-header bg-white py-3">
            <h5 class="mb-0 fw-bold text-success"><i class="bi bi-people-fill me-2"></i>人员组织调配</h5>
            <small class="text-muted">在此页面为注册员工分配所属部门，更改后立即生效。</small>
        </div>
        <div class="card-body p-0">
            <table class="table table-hover align-middle mb-0 text-center">
                <thead class="table-light">
                    <tr>
                        <th>员工编号</th>
                        <th>员工姓名</th>
                        <th>注册时间</th>
                        <th width="30%">所属部门 (直接选择)</th>
                    </tr>
                </thead>
                <tbody id="employeeTableBody">
                    </tbody>
            </table>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/jquery@3.6.0/dist/jquery.min.js"></script>
<script>
    const ctx = "${pageContext.request.contextPath}";
    let globalDepts = []; // 存一下所有的部门数据，用来生成下拉菜单

    $(document).ready(function() {
        // 先获取所有部门，再获取所有用户
        loadDepartments(function() {
            loadEmployees();
        });
    });

    // 1. 获取部门列表
        function loadDepartments(callback) {
            $.get(ctx + '/dept/list', function(res) {
                // 【修改点】：如果后端传过来的是纯文本，强行转成 JSON 数组
                globalDepts = typeof res === 'string' ? JSON.parse(res) : res;
                callback();
            });
        }

        // 2. 获取所有员工列表
        function loadEmployees() {
            $.get(ctx + '/user/list', function(res) {
                // 【修改点】：同样把员工数据也强行转成 JSON 数组
                let userData = typeof res === 'string' ? JSON.parse(res) : res;

                let html = '';
                // 【修改点】：这里把 res 改成 userData
                userData.forEach(function(user) {
                    // 动态生成下拉菜单
                    let optionsHtml = `<option value="">-- 尚未分配部门 --</option>`;
                    globalDepts.forEach(function(dept) {
                        // 如果用户的 dept_id 和当前部门 id 匹配，就默认选中
                        let selected = (user.dept_id === dept.id) ? 'selected' : '';
                        optionsHtml += `<option value="\${dept.id}" \${selected}>\${dept.dept_name}</option>`;
                    });

                    html += `
                        <tr>
                            <td><span class="badge bg-secondary"># \${user.id}</span></td>
                            <td class="fw-bold">\${user.visitor_name}</td>
                            <td class="text-muted">2026-04-14 (示例)</td>
                            <td>
                                <select class="form-select dept-select w-75 mx-auto"
                                        onchange="changeDept(\${user.id}, this.value)">
                                    \${optionsHtml}
                                </select>
                            </td>
                        </tr>
                    `;
                });
                $("#employeeTableBody").html(html);
            });
        }

    // 3. 丝滑的无刷新保存
    function changeDept(userId, newDeptId) {
        // 添加一点加载提示，让体验更好
        let selectEl = $(event.target);
        selectEl.prop('disabled', true);

        $.ajax({
            url: ctx + '/user/assignDept',
            type: 'POST',
            data: { userId: userId, deptId: newDeptId },
            success: function(res) {
                selectEl.prop('disabled', false);
                if(res === 'success') {
                    // 屏幕右上角可以使用 toastr 弹出一个绿色小勾，这里用 alert 简化
                    console.log("分配成功！");
                    // 变成绿色边框提示用户保存成功
                    selectEl.css('border-color', '#198754');
                    setTimeout(() => selectEl.css('border-color', '#dee2e6'), 1500);
                } else {
                    alert("分配失败，请重试！");
                }
            }
        });
    }
</script>
</body>
</html>