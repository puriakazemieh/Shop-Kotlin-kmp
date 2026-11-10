const fs=require('node:fs'),zlib=require('node:zlib'),crypto=require('node:crypto'),path=require('node:path');
const root=path.resolve(__dirname,'../../..');
const raw=zlib.gunzipSync(fs.readFileSync(path.join(root,'docs/evidence/P02-CI-CODE-010C3A1/plugin-check-results.txt.gz')));
const hash=crypto.createHash('sha256').update(raw).digest('hex');
if(hash!=='50f0c694447c4b9aab40b1d66c4091cecdad0614ba18c8f70208c3f2b36fd0bd')throw Error('Pinned report changed');
const key=x=>`${x.shippedZipPath}:${x.line}:${x.column}:${x.code}`;
const reported=new Map();let file='',all=0,testPathCount=0;
for(const line of raw.toString('utf8').split(/\r?\n/)){
 if(line.startsWith('FILE: ')){file=line.slice(6);continue}
 if(!line.startsWith('['))continue;
 for(const item of JSON.parse(line)){
  all++;
  if(/(^|\/)tests?\//.test(file))testPathCount++;
  if(item.code.startsWith('WordPress.DB.')||item.code.startsWith('WordPress.Security.')||item.code.startsWith('PluginCheck.Security.'))continue;
  const k=`${file}:${item.line}:${item.column}:${item.code}`;
  if(reported.has(k))throw Error(`Duplicate report finding ${k}`);
  reported.set(k,item);
 }
}
const ids=['A1','A2','B','C','D'],seen=new Map(),ledgers=[],dispositions={},fixOwners={};
for(const id of ids){
 const taskId=`P02-CI-CODE-010C3B3${id}`;
 const ledger=JSON.parse(fs.readFileSync(path.join(root,`docs/evidence/${taskId}/ledger.json`)));
 if(ledger.taskId!==taskId||ledger.reportSha256!==hash||ledger.count!==ledger.findings.length)throw Error(`Bad ledger header ${taskId}`);
 ledgers.push({taskId,count:ledger.count});
 for(const f of ledger.findings){
  const k=key(f),r=reported.get(k);
  if(seen.has(k))throw Error(`Duplicate ledger finding ${k}`);
  if(!r)throw Error(`Extra ledger finding ${k}`);
  if(f.message!==r.message||f.severity!==r.type)throw Error(`Message/severity mismatch ${k}`);
  if(!f.sourceAndRisk||!f.sourceTrace||!f.disposition||f.verificationTaskId!==taskId)throw Error(`Incomplete source/disposition ${k}`);
  if(f.fixTaskId&&!fs.existsSync(path.join(root,'docs/tasks',f.fixTaskId+'.md')))throw Error(`Missing owner card ${f.fixTaskId}`);
  seen.set(k,taskId);dispositions[f.disposition]=(dispositions[f.disposition]||0)+1;
  if(f.fixTaskId)fixOwners[f.fixTaskId]=(fixOwners[f.fixTaskId]||0)+1;
 }
}
for(const k of reported.keys())if(!seen.has(k))throw Error(`Missing ledger finding ${k}`);
if(all!==315||testPathCount!==0||reported.size!==122||seen.size!==122)throw Error('Aggregate count mismatch');
const result={schema:1,taskId:'P02-CI-CODE-010C3B3E',reportSha256:hash,allReportFindings:all,reportTestPathFindings:testPathCount,reportedCount:reported.size,ledgerCount:seen.size,duplicateCount:0,missingCount:0,extraCount:0,messageMismatchCount:0,ledgers,dispositions,fixOwners};
fs.writeFileSync(path.join(__dirname,'reconciliation.json'),JSON.stringify(result,null,2)+'\n');
console.log(`PASS report=${all} remaining=${reported.size} ledgers=${seen.size} testPaths=${testPathCount}`);
