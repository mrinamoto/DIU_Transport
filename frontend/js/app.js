(() => {
  'use strict';

  const apiBase = document.querySelector('meta[name="api-base-url"]')?.content.replace(/\/$/, '') || '';
  const state = { token: null, user: null, schedules: [], activeCatalog: null, resources: {}, currentView: 'schedules' };
  const byId = (id) => document.getElementById(id);
  const escapeHtml = (value) => String(value ?? '').replace(/[&<>'"]/g, (character) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' }[character]));

  async function api(path, options = {}) {
    const headers = { Accept: 'application/json', ...(options.headers || {}) };
    if (options.body) headers['Content-Type'] = 'application/json';
    if (state.token) headers.Authorization = `Bearer ${state.token}`;
    const response = await fetch(`${apiBase}${path}`, { ...options, headers });
    const payload = await response.json().catch(() => ({ message: 'The server returned an unreadable response.' }));
    if (!response.ok) {
      if (response.status === 401 && state.token) clearSession('Your session is no longer valid. Please sign in again.');
      const error = new Error(payload.message || 'The request could not be completed.');
      error.status = response.status;
      error.requestId = payload.request_id;
      throw error;
    }
    return payload;
  }

  function setMessage(element, message = '') { element.textContent = message; }
  function showWorkspaceMessage(message, isError = false) {
    const notice = byId('workspace-message');
    notice.textContent = message;
    notice.classList.toggle('is-error', isError);
    notice.hidden = !message;
  }

  function switchAuthTab(name) {
    const login = name === 'login';
    byId('login-panel').hidden = !login;
    byId('register-panel').hidden = login;
    for (const [id, active] of [['login-tab', login], ['register-tab', !login]]) {
      byId(id).classList.toggle('is-active', active);
      byId(id).setAttribute('aria-selected', String(active));
    }
  }

  async function establishSession(payload) {
    state.token = payload.token;
    state.user = payload.user;
    const admin = state.user.role === 'ADMIN';
    byId('auth-view').hidden = true;
    byId('workspace-view').hidden = false;
    byId('logout-button').hidden = false;
    byId('admin-nav').hidden = !admin;
    byId('user-nav').hidden = admin;
    byId('schedule-admin').hidden = !admin;
    byId('account-summary').textContent = `${state.user.full_name} · ${state.user.role}`;
    showWorkspaceMessage('');
    showAdminView('schedules');
    await Promise.all([loadActiveCatalog(), loadSchedules()]);
  }

  function clearSession(message = '') {
    state.token = null; state.user = null; state.schedules = []; state.activeCatalog = null; state.resources = {};
    byId('workspace-view').hidden = true; byId('auth-view').hidden = false; byId('logout-button').hidden = true;
    byId('login-panel').reset(); resetScheduleForm(); ['bus', 'driver', 'route'].forEach(resetCatalogForm);
    switchAuthTab('login'); setMessage(byId('login-message'), message);
  }

  async function loadHealth() {
    const status = byId('health-status');
    try {
      const payload = await api('/api/health');
      status.textContent = payload.database === 'connected' ? 'Service online' : 'Database unavailable';
      status.className = `status-pill ${payload.database === 'connected' ? 'is-online' : 'is-offline'}`;
    } catch { status.textContent = 'Service unavailable'; status.className = 'status-pill is-offline'; }
  }

  async function loadActiveCatalog() {
    if (!state.token) return;
    const payload = await api('/api/catalog');
    state.activeCatalog = payload.data;
    const definitions = [
      ['schedule-route', state.activeCatalog.routes, (item) => item.route_name],
      ['schedule-bus', state.activeCatalog.buses, (item) => `${item.bus_number} · ${item.capacity} seats`],
      ['schedule-driver', state.activeCatalog.drivers, (item) => item.full_name],
    ];
    for (const [id, items, label] of definitions) {
      const select = byId(id); const selected = select.value;
      select.replaceChildren(new Option('Select…', ''));
      items.forEach((item) => select.add(new Option(label(item), item.id)));
      if ([...select.options].some((option) => option.value === selected)) select.value = selected;
    }
  }

  async function loadSchedules() {
    if (!state.token) return;
    byId('schedule-loading').hidden = false; byId('schedule-empty').hidden = true; byId('schedule-list').replaceChildren();
    try { state.schedules = (await api('/api/schedules')).data; renderSchedules(); }
    catch (error) { showWorkspaceMessage(error.message, true); }
    finally { byId('schedule-loading').hidden = true; }
  }

  function renderSchedules() {
    const list = byId('schedule-list'); list.replaceChildren();
    byId('schedule-count').textContent = `${state.schedules.length} ${state.schedules.length === 1 ? 'trip' : 'trips'}`;
    byId('schedule-empty').hidden = state.schedules.length > 0;
    for (const schedule of state.schedules) {
      const article = document.createElement('article');
      article.className = `schedule-card${schedule.status === 'CANCELLED' ? ' is-cancelled' : ''}`;
      const actions = state.user.role === 'ADMIN' && schedule.status === 'ACTIVE'
        ? `<div class="card-actions"><button class="button button-quiet" data-action="edit-schedule" data-id="${schedule.id}" type="button">Edit</button><button class="button button-danger" data-action="cancel-schedule" data-id="${schedule.id}" type="button">Cancel trip</button></div>` : '';
      article.innerHTML = `<div class="schedule-card-header"><div><h3>${escapeHtml(schedule.route.name)}</h3><p class="route-line">${escapeHtml(schedule.route.origin)} → ${escapeHtml(schedule.route.destination)}</p></div><span class="trip-status ${schedule.status.toLowerCase()}">${escapeHtml(schedule.status)}</span></div><div class="trip-time"><time>${escapeHtml(schedule.departure_time)}</time><span></span><time>${escapeHtml(schedule.arrival_time)}</time></div><dl class="trip-meta"><div><dt>Date</dt><dd>${escapeHtml(schedule.service_date)}</dd></div><div><dt>Bus</dt><dd>${escapeHtml(schedule.bus.number)}</dd></div><div><dt>Driver</dt><dd>${escapeHtml(schedule.driver.name)}</dd></div></dl>${schedule.notes ? `<p class="muted">${escapeHtml(schedule.notes)}</p>` : ''}${actions}`;
      list.append(article);
    }
  }

  function schedulePayload() {
    return { route_id:Number(byId('schedule-route').value), bus_id:Number(byId('schedule-bus').value), driver_id:Number(byId('schedule-driver').value), service_date:byId('schedule-date').value, departure_time:byId('schedule-departure').value, arrival_time:byId('schedule-arrival').value, trip_type:byId('schedule-trip-type').value, notes:byId('schedule-notes').value };
  }
  function resetScheduleForm() { byId('schedule-form').reset(); byId('schedule-id').value=''; byId('save-schedule-button').textContent='Create schedule'; byId('cancel-edit-button').hidden=true; setMessage(byId('schedule-form-message')); }
  function editSchedule(id) {
    const item = state.schedules.find((schedule) => schedule.id === id); if (!item) return;
    byId('schedule-id').value=item.id; byId('schedule-route').value=item.route.id; byId('schedule-bus').value=item.bus.id; byId('schedule-driver').value=item.driver.id; byId('schedule-date').value=item.service_date; byId('schedule-departure').value=item.departure_time; byId('schedule-arrival').value=item.arrival_time; byId('schedule-trip-type').value=item.trip_type; byId('schedule-notes').value=item.notes||''; byId('save-schedule-button').textContent='Save changes'; byId('cancel-edit-button').hidden=false; byId('schedule-admin').scrollIntoView({ block:'start' });
  }

  const catalogDefinitions = {
    bus: { endpoint:'buses', body:'bus-body', loading:'bus-loading', empty:'bus-empty', message:'bus-message' },
    driver: { endpoint:'drivers', body:'driver-body', loading:'driver-loading', empty:'driver-empty', message:'driver-message' },
    route: { endpoint:'routes', body:'route-body', loading:'route-loading', empty:'route-empty', message:'route-message' },
  };

  async function loadCatalogResource(type) {
    const def = catalogDefinitions[type]; byId(def.loading).hidden=false; byId(def.empty).hidden=true;
    try { state.resources[type]=(await api(`/api/${def.endpoint}`)).data; renderCatalog(type); }
    catch(error){ setMessage(byId(def.message),error.message); }
    finally{ byId(def.loading).hidden=true; }
  }

  function actionButtons(type, item) {
    const active=item.status==='ACTIVE';
    return `<div class="row-actions"><button class="button button-quiet" data-catalog="${type}" data-action="edit" data-id="${item.id}" type="button">Edit</button><button class="button ${active?'button-danger':'button-secondary'}" data-catalog="${type}" data-action="${active?'deactivate':'reactivate'}" data-id="${item.id}" type="button">${active?'Deactivate':'Reactivate'}</button></div>`;
  }

  function renderCatalog(type) {
    const items=state.resources[type]||[]; const def=catalogDefinitions[type]; const body=byId(def.body); body.replaceChildren(); byId(def.empty).hidden=items.length>0;
    for(const item of items){const row=document.createElement('tr');
      if(type==='bus') row.innerHTML=`<td>${escapeHtml(item.bus_number)}</td><td>${item.capacity}</td><td><span class="status-text ${item.status}">${escapeHtml(item.status)}</span></td><td>${actionButtons(type,item)}</td>`;
      if(type==='driver') row.innerHTML=`<td>${escapeHtml(item.full_name)}</td><td>${escapeHtml(item.phone)}</td><td><span class="status-text ${item.status}">${escapeHtml(item.status)}</span></td><td>${actionButtons(type,item)}</td>`;
      if(type==='route') row.innerHTML=`<td>${escapeHtml(item.route_name)}</td><td>${escapeHtml(item.origin)} → ${escapeHtml(item.destination)}</td><td>${(item.stops||[]).map((stop)=>escapeHtml(stop.stop_name)).join(', ')||'—'}</td><td><span class="status-text ${item.status}">${escapeHtml(item.status)}</span></td><td>${actionButtons(type,item)}</td>`;
      body.append(row);
    }
  }

  function catalogPayload(type) {
    if(type==='bus') return {bus_number:byId('bus-number').value,capacity:Number(byId('bus-capacity').value),status:byId('bus-status').value};
    if(type==='driver') return {full_name:byId('driver-name').value,phone:byId('driver-phone').value,status:byId('driver-status').value};
    return {route_name:byId('route-name').value,origin:byId('route-origin').value,destination:byId('route-destination').value,status:byId('route-status').value,stops:byId('route-stops').value.split(/\r?\n/).map((stop)=>stop.trim()).filter(Boolean)};
  }

  function resetCatalogForm(type) { const form=byId(`${type}-form`); if(!form)return; form.reset(); byId(`${type}-id`).value=''; byId(`${type}-cancel`).hidden=true; setMessage(byId(`${type}-message`)); }
  function editCatalog(type,id){const item=(state.resources[type]||[]).find((entry)=>entry.id===id);if(!item)return;byId(`${type}-id`).value=id;byId(`${type}-cancel`).hidden=false;
    if(type==='bus'){byId('bus-number').value=item.bus_number;byId('bus-capacity').value=item.capacity;byId('bus-status').value=item.status;}
    if(type==='driver'){byId('driver-name').value=item.full_name;byId('driver-phone').value=item.phone;byId('driver-status').value=item.status;}
    if(type==='route'){byId('route-name').value=item.route_name;byId('route-origin').value=item.origin;byId('route-destination').value=item.destination;byId('route-status').value=item.status;byId('route-stops').value=(item.stops||[]).map((stop)=>stop.stop_name).join('\n');}
    byId(`${type}-form`).scrollIntoView({block:'start'});
  }

  async function submitCatalog(type) { const def=catalogDefinitions[type]; const id=byId(`${type}-id`).value; setMessage(byId(def.message),id?'Saving changes…':'Creating record…');
    try{await api(id?`/api/${def.endpoint}/${id}`:`/api/${def.endpoint}`,{method:id?'PATCH':'POST',body:JSON.stringify(catalogPayload(type))});resetCatalogForm(type);showWorkspaceMessage(id?'Catalog record updated.':'Catalog record created.');await Promise.all([loadCatalogResource(type),loadActiveCatalog()]);}
    catch(error){setMessage(byId(def.message),error.message);}
  }

  async function changeCatalogStatus(type,id,action){const def=catalogDefinitions[type];const label=type==='bus'?'bus':type; if(action==='deactivate'&&!window.confirm(`Deactivate this ${label}? Future active schedule assignments will block the change.`))return;
    try{if(action==='deactivate')await api(`/api/${def.endpoint}/${id}`,{method:'DELETE'});else await api(`/api/${def.endpoint}/${id}`,{method:'PATCH',body:JSON.stringify({status:'ACTIVE'})});showWorkspaceMessage(`Catalog record ${action}d.`);await Promise.all([loadCatalogResource(type),loadActiveCatalog()]);}
    catch(error){showWorkspaceMessage(error.message,true);}
  }

  function showAdminView(view){state.currentView=view;document.querySelectorAll('.management-view').forEach((section)=>{section.hidden=section.id!==`${view}-view`;});document.querySelectorAll('.nav-button').forEach((button)=>button.classList.toggle('is-active',button.dataset.view===view));if(view==='schedules')loadSchedules();const resourceType={buses:'bus',drivers:'driver',routes:'route'}[view];if(resourceType)loadCatalogResource(resourceType);if(view==='audit')loadAudit();window.diuPhase4?.load(view);}

  async function loadAudit(){byId('audit-loading').hidden=false;byId('audit-empty').hidden=true;setMessage(byId('audit-message'));const params=new URLSearchParams();for(const [id,key] of [['audit-action','action'],['audit-entity','entity'],['audit-outcome','outcome'],['audit-actor','actor'],['audit-start','start'],['audit-end','end']]){if(byId(id).value)params.set(key,byId(id).value);}params.set('limit','50');
    try{const payload=await api(`/api/audit-logs?${params}`);const body=byId('audit-body');body.replaceChildren();byId('audit-empty').hidden=payload.data.length>0;for(const item of payload.data){const row=document.createElement('tr');row.innerHTML=`<td>${escapeHtml(item.created_at)}</td><td>${escapeHtml(item.action)}</td><td>${escapeHtml(item.entity_type)}${item.entity_id?` #${item.entity_id}`:''}</td><td>${escapeHtml(item.actor_name||'System')}</td><td><span class="status-text ${item.outcome}">${escapeHtml(item.outcome)}</span></td><td><code>${escapeHtml(item.request_id)}</code></td>`;body.append(row);}}
    catch(error){setMessage(byId('audit-message'),error.message);}finally{byId('audit-loading').hidden=true;}}

  byId('login-tab').addEventListener('click',()=>switchAuthTab('login'));byId('register-tab').addEventListener('click',()=>switchAuthTab('register'));byId('logout-button').addEventListener('click',()=>clearSession('You have been logged out.'));byId('cancel-edit-button').addEventListener('click',resetScheduleForm);
  byId('refresh-button').addEventListener('click',()=>showAdminView(state.currentView));byId('admin-nav').addEventListener('click',(event)=>{const button=event.target.closest('[data-view]');if(button)showAdminView(button.dataset.view);});
  byId('user-nav').addEventListener('click',(event)=>{const button=event.target.closest('[data-view]');if(button)showAdminView(button.dataset.view);});
  byId('login-panel').addEventListener('submit',async(event)=>{event.preventDefault();const form=event.currentTarget;setMessage(byId('login-message'),'Signing in…');try{const payload=await api('/api/auth/login',{method:'POST',body:JSON.stringify({email:byId('login-email').value,password:byId('login-password').value})});form.reset();await establishSession(payload);}catch(error){setMessage(byId('login-message'),error.message);}});
  byId('register-panel').addEventListener('submit',async(event)=>{event.preventDefault();const form=event.currentTarget;setMessage(byId('register-message'),'Creating account…');try{const payload=await api('/api/auth/register',{method:'POST',body:JSON.stringify({full_name:byId('register-name').value,email:byId('register-email').value,role:byId('register-role').value,password:byId('register-password').value})});form.reset();await establishSession(payload);}catch(error){setMessage(byId('register-message'),error.message);}});
  byId('schedule-form').addEventListener('submit',async(event)=>{event.preventDefault();const id=byId('schedule-id').value;setMessage(byId('schedule-form-message'),id?'Saving changes…':'Creating schedule…');try{await api(id?`/api/schedules/${id}`:'/api/schedules',{method:id?'PATCH':'POST',body:JSON.stringify(schedulePayload())});resetScheduleForm();showWorkspaceMessage(id?'Schedule updated.':'Schedule created.');await loadSchedules();}catch(error){setMessage(byId('schedule-form-message'),error.message);}});
  byId('schedule-list').addEventListener('click',async(event)=>{const button=event.target.closest('button[data-action]');if(!button)return;const id=Number(button.dataset.id);if(button.dataset.action==='edit-schedule')return editSchedule(id);if(button.dataset.action==='cancel-schedule'&&window.confirm('Cancel this schedule? The record will remain available to administrators.')){try{await api(`/api/schedules/${id}`,{method:'DELETE'});showWorkspaceMessage('Schedule cancelled.');await loadSchedules();}catch(error){showWorkspaceMessage(error.message,true);}}});
  for(const type of ['bus','driver','route']){byId(`${type}-form`).addEventListener('submit',(event)=>{event.preventDefault();submitCatalog(type);});byId(`${type}-cancel`).addEventListener('click',()=>resetCatalogForm(type));byId(`${type}-body`).addEventListener('click',(event)=>{const button=event.target.closest('button[data-catalog]');if(!button)return;const id=Number(button.dataset.id);if(button.dataset.action==='edit')editCatalog(type,id);else changeCatalogStatus(type,id,button.dataset.action);});}
  byId('audit-filter-form').addEventListener('submit',(event)=>{event.preventDefault();loadAudit();});
  window.diuApp={api,getUser:()=>state.user,getCatalog:()=>state.activeCatalog,escapeHtml,showWorkspaceMessage,showView:showAdminView};
  loadHealth();
})();
