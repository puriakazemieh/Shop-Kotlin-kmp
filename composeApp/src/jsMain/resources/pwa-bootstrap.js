(() => {
  const parameters = new URLSearchParams(window.location.search);
  const isSpring = parameters.has('api') && !parameters.get('api')?.startsWith('http');
  const explicitApi = parameters.get('api');
  const explicitWordpressApi = parameters.get('wpApi');
  const isLocalhost = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1';
  // The isolated local QA fixture serves the PWA from the same WordPress
  // origin. Keep that connection after hash-route navigation drops a query
  // string; shipped non-local bundles still use their generated API root.
  const isLocalWordpressFixture = !isSpring
    && isLocalhost
    && /^\/pwa(?:\/|$)/.test(window.location.pathname);

  function isPrivateOrLocalHost(hostname) {
    if (!hostname) return true;
    if (hostname === 'localhost' || hostname === '127.0.0.1' || hostname === '[::1]' || hostname.endsWith('.localhost')) {
      return true;
    }
    const ipv4Match = /^(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})$/.exec(hostname);
    if (ipv4Match) {
      const octets = ipv4Match.slice(1, 5).map(Number);
      if (octets[0] === 10) return true;
      if (octets[0] === 172 && octets[1] >= 16 && octets[1] <= 31) return true;
      if (octets[0] === 192 && octets[1] === 168) return true;
      if (octets[0] === 127) return true;
      if (octets[0] === 169 && octets[1] === 254) return true;
      if (octets[0] === 0) return true;
    }
    if (hostname.endsWith('.local') || hostname.endsWith('.internal') || hostname.endsWith('.lan') || !hostname.includes('.')) {
      return true;
    }
    return false;
  }

  function validateUrlOverride(candidate, isRelease, allowedHosts) {
    if (!candidate || typeof candidate !== 'string') return null;
    try {
      const url = new URL(candidate, window.location.origin);
      if (isRelease) {
        // In release: HTTPS only, no localhost/loopback, no private IP, and host must match origin or allowedAuthHosts
        if (url.protocol !== 'https:') return null;
        if (isPrivateOrLocalHost(url.hostname)) return null;
        const isSameOrigin = url.origin === window.location.origin;
        const isAllowed = Array.isArray(allowedHosts) && allowedHosts.some(h => h.toLowerCase() === url.hostname.toLowerCase());
        if (!isSameOrigin && !isAllowed) return null;
      } else {
        // In local development
        const isLocal = isPrivateOrLocalHost(url.hostname);
        const isSameOrigin = url.origin === window.location.origin;
        const isAllowed = Array.isArray(allowedHosts) && allowedHosts.some(h => h.toLowerCase() === url.hostname.toLowerCase());
        if (!isLocal && !isSameOrigin && !isAllowed) return null;
      }
      return url.href;
    } catch (e) {
      return null;
    }
  }

  // Keep each generated PWA self-contained when it is hosted in a subdirectory.
  const configUrl = isSpring ? './app-config-spring.json' : './app-config-wp.json';
  // Service-worker scripts are checked through their own URL. Version the
  // local bundle so a freshly deployed QA build can replace a worker that
  // otherwise keeps serving its previously cached HTML shell.
  const workerUrl = isSpring ? './sw-spring.js?v=14' : './sw-wp.js?v=14';

  document.getElementById('pwa-manifest').href = isSpring ? './manifest-spring.json' : './manifest-wp.json';

  fetch(configUrl)
    .then(response => {
      if (!response.ok) throw new Error('Unable to load app configuration.');
      return response.json();
    })
    .then(config => {
      const isRelease = !isLocalhost;
      const allowedHosts = config.backend?.allowedAuthHosts || [];
      const candidateOverride = explicitWordpressApi || (explicitApi && (explicitApi.startsWith('http://') || explicitApi.startsWith('https://')) ? explicitApi : null);
      const validatedOverride = validateUrlOverride(candidateOverride, isRelease, allowedHosts);
      const effectiveWpOverride = validatedOverride || (isLocalWordpressFixture ? `${window.location.origin}/wp-json/carmilla/v1/` : null);

      if (!isSpring && effectiveWpOverride) {
        config.backend.apiRoot = effectiveWpOverride;
      }
      window.appConfig = config;
      const script = document.createElement('script');
      // Keep a deployed bundle from being confused with an older service-worker cache.
      script.src = './composeApp.js?v=14';
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
