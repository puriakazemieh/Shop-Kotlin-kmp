const fs=require('node:fs'),zlib=require('node:zlib'),crypto=require('node:crypto'),path=require('node:path');
const root=path.resolve(__dirname,'../../..');
const raw=zlib.gunzipSync(fs.readFileSync(path.join(root,'docs/evidence/P02-CI-CODE-010C3A1/plugin-check-results.txt.gz')));
const hash=crypto.createHash('sha256').update(raw).digest('hex');
if(hash!=='50f0c694447c4b9aab40b1d66c4091cecdad0614ba18c8f70208c3f2b36fd0bd')throw Error('Pinned report changed');
const ids=['A1','A2','B','C','D','E','F','G1','G2'];
const key=x=>`${x.shippedZipPath||x.path.replace(/^wordpress\/carmilla-bridge\//,'').replace(/^wordpress\//,'')}:${x.line}:${x.column}:${x.code}`;
const reported=new Map();let file='';
for(const line of raw.toString('utf8').split(/\r?\n/)){
 if(line.startsWith('FILE: ')){file=line.slice(6);continue}
 if(!line.startsWith('['))continue;
 for(const item of JSON.parse(line)){
  if(!item.code.startsWith('WordPress.DB.'))continue;
  const k=`${file}:${item.line}:${item.column}:${item.code}`;
  if(reported.has(k))throw Error(`Duplicate report finding ${k}`);
  reported.set(k,item);
 }
}
const wpcs=JSON.parse(fs.readFileSync(path.join(root,'tools/code-quality/wpcs-security-dispositions.json'))).securityDbFindings;
const ledgers=[];const seen=new Map();const dispositions={};const owners={};let wpcsMatches=0,coreWithoutWpcs=0;
for(const id of ids){
 const taskId=`P02-CI-CODE-010C3B2${id}`;
 const ledger=JSON.parse(fs.readFileSync(path.join(root,`docs/evidence/${taskId}/ledger.json`)));
 if(ledger.taskId!==taskId||ledger.reportSha256!==hash||ledger.count!==ledger.findings.length)throw Error(`Bad ledger header ${taskId}`);
 ledgers.push({taskId,count:ledger.count});
 for(const f of ledger.findings){
  const k=key(f),r=reported.get(k);
  if(seen.has(k))throw Error(`Duplicate ledger finding ${k}: ${seen.get(k)} and ${taskId}`);
  if(!r)throw Error(`Extra ledger finding ${k}`);
  if(f.message!==r.message||f.severity!==r.type)throw Error(`Message/severity mismatch ${k}`);
  if(!f.sqlOriginAndRisk&&!f.sqlOriginAndGuard)throw Error(`No source risk ${k}`);
  if(!f.disposition||!f.sourceTrace||!f.verificationTaskId)throw Error(`Incomplete disposition ${k}`);
  if(f.fixTaskId&&!fs.existsSync(path.join(root,'docs/tasks',f.fixTaskId+'.md')))throw Error(`Missing owner card ${f.fixTaskId}`);
  const matches=wpcs.filter(x=>x.path===f.path&&x.line===f.line&&x.source===f.code);
  if(f.path.startsWith('wordpress/packages/')){
   if(matches.length!==0)throw Error(`Unexpected bundled-core WPCS overlap ${k}`);
   coreWithoutWpcs++;
  }else{
   if(matches.length!==1||!f.wpcs)throw Error(`WPCS mismatch ${k}`);
   wpcsMatches++;
  }
  seen.set(k,taskId);dispositions[f.disposition]=(dispositions[f.disposition]||0)+1;
  if(f.fixTaskId)owners[f.fixTaskId]=(owners[f.fixTaskId]||0)+1;
 }
}
for(const k of reported.keys())if(!seen.has(k))throw Error(`Missing ledger finding ${k}`);
if(reported.size!==135||seen.size!==135||wpcsMatches!==123||coreWithoutWpcs!==12)throw Error('Aggregate count mismatch');
const result={schema:1,taskId:'P02-CI-CODE-010C3B2H',reportSha256:hash,reportedCount:reported.size,ledgerCount:seen.size,duplicateCount:0,missingCount:0,extraCount:0,messageMismatchCount:0,wpcsMatched:wpcsMatches,bundledCoreOutsideWpcs:coreWithoutWpcs,ledgers,dispositions,fixOwners:owners};
fs.writeFileSync(path.join(__dirname,'reconciliation.json'),JSON.stringify(result,null,2)+'\n');
console.log(`PASS reported=${reported.size} ledgers=${seen.size} wpcs=${wpcsMatches} bundledCore=${coreWithoutWpcs}`);
