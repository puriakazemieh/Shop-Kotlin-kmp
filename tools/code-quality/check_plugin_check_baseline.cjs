#!/usr/bin/env node
/** Exact shipped-file Plugin Check baseline; no code or severity is globally waived. */
const fs = require('node:fs');
const path = require('node:path');
const zlib = require('node:zlib');
const crypto = require('node:crypto');
const root = process.env.CARMILLA_PLUGIN_CHECK_FIXTURE_ROOT ? path.resolve(process.env.CARMILLA_PLUGIN_CHECK_FIXTURE_ROOT) : path.resolve(__dirname, '../..');
const baselinePath = path.join(__dirname, 'plugin-check-baseline.json');
const pinnedPath = path.join(root, 'docs/evidence/P02-CI-CODE-010C3A1/plugin-check-results.txt.gz');
const reportSha256 = '50f0c694447c4b9aab40b1d66c4091cecdad0614ba18c8f70208c3f2b36fd0bd';
const scannerVersion = '2.1.0';
const actionRef = '10857da14b6c2246d15402b3e69f777edcf8c12e';
const wpVersion = '6.4.3';
const ids = [
  ...['A1','A2','B','C','D'].map(x => `P02-CI-CODE-010C3B1${x}`),
  ...['A1','A2','B','C','D','E','F','G1','G2'].map(x => `P02-CI-CODE-010C3B2${x}`),
  ...['A1','A2','B','C','D'].map(x => `P02-CI-CODE-010C3B3${x}`),
];
const sha = data => crypto.createHash('sha256').update(data).digest('hex');
const decode = value => value.replace(/&#039;/g, "'").replace(/&quot;/g, '"').replace(/&gt;/g, '>').replace(/&lt;/g, '<').replace(/&amp;/g, '&');
const fail = message => { throw new Error(message); };
const own = (obj, key) => Object.prototype.hasOwnProperty.call(obj, key);
const key = x => `${x.path}\u0000${x.line}\u0000${x.column}\u0000${x.code}`;
function parseReport(data) {
  const rows = [];
  let file = null, headers = 0;
  for (const line of data.toString('utf8').split(/\r?\n/)) {
    if (line === '') continue;
    if (line.startsWith('FILE: ')) {
      file = line.slice(6); headers++;
      if (!file || file.includes('\\') || file.startsWith('/') || file.includes('..') || /(^|\/)tests?\//.test(file)) fail(`Invalid shipped path: ${file}`);
      continue;
    }
    if (!line.startsWith('[') || !file) fail('Malformed Plugin Check report');
    let block;
    try { block = JSON.parse(line); } catch { fail('Malformed Plugin Check JSON'); }
    if (!Array.isArray(block)) fail('Plugin Check block is not an array');
    for (const x of block) {
      if (!x || !Number.isInteger(x.line) || x.line < 0 || !Number.isInteger(x.column) || x.column < 0 || typeof x.code !== 'string' || !x.code || typeof x.message !== 'string' || !['ERROR','WARNING'].includes(x.type)) fail('Malformed Plugin Check finding');
      rows.push({path:file,line:x.line,column:x.column,code:x.code,message:x.message,severity:x.type});
    }
  }
  if (!headers) fail('Missing Plugin Check file headers');
  const seen = new Set();
  for (const row of rows) { const k=key(row); if (seen.has(k)) fail(`Duplicate report finding: ${k}`); seen.add(k); }
  return rows;
}
function ledgerRows() {
  const rows=[], hashes={};
  for (const id of ids) {
    const filename=path.join(root, 'docs/evidence', id, 'ledger.json');
    const bytes=fs.readFileSync(filename);
    hashes[id]=sha(bytes);
    const ledger=JSON.parse(bytes);
    if (ledger.schema!==1 || ledger.taskId!==id || (ledger.reportSha256||ledger.pluginCheckReportSha256)!==reportSha256 || !Number.isInteger(ledger.count) || !Array.isArray(ledger.findings) || ledger.count!==ledger.findings.length) fail(`Malformed ledger: ${id}`);
    for (const f of ledger.findings) {
      const source=f.reason||f.dataOriginAndRisk||f.variableOriginAndGuard||f.sqlOriginAndRisk||f.sqlOriginAndGuard||f.sourceAndRisk||f.risk;
      if (!f || typeof f.path!=='string' || !Number.isInteger(f.line) || !Number.isInteger(f.column) || typeof f.code!=='string' || typeof f.message!=='string' || !['ERROR','WARNING'].includes(f.severity) || !f.disposition || !f.sourceTrace || !source || f.verificationTaskId!==id) fail(`Incomplete ledger finding: ${id}`);
      if (f.fixTaskId && !fs.existsSync(path.join(root,'docs/tasks',f.fixTaskId+'.md'))) fail(`Missing fix owner: ${f.fixTaskId}`);
      if ((f.disposition.startsWith('deferred') || f.disposition.startsWith('fix_required')) && !f.fixTaskId) fail(`Unowned finding: ${id}`);
      const shipped=f.shippedZipPath||f.path.replace(/^wordpress\/carmilla-bridge\//,'').replace(/^wordpress\//,'');
      if (!shipped || shipped.includes('\\') || shipped.startsWith('/') || /(^|\/)tests?\//.test(shipped)) fail(`Invalid ledger path: ${id}`);
      rows.push({path:shipped,line:f.line,column:f.column,code:f.code,message:f.message,severity:f.severity,
        disposition:f.disposition,fixTaskId:f.fixTaskId||null,sourceTrace:f.sourceTrace,
        policy:f.fixTaskId&&f.fixTaskId.startsWith('P02-')?'must_disappear':'allow'});
    }
  }
  if (rows.length!==315) fail(`Expected 315 reviewed findings, found ${rows.length}`);
  const seen=new Set();for(const row of rows){const k=key(row);if(seen.has(k))fail(`Duplicate ledger finding: ${k}`);seen.add(k);}
  return {rows:rows.sort((a,b)=>key(a).localeCompare(key(b))),hashes};
}
function comparePinned(ledger) {
  const raw=zlib.gunzipSync(fs.readFileSync(pinnedPath));
  if (sha(raw)!==reportSha256) fail('Pinned report changed');
  const pinned=parseReport(raw);
  if (pinned.length!==315) fail('Pinned report count changed');
  const map=new Map(pinned.map(x=>[key(x),x]));
  for(const row of ledger){const original=map.get(key(row));if(!original||decode(original.message)!==decode(row.message)||original.severity!==row.severity)fail(`Ledger/pinned mismatch: ${key(row)}`);}
  return map;
}
function args(argv){const out={};for(let i=0;i<argv.length;i++){const a=argv[i];if(a==='--generate'){out.generate=true;continue;}if(!['--report','--scanner-version','--action-ref','--wp-version','--baseline'].includes(a)||!argv[i+1])fail(`Unknown or missing argument: ${a}`);out[a.slice(2)]=argv[++i];}return out;}
function main(){
  const opts=args(process.argv.slice(2));
  const {rows,hashes}=ledgerRows();
  const pinned=comparePinned(rows);
  if(opts.generate){
    if(Object.keys(opts).length!==1)fail('Generate takes no other arguments');
    const baseline={schema:1,scannerVersion,actionRef,wpVersion,pinnedReportSha256:reportSha256,ledgerHashes:hashes,
      findings:rows.map(row=>({...row,message:pinned.get(key(row)).message}))};
    fs.writeFileSync(baselinePath,JSON.stringify(baseline,null,2)+'\n');
    console.log(`Generated exact baseline: ${rows.length} reviewed, ${rows.filter(x=>x.policy==='must_disappear').length} must disappear`);
    return;
  }
  if(!opts.report||!opts['scanner-version']||!opts['action-ref']||!opts['wp-version'])fail('Report and pinned scanner/action/WP versions are required');
  const baseline=JSON.parse(fs.readFileSync(opts.baseline||baselinePath));
  if(baseline.schema!==1||baseline.scannerVersion!==scannerVersion||baseline.actionRef!==actionRef||baseline.wpVersion!==wpVersion||baseline.pinnedReportSha256!==reportSha256||!Array.isArray(baseline.findings)||baseline.findings.length!==315)fail('Malformed or drifted baseline');
  if(opts['scanner-version']!==scannerVersion||opts['action-ref']!==actionRef||opts['wp-version']!==wpVersion)fail('Scanner/action/WordPress version drift');
  if(JSON.stringify(baseline.ledgerHashes)!==JSON.stringify(hashes))fail('Ledger hash drift');
  const canonical=rows.map(row=>({...row,message:pinned.get(key(row)).message}));
  if(JSON.stringify(baseline.findings)!==JSON.stringify(canonical))fail('Baseline/ledger drift');
  const data=fs.readFileSync(opts.report);
  if(!data.length)fail('Empty Plugin Check report');
  const actual=parseReport(data);
  const allowed=new Map(baseline.findings.map(x=>[key(x),x]));
  const problems=[];
  for(const row of actual){const known=allowed.get(key(row));if(!known)problems.push(`New finding: ${key(row)}`);else if(known.message!==row.message||known.severity!==row.severity)problems.push(`Changed finding: ${key(row)}`);else if(known.policy!=='allow')problems.push(`Previously fixed finding remains: ${key(row)}`);}
  if(problems.length)fail(problems.slice(0,20).join('\n')+(problems.length>20?`\n... ${problems.length-20} more`:''));
  console.log(`PASS Plugin Check exact baseline: actual=${actual.length} reviewed=${rows.length} resolved=${rows.length-actual.length}`);
}
try{main();}catch(error){console.error(`FAIL Plugin Check baseline: ${error.message}`);process.exitCode=1;}
