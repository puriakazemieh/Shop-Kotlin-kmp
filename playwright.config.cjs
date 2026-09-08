const { defineConfig, devices } = require('@playwright/test');

const hosts = [
  {
    id: 'fake',
    baseURL: process.env.E2E_FAKE_BASE_URL || 'http://127.0.0.1:4173',
    evidenceClass: 'fixture',
    isProductionEvidence: false,
  },
  {
    id: 'staging',
    baseURL: process.env.E2E_STAGING_BASE_URL || '',
    evidenceClass: 'staging',
    isProductionEvidence: false,
  },
  {
    id: 'production',
    baseURL: process.env.E2E_PRODUCTION_BASE_URL || '',
    evidenceClass: 'production',
    isProductionEvidence: true,
  },
];

const profiles = [
  { id: 'desktop', use: { ...devices['Desktop Chrome'] } },
  { id: 'mobile', use: { ...devices['Pixel 5'] } },
];

const sku = process.env.E2E_SKU;
const fingerprint = process.env.E2E_BUILD_FINGERPRINT;

const projects = hosts.flatMap((host) => profiles.map((profile) => ({
  name: `${host.id}-${profile.id}`,
  metadata: {
    host: host.id,
    profile: profile.id,
    evidenceClass: host.evidenceClass,
    isProductionEvidence: host.isProductionEvidence
      && Boolean(host.baseURL && sku && fingerprint),
    sku: sku || 'unconfigured',
    fingerprint: fingerprint || 'unconfigured',
  },
  use: {
    ...profile.use,
    baseURL: host.baseURL || undefined,
  },
})));

module.exports = defineConfig({
  testDir: './tests/e2e',
  fullyParallel: true,
  forbidOnly: Boolean(process.env.CI),
  retries: process.env.CI ? 2 : 0,
  reporter: [
    ['list'],
    ['./tests/e2e/reporter.cjs', { outputFile: 'test-results/e2e/results.jsonl' }],
  ],
  use: {
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },
  projects,
});
