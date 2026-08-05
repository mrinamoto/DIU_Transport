const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const { randomBytes } = require('node:crypto');
const { test, expect } = require('@playwright/test');
const AxeBuilder = require('@axe-core/playwright').default;
const { initializeDatabase } = require('../backend/src/db/connection');
const { seedDatabase } = require('../backend/src/db/seed');
const { createAdministrator } = require('../backend/src/scripts/createAdmin');
const { createConfig } = require('../backend/src/config');
const { createApp } = require('../backend/src/app');

const strongPassword = (label) => `${label}!Aa1${randomBytes(20).toString('hex')}`;
const ADMIN_PASSWORD = strongPassword('browser-admin');
const STUDENT_PASSWORD = strongPassword('browser-student');
let temporaryDirectory; let db; let server; let baseUrl;

test.describe.configure({ mode: 'serial' });

test.beforeAll(async () => {
  temporaryDirectory=fs.mkdtempSync(path.join(os.tmpdir(),'diu-transport-browser-'));
  const databasePath=path.join(temporaryDirectory,'browser.db');db=initializeDatabase(databasePath);seedDatabase(db);
  createAdministrator(db,{fullName:'Fictional Browser Administrator',email:'browser.admin@example.test',password:ADMIN_PASSWORD});
  const config=createConfig({env:{NODE_ENV:'test',PORT:'4173',CLIENT_ORIGIN:'http://127.0.0.1:4173',WEB_DATABASE_PATH:databasePath,AUTH_SECRET:randomBytes(48).toString('hex'),AUTH_EXPIRES_IN:'20m',PUBLIC_REGISTRATION_ROLES:'STUDENT,TEACHER',AUTH_RATE_LIMIT_WINDOW_MS:'60000',AUTH_RATE_LIMIT_MAX:'100',API_RATE_LIMIT_WINDOW_MS:'60000',API_RATE_LIMIT_MAX:'1000',TRUST_PROXY:'false',AUDIT_PAGE_SIZE_MAX:'100',BACKUP_DIRECTORY:path.join(temporaryDirectory,'backups'),EXPORT_DIRECTORY:path.join(temporaryDirectory,'exports')}});
  server=createApp({db,config}).listen(4173);await new Promise((resolve)=>server.once('listening',resolve));baseUrl='http://127.0.0.1:4173';
});

test.afterAll(async()=>{if(server)await new Promise((resolve)=>server.close(resolve));if(db?.open)db.close();if(temporaryDirectory?.startsWith(os.tmpdir()))fs.rmSync(temporaryDirectory,{recursive:true,force:true});});

test('Chromium student and administrator critical workflow',async({page,request})=>{
  await test.step('approved registration and administrator rejection',async()=>{
    await page.goto(baseUrl);await expect(page).toHaveTitle('DIU Transport Schedule');
    await page.getByRole('tab',{name:'Register'}).click();
    await page.locator('#register-name').fill('Fictional Browser Student');await page.locator('#register-email').fill('browser.student@example.test');await page.locator('#register-password').fill(STUDENT_PASSWORD);await page.getByRole('button',{name:'Create account'}).click();
    await expect(page.getByRole('heading',{name:'Upcoming transport'})).toBeVisible();await expect(page.getByRole('heading',{name:'Schedules'})).toBeVisible();
    const rejected=await request.post(`${baseUrl}/api/auth/register`,{data:{full_name:'Rejected Browser Admin',email:'rejected.browser@example.test',role:'ADMIN',password:strongPassword('rejected')}});expect(rejected.status()).toBe(400);
  });

  await test.step('student cannot manage catalogs and backend returns 403',async()=>{
    await expect(page.getByRole('navigation',{name:'Administrator workspace'})).toBeHidden();
    const login=await request.post(`${baseUrl}/api/auth/login`,{data:{email:'browser.student@example.test',password:STUDENT_PASSWORD}});const token=(await login.json()).token;
    const denied=await request.post(`${baseUrl}/api/buses`,{headers:{Authorization:`Bearer ${token}`},data:{bus_number:'BROWSER-DENIED',capacity:20}});expect(denied.status()).toBe(403);
    await page.getByRole('button',{name:'Log out'}).click();await expect(page.getByRole('heading',{name:'Welcome back'})).toBeVisible();
  });

  await test.step('administrator creates bus, driver, and route',async()=>{
    await page.locator('#login-email').fill('browser.admin@example.test');await page.locator('#login-password').fill(ADMIN_PASSWORD);await page.getByRole('button',{name:'Sign in'}).click();
    await expect(page.getByRole('navigation',{name:'Administrator workspace'})).toBeVisible();
    await page.getByRole('button',{name:'Buses'}).click();await page.getByLabel('Bus number').fill('browser-bus-901');await page.getByLabel('Capacity').fill('28');await page.getByRole('button',{name:'Save bus'}).click();await expect(page.getByText('BROWSER-BUS-901',{exact:true})).toBeVisible();
    await page.getByLabel('Bus number').fill('browser-bus-901');await page.getByLabel('Capacity').fill('28');await page.getByRole('button',{name:'Save bus'}).click();await expect(page.locator('#bus-message')).toContainText('already exists');
    await page.getByRole('button',{name:'Drivers'}).click();await page.locator('#driver-name').fill('Fictional Browser Driver');await page.getByLabel('Phone').fill('000-000-0901');await page.getByRole('button',{name:'Save driver'}).click();await expect(page.getByRole('cell',{name:'Fictional Browser Driver'})).toBeVisible();
    await page.getByRole('button',{name:'Routes'}).click();await page.getByLabel('Route name').fill('Browser Test Route');await page.getByLabel('Origin').fill('Browser Demo Terminal');await page.getByLabel('Destination').fill('Browser Demo Campus');await page.getByLabel('Ordered stops').fill('Browser Stop One\nBrowser Stop Two');await page.getByRole('button',{name:'Save route'}).click();await expect(page.getByRole('cell',{name:'Browser Test Route'})).toBeVisible();
  });

  await test.step('administrator creates schedule from active catalogs and sees deactivation conflict',async()=>{
    await page.getByRole('button',{name:'Schedules'}).click();await page.locator('#schedule-route').selectOption({label:'Browser Test Route'});await page.locator('#schedule-bus').selectOption({label:'BROWSER-BUS-901 · 28 seats'});await page.locator('#schedule-driver').selectOption({label:'Fictional Browser Driver'});await page.locator('#schedule-date').fill('2099-08-20');await page.locator('#schedule-departure').fill('08:00');await page.locator('#schedule-arrival').fill('09:00');await page.getByRole('button',{name:'Create schedule'}).click();await expect(page.getByRole('heading',{name:'Browser Test Route'})).toBeVisible();
    await page.getByRole('button',{name:'Buses'}).click();const row=page.getByRole('row').filter({hasText:'BROWSER-BUS-901'});page.once('dialog',(dialog)=>dialog.accept());await row.getByRole('button',{name:'Deactivate'}).click();await expect(page.locator('#workspace-message')).toContainText('future active schedule');
  });

  await test.step('audit view is visible and logout removes access',async()=>{
    await page.getByRole('button',{name:'Audit log'}).click();await expect(page.getByText('BUS_CREATE',{exact:true}).first()).toBeVisible();
    await page.getByRole('button',{name:'Log out'}).click();await expect(page.getByRole('navigation',{name:'Administrator workspace'})).toBeHidden();await expect(page.getByRole('heading',{name:'Welcome back'})).toBeVisible();
  });
});

test('automated accessibility checks pass at login and administrator workspace',async({page})=>{
  await page.goto(baseUrl);expect(await page.locator('html').getAttribute('lang')).toBe('en');await expect(page).toHaveTitle('DIU Transport Schedule');
  await page.keyboard.press('Tab');await expect(page.locator(':focus')).toBeVisible();
  let results=await new AxeBuilder({page}).analyze();expect(results.violations.filter((item)=>['serious','critical'].includes(item.impact))).toEqual([]);
  await page.locator('#login-email').fill('browser.admin@example.test');await page.locator('#login-password').fill(ADMIN_PASSWORD);await page.getByRole('button',{name:'Sign in'}).click();await expect(page.getByRole('navigation',{name:'Administrator workspace'})).toBeVisible();
  results=await new AxeBuilder({page}).analyze();expect(results.violations.filter((item)=>['serious','critical'].includes(item.impact))).toEqual([]);
});
