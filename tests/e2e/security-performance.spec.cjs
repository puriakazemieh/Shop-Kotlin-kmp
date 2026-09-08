const { expect, test } = require('@playwright/test');
const { startPwaTestServer } = require('./pwa-test-server.cjs');

let server;

test.beforeAll(async () => {
  server = await startPwaTestServer();
});

test.afterAll(async () => {
  await server?.close();
});

function fixtureOnly(testInfo) {
  test.skip(testInfo.project.metadata.evidenceClass !== 'fixture', 'Real environments require a declared endpoint and release identity.');
}

test('serves the configured CSP and Referrer-Policy', async ({ page }, testInfo) => {
  fixtureOnly(testInfo);

  const response = await page.goto(server.url, { waitUntil: 'networkidle' });
  expect(response.status()).toBe(200);
  expect(response.headers()['content-security-policy']).toContain("default-src 'self'");
  expect(response.headers()['referrer-policy']).toBe('strict-origin-when-cross-origin');
});

test('reports FCP and LCP entries above the 3000ms budget on a throttled connection', async ({ page }, testInfo) => {
  fixtureOnly(testInfo);

  const warnings = [];
  page.on('console', (message) => {
    if (message.type() === 'warning' && message.text().startsWith('Performance Budget Exceeded:')) {
      warnings.push(message.text());
    }
  });
  await page.addInitScript(() => {
    class SlowPerformanceObserver {
      constructor(callback) {
        this.callback = callback;
      }

      observe(options) {
        const entry = options.type === 'largest-contentful-paint'
          ? { name: 'largest-contentful-paint', entryType: 'largest-contentful-paint', startTime: 3001 }
          : { name: 'first-contentful-paint', entryType: 'paint', startTime: 3001 };
        queueMicrotask(() => this.callback({ getEntries: () => [entry] }));
      }
    }
    window.PerformanceObserver = SlowPerformanceObserver;
  });

  const client = await page.context().newCDPSession(page);
  await client.send('Network.emulateNetworkConditions', {
    offline: false,
    latency: 3000,
    downloadThroughput: 50 * 1024,
    uploadThroughput: 50 * 1024,
  });
  await page.goto(server.url, { waitUntil: 'networkidle' });

  await expect.poll(() => warnings.length).toBe(2);
  expect(warnings).toEqual(expect.arrayContaining([
    expect.stringContaining('first-contentful-paint 3001'),
    expect.stringContaining('largest-contentful-paint 3001'),
  ]));
});
