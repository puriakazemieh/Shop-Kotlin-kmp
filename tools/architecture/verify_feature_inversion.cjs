/**
 * Automated Verification Script for P02-CORE-CODE-015B:
 * Validates Dependency Inversion across feature modules (Profile and Admin),
 * ensuring zero direct cross-feature project dependencies, clean source imports,
 * inverted navigation contracts, and successful Gradle architecture check execution.
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

// 1. Check build.gradle.kts dependencies of Profile and Admin modules
console.log('--- Test 1: Feature Build Dependencies Boundary Check ---');
const modulesToCheck = [
  path.join(repoRoot, 'feature', 'profile', 'build.gradle.kts'),
  path.join(repoRoot, 'feature', 'admin', 'products', 'build.gradle.kts'),
  path.join(repoRoot, 'feature', 'admin', 'academy', 'build.gradle.kts'),
  path.join(repoRoot, 'feature', 'admin', 'blog', 'build.gradle.kts'),
  path.join(repoRoot, 'feature', 'admin', 'clinic', 'build.gradle.kts'),
  path.join(repoRoot, 'feature', 'admin', 'options', 'build.gradle.kts'),
  path.join(repoRoot, 'feature', 'admin', 'orders', 'build.gradle.kts'),
  path.join(repoRoot, 'feature', 'admin', 'psychtest', 'build.gradle.kts'),
  path.join(repoRoot, 'feature', 'admin', 'wallet', 'build.gradle.kts'),
];

let allModulesClean = true;
const forbiddenDeps = [];

for (const buildFile of modulesToCheck) {
  if (!fs.existsSync(buildFile)) {
    allModulesClean = false;
    forbiddenDeps.push(`Missing: ${path.relative(repoRoot, buildFile)}`);
    continue;
  }
  const content = fs.readFileSync(buildFile, 'utf-8');
  const relPath = path.relative(repoRoot, buildFile);
  // Match project(":feature:...") or project(':feature:...')
  const matches = content.match(/project\(["']:feature:[^"']+["']\)/g);
  if (matches) {
    allModulesClean = false;
    forbiddenDeps.push(`${relPath} has forbidden dependencies: ${matches.join(', ')}`);
  }
}

if (allModulesClean) {
  recordResult('Feature Build Dependencies Boundary Check', true, 'Profile and all Admin submodules have 0 cross-feature dependencies in build.gradle.kts');
} else {
  recordResult('Feature Build Dependencies Boundary Check', false, forbiddenDeps.join('; '));
}

// 2. Check source code imports for cross-feature leakage
console.log('--- Test 2: Source Code Import Boundary Check ---');
function findKotlinFiles(dir) {
  let list = [];
  if (!fs.existsSync(dir)) return list;
  const entries = fs.readdirSync(dir, { withFileTypes: true });
  for (const entry of entries) {
    const fullPath = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      list = list.concat(findKotlinFiles(fullPath));
    } else if (entry.name.endsWith('.kt')) {
      list.push(fullPath);
    }
  }
  return list;
}

const profileSrcDir = path.join(repoRoot, 'feature', 'profile', 'src', 'commonMain', 'kotlin');
const adminSrcDir = path.join(repoRoot, 'feature', 'admin', 'src', 'commonMain', 'kotlin');
const adminProductsSrcDir = path.join(repoRoot, 'feature', 'admin', 'products', 'src', 'commonMain', 'kotlin');

const allSrcFiles = [
  ...findKotlinFiles(profileSrcDir),
  ...findKotlinFiles(adminSrcDir),
  ...findKotlinFiles(adminProductsSrcDir),
];

let importsClean = true;
const leakingImports = [];
// Forbidden imports: importing another feature package directly
const forbiddenImportPattern = /^import\s+com\.kazemieh\.(cart|catalog|details|clinic|academy|psychtest|main)\./m;

for (const f of allSrcFiles) {
  const content = fs.readFileSync(f, 'utf-8');
  const lines = content.split('\n');
  for (const line of lines) {
    if (forbiddenImportPattern.test(line.trim())) {
      importsClean = false;
      leakingImports.push(`${path.relative(repoRoot, f)}: ${line.trim()}`);
    }
  }
}

if (importsClean) {
  recordResult('Source Code Import Boundary Check', true, `Analyzed ${allSrcFiles.length} Kotlin files; 0 illegal cross-feature imports found`);
} else {
  recordResult('Source Code Import Boundary Check', false, `Found leaking imports: ${leakingImports.join('; ')}`);
}

// 3. Run Gradle architectureCheck
console.log('--- Test 3: Gradle architectureCheck Task ---');
try {
  const isWindows = process.platform === 'win32';
  const gradlewCmd = isWindows ? '.\\gradlew.bat' : './gradlew';
  const res = spawnSync(gradlewCmd, ['architectureCheck', '--no-daemon'], {
    cwd: repoRoot,
    encoding: 'utf-8',
    shell: true,
  });

  const output = (res.stdout || '') + (res.stderr || '');
  const passed = res.status === 0 && output.includes('Architecture check passed!') && output.includes('BUILD SUCCESSFUL');

  if (passed) {
    recordResult('Gradle architectureCheck Task', true, 'Gradle architectureCheck task executed and passed with exit code 0');
  } else {
    recordResult('Gradle architectureCheck Task', false, `Exit code ${res.status}: ${output}`);
  }
} catch (e) {
  recordResult('Gradle architectureCheck Task', false, e.message);
}

// 4. Run Kotlin JVM unit tests
console.log('--- Test 4: Kotlin Architecture Test Suite (:core:navigation:jvmTest) ---');
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
    recordResult('Kotlin Architecture Test Suite', true, 'FeatureDependencyInversionTest passed with exit code 0');
  } else {
    recordResult('Kotlin Architecture Test Suite', false, `Exit code ${res.status}: ${output}`);
  }
} catch (e) {
  recordResult('Kotlin Architecture Test Suite', false, e.message);
}

// Summary
console.log('\n========================================');
console.log('   FEATURE DEPENDENCY INVERSION CHECK   ');
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
