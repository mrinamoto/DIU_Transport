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
    await page.getByRole('button',{name:'Drivers'}).click();await page.locator('#driver-name').fill('Fictional Browser Driver');await page.locator('#driver-phone').fill('000-000-0901');await page.getByRole('button',{name:'Save driver'}).click();await expect(page.getByRole('cell',{name:'Fictional Browser Driver'})).toBeVisible();
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

test('Chromium Phase 4 operations and support workflow',async({page})=>{
  await page.goto(baseUrl);await page.locator('#login-email').fill('browser.admin@example.test');await page.locator('#login-password').fill(ADMIN_PASSWORD);await page.getByRole('button',{name:'Sign in'}).click();
  await test.step('administrator creates an employee and emergency contact',async()=>{
    await page.getByRole('button',{name:'Employees'}).click();await page.getByLabel('Employee code').fill('BROWSER-EMP-01');await page.locator('#employee-name').fill('Fictional Browser Officer');await page.locator('#employee-phone').fill('000-000-4901');await page.getByRole('button',{name:'Save employee'}).click();await expect(page.getByText('BROWSER-EMP-01')).toBeVisible();
    await page.getByRole('button',{name:'Emergency contacts'}).click();await page.getByLabel('Contact name').fill('Fictional Browser Hotline');await page.locator('#contact-role').selectOption('EMERGENCY_HOTLINE');await page.locator('#contact-phone').fill('000-000-4902');await page.getByLabel('Availability').fill('Synthetic 24-hour coverage');await page.getByRole('button',{name:'Add contact'}).click();await expect(page.getByRole('heading',{name:'Fictional Browser Hotline'})).toBeVisible();
  });
  await test.step('administrator publishes a notification and approves a special trip',async()=>{
    await page.getByRole('button',{name:'Notifications'}).click();await page.locator('#notification-title').fill('Browser Service Notice');await page.locator('#notification-message').fill('Synthetic browser workflow notification.');await page.getByRole('button',{name:'Create draft'}).click();const notice=page.locator('.record-card').filter({hasText:'Browser Service Notice'});await notice.getByRole('button',{name:'Publish'}).click();await expect(notice.getByText('PUBLISHED',{exact:true})).toBeVisible();
    await page.getByRole('button',{name:'Special trips'}).click();await page.locator('#special-title').fill('Browser Examination Shuttle');await page.getByLabel('Organizer').fill('Fictional Browser Office');await page.locator('#special-route').selectOption({label:'Browser Test Route'});await page.locator('#special-bus').selectOption({label:'BROWSER-BUS-901'});await page.locator('#special-driver').selectOption({label:'Fictional Browser Driver'});await page.locator('#special-date').fill('2099-08-21');await page.locator('#special-departure').fill('10:00');await page.locator('#special-arrival').fill('11:00');await page.getByRole('button',{name:'Create draft'}).click();const trip=page.locator('.record-card').filter({hasText:'Browser Examination Shuttle'});await trip.getByRole('button',{name:'Approve'}).click();await expect(trip.getByText('APPROVED',{exact:true})).toBeVisible();
  });
  await page.getByRole('button',{name:'Log out'}).click();await page.locator('#login-email').fill('browser.student@example.test');await page.locator('#login-password').fill(STUDENT_PASSWORD);await page.getByRole('button',{name:'Sign in'}).click();
  await test.step('student reads operations information and submits feedback',async()=>{
    await expect(page.getByRole('navigation',{name:'Transport support workspace'})).toBeVisible();await page.getByRole('button',{name:'Special trips'}).click();await expect(page.getByRole('heading',{name:'Browser Examination Shuttle'})).toBeVisible();await page.getByRole('button',{name:'Notifications'}).click();const generated=page.locator('.record-card').filter({hasText:'Special trip approved'});await expect(generated).toBeVisible();await generated.getByRole('button',{name:'Mark read'}).click();await expect(page.locator('#notification-unread')).toContainText('unread');await page.getByRole('button',{name:'Emergency contacts'}).click();await expect(page.getByRole('heading',{name:'Fictional Browser Hotline'})).toBeVisible();await page.getByRole('button',{name:'My feedback'}).click();await page.getByLabel('Subject').fill('Browser Workflow Feedback');await page.locator('#feedback-message-input').fill('Synthetic browser workflow feedback.');await page.getByRole('button',{name:'Submit feedback'}).click();await expect(page.getByRole('heading',{name:'Browser Workflow Feedback'})).toBeVisible();
  });
  await page.getByRole('button',{name:'Log out'}).click();await page.locator('#login-email').fill('browser.admin@example.test');await page.locator('#login-password').fill(ADMIN_PASSWORD);await page.getByRole('button',{name:'Sign in'}).click();await page.getByRole('button',{name:'Feedback'}).click();const feedback=page.locator('.record-card').filter({hasText:'Browser Workflow Feedback'});page.once('dialog',(dialog)=>dialog.accept('Synthetic browser review response.'));await feedback.getByRole('button',{name:'Start review'}).click();await expect(feedback.getByText('IN_REVIEW',{exact:true})).toBeVisible();page.once('dialog',(dialog)=>dialog.accept('Synthetic browser resolution response.'));await feedback.getByRole('button',{name:'Resolve'}).click();await expect(feedback.getByText('RESOLVED',{exact:true})).toBeVisible();await page.getByRole('button',{name:'Audit log'}).click();await expect(page.getByText('FEEDBACK_ADMIN_UPDATE',{exact:true}).first()).toBeVisible();await page.getByRole('button',{name:'Log out'}).click();await expect(page.getByRole('navigation',{name:'Administrator workspace'})).toBeHidden();
});

test('automated accessibility checks pass at login and administrator workspace',async({page})=>{
  await page.goto(baseUrl);expect(await page.locator('html').getAttribute('lang')).toBe('en');await expect(page).toHaveTitle('DIU Transport Schedule');
  await page.keyboard.press('Tab');await expect(page.locator(':focus')).toBeVisible();
  let results=await new AxeBuilder({page}).analyze();expect(results.violations.filter((item)=>['serious','critical'].includes(item.impact))).toEqual([]);
  await page.locator('#login-email').fill('browser.admin@example.test');await page.locator('#login-password').fill(ADMIN_PASSWORD);await page.getByRole('button',{name:'Sign in'}).click();await expect(page.getByRole('navigation',{name:'Administrator workspace'})).toBeVisible();
  for(const view of ['Employees','Special trips','Notifications','Emergency contacts','Feedback']){await page.getByRole('button',{name:view,exact:true}).click();results=await new AxeBuilder({page}).analyze();expect(results.violations.filter((item)=>['serious','critical'].includes(item.impact))).toEqual([]);}
});

test('mobile Chromium support workspace remains usable and accessible',async({page})=>{
  await page.setViewportSize({width:390,height:844});await page.goto(baseUrl);await page.locator('#login-email').fill('browser.student@example.test');await page.locator('#login-password').fill(STUDENT_PASSWORD);await page.getByRole('button',{name:'Sign in'}).click();await expect(page.getByRole('navigation',{name:'Transport support workspace'})).toBeVisible();await page.getByRole('button',{name:'Notifications'}).click();await expect(page.getByRole('heading',{name:'Notifications'})).toBeVisible();const overflow=await page.evaluate(()=>document.documentElement.scrollWidth>document.documentElement.clientWidth);expect(overflow).toBe(false);const results=await new AxeBuilder({page}).analyze();expect(results.violations.filter((item)=>['serious','critical'].includes(item.impact))).toEqual([]);
});

test('Phase 5 administrator identity, recovery, outbox, and metrics workflow',async({page})=>{
  await page.goto(baseUrl);await page.locator('#login-email').fill('browser.admin@example.test');await page.locator('#login-password').fill(ADMIN_PASSWORD);await page.getByRole('button',{name:'Sign in'}).click();
  await page.getByRole('button',{name:'Users'}).click();await expect(page.getByRole('heading',{name:'User administration'})).toBeVisible();const student=page.locator('.record-card').filter({hasText:'browser.student@example.test'});await expect(student).toBeVisible();await student.getByRole('button',{name:'Start recovery'}).click();const dialog=page.getByRole('dialog',{name:'One-time recovery token'});await expect(dialog).toBeVisible();await expect(page.locator('#recovery-output')).not.toBeEmpty();await page.keyboard.press('Escape');await expect(dialog).toBeHidden();await expect(student.getByRole('button',{name:'Start recovery'})).toBeFocused();await expect(page.locator('#recovery-output')).toBeEmpty();
  await page.getByRole('button',{name:'Outbox'}).click();await expect(page.getByRole('heading',{name:'Notification outbox'})).toBeVisible();await expect(page.getByText('NOOP').first()).toBeVisible();
  await page.getByRole('button',{name:'Operations'}).click();await expect(page.getByRole('heading',{name:'Operations metrics'})).toBeVisible();await expect(page.getByText(/Schema version 4/)).toBeVisible();
  for(const view of ['Users','Outbox','Operations']){await page.getByRole('button',{name:view,exact:true}).click();const results=await new AxeBuilder({page}).analyze();expect(results.violations.filter((item)=>['serious','critical'].includes(item.impact))).toEqual([]);}
});

test('Phase 5 keyboard, skip link, zoom, tablet, and forced-colors checks',async({page})=>{
  await page.setViewportSize({width:768,height:1024});await page.emulateMedia({forcedColors:'active'});await page.goto(baseUrl);await page.keyboard.press('Tab');await expect(page.getByRole('link',{name:'Skip to main workspace'})).toBeFocused();await page.keyboard.press('Tab');await expect(page.getByRole('link',{name:'DIU Transport Schedule home'})).toBeFocused();
  await page.evaluate(()=>{document.documentElement.style.zoom='200%';});const overflow=await page.evaluate(()=>document.documentElement.scrollWidth>document.documentElement.clientWidth);expect(overflow).toBe(false);await page.evaluate(()=>{document.documentElement.style.zoom='';});
  await page.locator('#login-email').fill('browser.admin@example.test');await page.locator('#login-password').fill(ADMIN_PASSWORD);await page.locator('#login-password').press('Enter');await expect(page.getByRole('navigation',{name:'Administrator workspace'})).toBeVisible();await page.getByRole('button',{name:'Users'}).focus();await page.keyboard.press('Enter');await expect(page.getByRole('heading',{name:'User administration'})).toBeVisible();
  const results=await new AxeBuilder({page}).analyze();expect(results.violations.filter((item)=>['serious','critical'].includes(item.impact))).toEqual([]);
});
