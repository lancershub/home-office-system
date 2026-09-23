'use strict';
const ctx=Office.ctx;
$(function(){$.get(ctx+'/dept/list',function(depts){$.get(ctx+'/user/list',function(users){
    const body=$('#employeeTableBody').empty();
    users.forEach(user=>{
        const tr=$('<tr>').append($('<td>').text(user.id),$('<td>').text(user.visitor_name),$('<td>').text('-'));
        const select=$('<select class="form-select">').append(new Option('-- 尚未分配部门 --',''));
        depts.forEach(d=>select.append(new Option(d.dept_name,d.id,false,user.dept_id===d.id)));
        select.on('change',function(){const previous=user.dept_id; select.prop('disabled',true); $.post(ctx+'/user/assignDept',{userId:user.id,deptId:select.val()},r=>{
            if(r==='success')user.dept_id=select.val();else{select.val(previous||'');alert('分配失败');}
        }).fail(()=>select.val(previous||'')).always(()=>select.prop('disabled',false));});
        body.append(tr.append($('<td>').append(select)));
    });
});});});
