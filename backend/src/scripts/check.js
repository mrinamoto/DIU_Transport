const fs = require('fs');
const path = require('path');
const { spawnSync } = require('child_process');
const { projectRoot } = require('../config');

function collectJavaScript(directory) {
  if (!fs.existsSync(directory)) return [];
  return fs.readdirSync(directory, { withFileTypes: true }).flatMap((entry) => {
    const target = path.join(directory, entry.name);
    if (entry.isDirectory()) return collectJavaScript(target);
    return entry.isFile() && entry.name.endsWith('.js') ? [target] : [];
  });
}

const files = [
  ...collectJavaScript(path.join(projectRoot, 'backend')),
  ...collectJavaScript(path.join(projectRoot, 'frontend')),
  ...collectJavaScript(path.join(projectRoot, 'browser-test')),
  path.join(projectRoot, 'playwright.config.js'),
];

let failures = 0;
for (const file of files) {
  const result = spawnSync(process.execPath, ['--check', file], { stdio: 'inherit' });
  if (result.status !== 0) failures += 1;
}

if (failures) {
  console.error(`${failures} JavaScript file(s) failed syntax checking.`);
  process.exitCode = 1;
} else {
  console.log(`${files.length} JavaScript file(s) passed syntax checking.`);
}
