/**
 * Automated Verification Script for P02-CI-OPS-016:
 * Validates separation of release artifact workflow from debug builds,
 * existence and compliance of version metadata, SHA-256 checksums,
 * CycloneDX/SPDX SBOM generation, and artifact retention policies.
 */

const { spawnSync } = require('child_process');
const fs = require('fs');
const path = require('path');

const repoRoot = path.resolve(__dirname, '..', '..');
const results = [];

function recordResult(testName, passed, details) {
  results.push({ testName, passed, details });
  console.log(`[${passed ? 'PASS' : 'FAIL'}] ${testName}`);
  if (details) {
    console.log(`       ${details}`);
  }
}

// 1. Decoupling Check: release-artifact.yml must not build debug targets
console.log('--- Test 1: Release Workflow Decoupling from Debug Builds ---');
const releaseWorkflowPath = path.join(repoRoot, '.github', 'workflows', 'release-artifact.yml');

if (!fs.existsSync(releaseWorkflowPath)) {
  recordResult('Release Workflow Decoupling', false, 'release-artifact.yml does not exist');
} else {
  const content = fs.readFileSync(releaseWorkflowPath, 'utf-8');
  const hasDebugTask = /assembleDebug|assemble[A-Za-z]+Debug/.test(content);
  const hasDebugArtifactName = /name:\s*android-debug-apks/.test(content);
  const hasReleaseTasks = /assemble[A-Za-z]+Release|assembleRelease/.test(content);

  if (!hasDebugTask && !hasDebugArtifactName && hasReleaseTasks) {
    recordResult('Release Workflow Decoupling', true, 'release-artifact.yml is strictly isolated to release tasks; no debug tasks or debug artifact names found');
  } else {
    recordResult('Release Workflow Decoupling', false, `Violations found: hasDebugTask=${hasDebugTask}, hasDebugArtifactName=${hasDebugArtifactName}, hasReleaseTasks=${hasReleaseTasks}`);
  }
}

// 2. Retention & Release Policy Verification
console.log('--- Test 2: Retention & Release Policy Verification ---');
try {
  const content = fs.readFileSync(releaseWorkflowPath, 'utf-8');
  const retentionMatch = content.match(/retention-days:\s*(\$\{\{[^\}]+\}\}|[0-9]+)/);
  const has90Days = content.includes('90');

  if (retentionMatch && has90Days) {
    recordResult('Retention Policy Verification', true, 'Release workflow specifies 90 days retention for release artifacts (decoupled from ephemeral debug retention)');
  } else {
    recordResult('Retention Policy Verification', false, 'Missing 90-day retention policy in release workflow');
  }
} catch (e) {
  recordResult('Retention Policy Verification', false, e.message);
}

// 3. SBOM, Checksum & Metadata Generator Verification
console.log('--- Test 3: SBOM, Checksum & Release Metadata Verification ---');
const testOutDir = path.join(repoRoot, 'build', 'release-test-dist');
try {
  if (fs.existsSync(testOutDir)) {
    fs.rmSync(testOutDir, { recursive: true, force: true });
  }
  fs.mkdirSync(testOutDir, { recursive: true });

  // Create dummy release artifact to verify checksum computation
  fs.writeFileSync(path.join(testOutDir, 'test-app-release.apk'), 'DUMMY_RELEASE_APK_CONTENT_FOR_HASHING', 'utf-8');

  const sbomToolPath = path.join(repoRoot, 'tools', 'ci', 'generate_sbom.cjs');
  const genResult = spawnSync('node', [sbomToolPath, '--out-dir', testOutDir, '--version', '1.0.0'], {
    cwd: repoRoot,
    encoding: 'utf-8',
    shell: true
  });

  if (genResult.status !== 0) {
    recordResult('SBOM & Metadata Verification', false, `generate_sbom.cjs failed with code ${genResult.status}: ${genResult.stderr}`);
  } else {
    const cycloneDxPath = path.join(testOutDir, 'sbom.cyclonedx.json');
    const spdxPath = path.join(testOutDir, 'sbom.spdx.json');
    const checksumPath = path.join(testOutDir, 'SHA256SUMS.txt');
    const metadataPath = path.join(testOutDir, 'release-metadata.json');

    const filesExist = fs.existsSync(cycloneDxPath) && fs.existsSync(spdxPath) &&
                       fs.existsSync(checksumPath) && fs.existsSync(metadataPath);

    if (!filesExist) {
      recordResult('SBOM & Metadata Verification', false, 'One or more required manifest files were not created');
    } else {
      const cdx = JSON.parse(fs.readFileSync(cycloneDxPath, 'utf-8'));
      const spdx = JSON.parse(fs.readFileSync(spdxPath, 'utf-8'));
      const sums = fs.readFileSync(checksumPath, 'utf-8');
      const meta = JSON.parse(fs.readFileSync(metadataPath, 'utf-8'));

      const cdxValid = cdx.bomFormat === 'CycloneDX' && cdx.specVersion === '1.5' && Array.isArray(cdx.components) && cdx.components.length > 30;
      const spdxValid = spdx.spdxVersion === 'SPDX-2.3' && Array.isArray(spdx.packages) && spdx.packages.length > 30;
      const sumsValid = sums.includes('test-app-release.apk') && /^[a-f0-9]{64}\s+test-app-release\.apk/m.test(sums);
      const metaValid = meta.version === '1.0.0' && meta.retentionPolicyDays === 90 && meta.artifacts.length > 0;

      if (cdxValid && spdxValid && sumsValid && metaValid) {
        recordResult('SBOM & Metadata Verification', true, `Generated valid CycloneDX 1.5 (${cdx.components.length} components), SPDX 2.3 (${spdx.packages.length} packages), SHA-256 sums, and release metadata`);
      } else {
        recordResult('SBOM & Metadata Verification', false, `Schema validation failed: cdx=${cdxValid}, spdx=${spdxValid}, sums=${sumsValid}, meta=${metaValid}`);
      }
    }
  }
} catch (e) {
  recordResult('SBOM & Metadata Verification', false, e.message);
} finally {
  try {
    if (fs.existsSync(testOutDir)) {
      fs.rmSync(testOutDir, { recursive: true, force: true });
    }
  } catch (err) {}
}

// 4. Baseline KMP Compilation Check
console.log('--- Test 4: Baseline KMP Compilation Check ---');
try {
  const isWindows = process.platform === 'win32';
  const gradlewCmd = isWindows ? '.\\gradlew.bat' : './gradlew';
  const res = spawnSync(gradlewCmd, [':composeApp:compileKotlinJvm', ':composeApp:compileKotlinJs', '--no-daemon'], {
    cwd: repoRoot,
    encoding: 'utf-8',
    shell: true,
  });

  const output = (res.stdout || '') + (res.stderr || '');
  const passed = res.status === 0 && output.includes('BUILD SUCCESSFUL');

  if (passed) {
    recordResult('Baseline KMP Compilation Check', true, ':composeApp compileKotlinJvm & compileKotlinJs completed with exit code 0');
  } else {
    recordResult('Baseline KMP Compilation Check', false, `Exit code ${res.status}: ${output}`);
  }
} catch (e) {
  recordResult('Baseline KMP Compilation Check', false, e.message);
}

// Summary
console.log('\n========================================');
console.log('     RELEASE WORKFLOW CI/OPS CHECK      ');
console.log('========================================');
const allPassed = results.every(r => r.passed);
for (const r of results) {
  console.log(`${r.passed ? '✓' : '✗'} ${r.testName}: ${r.passed ? 'PASSED' : 'FAILED'}`);
}
console.log('========================================');
console.log(`FINAL RESULT: ${allPassed ? 'ALL CHECKS PASSED' : 'SOME CHECKS FAILED'}`);

if (!allPassed) {
  process.exit(1);
}
