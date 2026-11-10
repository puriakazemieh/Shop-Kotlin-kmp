const fs=require('node:fs'),zlib=require('node:zlib'),crypto=require('node:crypto'),path=require('node:path');
const root=path.resolve(__dirname,'../../..');
const report=zlib.gunzipSync(fs.readFileSync(path.join(root,'docs/evidence/P02-CI-CODE-010C3A1/plugin-check-results.txt.gz')));
const hash=crypto.createHash('sha256').update(report).digest('hex');
if(hash!=='50f0c694447c4b9aab40b1d66c4091cecdad0614ba18c8f70208c3f2b36fd0bd')throw Error('Pinned report changed');
const source=fs.readFileSync(path.join(root,'wordpress/carmilla-bridge/includes/helpers.php'),'utf8').split(/\r?\n/);
let file='';const findings=[];
for(const line of report.toString('utf8').split(/\r?\n/)){
 if(line.startsWith('FILE: ')){file=line.slice(6);continue}
 if(file!=='includes/helpers.php'||!line.startsWith('['))continue;
 for(const item of JSON.parse(line)){
  if(!item.code.startsWith('WordPress.NamingConventions.'))continue;
  const src=source[item.line-1]||'';
  const symbol=item.code.includes('Hookname')?'cb_zarinpal_amount':(/function\s+(cb_[a-z_0-9]+)/.exec(src)||[])[1];
  if(!symbol||!src.includes(symbol))throw Error(`Source mismatch ${item.line}`);
  findings.push({path:'wordpress/carmilla-bridge/includes/helpers.php',shippedZipPath:file,line:item.line,column:item.column,code:item.code,message:item.message,severity:item.type,
   symbol,sourceTrace:`wordpress/carmilla-bridge/includes/helpers.php:${item.line}`,sourceAndRisk:`${symbol} is an existing global Bridge ${item.code.includes('Hookname')?'filter':'function'} using the short cb_ prefix; this public identifier can collide and a rename must preserve theme/plugin callers and hook subscribers.`,
   disposition:'deferred_to_p04_prefix_migration',fixTaskId:'P04-WP-PREFIX-CODE-058',verificationTaskId:'P02-CI-CODE-010C3B3A1'});
 }
}
if(findings.length!==40||findings.filter(x=>x.code.includes('Hookname')).length!==1||new Set(findings.map(x=>`${x.line}:${x.column}:${x.code}`)).size!==40)throw Error('Coverage mismatch');
fs.writeFileSync(path.join(__dirname,'ledger.json'),JSON.stringify({schema:1,taskId:'P02-CI-CODE-010C3B3A1',reportSha256:hash,count:findings.length,findings},null,2)+'\n');
console.log('findings=40 functions=39 hooks=1 owner=P04-WP-PREFIX-CODE-058');
