// Screenshot the Carmilla WordPress theme pages across breakpoints.
import { chromium } from 'playwright';
import fs from 'node:fs';

const BASE = process.env.WP_BASE || 'http://localhost:8090';
const OUT = process.env.OUT_DIR || 'shots/wordpress';

// Route map mirrors the app's key screens. Plain permalinks (query strings)
// so it works on the PHP built-in server without rewrite rules. The seed step
// writes concrete IDs into shots/wp-routes.json which, if present, wins.
const DEFAULT_ROUTES = [
  { name: 'home', url: `${BASE}/` },
  { name: 'shop', url: `${BASE}/?post_type=product` },
  { name: 'blog', url: `${BASE}/?post_type=post` },
  { name: 'courses', url: `${BASE}/?post_type=cb_course` },
  { name: 'therapists', url: `${BASE}/?post_type=cb_therapist` },
  { name: 'psychtests', url: `${BASE}/?post_type=cb_psychtest` },
];
let ROUTES = DEFAULT_ROUTES;
try {
  if (fs.existsSync('shots/wp-routes.json')) {
    const seeded = JSON.parse(fs.readFileSync('shots/wp-routes.json', 'utf8'));
    ROUTES = seeded.map((r) => ({ name: r.name, url: r.url.startsWith('http') ? r.url : `${BASE}${r.url}` }));
  }
} catch (e) { console.log('routes.json read failed, using defaults:', e.message); }

const BREAKPOINTS = [
  { name: 'mobile', width: 390, height: 844 },
  { name: 'tablet', width: 768, height: 1024 },
  { name: 'desktop', width: 1280, height: 900 },
];

async function main() {
  fs.mkdirSync(OUT, { recursive: true });
  const browser = await chromium.launch();
  try {
    for (const bp of BREAKPOINTS) {
      const ctx = await browser.newContext({
        viewport: { width: bp.width, height: bp.height },
        deviceScaleFactor: 2,
        locale: 'fa-IR',
      });
      const page = await ctx.newPage();
      page.on('pageerror', (e) => console.log(`[${bp.name}] pageerror:`, e.message));
      for (const r of ROUTES) {
        try {
          await page.goto(r.url, { waitUntil: 'networkidle', timeout: 45000 });
          await page.waitForTimeout(1500);
          // Viewport shot: fixed elements (the floating bottom nav) sit where they
          // really do on a device — nav docked at the bottom.
          await page.screenshot({ path: `${OUT}/${bp.name}-${r.name}.png` });
          // Full-page shot for whole-page content review, with the fixed bottom
          // nav hidden so it doesn't smear near the top of the tall image.
          await page.addStyleTag({ content: '.bottom-nav{display:none!important}' });
          await page.screenshot({ path: `${OUT}/${bp.name}-${r.name}-full.png`, fullPage: true });
          console.log(`captured ${bp.name}/${r.name}`);
        } catch (e) {
          console.log(`FAILED ${bp.name}/${r.name}:`, e.message);
        }
      }
      await ctx.close();
    }
  } finally {
    await browser.close();
  }
  console.log('wordpress screenshots ->', OUT);
}
main().catch((e) => { console.error(e); process.exit(1); });
