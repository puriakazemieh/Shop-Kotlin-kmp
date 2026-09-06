"""Render the Persian specification as an offline, right-to-left HTML document."""
from pathlib import Path
from html.parser import HTMLParser
import markdown

root=Path(__file__).resolve().parents[3]
source=root/'docs/INDEPENDENT_PRODUCTS_SPEC_FA.md'
text=source.read_text(encoding='utf-8').strip()
text=text.removeprefix('<div dir="rtl" align="right">').removesuffix('</div>').strip()
body=markdown.markdown(text,extensions=['tables','fenced_code','toc'],output_format='html5')
html='''<!doctype html>
<html lang="fa" dir="rtl"><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>تعریف محصولات مستقل Carmilla</title>
<style>
*{box-sizing:border-box}body{margin:0;background:#f5f5f2;color:#20252c;font-family:Tahoma,Arial,sans-serif;line-height:2;text-align:right}
main{max-width:1100px;margin:24px auto;padding:36px;background:white;border:1px solid #dfe3e6;border-radius:10px}
h1{font-size:1.8rem;color:#18364e;line-height:1.6}h2{font-size:1.3rem;margin-top:2.1em;border-bottom:1px solid #dfe3e6;padding-bottom:.4em;color:#18364e}
p,li{font-size:1rem}a{color:#075d8c;text-underline-offset:3px}li{margin:.5em 0}strong{font-weight:700}
table{width:100%;border-collapse:collapse;margin:1.5em 0;font-size:.93rem}th,td{border:1px solid #d9dfe3;padding:10px;vertical-align:top;text-align:right}th{background:#eaf0f4}
pre{direction:ltr;text-align:left;unicode-bidi:isolate;background:#132939;color:#edf4f7;padding:20px;overflow:auto;border-radius:8px;line-height:1.6}code{font-family:Consolas,monospace;direction:ltr;unicode-bidi:isolate}p code,td code,li code{background:#eef1f4;padding:1px 4px;border-radius:3px}
blockquote{margin:1em 0;padding:4px 18px;border-right:4px solid #607f91;background:#f1f5f7}
@media(max-width:700px){main{margin:0;border:0;border-radius:0;padding:20px}table{display:block;overflow-x:auto}h1{font-size:1.5rem}}
@media print{body{background:white}main{border:0;margin:0;max-width:none}pre{white-space:pre-wrap}h2{break-after:avoid}tr{break-inside:avoid}}
</style></head><body><main>'''+body+'''</main></body></html>'''
dest=root/'docs/INDEPENDENT_PRODUCTS_SPEC_FA.html'
dest.write_text(html,encoding='utf-8')

class Check(HTMLParser):
    def __init__(self):super().__init__();self.rtl=False;self.tables=0;self.codes=0
    def handle_starttag(self,tag,attrs):
        a=dict(attrs)
        if tag=='html':self.rtl=a.get('dir')=='rtl' and a.get('lang')=='fa'
        if tag=='table':self.tables+=1
        if tag=='pre':self.codes+=1
check=Check();check.feed(html)
assert check.rtl and check.tables>=5 and check.codes==1
assert 'artifactKind' in html and 'clinic.booking' in html
print('HTML PASS: Persian RTL,',check.tables,'tables, JSON example rendered; output:',dest.name)
