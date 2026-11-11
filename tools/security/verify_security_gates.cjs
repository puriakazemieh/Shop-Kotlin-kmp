/**
 * Automated Verification Script for P02-CI-CODE-011:
 * Enforces Gradle dependency locking/verification and Gitleaks secret scanning.
 * Validates both positive paths and negative failure paths.
 */

const { execSync, spawnSync } = require('child_process');
const fs = require('fs');
const path = require('path');

const repoRoot = path.resolve(__dirname, '..', '..');
const verificationXmlPath = path.join(repoRoot, 'gradle', 'verification-metadata.xml');
const gitleaksConfigPath = path.join(repoRoot, '.github', '.gitleaks.toml');

const results = [];

function recordResult(testName, passed, details) {
  results.push({ testName, passed, details });
  console.log(`[${passed ? 'PASS' : 'FAIL'}] ${testName}`);
  if (details) {
    console.log(`       ${details}`);
  }
}

// 1. Positive Gradle Dependency Verification
console.log('--- Test 1: Positive Gradle Dependency Verification ---');
try {
  const isWindows = process.platform === 'win32';
  const gradlewCmd = isWindows ? '.\\gradlew.bat' : './gradlew';
  const res = spawnSync(gradlewCmd, ['dependencies', ':composeApp:dependencies', '--no-daemon'], {
    cwd: repoRoot,
    encoding: 'utf-8',
    shell: true,
    maxBuffer: 64 * 1024 * 1024,
  });

  if (res.status === 0) {
    recordResult('Positive Gradle Dependency Verification', true, 'Dependencies resolved and verified against verification-metadata.xml with exit code 0');
  } else {
    recordResult('Positive Gradle Dependency Verification', false, `Failed with exit code ${res.status}: ${res.stderr || res.stdout}`);
  }
} catch (e) {
  recordResult('Positive Gradle Dependency Verification', false, e.message);
}

// 2. Negative Gradle Dependency Verification (Tampered Checksum Rejection)
console.log('--- Test 2: Negative Gradle Dependency Verification (Tampered Checksum) ---');
const originalXml = fs.readFileSync(verificationXmlPath, 'utf-8');
const targetHash = '5ca175b38df331fd64155b35cd8cae1251fa9ee369709b36d42e0a288ccce3fd';
const tamperedHash = '0000000000000000000000000000000000000000000000000000000000000000';

if (!originalXml.includes(targetHash)) {
  recordResult('Negative Gradle Dependency Verification', false, `Target hash ${targetHash} not found in verification-metadata.xml`);
} else {
  try {
    const tamperedXml = originalXml.replace(targetHash, tamperedHash);
    fs.writeFileSync(verificationXmlPath, tamperedXml, 'utf-8');

    const isWindows = process.platform === 'win32';
    const gradlewCmd = isWindows ? '.\\gradlew.bat' : './gradlew';
    const res = spawnSync(gradlewCmd, [':core:common:compileKotlinJvm', '--no-daemon'], {
      cwd: repoRoot,
      encoding: 'utf-8',
      shell: true,
    });

    const output = (res.stdout || '') + (res.stderr || '');
    const detectedMismatch = output.includes('Dependency verification failed') || output.includes('failed verification');

    if (res.status !== 0 && detectedMismatch) {
      recordResult('Negative Gradle Dependency Verification', true, 'Tampered checksum caused build failure with exit code ' + res.status + ' and detected dependency verification failure');
    } else {
      recordResult('Negative Gradle Dependency Verification', false, `Expected failure, got exit code ${res.status}, detected: ${detectedMismatch}`);
    }
  } catch (e) {
    recordResult('Negative Gradle Dependency Verification', false, e.message);
  } finally {
    fs.writeFileSync(verificationXmlPath, originalXml, 'utf-8');
    console.log('       [RESTORED] gradle/verification-metadata.xml restored to original');
  }
}

// 3. Positive Secret Scan (Gitleaks)
console.log('--- Test 3: Positive Secret Scan (Gitleaks) ---');
try {
  const dockerMountPath = repoRoot.replace(/\\/g, '/');
  const res = spawnSync('docker', [
    'run', '--rm',
    '-v', `${dockerMountPath}:/repo`,
    'zricethezav/gitleaks:latest',
    'detect',
    '--source=/repo',
    '--config=/repo/.github/.gitleaks.toml'
  ], {
    encoding: 'utf-8',
    shell: true,
  });

  const output = (res.stdout || '') + (res.stderr || '');
  if (res.status === 0 && output.includes('no leaks found')) {
    recordResult('Positive Gitleaks Secret Scan', true, 'Clean repository scanned with exit code 0 and no leaks found');
  } else {
    recordResult('Positive Gitleaks Secret Scan', false, `Exit code ${res.status}, output: ${output}`);
  }
} catch (e) {
  recordResult('Positive Gitleaks Secret Scan', false, e.message);
}

// 4. Negative Secret Scan (Gitleaks with synthetic known secret fixture)
console.log('--- Test 4: Negative Secret Scan (Synthetic Secret Fixture) ---');
const fixtureDir = path.join(repoRoot, 'docs', 'evidence', 'fixtures');
const fixtureFile = path.join(fixtureDir, 'synthetic_secret.txt');

try {
  if (!fs.existsSync(fixtureDir)) {
    fs.mkdirSync(fixtureDir, { recursive: true });
  }
  fs.writeFileSync(fixtureFile, 'ghp_FAKESECRETKEYFORTESTING00000000000000\n', 'utf-8');

  const dockerFixturePath = fixtureDir.replace(/\\/g, '/');
  const res = spawnSync('docker', [
    'run', '--rm',
    '-v', `${dockerFixturePath}:/test`,
    'zricethezav/gitleaks:latest',
    'detect',
    '--source=/test',
    '--no-git'
  ], {
    encoding: 'utf-8',
    shell: true,
  });

  const output = (res.stdout || '') + (res.stderr || '');
  const detectedSecret = output.includes('leaks found: 1') || output.includes('github-pat');

  if (res.status !== 0 && detectedSecret) {
    recordResult('Negative Gitleaks Secret Scan', true, 'Known secret fixture caused Gitleaks failure with exit code ' + res.status + ' and detected github-pat leak');
  } else {
    recordResult('Negative Gitleaks Secret Scan', false, `Expected failure, got exit code ${res.status}, detected: ${detectedSecret}`);
  }
} catch (e) {
  recordResult('Negative Gitleaks Secret Scan', false, e.message);
} finally {
  if (fs.existsSync(fixtureFile)) {
    fs.unlinkSync(fixtureFile);
  }
  console.log('       [CLEANED] synthetic_secret.txt removed');
}

// Summary
console.log('\n========================================');
console.log('        SECURITY VERIFICATION SUMMARY   ');
console.log('========================================');
const allPassed = results.every(r => r.passed);
for (const r of results) {
  console.log(`${r.passed ? '✓' : '✗'} ${r.testName}: ${r.passed ? 'PASSED' : 'FAILED'}`);
}
console.log('========================================');
console.log(`FINAL RESULT: ${allPassed ? 'ALL TESTS PASSED' : 'SOME TESTS FAILED'}`);

if (!allPassed) {
  process.exit(1);
}
