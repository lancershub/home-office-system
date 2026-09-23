'use strict';
const ctx=Office.ctx,instModal=new bootstrap.Modal(document.getElementById('instModal'));
$(loadInstData);
function loadInstData(){$.get(ctx+'/inst/list',function(data){
    const body=$('#instTableBody').empty();
    data.forEach(item=>{
        const tr=$('<tr>');
        [item.id,item.inst_name,item.inst_desc||'-',item.create_time?new Date(item.create_time).toLocaleString():'-'].forEach(v=>tr.append($('<td>').text(v)));
        const td=$('<td>');
        $('<button class="btn btn-outline-primary me-1">').text('编辑').on('click',()=>showEditModal(item)).appendTo(td);
        $('<button class="btn btn-outline-danger">').text('删除').on('click',()=>deleteInst(item.id)).appendTo(td);
        body.append(tr.append(td));
    });
});}
function showAddModal(){$('#instForm')[0].reset();$('#inst_id').val('');$('#modalTitle').text('新增机构');instModal.show();}
function showEditModal(item){$('#inst_id').val(item.id);$('#inst_name').val(item.inst_name);$('#inst_desc').val(item.inst_desc);$('#modalTitle').text('编辑机构');instModal.show();}
function saveInst(){$.post(ctx+'/inst/save',$('#instForm').serialize(),r=>{if(r==='success'){instModal.hide();loadInstData();}else alert('保存失败');});}
function deleteInst(id){if(confirm('确定删除该机构？'))$.post(ctx+'/inst/delete',{id:id},r=>{if(r==='success')loadInstData();else alert('删除失败');});}
