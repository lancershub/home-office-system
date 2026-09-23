// Run only against an isolated fixture. See docs/SECURITY_FIXES.md.
const {chromium}=require('playwright-core'),WebSocket=require('ws'),assert=require('node:assert/strict');
const base=process.env.OFFICE_SMOKE_URL||'http://127.0.0.1:18089/office';
if(base!=='http://127.0.0.1:18089/office')throw Error('This test is restricted to the isolated local fixture');
const adminPassword=process.env.OFFICE_SMOKE_ADMIN_PASSWORD;
if(!adminPassword)throw Error('Set OFFICE_SMOKE_ADMIN_PASSWORD for the isolated database');
const employeePassword='Browser-fixture-password-2026';
const attack='"><img src=x onerror="window.__xss=1">';
let browser;
const sockets=[];
async function csrf(context){
 const r=await context.request.get(base+'/hello');assert.equal(r.status(),200);
 const html=await r.text();const token=html.match(/name="csrf-token" content="([^"]+)"/);
 assert(token,'CSRF meta token missing');return token[1];
}
async function post(context,path,data){
 return context.request.post(base+path,{form:data,headers:{'X-CSRF-TOKEN':await csrf(context)},maxRedirects:0});
}
async function login(context,name,password){
 const r=await post(context,'/login',{username:name,password});assert.equal(r.status(),302);
 assert(!r.headers().location.includes('error'),'Login failed');
}
async function connect(context,id,origin='http://127.0.0.1:18089'){
 const cookies=await context.cookies(base);
 const ws=new WebSocket(base.replace('http:','ws:')+'/native/chat/'+id,{headers:{Origin:origin,Cookie:cookies.map(c=>c.name+'='+c.value).join('; ')}});
 return new Promise((resolve,reject)=>{
  ws.once('open',()=>{sockets.push(ws);resolve(ws);});
  ws.once('error',reject);
  ws.once('unexpected-response',(req,res)=>{res.resume();ws.terminate();reject(Error('HTTP '+res.statusCode));});
 });
}
function receive(ws){return new Promise((resolve,reject)=>{
 const timer=setTimeout(()=>reject(Error('WebSocket message timeout')),5000);
 ws.once('message',raw=>{clearTimeout(timer);resolve(JSON.parse(raw));});
});}
(async()=>{
 browser=await chromium.launch({headless:true,executablePath:process.env.CHROME_PATH||'C:/Program Files/Google/Chrome/Application/chrome.exe'});
 const admin=await browser.newContext(),employee=await browser.newContext(),anonymous=await browser.newContext();
 const reg=await post(employee,'/register',{visitorName:attack,phone:'123456789',purpose:attack,password:employeePassword});
 assert.equal(reg.status(),302);const userId=new URL(reg.headers().location,base).searchParams.get('newId');assert(userId);
 await login(admin,'admin',adminPassword);await login(employee,userId,employeePassword);
 for(const endpoint of ['/list','/attendanceList','/exportAttendance']){
  assert.equal((await anonymous.request.get(base+endpoint)).status(),401);
  assert.equal((await employee.request.get(base+endpoint)).status(),403);
 }
 assert.equal((await admin.request.get(base+'/deleteVisitor?id='+userId)).status(),405);
 assert.equal((await admin.request.post(base+'/deleteVisitor',{form:{id:userId}})).status(),403);
 assert.equal((await post(employee,'/clockOut',{})).status(),400);
 const punches=await Promise.all(Array.from({length:8},()=>post(employee,'/clockIn',{})));
 punches.forEach(r=>assert.equal(r.status(),302));
 assert.equal((await post(employee,'/clockOut',{})).status(),302);
 assert.equal((await post(employee,'/clockOut',{})).status(),302);
 assert.equal((await post(admin,'/inst/save',{inst_name:attack,inst_desc:attack})).status(),200);
 const insts=await (await admin.request.get(base+'/inst/list')).json();
 assert.equal((await post(admin,'/dept/save',{dept_name:attack,dept_desc:attack,inst_id:insts[0].id})).status(),200);
 const depts=await(await admin.request.get(base+'/dept/list')).json();
 assert.equal((await post(admin,'/user/assignDept',{userId,deptId:depts[0].id})).status(),200);
 assert.equal((await post(employee,'/schedule/add',{title:attack,content:'test',schedule_date:'2026-09-13'})).status(),200);
 const uploaded=await employee.request.post(base+'/file/upload',{headers:{'X-CSRF-TOKEN':await csrf(employee)},multipart:{file:{name:"quote'<>.txt",mimeType:'text/plain',buffer:Buffer.from('test file')}}});
 assert.equal(uploaded.status(),200);
 const list=await(await employee.request.get(base+'/file/list')).json();assert.equal(list.length,1);const file=list[0];
 const other=await browser.newContext();
 const another=await post(other,'/register',{visitorName:'Other',phone:'123',purpose:'',password:employeePassword});
 const otherId=new URL(another.headers().location,base).searchParams.get('newId');await login(other,otherId,employeePassword);
 assert.equal((await other.request.get(base+'/file/download/'+file.id)).status(),404);
 assert.equal(await(await post(other,'/file/delete/'+file.id,{})).text(),'fail');
 assert.equal((await employee.request.get(base+'/file/download/'+file.id)).status(),200);
 const errors=[];
 for(const [context,paths] of [[admin,['/main','/list','/attendanceList','/inst/view','/dept/view','/user/assignPage','/user/chatPage']],[employee,['/visitorIndex','/schedule/view','/file/view','/user/chatPage']]]){
  const page=await context.newPage();page.on('pageerror',e=>errors.push(e.message));
  for(const path of paths){
   const response=await page.goto(base+path,{waitUntil:'networkidle'});
   assert.equal(response.status(),200,path);
   console.log('Rendered',path,'errors:',errors);
   assert.deepEqual(errors,[]);
   assert.equal(await page.evaluate(()=>window.__xss),undefined,'XSS executed on '+path);
   if(path==='/list'){
    await page.locator('.edit-visitor[data-id="'+userId+'"]').click();
    assert.equal(await page.locator('#edit-name').inputValue(),attack);
   }
   if(path==='/file/view'){
    await page.locator('#fileInput').setInputFiles({name:'via-browser.txt',mimeType:'text/plain',buffer:Buffer.from('Browser upload')});
    const result=await Promise.all([page.waitForResponse(r=>r.url().endsWith('/file/upload')&&r.request().method()==='POST'),page.locator('#uploadBtn').click()]);
    assert.equal(result[0].status(),200);
   }
  }
  await page.close();
 }
 assert.deepEqual(errors,[]);
 await assert.rejects(connect(anonymous,'admin'));
 await assert.rejects(connect(employee,'admin'));
 await assert.rejects(connect(employee,userId,'https://untrusted.example'));
 const from=await connect(employee,userId),to=await connect(admin,'admin');
 const outgoing={to:'admin',content:'quote "\n'+attack,clientId:crypto.randomUUID()};
 const delivery=receive(to),ack=receive(from);from.send(JSON.stringify(outgoing));
 const [m,a]=await Promise.all([delivery,ack]);assert.equal(m.content,outgoing.content);assert.equal(a.type,'ack');
 const retried=receive(from);from.send(JSON.stringify(outgoing));assert.equal((await retried).id,a.id);
 const chatPage=await admin.newPage();await chatPage.goto(base+'/user/chatPage',{waitUntil:'networkidle'});
 await chatPage.locator('#targetUserId').fill(userId);await chatPage.locator('#targetUserId').dispatchEvent('change');
 await chatPage.waitForFunction(()=>document.querySelector('#chatBox').textContent.includes('quote'));
 assert.equal(await chatPage.evaluate(()=>window.__xss),undefined);
 assert.equal(await(await post(employee,'/file/delete/'+file.id,{})).text(),'success');
 assert.equal((await employee.request.get(base+'/file/download/'+file.id)).status(),404);
 assert.equal(await(await post(employee,'/file/restore/'+file.id,{})).text(),'success');
 assert.equal(await(await post(employee,'/file/delete/'+file.id,{})).text(),'success');
 assert.equal(await(await post(employee,'/file/permanentDelete/'+file.id,{})).text(),'success');
 const closed=new Promise((resolve,reject)=>{const t=setTimeout(()=>reject(Error('Logout did not close WebSocket')),5000);from.once('close',()=>{clearTimeout(t);resolve();});});
 assert.equal((await post(employee,'/logout.action',{})).status(),302);await closed;
 console.log('PASS: 11 JSP routes, escaped stored input, edit form, browser multipart CSRF, HTTP role checks, concurrent attendance, file ownership/lifecycle, WebSocket identity/origin/ACK/dedup/logout');
})().catch(e=>{console.error(e);process.exitCode=1;}).finally(async()=>{sockets.forEach(s=>s.terminate());if(browser)await browser.close();});
