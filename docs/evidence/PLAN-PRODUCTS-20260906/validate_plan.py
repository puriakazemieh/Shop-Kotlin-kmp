"""Read-only validation of the planning artifacts; writes only its JSON report."""
from pathlib import Path
import collections, json, re, subprocess, sys
from urllib.parse import unquote, urlparse
sys.dont_write_bytecode=True
from types import SimpleNamespace

ROOT=Path(__file__).resolve().parents[3]
def read(path):return Path(path).read_text(encoding='utf-8-sig')
def write(path,text):Path(path).write_text(text.rstrip()+'\n',encoding='utf-8')
def git(*args):return subprocess.check_output(['git',*args],cwd=ROOT,stderr=subprocess.DEVNULL).decode('utf-8-sig')
def rows(text):
    result=[]
    for line in text.splitlines():
        c=line.split('|')
        if len(c)<7 or c[1].strip() not in ['DONE','TODO','READY','CODE_COMPLETE','AWAITING_MANUAL_QA','BLOCKED']:continue
        result.append(dict(id=c[2].strip(),status=c[1].strip(),owner=c[3].strip(),deps=re.findall(r'P\d{2}-[A-Z]+-[A-Z]+-\d+[A-Z]?',c[4])))
    return result
p=SimpleNamespace(ROOT=ROOT,DOCS=ROOT/'docs',OUT=Path(__file__).parent,read=read,write=write,git=git,rows=rows)

errors=[];historical=[]
q=p.read(p.DOCS/'tasks.md');rs=p.rows(q)
ids=[r['id'] for r in rs];byid={r['id']:r for r in rs}
if len(ids)!=len(set(ids)):errors.append('Duplicate queue task ID')
baseline=json.loads(p.read(p.OUT/'baseline.json'))
for r in baseline['rows']:
    if r['id'] not in byid:errors.append('Lost existing queue task '+r['id'])
    elif r['status']=='DONE' and byid[r['id']]['status']!='DONE':errors.append('Rewrote historical DONE '+r['id'])
ready=[r['id'] for r in rs if r['status']=='READY']
if ready!=['P03-QA-REVIEW-023']:errors.append('Unexpected READY list '+str(ready))

edges={r['id']:r['deps'] for r in rs}
for r in rs:
    for d in r['deps']:
        if d not in byid:errors.append('Unknown dependency '+r['id']+' -> '+d)
    if r['status']=='READY' and any(byid[d]['status']!='DONE' for d in r['deps']):
        errors.append('Unmet READY dependency '+r['id'])
visiting=set();visited=set()
def visit(id_):
    if id_ in visiting:raise ValueError('Cycle at '+id_)
    if id_ in visited:return
    visiting.add(id_)
    for d in edges.get(id_,[]):visit(d)
    visiting.remove(id_);visited.add(id_)
try:
    for id_ in ids:visit(id_)
except ValueError as ex:errors.append(str(ex))

for r in rs:
    path=p.DOCS/'tasks'/f"{r['id']}.md"
    if not path.exists():errors.append('Missing card '+r['id']);continue
    text=p.read(path);m=re.search(r'(?m)^- Status:[ \t]*(\w+)',text)
    if not m or m[1]!=r['status']:
        (historical if r['status']=='DONE' else errors).append('Card status mismatch '+r['id']+'; card='+(m[1] if m else 'missing')+'; queue='+r['status'])
    m=re.search(r'(?m)^- Depends on:([^\n]*)',text)
    actual=set(re.findall(r'P\d{2}-[A-Z]+-[A-Z]+-\d+[A-Z]?',m[1] if m else ''))
    if actual!=set(r['deps']):
        (historical if r['status']=='DONE' else errors).append('Card dependency mismatch '+r['id'])

master=p.read(p.DOCS/'MASTER_IMPLEMENTATION_CHECKLIST_FA.md')
masterids=[]
for line in master.splitlines():
    c=line.split('|')
    if len(c)>=7 and re.fullmatch(r'`P\d{2}-[A-Z]+-[A-Z]+-\d+[A-Z]?`',c[2].strip()):masterids.append(c[2].strip(' `'))
new=json.loads(p.read(p.OUT/'plan-mutations.json'))['new']+json.loads(p.read(p.OUT/'final-acceptance-tasks.json'))
for n in new:
    if masterids.count(n['id'])!=1:errors.append('New task missing/duplicated in Master '+n['id'])
    if n['id'] not in p.read(p.DOCS/'PHASE_SUMMARY_FA.md'):errors.append('New task missing phase summary '+n['id'])
    text=p.read(p.DOCS/'tasks'/f"{n['id']}.md")
    for needle in ['## Acceptance Criteria','## Manual QA','## Evidence','- Depends on:']:
        if needle not in text:errors.append('Incomplete new task '+n['id']+' '+needle)
    if re.search(r'- Priority/Risk/Size:.* / (?:L|XL)\b',text):errors.append('Oversized new card '+n['id'])

changed=p.git('-c','core.safecrlf=false','diff','--name-only').splitlines()
for path in changed:
    if not path.startswith('docs/'):errors.append('Unexpected source mutation '+path)
for r in baseline['rows']:
    if r['status']=='DONE' and f"docs/tasks/{r['id']}.md" in changed:errors.append('Changed DONE card '+r['id'])

# Only inspect added links in edited artifacts; existing historical links may already be stale.
paths=set(changed)|{f"docs/tasks/{n['id']}.md" for n in new}|{
 'docs/INDEPENDENT_PRODUCTS_SPEC_FA.md','docs/architecture/adr/ADR-006-INDEPENDENT-PRODUCTS-AND-ENTITLEMENTS.md','docs/evidence/PLAN-PRODUCTS-20260906/summary.md'}
for relative in paths:
    file=p.ROOT/relative
    if file.suffix!='.md':continue
    text=p.read(file)
    try:old=p.git('show','HEAD:'+relative)
    except subprocess.CalledProcessError:old=''
    for target in re.findall(r'(?<!!)\[[^\]\n]+\]\(([^)]+)\)',text):
        if ']('+target+')' in old:continue
        dest=target.strip('<>')
        if dest.startswith('#') or urlparse(dest).scheme in ['https','http','mailto']:continue
        dest=unquote(dest.split('#')[0])
        if not (file.parent/dest).resolve().exists():
            # The report itself is written at the end of this verification.
            if dest=='validation.json' and file.parent==p.OUT:continue
            errors.append('Broken added link '+relative+' -> '+target)

old_master=p.git('show','HEAD:docs/MASTER_IMPLEMENTATION_CHECKLIST_FA.md')
def checked(text):
    return set(re.findall(r'(?m)^\|[ \t]*\[x\][ \t]*\|[ \t]*`([^`]+)`',text))
if checked(master)-checked(old_master):errors.append('New DONE checkbox without evidence')

controls=re.findall(r'^- (TODO|READY|DONE|BLOCKED|AWAITING_MANUAL_QA) — \[([^\]]+)\]',q,re.M)
all_counts=collections.Counter([r['status'] for r in rs]+[s for s,_ in controls])
diff=subprocess.run(['git','-c','core.safecrlf=false','diff','--check'],cwd=p.ROOT,capture_output=True,text=True)
if diff.returncode:errors.append('git diff --check: '+diff.stdout.strip())
report=dict(validation='PASS' if not errors else 'FAIL',errors=errors,phased_tasks=len(rs),controls=len(controls),
 total_tasks=len(rs)+len(controls),status_counts=dict(all_counts),new_tasks=len(new),ready=ready,
 dependency_graph='acyclic' if not any('Cycle' in x for x in errors) else 'cycle',
 historical_inconsistencies_preserved=historical,changed_tracked_files=len(changed),source_changes=False,
 git_diff_check_exit=diff.returncode,product_builds_and_manual_QA='NOT RUN: documentation-only request')
p.write(p.OUT/'validation.json',json.dumps(report,ensure_ascii=False,indent=2))
print(json.dumps(report,ensure_ascii=True))
sys.exit(1 if errors else 0)
