const fs=require('node:fs'),zlib=require('node:zlib'),crypto=require('node:crypto'),path=require('node:path');
const root=path.resolve(__dirname,'../../..');
const report=zlib.gunzipSync(fs.readFileSync(path.join(root,'docs/evidence/P02-CI-CODE-010C3A1/plugin-check-results.txt.gz')));
const hash=crypto.createHash('sha256').update(report).digest('hex');
if(hash!=='50f0c694447c4b9aab40b1d66c4091cecdad0614ba18c8f70208c3f2b36fd0bd')throw Error('Pinned report changed');
let file='';const findings=[];
for(const line of report.toString('utf8').split(/\r?\n/)){
 if(line.startsWith('FILE: ')){file=line.slice(6);continue}
 if(!line.startsWith('['))continue;
 for(const item of JSON.parse(line)){
  if(!item.code.startsWith('WordPress.WP.I18n.'))continue;
  const shared=file.startsWith('packages/carmilla-core/');
  const risk=item.code.endsWith('TextDomainMismatch')?'Shared core uses Theme domain carmilla while the Bridge ZIP declares carmilla-bridge; Bridge-only translation loading and published domain alignment are unverified.':
   item.code.endsWith('NonSingularStringLiteralText')?'Concatenated privacy-policy text cannot be extracted as one translation string and includes legally sensitive copy requiring product review.':
   item.code.endsWith('MissingTranslatorsComment')?'Placeholder meaning is not documented for translators and may produce incorrect localized administrative/privacy text.':
   'Bridge-local permission error omits the plugin text domain, preventing product catalog lookup.';
  findings.push({path:`wordpress/${shared?'':'carmilla-bridge/'}${file}`,shippedZipPath:file,line:item.line,column:item.column,code:item.code,message:item.message,severity:item.type,
   declaredBridgeDomain:'carmilla-bridge',declaredThemeDomain:'carmilla',sourceTrace:`wordpress/${shared?'':'carmilla-bridge/'}${file}:${item.line}`,
   sourceAndRisk:risk,disposition:'deferred_to_independent_product_i18n',fixTaskId:'P04-WP-I18N-CODE-059',verificationTaskId:'P02-CI-CODE-010C3B3B'});
 }
}
if(findings.length!==35||findings.filter(x=>x.code.endsWith('TextDomainMismatch')).length!==31||new Set(findings.map(x=>`${x.shippedZipPath}:${x.line}:${x.column}:${x.code}`)).size!==35)throw Error('Coverage mismatch');
fs.writeFileSync(path.join(__dirname,'ledger.json'),JSON.stringify({schema:1,taskId:'P02-CI-CODE-010C3B3B',reportSha256:hash,count:findings.length,findings},null,2)+'\n');
console.log('findings=35 domainMismatch=31 other=4 owner=P04-WP-I18N-CODE-059');
