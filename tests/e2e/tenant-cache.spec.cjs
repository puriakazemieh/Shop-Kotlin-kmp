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

test.describe('PWA public-cache policy', () => {
  test('caches only app shell and allowlisted public catalog/content', async ({ page }, testInfo) => {
    fixtureOnly(testInfo);
    await page.goto(server.url, { waitUntil: 'networkidle' });
    await page.evaluate(() => navigator.serviceWorker.ready);
    await page.reload({ waitUntil: 'networkidle' });

    await page.evaluate(async () => {
      await fetch('/catalog/products');
      await fetch('/content/articles');
      await Promise.all([
        fetch('/auth/session'), fetch('/profile/me'), fetch('/address/list'), fetch('/favorite/list'),
        fetch('/support/tickets'), fetch('/order/1'), fetch('/payment/1'), fetch('/message/inbox'),
        fetch('/health'), fetch('/clinic/record'), fetch('/psych/result'),
      ]);
    });

    const cachedPaths = await page.evaluate(async () => {
      const names = await caches.keys();
      const entries = await Promise.all(names.map(async name => (await caches.open(name)).keys()));
      return entries.flat().map(request => new URL(request.url).pathname);
    });

    expect(cachedPaths).toEqual(expect.arrayContaining(['/catalog/products', '/content/articles']));
    expect(cachedPaths).not.toEqual(expect.arrayContaining([
      '/auth/session', '/profile/me', '/address/list', '/favorite/list', '/support/tickets',
      '/order/1', '/payment/1', '/message/inbox', '/health', '/clinic/record', '/psych/result',
    ]));
  });

  test('returns a safe 503 for offline sensitive writes without queueing them', async ({ page }, testInfo) => {
    fixtureOnly(testInfo);
    await page.goto(server.url, { waitUntil: 'networkidle' });
    await page.evaluate(() => navigator.serviceWorker.ready);
    await page.reload({ waitUntil: 'networkidle' });
    server.setNetworkAvailable(false);
    let result;
    try {
      result = await page.evaluate(async () => {
        const response = await fetch('/payment/checkout', { method: 'POST', body: '{}' });
        return { status: response.status, body: await response.json() };
      });
    } finally {
      server.setNetworkAvailable(true);
    }

    expect(result).toEqual({
      status: 503,
      body: { error: 'offline', message: 'عملیات در حالت آفلاین امکان‌پذیر نیست.' },
    });
  });
});
