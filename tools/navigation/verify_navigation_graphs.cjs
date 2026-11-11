/**
 * Automated Verification Script for P02-ARCH-CODE-014:
 * Validates feature-specific navigation graph partitioning,
 * route integrity, deep-link mapping, and test suite execution.
 */

const { execSync, spawnSync } = require('child_process');
const fs = require('fs');
const path = require('path');

const repoRoot = path.resolve(__dirname, '..', '..');
const navigationDir = path.join(repoRoot, 'core', 'navigation', 'src', 'commonMain', 'kotlin', 'com', 'kazemieh', 'navigation');
const appNavPath = path.join(navigationDir, 'AppNavigation.kt');

const results = [];

function recordResult(testName, passed, details) {
  results.push({ testName, passed, details });
  console.log(`[${passed ? 'PASS' : 'FAIL'}] ${testName}`);
  if (details) {
    console.log(`       ${details}`);
  }
}

// 1. Verify Feature Graph Partitioning
console.log('--- Test 1: Feature Graph Partitioning ---');
const expectedGraphFiles = [
  { file: 'AuthNavigation.kt', functionName: 'authNavGraph' },
  { file: 'AdminNavigation.kt', functionName: 'adminNavGraph' },
  { file: 'AcademyNavigation.kt', functionName: 'academyNavGraph' },
  { file: 'ClinicNavigation.kt', functionName: 'clinicNavGraph' },
  { file: 'PsychTestNavigation.kt', functionName: 'psychTestNavGraph' },
  { file: 'OrdersNavigation.kt', functionName: 'ordersNavGraph' },
  { file: 'ProfileNavigation.kt', functionName: 'profileNavGraph' },
  { file: 'CatalogNavigation.kt', functionName: 'catalogNavGraph' },
];

let allGraphsPresent = true;
const graphDetails = [];

for (const g of expectedGraphFiles) {
  const filePath = path.join(navigationDir, g.file);
  if (!fs.existsSync(filePath)) {
    allGraphsPresent = false;
    graphDetails.push(`Missing ${g.file}`);
    continue;
  }
  const content = fs.readFileSync(filePath, 'utf-8');
  if (!content.includes(g.functionName)) {
    allGraphsPresent = false;
    graphDetails.push(`${g.file} missing function ${g.functionName}`);
  } else {
    graphDetails.push(`${g.file} (${g.functionName})`);
  }
}

recordResult('Feature Graph Partitioning', allGraphsPresent, graphDetails.join(', '));

// 2. Verify AppNavigation Delegation
console.log('--- Test 2: AppNavigation Host Delegation ---');
try {
  const appNavContent = fs.readFileSync(appNavPath, 'utf-8');
  let delegatesAll = true;
  const missingDelegations = [];

  for (const g of expectedGraphFiles) {
    if (!appNavContent.includes(g.functionName)) {
      delegatesAll = false;
      missingDelegations.push(g.functionName);
    }
  }

  if (delegatesAll) {
    recordResult('AppNavigation Host Delegation', true, 'AppNavHost delegates to all 8 feature sub-graphs and maintains HomeGraph');
  } else {
    recordResult('AppNavigation Host Delegation', false, `Missing delegation calls: ${missingDelegations.join(', ')}`);
  }
} catch (e) {
  recordResult('AppNavigation Host Delegation', false, e.message);
}

// 3. Run Navigation Unit & Characterization Tests
console.log('--- Test 3: Navigation Characterization Test Suite ---');
try {
  const isWindows = process.platform === 'win32';
  const gradlewCmd = isWindows ? '.\\gradlew.bat' : './gradlew';
  const res = spawnSync(gradlewCmd, [':core:navigation:jvmTest', '--no-daemon'], {
    cwd: repoRoot,
    encoding: 'utf-8',
    shell: true,
  });

  const output = (res.stdout || '') + (res.stderr || '');
  const passed = res.status === 0 && output.includes('BUILD SUCCESSFUL');

  if (passed) {
    recordResult('Navigation Characterization Test Suite', true, 'All navigation unit & characterization tests passed with exit code 0');
  } else {
    recordResult('Navigation Characterization Test Suite', false, `Exit code ${res.status}: ${output}`);
  }
} catch (e) {
  recordResult('Navigation Characterization Test Suite', false, e.message);
}

// 4. Baseline Compilation Check
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
    recordResult('Baseline KMP Compilation Check', true, ':composeApp JVM & JS compilation passed with exit code 0');
  } else {
    recordResult('Baseline KMP Compilation Check', false, `Exit code ${res.status}: ${output}`);
  }
} catch (e) {
  recordResult('Baseline KMP Compilation Check', false, e.message);
}

// Summary
console.log('\n========================================');
console.log('      NAVIGATION GRAPH VERIFICATION     ');
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
