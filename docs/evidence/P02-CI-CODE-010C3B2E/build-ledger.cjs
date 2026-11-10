const fs=require('node:fs'),zlib=require('node:zlib'),crypto=require('node:crypto'),path=require('node:path');
const root=path.resolve(__dirname,'../../..');
const report=zlib.gunzipSync(fs.readFileSync(path.join(root,'docs/evidence/P02-CI-CODE-010C3A1/plugin-check-results.txt.gz')));
const hash=crypto.createHash('sha256').update(report).digest('hex');
if(hash!=='50f0c694447c4b9aab40b1d66c4091cecdad0614ba18c8f70208c3f2b36fd0bd')throw Error('Pinned report changed');
const reasons={181:'Seed registry lookup uses fixed-prefix table and wpdb-prepared seed ID; read is live for import idempotency.',219:'Attachment registry lookup uses fixed-prefix table and wpdb-prepared seed ID; read is live for import idempotency.',261:'Rollback deletes from fixed-prefix registry by wpdb->delete, which formats values.',271:'Rollback registry lookup uses fixed-prefix table and wpdb-prepared seed ID.',280:'Rollback deletes registry entry by wpdb->delete with an ID value.',303:'Import registry insert uses wpdb->insert, which formats values.'};
let file='';const findings=[];
for(const line of report.toString('utf8').split(/\r?\n/)){
 if(line.startsWith('FILE: ')){file=line.slice(6);continue}
 if(!line.startsWith('[')||!/^packages\/carmilla-core\/inc\/(Import\/Carmilla_Importer|Payment\/WalletService)\.php$/.test(file))continue;
 for(const item of JSON.parse(line)){
  if(!item.code.startsWith('WordPress.DB.'))continue;
  const wallet=file.includes('WalletService');if(!wallet&&!reasons[item.line])throw Error(`Unreviewed ${file}:${item.line}`);
  findings.push({path:`wordpress/${file}`,shippedZipPath:file,line:item.line,column:item.column,code:item.code,message:item.message,severity:item.type,
   sqlOriginAndRisk:wallet?'wpdb->insert escapes values, but wallet balance is updated before this unchecked ledger write; concurrent debits and failed insert can produce incorrect payment success. Gateway is registered and enabled by default.':reasons[item.line],
   disposition:wallet?'fix_required':'context_justified',fixTaskId:wallet?'P02-SEC-CODE-010C3B2E1':null,
   verificationTaskId:'P02-CI-CODE-010C3B2E',sourceTrace:`wordpress/${file}:${item.line}`,
   testPath:null,wpcs:{matchBasis:'no prior entry: bundled core outside WPCS scan root'}})
 }
}
if(findings.length!==12||findings.filter(x=>x.fixTaskId).length!==1)throw Error('Coverage mismatch');
fs.writeFileSync(path.join(__dirname,'ledger.json'),JSON.stringify({schema:1,taskId:'P02-CI-CODE-010C3B2E',reportSha256:hash,count:findings.length,findings},null,2)+'\n');
console.log('findings=12 importerContext=11 walletFix=1 priorWpcs=0');
