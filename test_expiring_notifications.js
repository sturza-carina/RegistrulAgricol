const { chromium } = require('playwright');
const path = require('path');

(async () => {
  console.log('Starting Playwright test for contract expiration notifications...');
  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext();
  const page = await context.newPage();

  try {
    // 1. Go to register page
    console.log('Navigating to register page...');
    await page.goto('http://localhost:4202/register');
    
    // Fill register form
    console.log('Filling in registration form for CNP 1900101120001...');
    await page.fill('input[name="nume"]', 'Popescu');
    await page.fill('input[name="prenume"]', 'Ion');
    await page.fill('input[name="cnp"]', '1900101120001');
    await page.fill('input[name="email"]', 'ion.popescu.expiring@gmail.com');
    await page.fill('input[name="parola"]', 'password123');
    await page.fill('input[name="telefon"]', '0722111111');
    
    // Select County and Local UAT
    await page.selectOption('select[name="judet"]', 'Cluj');
    await page.selectOption('select[name="localitate"]', 'Cluj-Napoca');
    
    await page.fill('input[name="strada"]', 'Libertatii');
    await page.fill('input[name="numar"]', '10');
    
    console.log('Submitting registration...');
    await page.click('button[type="submit"]');
    await page.waitForTimeout(3000); // Wait for API register response

    // 2. Go to login page
    console.log('Navigating to login page...');
    await page.goto('http://localhost:4202/login');
    
    // Fill login form
    console.log('Logging in as ion.popescu.expiring@gmail.com...');
    await page.fill('input[name="email"]', 'ion.popescu.expiring@gmail.com');
    await page.fill('input[name="parola"]', 'password123');
    await page.click('button[type="submit"]');
    await page.waitForTimeout(3000); // Wait for login

    // 3. Go to contul-meu
    console.log('Navigating to contul-meu dashboard settings page...');
    await page.goto('http://localhost:4202/contul-meu');
    
    // 4. Wait for WebSocket notification to be received
    console.log('Waiting for backend scheduler to trigger (up to 70 seconds)...');
    let notificationFound = false;
    for (let i = 0; i < 70; i++) {
      await page.waitForTimeout(1000);
      const isVisible = await page.isVisible('.alert-banner.warning');
      if (isVisible) {
        console.log(`[SUCCESS] Notification banner found visible after ${i + 1} seconds!`);
        const text = await page.locator('.alert-banner.warning').innerText();
        console.log(`Banner content: "${text.replace(/\n/g, ' ')}"`);
        notificationFound = true;
        break;
      }
    }
    
    if (!notificationFound) {
      console.log('[WARNING] Notification banner not found after 70 seconds. Taking screenshot anyway to inspect state.');
    }

    // Take screenshot
    const screenshotPath = path.join(__dirname, 'notifications_screenshot.png');
    await page.screenshot({ path: screenshotPath });
    console.log(`Saved screenshot to ${screenshotPath}`);

  } catch (error) {
    console.error('Test execution failed with error:', error);
  } finally {
    await browser.close();
    console.log('Browser closed. Test finished.');
  }
})();
