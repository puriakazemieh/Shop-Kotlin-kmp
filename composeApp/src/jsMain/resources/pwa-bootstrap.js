(() => {
  const isSpring = window.location.search.includes('api=');
  const configUrl = isSpring ? '/app-config-spring.json' : '/app-config-wp.json';
  const workerUrl = isSpring ? '/sw-spring.js' : '/sw-wp.js';

  document.getElementById('pwa-manifest').href = isSpring ? '/manifest-spring.json' : '/manifest-wp.json';

  fetch(configUrl)
    .then(response => {
      if (!response.ok) throw new Error('Unable to load app configuration.');
      return response.json();
    })
    .then(config => {
      window.appConfig = config;
      const script = document.createElement('script');
      script.src = '/composeApp.js';
      document.body.appendChild(script);
    })
    .catch(error => console.error('Failed to load app config', error));

  if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
      navigator.serviceWorker.register(workerUrl).catch(error => {
        console.error('Service worker registration failed', error);
      });
    });
  }
})();
