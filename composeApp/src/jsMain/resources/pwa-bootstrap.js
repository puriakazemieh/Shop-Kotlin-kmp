(() => {
  const parameters = new URLSearchParams(window.location.search);
  const isSpring = parameters.has('api');
  const explicitWordpressApi = parameters.get('wpApi');
  // The isolated local QA fixture serves the PWA from the same WordPress
  // origin. Keep that connection after hash-route navigation drops a query
  // string; shipped non-local bundles still use their generated API root.
  const isLocalWordpressFixture = !isSpring
    && (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1')
    && /^\/pwa(?:\/|$)/.test(window.location.pathname);
  const wordpressApiOverride = explicitWordpressApi
    || (isLocalWordpressFixture ? `${window.location.origin}/wp-json/carmilla/v1/` : null);
  // Keep each generated PWA self-contained when it is hosted in a subdirectory.
  const configUrl = isSpring ? './app-config-spring.json' : './app-config-wp.json';
  // Service-worker scripts are checked through their own URL. Version the
  // local bundle so a freshly deployed QA build can replace a worker that
  // otherwise keeps serving its previously cached HTML shell.
  const workerUrl = isSpring ? './sw-spring.js?v=11' : './sw-wp.js?v=11';

  document.getElementById('pwa-manifest').href = isSpring ? './manifest-spring.json' : './manifest-wp.json';

  fetch(configUrl)
    .then(response => {
      if (!response.ok) throw new Error('Unable to load app configuration.');
      return response.json();
    })
    .then(config => {
      // A WordPress bundle may be hosted on the same local QA site as its
      // REST API. The explicit override avoids shipping an Android-emulator
      // address to a desktop browser while keeping the selected SKU intact.
      if (!isSpring && wordpressApiOverride) {
        config.backend.apiRoot = wordpressApiOverride;
      }
      window.appConfig = config;
      const script = document.createElement('script');
      // Keep a deployed bundle from being confused with an older service-worker cache.
      script.src = './composeApp.js?v=11';
      document.body.appendChild(script);
    })
    .catch(error => console.error('Failed to load app config', error));

  if ('serviceWorker' in navigator) {
    let reloadingForWorker = false;
    navigator.serviceWorker.addEventListener('controllerchange', () => {
      if (!reloadingForWorker) {
        reloadingForWorker = true;
        window.location.reload();
      }
    });
    window.addEventListener('load', () => {
      navigator.serviceWorker.register(workerUrl).then(registration => registration.update()).catch(error => {
        console.error('Service worker registration failed', error);
      });
    });
  }
})();
