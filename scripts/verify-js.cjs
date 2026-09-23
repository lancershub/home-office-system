const fs=require('fs'),path=require('path'),vm=require('vm');
const root=path.resolve(__dirname,'../src/main/webapp');
let count=0;
for(const name of fs.readdirSync(path.join(root,'js'))){
 if(name.endsWith('.js')){new vm.Script(fs.readFileSync(path.join(root,'js',name),'utf8'),{filename:name});count++;}
}
for(const name of fs.readdirSync(path.join(root,'WEB-INF/views/reception'))){
 if(!name.endsWith('.jsp'))continue;
 const text=fs.readFileSync(path.join(root,'WEB-INF/views/reception',name),'utf8');
 for(const match of text.matchAll(/<script>([\s\S]*?)<\/script>/g)){
  const source=match[1].replace(/<c:out[^>]*\/>/g,'').replace(/(?<!\\)\$\{[^}]*\}/g,'');
  new vm.Script(source,{filename:name});count++;
 }
}
console.log(count+' JavaScript sources parsed successfully');
