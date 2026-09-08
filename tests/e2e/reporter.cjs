const fs = require('node:fs');
const path = require('node:path');

module.exports = class CarmillaE2EReporter {
  constructor(options) {
    this.outputFile = options.outputFile;
    this.projects = new Map();
  }

  onBegin(config) {
    for (const project of config.projects) {
      this.projects.set(project.name, project.metadata);
    }
    fs.mkdirSync(path.dirname(this.outputFile), { recursive: true });
    fs.writeFileSync(this.outputFile, '');
  }

  onTestEnd(test, result) {
    const projectName = test.parent.project()?.name;
    const metadata = this.projects.get(projectName) || {};
    const outcome = result.status !== 'passed'
      ? result.status
      : metadata.isProductionEvidence
        ? 'PRODUCTION_PASS'
        : `${String(metadata.evidenceClass).toUpperCase()}_PASS_NOT_PRODUCTION`;

    const record = {
      project: projectName,
      host: metadata.host,
      profile: metadata.profile,
      evidenceClass: metadata.evidenceClass,
      sku: metadata.sku,
      fingerprint: metadata.fingerprint,
      outcome,
      retry: result.retry,
    };
    fs.appendFileSync(this.outputFile, `${JSON.stringify(record)}\n`);
  }
};
