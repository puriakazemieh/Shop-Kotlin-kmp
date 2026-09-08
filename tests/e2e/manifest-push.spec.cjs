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

async function stubPushDependencies(page, permission) {
  await page.addInitScript((selectedPermission) => {
    const registration = {
      addEventListener: () => {},
      pushManager: {
        subscribe: async () => ({ endpoint: 'https://push.invalid/subscription' }),
      },
    };
    Object.defineProperty(window, 'Notification', {
      configurable: true,
      value: { requestPermission: async () => selectedPermission },
    });
    Object.defineProperty(navigator, 'serviceWorker', {
      configurable: true,
      value: {
        ready: Promise.resolve(registration),
        register: async () => registration,
        addEventListener: () => {},
        controller: null,
      },
    });
  }, permission);
}

for (const [name, suffix, expectedId] of [
  ['WordPress', '', 'com.kazemieh.shop.wp'],
  ['Spring', '?api=http://127.0.0.1:8081', 'com.kazemieh.shop.spring'],
]) {
  test(`parses the standalone ${name} manifest`, async ({ page }, testInfo) => {
    fixtureOnly(testInfo);

    await page.goto(`${server.url}${suffix}`, { waitUntil: 'networkidle' });
    const manifestHref = await page.locator('#pwa-manifest').getAttribute('href');
    const response = await page.request.get(`${server.url}${manifestHref}`);
    const manifest = await response.json();

    expect(response.ok()).toBe(true);
    expect(manifest.id).toBe(expectedId);
    expect(manifest.display).toBe('standalone');
    expect(manifest.start_url).toBe('/');
    expect(manifest.scope).toBe('/');
    expect(manifest.icons).toEqual(expect.arrayContaining([
      expect.objectContaining({ sizes: '192x192', type: 'image/png' }),
      expect.objectContaining({ sizes: '512x512', type: 'image/png' }),
    ]));
  });
}

test('resolves a granted push permission without crashing', async ({ page }, testInfo) => {
  fixtureOnly(testInfo);
  await stubPushDependencies(page, 'granted');

  await page.goto(server.url, { waitUntil: 'networkidle' });
  const result = await page.evaluate(async () => {
    try {
      const subscription = await window.requestPushPermission();
      return { endpoint: subscription.endpoint };
    } catch (error) {
      return { error: String(error) };
    }
  });

  expect(result).toEqual({ endpoint: 'https://push.invalid/subscription' });
});

test('rejects a denied push permission without crashing', async ({ page }, testInfo) => {
  fixtureOnly(testInfo);
  await stubPushDependencies(page, 'denied');

  await page.goto(server.url, { waitUntil: 'networkidle' });
  const result = await page.evaluate(async () => {
    try {
      await window.requestPushPermission();
      return 'unexpected-success';
    } catch (error) {
      return String(error);
    }
  });

  expect(result).toBe('Permission denied');
});
