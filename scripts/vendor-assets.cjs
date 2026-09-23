// Copy only runtime assets and licenses from a pinned npm install.
const fs=require('fs'),path=require('path');
const source=process.argv[2];if(!source)throw Error('Usage: node scripts/vendor-assets.cjs <node_modules directory>');
const root=path.resolve(__dirname,'../src/main/webapp');
const assets={
 'jquery/dist/jquery.min.js':'jquery/jquery.min.js','jquery/LICENSE.txt':'jquery/LICENSE.txt',
 'bootstrap/dist/css/bootstrap.min.css':'bootstrap/css/bootstrap.min.css','bootstrap/dist/js/bootstrap.bundle.min.js':'bootstrap/js/bootstrap.bundle.min.js','bootstrap/LICENSE':'bootstrap/LICENSE',
 'bootstrap-icons/font/bootstrap-icons.css':'bootstrap-icons/font/bootstrap-icons.css','bootstrap-icons/font/fonts/bootstrap-icons.woff':'bootstrap-icons/font/fonts/bootstrap-icons.woff','bootstrap-icons/font/fonts/bootstrap-icons.woff2':'bootstrap-icons/font/fonts/bootstrap-icons.woff2','bootstrap-icons/LICENSE':'bootstrap-icons/LICENSE',
 'fullcalendar/main.min.js':'fullcalendar/main.min.js','fullcalendar/main.min.css':'fullcalendar/main.min.css','fullcalendar/locales/zh-cn.js':'fullcalendar/locales/zh-cn.js','fullcalendar/LICENSE.txt':'fullcalendar/LICENSE.txt'
};
for(const [src,dest]of Object.entries(assets)){const out=path.join(root,'lib',dest);fs.mkdirSync(path.dirname(out),{recursive:true});fs.copyFileSync(path.join(source,src),out);}
for(const name of fs.readdirSync(path.join(root,'WEB-INF/views/reception')).filter(n=>n.endsWith('.jsp'))){
 const file=path.join(root,'WEB-INF/views/reception',name);
 let content=fs.readFileSync(file,'utf8')
 .replace(/https:\/\/cdn\.jsdelivr\.net\/npm\/jquery@[^/]+\/dist\/jquery\.min\.js/g,'\${pageContext.request.contextPath}/lib/jquery/jquery.min.js')
 .replace(/https:\/\/cdn\.jsdelivr\.net\/npm\/bootstrap@[^/]+\/dist\//g,'\${pageContext.request.contextPath}/lib/bootstrap/')
 .replace(/https:\/\/cdn\.jsdelivr\.net\/npm\/bootstrap-icons@[^/]+\//g,'\${pageContext.request.contextPath}/lib/bootstrap-icons/')
 .replace(/https:\/\/cdn\.jsdelivr\.net\/npm\/fullcalendar@[^/]+\//g,'\${pageContext.request.contextPath}/lib/fullcalendar/');
 fs.writeFileSync(file,content);
}
console.log('Vendored '+Object.keys(assets).length+' assets and licenses; pages use same-origin URLs.');
