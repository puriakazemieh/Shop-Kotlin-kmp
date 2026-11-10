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
  if(!item.code.startsWith('WordPress.WP.AlternativeFunctions.')&&!item.code.startsWith('WordPress.PHP.DevelopmentFunctions.'))continue;
  const integration=file==='includes/class-cb-integrations-controller.php';
  const importer=file==='packages/carmilla-core/inc/Import/Carmilla_Importer.php';
  const target=integration?'P02-SEC-CODE-010C3B3C1':importer?'P07-SEED-SEC-CODE-030':null;
  const risk=integration?'Admin test-message success log retains part of a recipient; failure log includes exception text, whose contents cannot be assumed nonidentifying.':
   importer?'Importer checks parse_url host against an allowlist before media_sideload_image, but does not explicitly require HTTPS or prove redirect/destination safety.':
   file.includes('web-request-policy')||file.includes('web-session-wp-adapter')?'Native parse_url is used inside restrictive URL/origin canonicalization; schemes, hosts, credentials and forbidden components are checked by surrounding fail-closed policy.':
   file.includes('Migration/Export_Command')?'CLI migration export writes encrypted temporary files, atomically renames them, and checks cleanup failure; direct local filesystem semantics are deliberate and need P07 migration acceptance.':
   file==='tools/user-integrity.php'?'Operator-only CLI prints fixed failure/result to STDERR/STDOUT after rejecting non-CLI requests; no browser filesystem write.':
   file==='includes/class-cb-plugin.php'?'Legacy alias telemetry logs only a match selected from a fixed four-entry route allowlist; it does not log the request URI or query.':
   file==='includes/class-cb-auth-controller.php'?'OTP dispatch failure log is fixed text without mobile, code, message or provider exception.':
   file==='includes/class-cb-message-queue.php'?'SMS queue logs only a fixed safe error code (invalid_config, missing_config or http_400), not the recipient or message.':null;
  if(!risk)throw Error(`Unreviewed ${file}:${item.line}`);
  findings.push({path:`wordpress/${importer?'':'carmilla-bridge/'}${file}`,shippedZipPath:file,line:item.line,column:item.column,code:item.code,message:item.message,severity:item.type,
   sourceTrace:`wordpress/${importer?'':'carmilla-bridge/'}${file}:${item.line}`,sourceAndRisk:risk,
   disposition:integration?'fix_required_in_p02':importer?'deferred_to_seed_security':'context_justified',fixTaskId:target,verificationTaskId:'P02-CI-CODE-010C3B3C'});
 }
}
if(findings.length!==20||findings.filter(x=>x.fixTaskId==='P02-SEC-CODE-010C3B3C1').length!==2||findings.filter(x=>x.fixTaskId==='P07-SEED-SEC-CODE-030').length!==1||new Set(findings.map(x=>`${x.shippedZipPath}:${x.line}:${x.column}:${x.code}`)).size!==20)throw Error('Coverage mismatch');
fs.writeFileSync(path.join(__dirname,'ledger.json'),JSON.stringify({schema:1,taskId:'P02-CI-CODE-010C3B3C',reportSha256:hash,count:findings.length,findings},null,2)+'\n');
console.log('findings=20 context=17 p02Fix=2 p07Seed=1');
