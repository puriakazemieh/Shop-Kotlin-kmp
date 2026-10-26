(() => {
  const parameters = new URLSearchParams(window.location.search);
  const isSpring = parameters.has('api');
  const wordpressApiOverride = parameters.get('wpApi');
  // Keep each generated PWA self-contained when it is hosted in a subdirectory.
  const configUrl = isSpring ? './app-config-spring.json' : './app-config-wp.json';
  const workerUrl = isSpring ? './sw-spring.js' : './sw-wp.js';

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
      script.src = './composeApp.js?v=8';
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
