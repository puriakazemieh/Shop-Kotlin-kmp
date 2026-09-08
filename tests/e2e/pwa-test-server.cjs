const fs = require('node:fs');
const http = require('node:http');
const path = require('node:path');

const indexPath = path.resolve(__dirname, '../../composeApp/src/webMain/resources/index.html');

function policyFromIndex(index, expression) {
  return index.match(expression)?.[1] || '';
}

function startPwaTestServer() {
  const index = fs.readFileSync(indexPath, 'utf8');
  const csp = policyFromIndex(index, /http-equiv="Content-Security-Policy" content="([^"]+)"/);
  const referrerPolicy = policyFromIndex(index, /<meta name="referrer" content="([^"]+)"/);

  const server = http.createServer((request, response) => {
    const pathname = new URL(request.url, 'http://localhost').pathname;
    if (pathname === '/' || pathname === '/index.html') {
      if (csp) response.setHeader('Content-Security-Policy', csp);
      if (referrerPolicy) response.setHeader('Referrer-Policy', referrerPolicy);
      response.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
      response.end(index);
      return;
    }
    if (pathname === '/app-config-wp.json') {
      response.writeHead(200, { 'Content-Type': 'application/json' });
      response.end('{}');
      return;
    }
    if (pathname === '/composeApp.js') {
      response.writeHead(200, { 'Content-Type': 'text/javascript' });
      response.end('window.__carmillaE2EFakeAppLoaded = true;');
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
        close: () => new Promise((done) => server.close(done)),
      });
    });
  });
}

module.exports = { startPwaTestServer };
