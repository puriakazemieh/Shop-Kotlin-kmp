#!/usr/bin/env node
const fs=require('node:fs'),os=require('node:os'),path=require('node:path'),zlib=require('node:zlib'),cp=require('node:child_process');
const root=path.resolve(__dirname,'../..');
const checker=path.join(__dirname,'check_plugin_check_baseline.cjs');
const baseline=JSON.parse(fs.readFileSync(path.join(__dirname,'plugin-check-baseline.json')));
const source=zlib.gunzipSync(fs.readFileSync(path.join(root,'docs/evidence/P02-CI-CODE-010C3A1/plugin-check-results.txt.gz'))).toString('utf8');
const current=zlib.gunzipSync(fs.readFileSync(path.join(root,'docs/evidence/P02-CI-CODE-010C3C4D/plugin-check-current.txt.gz'))).toString('utf8');
const temp=fs.mkdtempSync(path.join(os.tmpdir(),'cb-plugin-baseline-'));
const args=report=>[checker,'--report',report,'--scanner-version','2.1.0','--action-ref','10857da14b6c2246d15402b3e69f777edcf8c12e','--wp-version','6.4.3'];
const key=(file,x)=>`${file}\u0000${x.line}\u0000${x.column}\u0000${x.code}`;
function blocks(text){const out=[];let file=null;for(const line of text.split(/\r?\n/)){if(line.startsWith('FILE: ')){file=line.slice(6);continue;}if(line.startsWith('['))out.push({file,items:JSON.parse(line)});}return out;}
function write(name,parts){const filename=path.join(temp,name);fs.writeFileSync(filename,parts.map(x=>`FILE: ${x.file}\n${JSON.stringify(x.items)}\n`).join('\n'));return filename;}
function run(name,filename,expected,extra=[],env={}){const result=cp.spawnSync(process.execPath,[...args(filename),...extra],{encoding:'utf8',env:{...process.env,...env}});const okay=(result.status===0)===expected;if(!okay)throw Error(`${name}: expected ${expected?'pass':'failure'}, got ${result.status}: ${result.stdout}${result.stderr}`);console.log(`PASS ${name}`);}
try{
 const allowed=new Set(baseline.findings.filter(x=>x.policy==='allow').map(x=>key(x.path,x)));
 const original=blocks(source);
 const positive=original.map(x=>({file:x.file,items:x.items.filter(i=>allowed.has(key(x.file,i)))}));
 const good=write('positive.txt',positive);
 run('approved subset',good,true);
 const currentBlocks=blocks(current);
 run('current shipped reviewed report',write('current.txt',currentBlocks),true);
 const currentRegression=structuredClone(currentBlocks);
 currentRegression[0].items.push({line:999,column:1,type:'ERROR',code:'WordPress.Security.Synthetic.NewFinding',message:'Synthetic new finding.'});
 run('current report new finding rejected',write('current-regression.txt',currentRegression),false);
 const fewer=positive.map((x,i)=>({file:x.file,items:i===0?x.items.slice(1):x.items}));
 run('resolved finding may disappear',write('resolved.txt',fewer),true);
 run('unresolved P02 finding rejected',write('original.txt',original),false);
 for(const [label,code] of [['new security','WordPress.Security.Test.New'],['new DB','WordPress.DB.Test.New'],['new other','WordPress.WP.Test.New']]){
  const changed=structuredClone(positive);changed[0].items.push({line:999,column:1,type:'ERROR',code,message:'Synthetic regression.'});
  run(label,write(label.replaceAll(' ','-')+'.txt',changed),false);
 }
 const changed=structuredClone(positive);changed[0].items[0].message+=' altered';
 run('message mutation rejected',write('changed.txt',changed),false);
 const duplicate=structuredClone(positive);duplicate[0].items.push({...duplicate[0].items[0]});
 run('duplicate report finding rejected',write('duplicate.txt',duplicate),false);
 const empty=path.join(temp,'empty.txt');fs.writeFileSync(empty,'');run('empty report rejected',empty,false);
 run('missing report rejected',path.join(temp,'missing.txt'),false);
 run('version drift rejected',good,false,['--scanner-version','9.9.9']);
 const badBaseline={...baseline,findings:baseline.findings.slice(1)};const badPath=path.join(temp,'bad-baseline.json');fs.writeFileSync(badPath,JSON.stringify(badBaseline));
 run('malformed baseline rejected',good,false,['--baseline',badPath]);
 const fixture=path.join(temp,'fixture');
 for(const id of Object.keys(baseline.ledgerHashes)){const relative=`docs/evidence/${id}/ledger.json`;const target=path.join(fixture,relative);fs.mkdirSync(path.dirname(target),{recursive:true});fs.copyFileSync(path.join(root,relative),target);}
 const pinnedRelative='docs/evidence/P02-CI-CODE-010C3A1/plugin-check-results.txt.gz';fs.mkdirSync(path.dirname(path.join(fixture,pinnedRelative)),{recursive:true});fs.copyFileSync(path.join(root,pinnedRelative),path.join(fixture,pinnedRelative));
 const currentDir='docs/evidence/P02-CI-CODE-010C3C4D';
 for(const name of ['ledger.json','plugin-check-current.txt.gz']){const relative=`${currentDir}/${name}`;fs.mkdirSync(path.dirname(path.join(fixture,relative)),{recursive:true});fs.copyFileSync(path.join(root,relative),path.join(fixture,relative));}
 for(const id of new Set(baseline.findings.map(x=>x.fixTaskId).filter(Boolean))){const relative=`docs/tasks/${id}.md`;const target=path.join(fixture,relative);fs.mkdirSync(path.dirname(target),{recursive:true});fs.copyFileSync(path.join(root,relative),target);}
 const env={CARMILLA_PLUGIN_CHECK_FIXTURE_ROOT:fixture};run('fixture ledgers accepted',good,true,[],env);
 const currentLedgerPath=path.join(fixture,currentDir,'ledger.json'),currentLedgerBytes=fs.readFileSync(currentLedgerPath);
 fs.appendFileSync(currentLedgerPath,'\n');run('current ledger tamper rejected',good,false,[],env);fs.writeFileSync(currentLedgerPath,currentLedgerBytes);
 const currentReportPath=path.join(fixture,currentDir,'plugin-check-current.txt.gz'),currentReportBytes=fs.readFileSync(currentReportPath);
 fs.writeFileSync(currentReportPath,zlib.gzipSync(current+'\n'));run('current report tamper rejected',good,false,[],env);fs.writeFileSync(currentReportPath,currentReportBytes);
 const first=Object.keys(baseline.ledgerHashes)[0],ledgerPath=path.join(fixture,'docs/evidence',first,'ledger.json');
 const ledger=JSON.parse(fs.readFileSync(ledgerPath));ledger.findings.push({...ledger.findings[0]});ledger.count++;
 fs.writeFileSync(ledgerPath,JSON.stringify(ledger));run('duplicate or tampered ledger rejected',good,false,[],env);
 fs.rmSync(ledgerPath);run('missing ledger rejected',good,false,[],env);
 console.log('PASS all baseline scenarios');
}finally{fs.rmSync(temp,{recursive:true,force:true});}
