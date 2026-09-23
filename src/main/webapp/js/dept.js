'use strict';
const ctx=Office.ctx, deptModal=new bootstrap.Modal(document.getElementById('deptModal'));
let instData=[];
$(function(){ $.get(ctx+'/inst/list',function(data){instData=data;buildInstOptions();loadDeptData();}); });
function buildInstOptions(selectedId){
    const select=$('#inst_id').empty().append(new Option('-- 暂不归属任何机构 --',''));
    instData.forEach(i=>select.append(new Option(i.inst_name,i.id,false,i.id===selectedId)));
}
function loadDeptData(){
    $.get(ctx+'/dept/list',function(data){
        const body=$('#deptTableBody').empty();
        data.forEach(d=>{
            const tr=$('<tr>');
            [d.id,d.dept_name,(instData.find(i=>i.id===d.inst_id)||{}).inst_name||'未归属',d.dept_desc||'-',d.create_time?new Date(d.create_time).toLocaleString():'-'].forEach(v=>tr.append($('<td>').text(v)));
            const actions=$('<td>');
            $('<button class="btn btn-outline-primary me-1">').text('编辑').on('click',()=>showEditModal(d)).appendTo(actions);
            $('<button class="btn btn-outline-danger">').text('删除').on('click',()=>deleteDept(d.id,d.dept_name)).appendTo(actions);
            body.append(tr.append(actions));
        });
    });
}
function showAddModal(){$('#deptForm')[0].reset();$('#dept_id').val('');buildInstOptions();$('#modalTitle').text('新增部门');deptModal.show();}
function showEditModal(d){$('#dept_id').val(d.id);$('#dept_name').val(d.dept_name);$('#dept_desc').val(d.dept_desc);$('#manager_id').val(d.manager_id);buildInstOptions(d.inst_id);$('#modalTitle').text('编辑部门');deptModal.show();}
function saveDept(){$.post(ctx+'/dept/save',$('#deptForm').serialize(),r=>{if(r==='success'){deptModal.hide();loadDeptData();}else alert('保存失败');});}
function deleteDept(id,name){if(confirm('删除部门：'+name+'？'))$.post(ctx+'/dept/delete',{id:id},r=>{if(r==='success')loadDeptData();else alert('删除失败');});}
