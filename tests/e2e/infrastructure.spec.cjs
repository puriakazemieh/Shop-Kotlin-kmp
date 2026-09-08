const { expect, test } = require('@playwright/test');

test('fixture matrix is explicitly non-production evidence', async ({}, testInfo) => {
  const metadata = testInfo.project.metadata;
  test.skip(metadata.evidenceClass !== 'fixture', 'Only fixture infrastructure is executable without a declared real backend.');

  expect(metadata.host).toBe('fake');
  expect(metadata.isProductionEvidence).toBe(false);
  expect(metadata.sku).toBeTruthy();
  expect(metadata.fingerprint).toBeTruthy();
});
