const express = require('express');
const puppeteer = require('puppeteer');
const cors = require('cors');

const app = express();
app.use(cors());

const PORT = 3000;
const FRONTEND_URL = 'http://localhost:8881'; 

app.get('/screenshot', async (req, res) => {
  const { id, token } = req.query;
  if (!id) {
    return res.status(400).send('Missing id parameter');
  }

  console.log(`Generating screenshot for trip ${id}...`);
  let browser;
  try {
    browser = await puppeteer.launch({
      headless: 'new',
      ignoreHTTPSErrors: true, // 忽略 HTTPS 证书错误
      args: [
        '--no-sandbox', 
        '--disable-setuid-sandbox',
        '--ignore-certificate-errors'
      ]
    });
    const page = await browser.newPage();
    
    // 1. 注入 Token 到 localStorage (模拟登录状态)
    if (token) {
      await page.evaluateOnNewDocument((token) => {
        localStorage.setItem('token', JSON.stringify({ accessToken: token, refreshToken: '' }));
      }, token);
    }

    // 2. 监听浏览器控制台日志 (用于调试)
    page.on('console', msg => console.log('PAGE LOG:', msg.text()));
    page.on('pageerror', err => console.log('PAGE ERROR:', err.toString()));

    // Set viewport to a reasonable width for desktop view
    await page.setViewport({ width: 1280, height: 800, deviceScaleFactor: 2 });

    // Navigate to the share page
    await page.goto(`${FRONTEND_URL}/share?id=${id}`, { waitUntil: 'networkidle0', timeout: 60000 });

    // Wait for the map and data to be ready
    await page.waitForFunction('window.screenshotReady === true', { timeout: 30000 });

    // Find the container to screenshot
    const element = await page.$('#share-card');
    if (!element) {
      throw new Error('Content container not found');
    }

    const imageBuffer = await element.screenshot({ type: 'png' });

    res.set('Content-Type', 'image/png');
    res.send(imageBuffer);
    console.log(`Screenshot generated for trip ${id}`);

  } catch (error) {
    console.error('Screenshot error:', error);
    res.status(500).send('Failed to generate screenshot: ' + error.message);
  } finally {
    if (browser) {
      await browser.close();
    }
  }
});

app.listen(PORT, () => {
  console.log(`Screenshot service running on http://localhost:${PORT}`);
});