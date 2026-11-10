const fs=require('node:fs'),zlib=require('node:zlib'),crypto=require('node:crypto'),path=require('node:path');
const root=path.resolve(__dirname,'../../..');
const report=zlib.gunzipSync(fs.readFileSync(path.join(root,'docs/evidence/P02-CI-CODE-010C3A1/plugin-check-results.txt.gz')));
const hash=crypto.createHash('sha256').update(report).digest('hex');
if(hash!=='50f0c694447c4b9aab40b1d66c4091cecdad0614ba18c8f70208c3f2b36fd0bd')throw Error('Pinned report changed');
const sourceCache=new Map();let file='';const findings=[];
for(const line of report.toString('utf8').split(/\r?\n/)){
 if(line.startsWith('FILE: ')){file=line.slice(6);continue}
 if(file==='includes/helpers.php'||!line.startsWith('['))continue;
 for(const item of JSON.parse(line)){
  if(!item.code.startsWith('WordPress.NamingConventions.'))continue;
  if(!sourceCache.has(file))sourceCache.set(file,fs.readFileSync(path.join(root,'wordpress/carmilla-bridge',file),'utf8').split(/\r?\n/));
  const sourceLine=sourceCache.get(file)[item.line-1]||'';
  const symbol=(/&quot;([^&]+)&quot;/.exec(item.message)||[])[1];
  if(!symbol||!sourceLine.includes(symbol.replace(/^\$/,'')))throw Error(`Source mismatch ${file}:${item.line}`);
  const wpCoreHook=symbol==='the_content';
  const risk=wpCoreHook?'This is the documented WordPress content filter applied to post content; renaming it would skip core and plugin formatting hooks.':
   file==='tools/user-integrity.php'?'The operator-only CLI script rejects non-CLI requests; its short/global identifier can be renamed only with CLI and migration call-site compatibility checks.':
   file==='carmilla-bridge.php'?'The plugin bootstrap variable is file-scope and only chooses the bundled shared core; collision/name isolation still warrants cleanup.':
   'A short cb_ global class or public filter may collide with another plugin; migration must preserve existing Bridge/theme callers and external filter subscribers.';
  findings.push({path:`wordpress/carmilla-bridge/${file}`,shippedZipPath:file,line:item.line,column:item.column,code:item.code,message:item.message,severity:item.type,symbol,
   sourceTrace:`wordpress/carmilla-bridge/${file}:${item.line}`,sourceAndRisk:risk,disposition:wpCoreHook?'context_justified_wordpress_core_filter':'deferred_to_p04_prefix_migration',
   fixTaskId:wpCoreHook?null:'P04-WP-PREFIX-CODE-058',verificationTaskId:'P02-CI-CODE-010C3B3A2'});
 }
}
if(findings.length!==15||findings.filter(x=>x.fixTaskId).length!==14||new Set(findings.map(x=>`${x.shippedZipPath}:${x.line}:${x.column}:${x.code}`)).size!==15)throw Error('Coverage mismatch');
fs.writeFileSync(path.join(__dirname,'ledger.json'),JSON.stringify({schema:1,taskId:'P02-CI-CODE-010C3B3A2',reportSha256:hash,count:findings.length,findings},null,2)+'\n');
console.log('findings=15 deferredPrefix=14 coreFilter=1');
