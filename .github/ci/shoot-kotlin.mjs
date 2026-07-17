// Screenshot the built Compose-Web (canvas) app across breakpoints.
// The app is a single-canvas SPA, so we capture the entry screen and a few
// wheel-scroll steps at mobile / tablet / desktop widths.
import { chromium } from 'playwright';
import http from 'node:http';
import fs from 'node:fs';
import path from 'node:path';

const DIST = process.env.DIST_DIR || 'composeApp/build/dist/js/productionExecutable';
const OUT = process.env.OUT_DIR || 'shots/kotlin';
const PORT = 8091;
// If set, the web app is pointed at this WordPress REST base (same data as the
// theme) via the ?api= param the web entrypoint reads.
const KWEB_API = process.env.KWEB_API || '';
const ENTRY = KWEB_API ? `/?api=${encodeURIComponent(KWEB_API)}` : '/';

const MIME = {
  '.html': 'text/html', '.js': 'text/javascript', '.mjs': 'text/javascript',
  '.css': 'text/css', '.wasm': 'application/wasm', '.json': 'application/json',
  '.ttf': 'font/ttf', '.woff': 'font/woff', '.woff2': 'font/woff2',
  '.png': 'image/png', '.jpg': 'image/jpeg', '.svg': 'image/svg+xml',
  '.map': 'application/json', '.ico': 'image/x-icon',
};

function serve(dir) {
  return http.createServer((req, res) => {
    let p = decodeURIComponent(req.url.split('?')[0]);
    if (p === '/' || p === '') p = '/index.html';
    let file = path.join(dir, p);
    if (!fs.existsSync(file) || fs.statSync(file).isDirectory()) {
      // SPA fallback
      file = path.join(dir, 'index.html');
    }
    fs.readFile(file, (err, data) => {
      if (err) { res.writeHead(404); res.end('not found'); return; }
      res.writeHead(200, { 'Content-Type': MIME[path.extname(file)] || 'application/octet-stream' });
      res.end(data);
    });
  });
}

const BREAKPOINTS = [
  { name: 'mobile', width: 390, height: 844 },
  { name: 'tablet', width: 768, height: 1024 },
  { name: 'desktop', width: 1280, height: 900 },
];

async function main() {
  if (!fs.existsSync(path.join(DIST, 'index.html'))) {
    console.error('DIST not found at', DIST, '- listing build/dist:');
    try { console.error(fs.readdirSync('composeApp/build/dist', { recursive: true })); } catch {}
    process.exit(2);
  }
  fs.mkdirSync(OUT, { recursive: true });
  const server = serve(DIST);
  await new Promise((r) => server.listen(PORT, r));
  const browser = await chromium.launch();
  try {
    for (const bp of BREAKPOINTS) {
      const ctx = await browser.newContext({
        viewport: { width: bp.width, height: bp.height },
        deviceScaleFactor: 2,
        locale: 'fa-IR',
      });
      const page = await ctx.newPage();
      page.on('console', (m) => console.log(`[${bp.name}] console:`, m.text()));
      page.on('pageerror', (e) => console.log(`[${bp.name}] pageerror:`, e.message));
      await page.goto(`http://localhost:${PORT}${ENTRY}`, { waitUntil: 'networkidle', timeout: 60000 });
      // Compose canvas needs time to boot & paint.
      await page.waitForTimeout(9000);
      await page.screenshot({ path: `${OUT}/${bp.name}-01-top.png` });
      // A few scroll steps to reveal below the fold (wheel on the canvas).
      for (let i = 2; i <= 4; i++) {
        await page.mouse.move(bp.width / 2, bp.height / 2);
        await page.mouse.wheel(0, bp.height * 0.85);
        await page.waitForTimeout(1200);
        await page.screenshot({ path: `${OUT}/${bp.name}-0${i}-scroll.png` });
      }
      await ctx.close();
      console.log(`captured ${bp.name}`);
    }
  } finally {
    await browser.close();
    server.close();
  }
  console.log('kotlin screenshots ->', OUT);
}
main().catch((e) => { console.error(e); process.exit(1); });
