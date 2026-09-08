const { spawnSync } = require('node:child_process');

const isWindows = process.platform === 'win32';
const command = isWindows ? 'cmd.exe' : './gradlew';
const args = isWindows
  ? ['/d', '/s', '/c', 'gradlew.bat :composeApp:generatePwaFiles --no-daemon']
  : [':composeApp:generatePwaFiles', '--no-daemon'];
const result = spawnSync(command, args, { stdio: 'inherit' });

if (result.error) throw result.error;
process.exit(result.status ?? 1);
