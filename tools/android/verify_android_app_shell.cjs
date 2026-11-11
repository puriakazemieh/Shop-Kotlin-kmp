/**
 * Automated Verification Script for P02-ARCH-CODE-013:
 * Validates thin androidApp shell separation, multi-flavor build behavior,
 * applicationId stability, RTK SDK compliance, and package upgrade compatibility.
 */

const { execSync, spawnSync } = require('child_process');
const fs = require('fs');
const path = require('path');

const repoRoot = path.resolve(__dirname, '..', '..');
const androidAppDir = path.join(repoRoot, 'androidApp');
const androidGradlePath = path.join(androidAppDir, 'build.gradle.kts');
const conventionGradlePath = path.join(repoRoot, 'build-logic', 'convention', 'src', 'main', 'kotlin', 'carmilla.compose.application.gradle.kts');
const apkOutputDir = path.join(androidAppDir, 'build', 'outputs', 'apk');

const results = [];

function recordResult(testName, passed, details) {
  results.push({ testName, passed, details });
  console.log(`[${passed ? 'PASS' : 'FAIL'}] ${testName}`);
  if (details) {
    console.log(`       ${details}`);
  }
}

// 1. Architecture Boundary Verification
console.log('--- Test 1: Architecture Boundary Verification ---');
try {
  const conventionContent = fs.readFileSync(conventionGradlePath, 'utf-8');
  const androidAppContent = fs.readFileSync(androidGradlePath, 'utf-8');
  const manifestPath = path.join(androidAppDir, 'src', 'main', 'AndroidManifest.xml');
  const manifestExists = fs.existsSync(manifestPath);

  const composeAppIsLibrary = conventionContent.includes('com.android.kotlin.multiplatform.library') &&
                             !conventionContent.includes('com.android.application');
  const androidAppIsApplication = androidAppContent.includes('com.android.application');
  const androidAppDependsOnCompose = androidAppContent.includes('project(":composeApp")');

  const boundaryValid = composeAppIsLibrary && androidAppIsApplication && androidAppDependsOnCompose && manifestExists;

  if (boundaryValid) {
    recordResult('Architecture Boundary Verification', true,
      'composeApp configured as KMP library; androidApp configured as thin application shell depending on :composeApp');
  } else {
    recordResult('Architecture Boundary Verification', false,
      `Boundary check failed: composeAppIsLibrary=${composeAppIsLibrary}, androidAppIsApplication=${androidAppIsApplication}, dependsOnCompose=${androidAppDependsOnCompose}`);
  }
} catch (e) {
  recordResult('Architecture Boundary Verification', false, e.message);
}

// 2. Technical Standards & SDK Compliance
console.log('--- Test 2: Technical Standards & SDK Compliance ---');
try {
  const androidAppContent = fs.readFileSync(androidGradlePath, 'utf-8');
  const compileSdkMatch = androidAppContent.match(/compileSdk\s*=\s*(\d+)/);
  const minSdkMatch = androidAppContent.match(/minSdk\s*=\s*(\d+)/);
  const targetSdkMatch = androidAppContent.match(/targetSdk\s*=\s*(\d+)/);

  const compileSdk = compileSdkMatch ? parseInt(compileSdkMatch[1], 10) : null;
  const minSdk = minSdkMatch ? parseInt(minSdkMatch[1], 10) : null;
  const targetSdk = targetSdkMatch ? parseInt(targetSdkMatch[1], 10) : null;

  const sdkCompliant = compileSdk === 36 && minSdk === 24 && targetSdk === 36;
  if (sdkCompliant) {
    recordResult('SDK Compliance (RTK.md)', true, `compileSdk=${compileSdk}, minSdk=${minSdk}, targetSdk=${targetSdk} conform to RTK.md standards`);
  } else {
    recordResult('SDK Compliance (RTK.md)', false, `Non-compliant SDK versions: compileSdk=${compileSdk}, minSdk=${minSdk}, targetSdk=${targetSdk}`);
  }
} catch (e) {
  recordResult('SDK Compliance (RTK.md)', false, e.message);
}

// 3. Multi-Flavor Build & ApplicationId Stability Verification
console.log('--- Test 3: Multi-Flavor Build & ApplicationId Stability ---');
const expectedFlavors = [
  { flavor: 'carmila', expectedAppId: 'com.kazemieh.shop', expectedVersion: '1.0' },
  { flavor: 'academy', expectedAppId: 'com.kazemieh.shop.academy', expectedVersion: '1.0-academy' },
  { flavor: 'atris', expectedAppId: 'com.kazemieh.shop.atris', expectedVersion: '1.0-atris' },
  { flavor: 'chronos', expectedAppId: 'com.kazemieh.shop.chronos', expectedVersion: '1.0-chronos' },
  { flavor: 'psych', expectedAppId: 'com.kazemieh.shop.psych', expectedVersion: '1.0-psych' },
  { flavor: 'wp', expectedAppId: 'com.kazemieh.shop.wp', expectedVersion: '1.0-wp' },
];

let allFlavorsValid = true;
const flavorDetails = [];

for (const exp of expectedFlavors) {
  const metaPath = path.join(apkOutputDir, exp.flavor, 'debug', 'output-metadata.json');
  if (!fs.existsSync(metaPath)) {
    allFlavorsValid = false;
    flavorDetails.push(`${exp.flavor}: missing metadata at ${metaPath}`);
    continue;
  }
  const meta = JSON.parse(fs.readFileSync(metaPath, 'utf-8'));
  const appId = meta.applicationId;
  const version = meta.elements && meta.elements[0] ? meta.elements[0].versionName : null;
  const apkName = meta.elements && meta.elements[0] ? meta.elements[0].outputFile : null;
  const apkPath = path.join(apkOutputDir, exp.flavor, 'debug', apkName || '');
  const apkExists = fs.existsSync(apkPath);

  const match = appId === exp.expectedAppId && version === exp.expectedVersion && apkExists;
  if (!match) {
    allFlavorsValid = false;
    flavorDetails.push(`${exp.flavor}: mismatch (appId=${appId}, version=${version}, apkExists=${apkExists})`);
  } else {
    flavorDetails.push(`${exp.flavor}: appId=${appId}, version=${version}, apk=${apkName} (${(fs.statSync(apkPath).size / 1024 / 1024).toFixed(2)} MB)`);
  }
}

recordResult('Multi-Flavor Build & ApplicationId Stability', allFlavorsValid, flavorDetails.join(' | '));

// 4. Android Package Upgrade Compatibility Test
console.log('--- Test 4: Android Package Upgrade Compatibility Test ---');
const originalGradle = fs.readFileSync(androidGradlePath, 'utf-8');
try {
  // Simulate an upgrade: versionCode 1 -> 2, versionName 1.0 -> 1.1
  const upgradedGradle = originalGradle
    .replace('versionCode = 1', 'versionCode = 2')
    .replace('versionName = "1.0"', 'versionName = "1.1"');

  fs.writeFileSync(androidGradlePath, upgradedGradle, 'utf-8');

  const isWindows = process.platform === 'win32';
  const gradlewCmd = isWindows ? '.\\gradlew.bat' : './gradlew';
  const res = spawnSync(gradlewCmd, [':androidApp:assembleCarmilaDebug', '--no-daemon'], {
    cwd: repoRoot,
    encoding: 'utf-8',
    shell: true,
  });

  const carmilaMetaPath = path.join(apkOutputDir, 'carmila', 'debug', 'output-metadata.json');
  const upgradedMeta = JSON.parse(fs.readFileSync(carmilaMetaPath, 'utf-8'));
  const upgradedCode = upgradedMeta.elements[0].versionCode;
  const upgradedName = upgradedMeta.elements[0].versionName;
  const upgradedAppId = upgradedMeta.applicationId;

  const upgradePassed = res.status === 0 &&
                        upgradedCode === 2 &&
                        upgradedName === '1.1' &&
                        upgradedAppId === 'com.kazemieh.shop';

  if (upgradePassed) {
    recordResult('Android Package Upgrade Compatibility', true,
      `Upgraded APK built successfully with versionCode=${upgradedCode} (>1), versionName=${upgradedName}, matching applicationId=${upgradedAppId}`);
  } else {
    recordResult('Android Package Upgrade Compatibility', false,
      `Upgrade test failed: status=${res.status}, code=${upgradedCode}, name=${upgradedName}, appId=${upgradedAppId}`);
  }
} catch (e) {
  recordResult('Android Package Upgrade Compatibility', false, e.message);
} finally {
  fs.writeFileSync(androidGradlePath, originalGradle, 'utf-8');
  console.log('       [RESTORED] androidApp/build.gradle.kts restored to baseline version 1.0');
}

// Summary
console.log('\n========================================');
console.log('      ANDROID APP SHELL VERIFICATION    ');
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
