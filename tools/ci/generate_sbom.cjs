/**
 * Software Bill of Materials (SBOM) & Release Metadata Generator
 * For P02-CI-OPS-016: Produces CycloneDX 1.5 and SPDX 2.3 compliant SBOMs,
 * SHA-256 checksum manifests, and release metadata.
 */

const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

const repoRoot = path.resolve(__dirname, '..', '..');

function parseTomlDependencies() {
  const tomlPath = path.join(repoRoot, 'gradle', 'libs.versions.toml');
  if (!fs.existsSync(tomlPath)) {
    throw new Error(`libs.versions.toml not found at: ${tomlPath}`);
  }

  const content = fs.readFileSync(tomlPath, 'utf-8');
  const versions = {};
  const libraries = [];

  let currentSection = '';
  const lines = content.split('\n');

  for (let rawLine of lines) {
    const line = rawLine.trim();
    if (!line || line.startsWith('#')) continue;

    if (line.startsWith('[') && line.endsWith(']')) {
      currentSection = line.slice(1, -1).trim();
      continue;
    }

    if (currentSection === 'versions') {
      const match = line.match(/^([a-zA-Z0-9_\-]+)\s*=\s*["']([^"']+)["']/);
      if (match) {
        versions[match[1]] = match[2];
      }
    } else if (currentSection === 'libraries') {
      // Handles: name = { group = "...", name = "...", version.ref = "..." } or module = "..."
      const nameMatch = line.match(/^([a-zA-Z0-9_\-]+)\s*=\s*\{(.+)\}/);
      if (nameMatch) {
        const libKey = nameMatch[1];
        const body = nameMatch[2];
        let group = '';
        let artifact = '';
        let version = '';

        const groupMatch = body.match(/group\s*=\s*["']([^"']+)["']/);
        const nameAttrMatch = body.match(/name\s*=\s*["']([^"']+)["']/);
        const moduleMatch = body.match(/module\s*=\s*["']([^"']+)["']/);
        const verRefMatch = body.match(/version\.ref\s*=\s*["']([^"']+)["']/);
        const verValMatch = body.match(/version\s*=\s*["']([^"']+)["']/);

        if (moduleMatch) {
          const parts = moduleMatch[1].split(':');
          group = parts[0] || '';
          artifact = parts[1] || '';
        } else {
          group = groupMatch ? groupMatch[1] : '';
          artifact = nameAttrMatch ? nameAttrMatch[1] : '';
        }

        if (verRefMatch && versions[verRefMatch[1]]) {
          version = versions[verRefMatch[1]];
        } else if (verValMatch) {
          version = verValMatch[1];
        }

        if (group && artifact) {
          libraries.push({
            name: libKey,
            group,
            artifact,
            version: version || 'unspecified',
            purl: `pkg:maven/${group}/${artifact}@${version || 'unspecified'}`
          });
        }
      }
    }
  }

  return { versions, libraries };
}

function getAppVersion() {
  const buildGradlePath = path.join(repoRoot, 'androidApp', 'build.gradle.kts');
  if (fs.existsSync(buildGradlePath)) {
    const content = fs.readFileSync(buildGradlePath, 'utf-8');
    const match = content.match(/versionName\s*=\s*["']([^"']+)["']/);
    if (match) return match[1];
  }
  return '1.0.0';
}

function generateCycloneDxJson(appVersion, libraries) {
  const timestamp = new Date().toISOString();
  const serialUuid = crypto.randomUUID();

  const components = libraries.map((lib, idx) => ({
    type: 'library',
    'bom-ref': `pkg:maven/${lib.group}/${lib.artifact}@${lib.version}#${idx}`,
    group: lib.group,
    name: lib.artifact,
    version: lib.version,
    purl: lib.purl,
    scope: 'required'
  }));

  // Add internal architectural root components
  components.unshift(
    {
      type: 'application',
      'bom-ref': `pkg:kmp/carmilla/composeApp@${appVersion}`,
      group: 'com.kazemieh.shop',
      name: 'composeApp',
      version: appVersion,
      scope: 'required'
    },
    {
      type: 'application',
      'bom-ref': `pkg:android/carmilla/androidApp@${appVersion}`,
      group: 'com.kazemieh.shop.app',
      name: 'androidApp',
      version: appVersion,
      scope: 'required'
    }
  );

  return {
    bomFormat: 'CycloneDX',
    specVersion: '1.5',
    serialNumber: `urn:uuid:${serialUuid}`,
    version: 1,
    metadata: {
      timestamp,
      tools: [
        {
          vendor: 'Carmilla CI/OPS',
          name: 'carmilla-sbom-generator',
          version: '1.0.0'
        }
      ],
      component: {
        type: 'application',
        'bom-ref': `pkg:generic/carmilla-shop@${appVersion}`,
        name: 'carmilla-shop',
        version: appVersion,
        description: 'Carmilla Multiplatform E-Commerce & Service Platform'
      }
    },
    components
  };
}

function generateSpdxJson(appVersion, libraries) {
  const timestamp = new Date().toISOString();
  const docId = `SPDXRef-DOCUMENT-${crypto.randomUUID()}`;

  const packages = [
    {
      SPDXID: 'SPDXRef-Package-CarmillaShop',
      name: 'carmilla-shop',
      versionInfo: appVersion,
      downloadLocation: 'NOASSERTION',
      filesAnalyzed: false,
      supplier: 'Organization: Carmilla',
      licenseConcluded: 'NOASSERTION',
      licenseDeclared: 'NOASSERTION',
      copyrightText: 'NOASSERTION'
    }
  ];

  libraries.forEach((lib, idx) => {
    packages.push({
      SPDXID: `SPDXRef-Package-${idx}-${lib.artifact.replace(/[^a-zA-Z0-9_\-]/g, '-')}`,
      name: `${lib.group}:${lib.artifact}`,
      versionInfo: lib.version,
      downloadLocation: 'NOASSERTION',
      filesAnalyzed: false,
      externalRefs: [
        {
          referenceCategory: 'PACKAGE-MANAGER',
          referenceType: 'purl',
          referenceLocator: lib.purl
        }
      ],
      licenseConcluded: 'NOASSERTION',
      licenseDeclared: 'NOASSERTION',
      copyrightText: 'NOASSERTION'
    });
  });

  return {
    spdxVersion: 'SPDX-2.3',
    dataLicense: 'CC0-1.0',
    SPDXID: docId,
    name: `carmilla-shop-${appVersion}`,
    documentNamespace: `https://github.com/puriakazemieh/Shop-Kotlin-kmp/spdxdocs/${docId}`,
    creationInfo: {
      created: timestamp,
      creators: ['Tool: carmilla-sbom-generator-1.0.0']
    },
    packages
  };
}

function calculateFileSha256(filePath) {
  const fileBuffer = fs.readFileSync(filePath);
  return crypto.createHash('sha256').update(fileBuffer).digest('hex');
}

function generateChecksums(dirPath) {
  if (!fs.existsSync(dirPath)) return [];
  const entries = fs.readdirSync(dirPath);
  const checksums = [];

  for (const entry of entries) {
    if (entry === 'SHA256SUMS.txt' || entry.endsWith('.sha256')) continue;
    const fullPath = path.join(dirPath, entry);
    const stat = fs.statSync(fullPath);
    if (stat.isFile()) {
      const sha256 = calculateFileSha256(fullPath);
      checksums.push({ fileName: entry, sha256, sizeBytes: stat.size });
    }
  }

  const checksumFileContent = checksums
    .map(c => `${c.sha256}  ${c.fileName}`)
    .join('\n') + '\n';

  fs.writeFileSync(path.join(dirPath, 'SHA256SUMS.txt'), checksumFileContent, 'utf-8');
  return checksums;
}

function generateReleaseMetadata(outDir, appVersion, checksums) {
  const metadata = {
    version: appVersion,
    generatedAt: new Date().toISOString(),
    retentionPolicyDays: 90,
    artifactType: 'release',
    artifacts: checksums.map(c => ({
      name: c.fileName,
      sha256: c.sha256,
      sizeBytes: c.sizeBytes
    }))
  };

  const metadataPath = path.join(outDir, 'release-metadata.json');
  fs.writeFileSync(metadataPath, JSON.stringify(metadata, null, 2), 'utf-8');
  return metadata;
}

function main() {
  const args = process.argv.slice(2);
  let outDir = path.join(repoRoot, 'dist');
  let appVersion = getAppVersion();

  for (let i = 0; i < args.length; i++) {
    if (args[i] === '--out-dir' && args[i + 1]) {
      outDir = path.resolve(args[++i]);
    } else if (args[i] === '--version' && args[i + 1]) {
      appVersion = args[++i];
    }
  }

  if (!fs.existsSync(outDir)) {
    fs.mkdirSync(outDir, { recursive: true });
  }

  const { libraries } = parseTomlDependencies();
  console.log(`Parsed ${libraries.length} dependencies from libs.versions.toml`);

  // 1. Generate CycloneDX SBOM
  const cycloneDx = generateCycloneDxJson(appVersion, libraries);
  const cycloneDxPath = path.join(outDir, 'sbom.cyclonedx.json');
  fs.writeFileSync(cycloneDxPath, JSON.stringify(cycloneDx, null, 2), 'utf-8');
  console.log(`[SBOM] Wrote CycloneDX 1.5 JSON to: ${cycloneDxPath}`);

  // 2. Generate SPDX SBOM
  const spdx = generateSpdxJson(appVersion, libraries);
  const spdxPath = path.join(outDir, 'sbom.spdx.json');
  fs.writeFileSync(spdxPath, JSON.stringify(spdx, null, 2), 'utf-8');
  console.log(`[SBOM] Wrote SPDX 2.3 JSON to: ${spdxPath}`);

  // 3. Generate Checksums
  const checksums = generateChecksums(outDir);
  console.log(`[CHECKSUM] Computed SHA-256 for ${checksums.length} artifacts in ${outDir}`);

  // 4. Generate Release Metadata
  const metadata = generateReleaseMetadata(outDir, appVersion, checksums);
  console.log(`[METADATA] Generated release-metadata.json (retention: ${metadata.retentionPolicyDays} days)`);
}

module.exports = {
  parseTomlDependencies,
  generateCycloneDxJson,
  generateSpdxJson,
  calculateFileSha256,
  generateChecksums,
  generateReleaseMetadata
};

if (require.main === module) {
  main();
}
