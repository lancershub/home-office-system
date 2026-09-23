'use strict';
let ws=null,reconnects=0,stopped=false;
const pending=new Map(),confirmed=new Map();
const ctx=Office.ctx,myId=document.querySelector('meta[name="chat-user"]').content;
let conversation='',generation=0;
$(function(){
    $('<div id="pendingMessages" class="small">').insertAfter('#chatBox');
    $('<button class="btn btn-sm btn-outline-secondary mb-2">').text('加载更早消息').on('click',()=>loadChatHistory(true)).insertBefore('#chatBox');
    $('#targetUserId').on('change',()=>{conversation=$('#targetUserId').val().trim();generation++;confirmed.clear();render();loadChatHistory(false);});
    conversation=$('#targetUserId').val().trim();
    connectWebSocket();
    window.addEventListener('beforeunload',()=>{stopped=true;if(ws)ws.close();});
});
function connectWebSocket(){
    ws=new WebSocket((location.protocol==='https:'?'wss://':'ws://')+location.host+ctx+'/native/chat/'+encodeURIComponent(myId));
    ws.onopen=()=>{reconnects=0;$('#statusLabel').text('已连接').removeClass('bg-danger').addClass('bg-success');loadChatHistory(false);};
    ws.onmessage=event=>{
        let m;try{m=JSON.parse(event.data);}catch(e){return;}
        if(m.type==='ack'){
            pending.delete(m.clientId);
            if(m.to===conversation){confirmed.set(Number(m.id),{id:Number(m.id),senderId:myId,content:m.content});render();}
            renderPending();
        }else if(m.type==='message'&&m.from===conversation){
            confirmed.set(Number(m.id),{id:Number(m.id),senderId:m.from,content:m.content});render();
        }else if(m.type==='error'){
            const item=pending.get(m.clientId);
            if(item){item.status=m.message||'发送失败';renderPending();}else alert(m.message||'发送失败');
        }
    };
    ws.onclose=event=>{
        $('#statusLabel').text('连接断开').removeClass('bg-success').addClass('bg-danger');
        for(const item of pending.values())item.status='未确认，请重试';
        renderPending();
        if(!stopped&&event.code!==1008&&reconnects++<5)setTimeout(connectWebSocket,2000);
    };
    ws.onerror=()=>$('#statusLabel').text('连接失败');
}
function sendMessage(){
    const to=$('#targetUserId').val().trim(),content=$('#msgInput').val();
    if(!to||!content.trim()||content.length>4000)return alert('填写接收人和消息，最多 4000 字符');
    if(pending.size>=50)return alert('请先处理未确认消息');
    if(to!==conversation){conversation=to;generation++;confirmed.clear();loadChatHistory(false);}
    const bytes=new Uint8Array(16);crypto.getRandomValues(bytes);
    const clientId=Array.from(bytes,b=>b.toString(16).padStart(2,'0')).join('');
    const item={to,content,clientId,status:'发送中'};
    pending.set(clientId,item);$('#msgInput').val('');transmit(item);
}
function transmit(item){
    if(!ws||ws.readyState!==WebSocket.OPEN){item.status='未连接，请连接后重试';renderPending();return;}
    item.status='等待保存确认';
    try{ws.send(JSON.stringify({to:item.to,content:item.content,clientId:item.clientId}));}
    catch(e){item.status='发送失败，请重试';}
    renderPending();
    setTimeout(()=>{if(pending.has(item.clientId)){item.status='未确认，可重试（不会重复入库）';renderPending();}},10000);
}
function render(){
    const box=$('#chatBox').empty();
    Array.from(confirmed.values()).sort((a,b)=>a.id-b.id).forEach(m=>{
        const mine=m.senderId===myId;
        const row=$('<div>').addClass('msg-row '+(mine?'msg-right':'msg-left'));
        const bubble=$('<div>').addClass('msg-bubble '+(mine?'bubble-right':'bubble-left')).css('white-space','pre-wrap').text(m.content);
        box.append(row.append(bubble));
    });
    renderPending();
}
function renderPending(){
    const area=$('#pendingMessages').empty();
    for(const item of pending.values()){
        const row=$('<div class="mb-2">').text('发给 '+item.to+'：'+item.content+' — '+item.status+' ');
        $('<button class="btn btn-sm btn-outline-warning">').text('重试').on('click',()=>transmit(item)).appendTo(row);
        area.append(row);
    }
}
function loadChatHistory(older){
    const to=conversation,version=generation;
    if(!to)return;
    const data={withUserId:to};
    if(older&&confirmed.size)data.beforeId=Math.min(...confirmed.keys());
    $.get(ctx+'/user/chatHistory',data,function(rows){
        if(version!==generation||to!==conversation)return;
        rows.forEach(m=>confirmed.set(m.id,m));render();
    });
}
