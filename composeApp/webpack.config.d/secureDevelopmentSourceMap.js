// Kotlin/JS uses an eval-based source map by default in development mode.
// The QA PWA keeps a strict CSP, so use an external source map instead.
if (config.mode === 'development') {
  config.devtool = 'source-map';
}
