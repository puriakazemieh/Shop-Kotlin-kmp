const fs = require('node:fs');
const http = require('node:http');
const path = require('node:path');

const indexPath = path.resolve(__dirname, '../../composeApp/src/webMain/resources/index.html');
const generatedPwaDir = path.resolve(__dirname, '../../composeApp/build/generated/pwa');

function policyFromIndex(index, expression) {
  return index.match(expression)?.[1] || '';
}

function startPwaTestServer() {
  const index = fs.readFileSync(indexPath, 'utf8');
  const manifests = new Map([
    ['/manifest-wp.json', fs.readFileSync(path.join(generatedPwaDir, 'manifest-wp.json'), 'utf8')],
    ['/manifest-spring.json', fs.readFileSync(path.join(generatedPwaDir, 'manifest-spring.json'), 'utf8')],
  ]);
  const csp = policyFromIndex(index, /http-equiv="Content-Security-Policy" content="([^"]+)"/);
  const referrerPolicy = policyFromIndex(index, /<meta name="referrer" content="([^"]+)"/);
  let networkAvailable = true;

  const server = http.createServer((request, response) => {
    if (!networkAvailable) {
      request.socket.destroy();
      return;
    }
    const pathname = new URL(request.url, 'http://localhost').pathname;
    if (pathname === '/' || pathname === '/index.html') {
      if (csp) response.setHeader('Content-Security-Policy', csp);
      if (referrerPolicy) response.setHeader('Referrer-Policy', referrerPolicy);
      response.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
      response.end(index);
      return;
    }
    if (manifests.has(pathname)) {
      response.writeHead(200, { 'Content-Type': 'application/manifest+json' });
      response.end(manifests.get(pathname));
      return;
    }
    if (pathname === '/sw-wp.js' || pathname === '/sw-spring.js') {
      response.writeHead(200, {
        'Content-Type': 'text/javascript; charset=utf-8',
        'Service-Worker-Allowed': '/',
        'Cache-Control': 'no-store',
      });
      response.end(fs.readFileSync(path.join(generatedPwaDir, pathname.slice(1)), 'utf8'));
      return;
    }
    if (pathname === '/app-config-wp.json' || pathname === '/app-config-spring.json') {
      response.writeHead(200, { 'Content-Type': 'application/json' });
      response.end('{}');
      return;
    }
    if (pathname === '/composeApp.js') {
      response.writeHead(200, { 'Content-Type': 'text/javascript' });
      response.end('window.__carmillaE2EFakeAppLoaded = true;');
      return;
    }
    if (pathname === '/catalog/products' || pathname === '/content/articles') {
      response.writeHead(200, { 'Content-Type': 'application/json; charset=utf-8' });
      response.end('{"public":true}');
      return;
    }
    // Browser navigations, including protected client routes, receive only the
    // public SPA shell. Their data is still fetched separately and is never
    // cached by the service worker policy.
    if ((request.headers.accept || '').includes('text/html')) {
      response.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
      response.end(index);
      return;
    }
    if (pathname.startsWith('/auth') || pathname.startsWith('/profile') || pathname.startsWith('/address')
      || pathname.startsWith('/favorite') || pathname.startsWith('/support') || pathname.startsWith('/order')
      || pathname.startsWith('/payment') || pathname.startsWith('/message') || pathname.startsWith('/health')
      || pathname.startsWith('/clinic') || pathname.startsWith('/psych')) {
      response.writeHead(200, { 'Content-Type': 'application/json; charset=utf-8', 'Cache-Control': 'no-store' });
      response.end('{"private":true}');
      return;
    }
    if (pathname.startsWith('/')) {
      response.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
      response.end(index);
      return;
    }
    response.writeHead(404);
    response.end('not found');
  });

  return new Promise((resolve) => {
    server.listen(0, '127.0.0.1', () => {
      const { port } = server.address();
      resolve({
        url: `http://127.0.0.1:${port}`,
        setNetworkAvailable: (available) => { networkAvailable = available; },
        close: () => new Promise((done) => server.close(done)),
      });
    });
  });
}

module.exports = { startPwaTestServer };
