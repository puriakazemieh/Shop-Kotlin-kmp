const fs=require('node:fs'),zlib=require('node:zlib'),crypto=require('node:crypto'),path=require('node:path');
const root=path.resolve(__dirname,'../../..');
const report=zlib.gunzipSync(fs.readFileSync(path.join(root,'docs/evidence/P02-CI-CODE-010C3A1/plugin-check-results.txt.gz')));
const hash=crypto.createHash('sha256').update(report).digest('hex');
if(hash!=='50f0c694447c4b9aab40b1d66c4091cecdad0614ba18c8f70208c3f2b36fd0bd')throw Error('Pinned report changed');
const selected=c=>c==='missing_direct_file_access_protection'||c.startsWith('WordPressVIPMinimum.Performance.')||['missing_readme_header_tested','no_license','no_stable_tag','readme_short_description_non_official_language','readme_description_non_official_language','mismatched_plugin_name','readme_parser_warnings_trimmed_short_description','trademarked_term'].includes(c);
let file='';const findings=[];
for(const line of report.toString('utf8').split(/\r?\n/)){
 if(line.startsWith('FILE: ')){file=line.slice(6);continue}
 if(!line.startsWith('['))continue;
 for(const item of JSON.parse(line)){
  if(!selected(item.code))continue;
  let target=null,risk='',disposition='';
  if(file==='tools/user-integrity.php'){
   risk='Operator-only CLI entry checks PHP_SAPI before loading WordPress and returns 404/exit 2 for a browser request; scanner specifically expects ABSPATH but the existing direct-request boundary is fail-closed.';
   disposition='context_justified_cli_guard';
  }else if(file==='packages/carmilla-core/Carmilla_Kernel.php'){
   target='P02-SEC-CODE-010C3B3D1';risk='Shared-core entry loads class definitions without an explicit ABSPATH guard when directly requested; require fail-closed entry without harming normal hosts.';disposition='fix_required_in_p02';
  }else if(file==='README.md'){
   target='P04-WP-README-CODE-060';risk='Shipped README.md is Persian developer/install documentation, not a WordPress.org compliant plugin readme; the active product contract has not selected that publication channel.';disposition='deferred_to_channel_specific_package';
  }else if(item.code.startsWith('WordPressVIPMinimum.Performance.')){
   target='P04-WP-RELATED-PERF-CODE-061';risk='Related item query excludes the current ID and requests four results; output is bounded but underlying exclusion query cost needs synthetic SQL measurement.';disposition='deferred_to_p04_performance';
  }else throw Error(`Unreviewed ${file}:${item.code}`);
  findings.push({path:`wordpress/${file.startsWith('packages/')?'':'carmilla-bridge/'}${file}`,shippedZipPath:file,line:item.line,column:item.column,code:item.code,message:item.message,severity:item.type,
   sourceTrace:`wordpress/${file.startsWith('packages/')?'':'carmilla-bridge/'}${file}${item.line?':'+item.line:''}`,sourceAndRisk:risk,disposition,fixTaskId:target,verificationTaskId:'P02-CI-CODE-010C3B3D'});
 }
}
if(findings.length!==12||findings.filter(x=>x.shippedZipPath==='README.md').length!==8||new Set(findings.map(x=>`${x.shippedZipPath}:${x.line}:${x.column}:${x.code}`)).size!==12)throw Error('Coverage mismatch');
fs.writeFileSync(path.join(__dirname,'ledger.json'),JSON.stringify({schema:1,taskId:'P02-CI-CODE-010C3B3D',reportSha256:hash,count:findings.length,findings},null,2)+'\n');
console.log('findings=12 cliContext=1 coreFix=1 readmeP04=8 perfP04=2');
