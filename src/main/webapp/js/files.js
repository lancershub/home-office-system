'use strict';
const ctx=Office.ctx;
let isTrashMode=false;
$(function(){
    loadFiles();
    $('#uploadForm').on('submit',function(event){
        event.preventDefault();$('#uploadBtn').prop('disabled',true);
        $.ajax({url:ctx+'/file/upload',type:'POST',data:new FormData(this),processData:false,contentType:false})
            .done(()=>{this.reset();$('#fileNameDisplay').text('支持办公文档、图片和 ZIP，最大 20MB');loadFiles();})
            .always(()=>$('#uploadBtn').prop('disabled',false));
    });
});
function onFileSelected(input){
    const f=input.files[0]; if(!f)return;
    $('#fileNameDisplay').text(f.name+' ('+(f.size/1024/1024).toFixed(2)+' MB)');
    $('#uploadBtn').prop('disabled',f.size===0||f.size>20*1024*1024);
}
function loadFiles(){
    $.get(ctx+(isTrashMode?'/file/trash':'/file/list'),function(files){
        const body=$('#fileTableBody').empty();
        files.forEach(f=>{
            const tr=$('<tr>');
            [f.original_name,(f.file_size/1024).toFixed(1)+' KB',f.storage_status==='PENDING_DELETE'?'删除处理中':f.file_type,new Date(f.upload_time).toLocaleString()].forEach(v=>tr.append($('<td>').text(v)));
            const td=$('<td>');
            if(isTrashMode){
                if(f.storage_status==='ACTIVE')$('<button class="btn btn-outline-success me-1">').text('恢复').on('click',()=>mutate('/restore/',f.id)).appendTo(td);
                $('<button class="btn btn-outline-danger">').text('彻底删除 / 重试').on('click',()=>{if(confirm('彻底删除 '+f.original_name+'？'))mutate('/permanentDelete/',f.id);}).appendTo(td);
            }else{
                $('<a class="btn btn-outline-primary me-1">').text('下载').attr('href',ctx+'/file/download/'+encodeURIComponent(f.id)).appendTo(td);
                $('<button class="btn btn-outline-danger">').text('删除').on('click',()=>{if(confirm('移入回收站：'+f.original_name+'？'))mutate('/delete/',f.id);}).appendTo(td);
            }
            body.append(tr.append(td));
        });
        if(!files.length)body.append($('<tr>').append($('<td colspan="5">').text('暂无文件')));
    });
}
function mutate(action,id){$.post(ctx+'/file'+action+id,r=>{if(r==='success')loadFiles();else alert('文件状态已变化，请刷新');});}
function toggleTrash(){isTrashMode=!isTrashMode;$('#trashBtnText').text(isTrashMode?'返回文件列表':'查看回收站');$('#tableTitle').text(isTrashMode?'回收站':'我的文件');loadFiles();}
