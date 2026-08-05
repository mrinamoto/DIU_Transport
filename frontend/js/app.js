(() => {
  'use strict';

  const apiBase = document.querySelector('meta[name="api-base-url"]')?.content.replace(/\/$/, '') || '';
  const state = { token: null, user: null, schedules: [], catalog: null };
  const byId = (id) => document.getElementById(id);

  async function api(path, options = {}) {
    const headers = { Accept: 'application/json', ...(options.headers || {}) };
    if (options.body) headers['Content-Type'] = 'application/json';
    if (state.token) headers.Authorization = `Bearer ${state.token}`;
    const response = await fetch(`${apiBase}${path}`, { ...options, headers });
    const payload = await response.json().catch(() => ({ message: 'The server returned an unreadable response.' }));
    if (!response.ok) {
      if (response.status === 401 && state.token) clearSession('Your session is no longer valid. Please sign in again.');
      throw new Error(payload.message || 'The request could not be completed.');
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
    byId('login-tab').classList.toggle('is-active', login);
    byId('register-tab').classList.toggle('is-active', !login);
    byId('login-tab').setAttribute('aria-selected', String(login));
    byId('register-tab').setAttribute('aria-selected', String(!login));
  }

  async function establishSession(payload) {
    state.token = payload.token;
    state.user = payload.user;
    byId('auth-view').hidden = true;
    byId('workspace-view').hidden = false;
    byId('logout-button').hidden = false;
    byId('admin-panel').hidden = state.user.role !== 'ADMIN';
    byId('account-summary').textContent = `${state.user.full_name} · ${state.user.role}`;
    showWorkspaceMessage('');
    await Promise.all([loadCatalog(), loadSchedules()]);
  }

  function clearSession(message = '') {
    state.token = null;
    state.user = null;
    state.schedules = [];
    state.catalog = null;
    byId('workspace-view').hidden = true;
    byId('auth-view').hidden = false;
    byId('logout-button').hidden = true;
    byId('login-panel').reset();
    resetScheduleForm();
    switchAuthTab('login');
    setMessage(byId('login-message'), message);
  }

  async function loadHealth() {
    const status = byId('health-status');
    try {
      const payload = await api('/api/health');
      status.textContent = payload.database === 'connected' ? 'Service online' : 'Database unavailable';
      status.className = `status-pill ${payload.database === 'connected' ? 'is-online' : 'is-offline'}`;
    } catch (error) {
      status.textContent = 'Service unavailable';
      status.className = 'status-pill is-offline';
    }
  }

  async function loadCatalog() {
    if (!state.token) return;
    const payload = await api('/api/catalog');
    state.catalog = payload.data;
    const definitions = [
      ['schedule-route', state.catalog.routes, (item) => item.route_name],
      ['schedule-bus', state.catalog.buses, (item) => `${item.bus_number} · ${item.capacity} seats`],
      ['schedule-driver', state.catalog.drivers, (item) => item.full_name],
    ];
    for (const [id, items, label] of definitions) {
      const select = byId(id);
      select.replaceChildren(new Option('Select…', ''));
      items.forEach((item) => select.add(new Option(label(item), item.id)));
    }
  }

  async function loadSchedules() {
    if (!state.token) return;
    byId('schedule-loading').hidden = false;
    byId('schedule-empty').hidden = true;
    byId('schedule-list').replaceChildren();
    try {
      const payload = await api('/api/schedules');
      state.schedules = payload.data;
      renderSchedules();
    } catch (error) {
      showWorkspaceMessage(error.message, true);
    } finally {
      byId('schedule-loading').hidden = true;
    }
  }

  function renderSchedules() {
    const list = byId('schedule-list');
    list.replaceChildren();
    byId('schedule-count').textContent = `${state.schedules.length} ${state.schedules.length === 1 ? 'trip' : 'trips'}`;
    byId('schedule-empty').hidden = state.schedules.length > 0;

    for (const schedule of state.schedules) {
      const article = document.createElement('article');
      article.className = `schedule-card${schedule.status === 'CANCELLED' ? ' is-cancelled' : ''}`;
      const actions = state.user.role === 'ADMIN' && schedule.status === 'ACTIVE'
        ? `<div class="card-actions"><button class="button button-quiet" data-action="edit" data-id="${schedule.id}" type="button">Edit</button><button class="button button-danger" data-action="delete" data-id="${schedule.id}" type="button">Cancel trip</button></div>` : '';
      article.innerHTML = `
        <div class="schedule-card-header"><div><h3>${escapeHtml(schedule.route.name)}</h3><p class="route-line">${escapeHtml(schedule.route.origin)} → ${escapeHtml(schedule.route.destination)}</p></div><span class="trip-status ${schedule.status.toLowerCase()}">${schedule.status}</span></div>
        <div class="trip-time"><time>${escapeHtml(schedule.departure_time)}</time><span></span><time>${escapeHtml(schedule.arrival_time)}</time></div>
        <dl class="trip-meta"><div><dt>Date</dt><dd>${escapeHtml(schedule.service_date)}</dd></div><div><dt>Bus</dt><dd>${escapeHtml(schedule.bus.number)}</dd></div><div><dt>Driver</dt><dd>${escapeHtml(schedule.driver.name)}</dd></div></dl>
        ${schedule.notes ? `<p class="muted">${escapeHtml(schedule.notes)}</p>` : ''}${actions}`;
      list.append(article);
    }
  }

  function escapeHtml(value) {
    return String(value ?? '').replace(/[&<>'"]/g, (character) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' }[character]));
  }

  function schedulePayload() {
    return {
      route_id: Number(byId('schedule-route').value),
      bus_id: Number(byId('schedule-bus').value),
      driver_id: Number(byId('schedule-driver').value),
      service_date: byId('schedule-date').value,
      departure_time: byId('schedule-departure').value,
      arrival_time: byId('schedule-arrival').value,
      trip_type: byId('schedule-trip-type').value,
      notes: byId('schedule-notes').value,
    };
  }

  function resetScheduleForm() {
    byId('schedule-form').reset();
    byId('schedule-id').value = '';
    byId('save-schedule-button').textContent = 'Create schedule';
    byId('cancel-edit-button').hidden = true;
    setMessage(byId('schedule-form-message'));
  }

  function editSchedule(id) {
    const schedule = state.schedules.find((item) => item.id === id);
    if (!schedule) return;
    byId('schedule-id').value = schedule.id;
    byId('schedule-route').value = schedule.route.id;
    byId('schedule-bus').value = schedule.bus.id;
    byId('schedule-driver').value = schedule.driver.id;
    byId('schedule-date').value = schedule.service_date;
    byId('schedule-departure').value = schedule.departure_time;
    byId('schedule-arrival').value = schedule.arrival_time;
    byId('schedule-trip-type').value = schedule.trip_type;
    byId('schedule-notes').value = schedule.notes || '';
    byId('save-schedule-button').textContent = 'Save changes';
    byId('cancel-edit-button').hidden = false;
    byId('admin-panel').scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  byId('login-tab').addEventListener('click', () => switchAuthTab('login'));
  byId('register-tab').addEventListener('click', () => switchAuthTab('register'));
  byId('logout-button').addEventListener('click', () => clearSession('You have been logged out.'));
  byId('refresh-button').addEventListener('click', loadSchedules);
  byId('cancel-edit-button').addEventListener('click', resetScheduleForm);

  byId('login-panel').addEventListener('submit', async (event) => {
    event.preventDefault();
    setMessage(byId('login-message'), 'Signing in…');
    try {
      const payload = await api('/api/auth/login', {
        method: 'POST', body: JSON.stringify({ email: byId('login-email').value, password: byId('login-password').value }),
      });
      event.currentTarget.reset();
      await establishSession(payload);
    } catch (error) {
      setMessage(byId('login-message'), error.message);
    }
  });

  byId('register-panel').addEventListener('submit', async (event) => {
    event.preventDefault();
    setMessage(byId('register-message'), 'Creating account…');
    try {
      const payload = await api('/api/auth/register', {
        method: 'POST', body: JSON.stringify({
          full_name: byId('register-name').value,
          email: byId('register-email').value,
          role: byId('register-role').value,
          password: byId('register-password').value,
        }),
      });
      event.currentTarget.reset();
      await establishSession(payload);
    } catch (error) {
      setMessage(byId('register-message'), error.message);
    }
  });

  byId('schedule-form').addEventListener('submit', async (event) => {
    event.preventDefault();
    const id = byId('schedule-id').value;
    setMessage(byId('schedule-form-message'), id ? 'Saving changes…' : 'Creating schedule…');
    try {
      await api(id ? `/api/schedules/${id}` : '/api/schedules', {
        method: id ? 'PATCH' : 'POST', body: JSON.stringify(schedulePayload()),
      });
      resetScheduleForm();
      showWorkspaceMessage(id ? 'Schedule updated.' : 'Schedule created.');
      await loadSchedules();
    } catch (error) {
      setMessage(byId('schedule-form-message'), error.message);
    }
  });

  byId('schedule-list').addEventListener('click', async (event) => {
    const button = event.target.closest('button[data-action]');
    if (!button) return;
    const id = Number(button.dataset.id);
    if (button.dataset.action === 'edit') return editSchedule(id);
    if (button.dataset.action === 'delete' && window.confirm('Cancel this schedule? It will remain in the audit trail.')) {
      try {
        await api(`/api/schedules/${id}`, { method: 'DELETE' });
        showWorkspaceMessage('Schedule cancelled.');
        await loadSchedules();
      } catch (error) {
        showWorkspaceMessage(error.message, true);
      }
    }
  });

  loadHealth();
})();
