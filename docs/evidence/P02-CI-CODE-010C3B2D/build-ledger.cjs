const fs=require('node:fs'),zlib=require('node:zlib'),crypto=require('node:crypto'),path=require('node:path');
const root=path.resolve(__dirname,'../../..');
const report=zlib.gunzipSync(fs.readFileSync(path.join(root,'docs/evidence/P02-CI-CODE-010C3A1/plugin-check-results.txt.gz')));
const hash=crypto.createHash('sha256').update(report).digest('hex');
if(hash!=='50f0c694447c4b9aab40b1d66c4091cecdad0614ba18c8f70208c3f2b36fd0bd')throw Error('Pinned report changed');
const wpcs=JSON.parse(fs.readFileSync(path.join(root,'tools/code-quality/wpcs-security-dispositions.json'))).securityDbFindings;
const details={
 'Export_Command.php:34':'One-shot CLI export scans all allowed posts from trusted WP table; resource bound and query shape require P07 review.',
 'Export_Command.php:35':'Denied types are fixed and esc_sql-escaped, but placeholders are pending P07 security work.',
 'Export_Command.php:53':'One-shot CLI attachment scan uses internal WP posts table; resource bound requires P07 review.',
 'Export_Command.php:102':'Per-post prepared postmeta read can become N+1; P07 performance review owns bounds.',
 'Export_Overlay_Command.php:35':'Overlay scan uses internal posts table and fixed-prefix seed table; fixed denied types, placeholder and resource work belong to P07.',
 'Export_Overlay_Command.php:62':'Per-post prepared postmeta read can become N+1; P07 performance review owns bounds.',
 'class-cb-identity-synthetic-migration.php:42':'Explicit bounded synthetic migration uses wpdb->insert with fixed meta key and explicit formats inside transaction.',
 'class-cb-identity-synthetic-migration.php:43':'Explicit bounded synthetic migration uses wpdb->insert with fixed meta key and explicit formats inside transaction.'};
let file='';const findings=[];
for(const line of report.toString('utf8').split(/\r?\n/)){
 if(line.startsWith('FILE: ')){file=line.slice(6);continue}
 if(!line.startsWith('[')||!(/includes\/Migration\/Export/.test(file)||file==='includes/class-cb-identity-synthetic-migration.php'))continue;
 for(const item of JSON.parse(line)){
  if(!item.code.startsWith('WordPress.DB.'))continue;
  const detail=details[`${path.basename(file)}:${item.line}`];if(!detail)throw Error(`Unreviewed ${file}:${item.line}`);
  const sourcePath=`wordpress/carmilla-bridge/${file}`;
  const matches=wpcs.filter(x=>x.path===sourcePath&&x.line===item.line&&x.source===item.code);
  if(matches.length!==1)throw Error(`WPCS mismatch ${file}:${item.line}:${item.code}`);
  const target=file.includes('/Migration/')?(item.code.startsWith('WordPress.DB.PreparedSQL.')?'P07-MIGRATION-SEC-CODE-028':'P07-MIGRATION-PERF-REVIEW-029'):null;
  if(matches[0].disposition!==(target?'deferred':'context_justified')||(matches[0].targetTask||null)!==target)throw Error('Disposition mismatch');
  findings.push({path:sourcePath,line:item.line,column:item.column,code:item.code,message:item.message,severity:item.type,
   sqlOriginAndRisk:detail,disposition:target?'deferred_to_migration_phase':'context_justified',fixTaskId:target,
   verificationTaskId:'P02-CI-CODE-010C3B2D',sourceTrace:sourcePath+':'+item.line,
   testPath:file.includes('/Migration/')?'wordpress/carmilla-bridge/tests/smoke-migration-export-no-plaintext.php':'wordpress/carmilla-bridge/tests/smoke-identity-synthetic-migration-real-wp.php',
   wpcs:{column:matches[0].column,disposition:matches[0].disposition,targetTask:matches[0].targetTask||null,matchBasis:'path+line+code'}})
 }
}
if(findings.length!==16||findings.filter(x=>x.fixTaskId).length!==12)throw Error('Count mismatch');
fs.writeFileSync(path.join(__dirname,'ledger.json'),JSON.stringify({schema:1,taskId:'P02-CI-CODE-010C3B2D',reportSha256:hash,count:findings.length,findings},null,2)+'\n');
console.log('findings=16 deferredToP07=12 context=4 wpcsMatched=16');
