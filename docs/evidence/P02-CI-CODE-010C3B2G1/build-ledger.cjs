const fs=require('node:fs'),zlib=require('node:zlib'),crypto=require('node:crypto'),path=require('node:path');
const root=path.resolve(__dirname,'../../..');
const report=zlib.gunzipSync(fs.readFileSync(path.join(root,'docs/evidence/P02-CI-CODE-010C3A1/plugin-check-results.txt.gz')));
const hash=crypto.createHash('sha256').update(report).digest('hex');
if(hash!=='50f0c694447c4b9aab40b1d66c4091cecdad0614ba18c8f70208c3f2b36fd0bd')throw Error('Pinned report changed');
const wpcs=JSON.parse(fs.readFileSync(path.join(root,'tools/code-quality/wpcs-security-dispositions.json'))).securityDbFindings;
const targets={
 'class-cb-admin-content-controller.php:302':'P04-WP-ADMIN-DATA-052','class-cb-admin-content-controller.php:616':'P04-WP-ADMIN-DATA-052','class-cb-admin-content-controller.php:747':'P04-WP-ADMIN-DATA-052','class-cb-admin-content-controller.php:764':'P04-WP-ADMIN-DATA-052','class-cb-admin-content-controller.php:863':'P04-WP-ADMIN-DATA-052',
 'class-cb-admin-controller.php:443':'P04-WP-ADMIN-DATA-052','class-cb-admin-controller.php:459':'P04-WP-ADMIN-DATA-052',
 'class-cb-catalog-controller.php:155':'P04-WP-CATALOG-CODE-055','class-cb-catalog-controller.php:179':'P04-WP-CATALOG-CODE-055','class-cb-catalog-controller.php:184':'P04-WP-CATALOG-CODE-055',
 'class-cb-catalog-controller.php:491':'P04-WP-CONTENT-PERF-CODE-057','class-cb-catalog-controller.php:492':'P04-WP-CONTENT-PERF-CODE-057','class-cb-catalog-controller.php:527':'P04-WP-CONTENT-PERF-CODE-056'};
const context='class-cb-admin-content-controller.php:465';
const selected=new Set(['class-cb-admin-content-controller.php','class-cb-admin-controller.php','class-cb-catalog-controller.php']);
let file='';const findings=[];
for(const line of report.toString('utf8').split(/\r?\n/)){
 if(line.startsWith('FILE: ')){file=line.slice(6);continue}
 if(!line.startsWith('[')||!selected.has(path.basename(file)))continue;
 for(const item of JSON.parse(line)){
  if(!item.code.startsWith('WordPress.DB.'))continue;
  const key=`${path.basename(file)}:${item.line}`,target=targets[key]||null;
  if(!target&&key!==context)throw Error(`Unreviewed ${key}`);
  const sourcePath=`wordpress/carmilla-bridge/${file}`;
  const matches=wpcs.filter(x=>x.path===sourcePath&&x.line===item.line&&x.source===item.code);
  if(matches.length!==1||matches[0].disposition!==(target?'deferred':'context_justified')||(matches[0].targetTask||null)!==target)throw Error(`WPCS mismatch ${key}:${item.code}`);
  if(target&&!fs.existsSync(path.join(root,'docs/tasks',target+'.md')))throw Error(`Missing target ${target}`);
  const risk=target ? (key.includes('catalog-controller.php:155')?'The measured WooCommerce environment ignores this product meta_query argument, so the advertised price range does not filter results; P04 owns a supported query path and performance.':key.includes('catalog-controller.php:179')||key.includes('catalog-controller.php:184')?'WooCommerce numeric price sort uses postmeta; page size bounds output but not the database sort.':key.includes('catalog-controller.php:491')||key.includes('catalog-controller.php:492')?'Public active-campaign lookup filters postmeta without a dedicated indexed projection.':key.includes('catalog-controller.php:527')?'Unbounded banner list sorted by numeric postmeta may scan/sort all banners.':'Admin get_users meta-key scan is capped at 500 users but may miss later matching users and update/list logic can be costly; data model and pagination belong to P04.') : 'Therapist booking read uses a trusted bookings-table identifier and wpdb->prepare for numeric ID; live availability must not be cached and last_error returns 503.';
  findings.push({path:sourcePath,shippedZipPath:file,line:item.line,column:item.column,code:item.code,message:item.message,severity:item.type,sqlOriginAndRisk:risk,
   disposition:target?'deferred_to_p04':'context_justified',fixTaskId:target,verificationTaskId:'P02-CI-CODE-010C3B2G1',sourceTrace:`${sourcePath}:${item.line}`,testPath:null,
   wpcs:{column:matches[0].column,disposition:matches[0].disposition,targetTask:matches[0].targetTask||null,matchBasis:'path+line+code'}});
 }
}
if(findings.length!==16||findings.filter(x=>x.fixTaskId).length!==13||new Set(findings.map(x=>`${x.shippedZipPath}:${x.line}:${x.column}:${x.code}`)).size!==16)throw Error('Coverage mismatch');
fs.writeFileSync(path.join(__dirname,'ledger.json'),JSON.stringify({schema:1,taskId:'P02-CI-CODE-010C3B2G1',reportSha256:hash,count:findings.length,findings},null,2)+'\n');
console.log('findings=16 deferredP04=13 context=3 wpcsMatched=16');
