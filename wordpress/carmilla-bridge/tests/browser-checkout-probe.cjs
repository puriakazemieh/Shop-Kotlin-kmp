const { chromium } = require('@playwright/test');

(async () => {
  if (process.env.CARMILLA_BROWSER_QA_ONLY !== 'yes') throw new Error('Disposable fixture required');
  const base = new URL(process.env.CARMILLA_BROWSER_QA_URL || '');
  if (base.protocol !== 'http:' || base.hostname !== '127.0.0.1' || Number(base.port) < 1024 || base.pathname !== '/') throw new Error('Loopback synthetic fixture required');
  const instance = process.env.CARMILLA_BROWSER_QA_INSTANCE || '';
  if (!/^[0-9a-f]{12}$/.test(instance)) throw new Error('Synthetic fixture instance required');
  const ids = ['PRODUCT_ID', 'CART_ID', 'CHECKOUT_ID'].map((name) => {
    const value = process.env[name];
    if (!/^[1-9][0-9]*$/.test(value || '')) throw new Error('Synthetic fixture identifiers required');
    return value;
  });
  const browser = await chromium.launch({ headless: true, executablePath: process.env.CARMILLA_BROWSER_QA_CHROME });
  try {
    const page = await browser.newPage();
    const home = await page.goto(`${base.origin}/`);
    if (!home || !home.ok() || !(await page.locator('body').innerText()).includes(`Synthetic P00 Browser QA ${instance}`)) throw new Error('Synthetic fixture identity mismatch');
    const product = await page.goto(`${base.origin}/?add-to-cart=${ids[0]}`);
    if (!product || !product.ok()) throw new Error('Synthetic product request failed');
    await page.goto(`${base.origin}/?page_id=${ids[1]}`);
    await page.getByText('Synthetic QA Product', { exact: false }).first().waitFor();
    if (await page.locator('input[name^="cart["][name$="][qty]"]').first().inputValue() !== '1') throw new Error('Synthetic cart quantity mismatch');
    console.log('PASS browser cart contains synthetic product and quantity');
    await page.goto(`${base.origin}/?page_id=${ids[2]}`);
    await page.locator('#billing_first_name').fill('Synthetic');
    await page.locator('#billing_last_name').fill('Customer');
    await page.locator('#billing_address_1').fill('123 Example Street');
    await page.locator('#billing_city').fill('Example City');
    await page.locator('#billing_state').selectOption('CA');
    await page.locator('#billing_postcode').fill('90210');
    await page.locator('#billing_phone').fill('2025550100');
    await page.locator('#billing_email').fill('checkout@example.test');
    const payment = page.locator('#payment_method_cod');
    if (!(await payment.isChecked())) await payment.check({ force: true });
    await page.locator('#place_order').click();
    await page.getByText('Thank you. Your order has been received.', { exact: false }).first().waitFor({ timeout: 30000 });
    console.log('PASS browser synthetic offline checkout reached order confirmation');
  } finally { await browser.close(); }
})().catch((error) => { console.error(`FAIL synthetic checkout: ${error.message.split('\n')[0]}`); process.exitCode = 1; });
