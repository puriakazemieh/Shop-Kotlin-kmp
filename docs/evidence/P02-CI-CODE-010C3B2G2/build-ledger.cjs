const fs=require('node:fs'),zlib=require('node:zlib'),crypto=require('node:crypto'),path=require('node:path');
const root=path.resolve(__dirname,'../../..');
const report=zlib.gunzipSync(fs.readFileSync(path.join(root,'docs/evidence/P02-CI-CODE-010C3A1/plugin-check-results.txt.gz')));
const hash=crypto.createHash('sha256').update(report).digest('hex');
if(hash!=='50f0c694447c4b9aab40b1d66c4091cecdad0614ba18c8f70208c3f2b36fd0bd')throw Error('Pinned report changed');
const wpcs=JSON.parse(fs.readFileSync(path.join(root,'tools/code-quality/wpcs-security-dispositions.json'))).securityDbFindings;
const targets={'class-cb-academy-controller.php':'P13-LMS-PERF-CODE-029','class-cb-blog-controller.php':'P04-WP-CONTENT-PERF-CODE-057','class-cb-admin-clinic-controller.php':'P14-CLINIC-PERF-CODE-034D'};
const risks={'class-cb-academy-controller.php':'Lesson-question commentmeta key/value filters and unbounded oldest-first list can scan and materialize large comment sets.',
 'class-cb-blog-controller.php':'Public featured-post meta key/value filters may scan postmeta; response size is capped but query selectivity needs measurement.',
 'class-cb-admin-clinic-controller.php':'Patient-message commentmeta key/value filters and unbounded date ordering can scan and materialize a growing conversation.'};
let file='';const findings=[];
for(const line of report.toString('utf8').split(/\r?\n/)){
 if(line.startsWith('FILE: ')){file=line.slice(6);continue}
 const base=path.basename(file);if(!line.startsWith('[')||!targets[base])continue;
 for(const item of JSON.parse(line)){
  if(!item.code.startsWith('WordPress.DB.'))continue;
  const target=targets[base],sourcePath=`wordpress/carmilla-bridge/${file}`;
  const matches=wpcs.filter(x=>x.path===sourcePath&&x.line===item.line&&x.source===item.code);
  if(matches.length!==1||matches[0].disposition!=='deferred'||matches[0].targetTask!==target)throw Error(`WPCS mismatch ${file}:${item.line}:${item.code}`);
  if(!fs.existsSync(path.join(root,'docs/tasks',target+'.md')))throw Error(`Missing target ${target}`);
  findings.push({path:sourcePath,shippedZipPath:file,line:item.line,column:item.column,code:item.code,message:item.message,severity:item.type,sqlOriginAndRisk:risks[base],
   disposition:'deferred_to_owner_phase',fixTaskId:target,verificationTaskId:'P02-CI-CODE-010C3B2G2',sourceTrace:`${sourcePath}:${item.line}`,testPath:null,
   wpcs:{column:matches[0].column,disposition:matches[0].disposition,targetTask:target,matchBasis:'path+line+code'}});
 }
}
if(findings.length!==6||new Set(findings.map(x=>`${x.shippedZipPath}:${x.line}:${x.column}:${x.code}`)).size!==6)throw Error('Coverage mismatch');
fs.writeFileSync(path.join(__dirname,'ledger.json'),JSON.stringify({schema:1,taskId:'P02-CI-CODE-010C3B2G2',reportSha256:hash,count:findings.length,findings},null,2)+'\n');
console.log('findings=6 deferredP04=2 deferredP13=2 deferredP14=2 wpcsMatched=6');
