/**
 * editor.js — Main canvas editor: rendering loop, selection, drag, properties panel.
 */

/* ── State ─────────────────────────────────────────────── */
const state = {
  nodes: [],
  selectedIdx: -1,
  canvasW: 256,
  canvasH: 192,
  zoom: 2,
  grid: true,
  name: 'untitled',
  dragging: false,
  dragOffX: 0, dragOffY: 0,
  dragType: null,  // 'move' | 'resize-br' | 'line-p2'
};

/* ── DOM refs ───────────────────────────────────────────── */
const canvas   = document.getElementById('canvas');
const ctx      = canvas.getContext('2d');
const propsBody = document.getElementById('props-body');

/* ── Initialise canvas ──────────────────────────────────── */
function resizeCanvas() {
  canvas.width  = state.canvasW * state.zoom;
  canvas.height = state.canvasH * state.zoom;
  canvas.style.width  = canvas.width + 'px';
  canvas.style.height = canvas.height + 'px';
  render();
}

/* ── Render loop ────────────────────────────────────────── */
function render() {
  const z = state.zoom;
  ctx.clearRect(0, 0, canvas.width, canvas.height);

  // Canvas background (dark)
  ctx.fillStyle = '#111520';
  ctx.fillRect(0, 0, canvas.width, canvas.height);

  // Grid
  if (state.grid) {
    ctx.save();
    ctx.strokeStyle = 'rgba(255,255,255,0.04)';
    ctx.lineWidth = 1;
    const step = 8 * z;
    for (let x = 0; x <= canvas.width; x += step) {
      ctx.beginPath(); ctx.moveTo(x, 0); ctx.lineTo(x, canvas.height); ctx.stroke();
    }
    for (let y = 0; y <= canvas.height; y += step) {
      ctx.beginPath(); ctx.moveTo(0, y); ctx.lineTo(canvas.width, y); ctx.stroke();
    }
    ctx.restore();
  }

  // Nodes in order (bottom → top)
  state.nodes.forEach((n, i) => {
    const def = NodeDefs[n.type];
    if (def) def.render(ctx, n, z);

    // Selection highlight
    if (i === state.selectedIdx) drawSelection(n);
  });
}

function drawSelection(n) {
  const z = state.zoom;
  ctx.save();
  ctx.strokeStyle = '#4f8ef7';
  ctx.lineWidth = 2;
  ctx.setLineDash([4, 3]);

  if (n.type === 'text') {
    ctx.strokeRect(n.boxX * z - 1, n.boxY * z - 1, n.width * z + 2, n.height * z + 2);
    drawHandle((n.boxX + n.width) * z, (n.boxY + n.height) * z);
  } else if (n.type === 'background') {
    ctx.strokeRect(n.x * z - 1, n.y * z - 1, n.width * z + 2, n.height * z + 2);
    drawHandle((n.x + n.width) * z, (n.y + n.height) * z);
  } else if (n.type === 'block') {
    ctx.strokeRect(n.x * z - 1, n.y * z - 1, n.width * z + 2, n.height * z + 2);
    drawHandle((n.x + n.width) * z, (n.y + n.height) * z);
  } else if (n.type === 'item') {
    const sz = 16 * n.scale * z;
    ctx.strokeRect(n.x * z - sz/2 - 2, n.y * z - sz/2 - 2, sz + 4, sz + 4);
  } else if (n.type === 'line') {
    ctx.setLineDash([]);
    ctx.beginPath();
    ctx.moveTo(n.x1 * z, n.y1 * z);
    ctx.lineTo(n.x2 * z, n.y2 * z);
    ctx.stroke();
    drawHandle(n.x1 * z, n.y1 * z);
    drawHandle(n.x2 * z, n.y2 * z);
  }
  ctx.restore();
}

function drawHandle(px, py) {
  ctx.setLineDash([]);
  ctx.fillStyle = '#4f8ef7';
  ctx.fillRect(px - 4, py - 4, 8, 8);
  ctx.strokeStyle = '#fff';
  ctx.lineWidth = 1;
  ctx.strokeRect(px - 4, py - 4, 8, 8);
}

/* ── Hit testing ────────────────────────────────────────── */
function hitTest(mx, my) {
  const z = state.zoom;
  // Test in reverse order (top node first)
  for (let i = state.nodes.length - 1; i >= 0; i--) {
    const n = state.nodes[i];
    if (hitsNode(n, mx / z, my / z)) return i;
  }
  return -1;
}

function hitsNode(n, px, py) {
  const slop = 4;
  switch (n.type) {
    case 'background':
      return px >= n.x - slop && px <= n.x + n.width + slop &&
             py >= n.y - slop && py <= n.y + n.height + slop;
    case 'text':
      return px >= n.boxX - slop && px <= n.boxX + n.width + slop &&
             py >= n.boxY - slop && py <= n.boxY + n.height + slop;
    case 'block':
      return px >= n.x - slop && px <= n.x + n.width + slop &&
             py >= n.y - slop && py <= n.y + n.height + slop;
    case 'item': {
      const hs = 8 * n.scale;
      return px >= n.x - hs - slop && px <= n.x + hs + slop &&
             py >= n.y - hs - slop && py <= n.y + hs + slop;
    }
    case 'line': {
      // Distance from point to line segment
      const dx = n.x2 - n.x1, dy = n.y2 - n.y1;
      const len2 = dx*dx + dy*dy;
      if (len2 === 0) return dist(px, py, n.x1, n.y1) <= slop;
      let t = ((px - n.x1)*dx + (py - n.y1)*dy) / len2;
      t = Math.max(0, Math.min(1, t));
      return dist(px, py, n.x1 + t*dx, n.y1 + t*dy) <= (n.thickness / 2 + slop);
    }
    default: return false;
  }
}

function dist(ax, ay, bx, by) { return Math.sqrt((ax-bx)**2 + (ay-by)**2); }

/** Check if click hits the resize handle (bottom-right corner) */
function hitsResizeHandle(n, mx, my) {
  const z = state.zoom;
  let hx, hy;
  if (n.type === 'background' || n.type === 'block') { hx = (n.x + n.width) * z; hy = (n.y + n.height) * z; }
  else if (n.type === 'text') { hx = (n.boxX + n.width) * z; hy = (n.boxY + n.height) * z; }
  else return false;
  return Math.abs(mx - hx) <= 6 && Math.abs(my - hy) <= 6;
}

/** Check if click hits line endpoint 2 */
function hitsLineP2(n, mx, my) {
  const z = state.zoom;
  return n.type === 'line' && Math.abs(mx - n.x2 * z) <= 7 && Math.abs(my - n.y2 * z) <= 7;
}

/* ── Mouse events ───────────────────────────────────────── */
canvas.addEventListener('mousedown', e => {
  const r = canvas.getBoundingClientRect();
  const mx = e.clientX - r.left, my = e.clientY - r.top;
  const z = state.zoom;

  const sel = state.nodes[state.selectedIdx];

  if (sel) {
    if (hitsResizeHandle(sel, mx, my)) {
      state.dragging = true;
      state.dragType = 'resize-br';
      state.dragOffX = mx; state.dragOffY = my;
      return;
    }
    if (hitsLineP2(sel, mx, my)) {
      state.dragging = true;
      state.dragType = 'line-p2';
      return;
    }
  }

  const idx = hitTest(mx, my);
  if (idx !== -1) {
    state.selectedIdx = idx;
    state.dragging = true;
    state.dragType = 'move';
    const n = state.nodes[idx];
    const ox = n.type === 'text' ? n.boxX : (n.type === 'line' ? n.x1 : n.x);
    const oy = n.type === 'text' ? n.boxY : (n.type === 'line' ? n.y1 : n.y);
    state.dragOffX = mx / z - ox;
    state.dragOffY = my / z - oy;
    buildPropsPanel();
    render();
  } else {
    state.selectedIdx = -1;
    buildPropsPanel();
    render();
  }
});

window.addEventListener('mousemove', e => {
  if (!state.dragging) return;
  const r = canvas.getBoundingClientRect();
  const mx = e.clientX - r.left, my = e.clientY - r.top;
  const z = state.zoom;
  const n = state.nodes[state.selectedIdx];
  if (!n) return;

  if (state.dragType === 'move') {
    const nx = snap(mx / z - state.dragOffX);
    const ny = snap(my / z - state.dragOffY);
    if (n.type === 'text') { n.boxX = nx; n.boxY = ny; }
    else if (n.type === 'line') {
      const dx = nx - n.x1, dy = ny - n.y1;
      n.x1 = nx; n.y1 = ny; n.x2 += dx; n.y2 += dy;
    } else { n.x = nx; n.y = ny; }
  } else if (state.dragType === 'resize-br') {
    if (n.type === 'background' || n.type === 'block') {
      n.width  = Math.max(8, snap(mx / z) - n.x);
      n.height = Math.max(8, snap(my / z) - n.y);
    } else if (n.type === 'text') {
      n.width  = Math.max(20, snap(mx / z) - n.boxX);
      n.height = Math.max(8,  snap(my / z) - n.boxY);
    }
  } else if (state.dragType === 'line-p2') {
    n.x2 = snap(mx / z); n.y2 = snap(my / z);
  }

  refreshPropsValues(n);
  render();
});

window.addEventListener('mouseup', () => { state.dragging = false; });

function snap(v) { return Math.round(v / 2) * 2; }

/* ── Keyboard shortcuts ─────────────────────────────────── */
window.addEventListener('keydown', e => {
  if (e.target !== document.body) return;
  if (e.key === 'Delete' || e.key === 'Backspace') {
    deleteSelected();
  }
  if (e.key === 'ArrowUp')    nudge(0, -1, e.shiftKey ? 8 : 1);
  if (e.key === 'ArrowDown')  nudge(0,  1, e.shiftKey ? 8 : 1);
  if (e.key === 'ArrowLeft')  nudge(-1, 0, e.shiftKey ? 8 : 1);
  if (e.key === 'ArrowRight') nudge( 1, 0, e.shiftKey ? 8 : 1);
  if ((e.ctrlKey || e.metaKey) && e.key === 'z') undo();
  if ((e.ctrlKey || e.metaKey) && e.key === 'd') { e.preventDefault(); duplicateSelected(); }
});

function nudge(dx, dy, step) {
  const n = state.nodes[state.selectedIdx];
  if (!n) return;
  if (n.type === 'text') { n.boxX += dx*step; n.boxY += dy*step; }
  else if (n.type === 'line') { n.x1 += dx*step; n.y1 += dy*step; n.x2 += dx*step; n.y2 += dy*step; }
  else { n.x += dx*step; n.y += dy*step; }
  refreshPropsValues(n);
  render();
}

/* ── Undo stack ─────────────────────────────────────────── */
const undoStack = [];
function pushUndo() { undoStack.push(JSON.stringify(state.nodes)); if (undoStack.length > 50) undoStack.shift(); }
function undo() {
  if (!undoStack.length) return;
  state.nodes = JSON.parse(undoStack.pop());
  state.selectedIdx = -1;
  buildPropsPanel(); render(); updateLayersList();
}

function duplicateSelected() {
  const n = state.nodes[state.selectedIdx];
  if (!n) return;
  pushUndo();
  const copy = JSON.parse(JSON.stringify(n));
  copy.id = copy.id + '_copy';
  if (copy.type !== 'line') { copy.x = (copy.x ?? copy.boxX) + 8; copy.y = (copy.y ?? copy.boxY) + 8; }
  if (copy.boxX !== undefined) { copy.boxX += 8; copy.boxY += 8; }
  state.nodes.push(copy);
  state.selectedIdx = state.nodes.length - 1;
  buildPropsPanel(); render(); updateLayersList();
}

/* ── Toolbar: add nodes ─────────────────────────────────── */
document.querySelectorAll('.tool-btn[data-type]').forEach(btn => {
  btn.addEventListener('click', () => {
    const type = btn.dataset.type;
    const def = NodeDefs[type];
    if (!def) return;
    pushUndo();
    const node = def.defaults();
    // Centre in visible canvas area
    if (node.type === 'text') { node.boxX = Math.round(state.canvasW/2 - 60); node.boxY = Math.round(state.canvasH/2 - 10); }
    else if (node.type === 'line') { node.x1 = Math.round(state.canvasW/4); node.y1 = Math.round(state.canvasH/2); node.x2 = Math.round(3*state.canvasW/4); node.y2 = node.y1; }
    else { node.x = Math.round(state.canvasW/2 - 30); node.y = Math.round(state.canvasH/2 - 20); }
    state.nodes.push(node);
    state.selectedIdx = state.nodes.length - 1;
    buildPropsPanel(); render(); updateLayersList();
  });
});

/* ── Properties panel ───────────────────────────────────── */
function buildPropsPanel() {
  const n = state.nodes[state.selectedIdx];
  if (!n) { propsBody.innerHTML = '<div class="no-selection">Select a node to edit its properties.</div>'; return; }

  const def = NodeDefs[n.type];
  let html = `<div class="prop-group"><div class="prop-group-title">${n.type.toUpperCase()}</div>`;

  // Generic props
  html += propRow('id', 'text', n.id, 'ID');
  def.props.filter(p => p !== 'id').forEach(p => {
    html += buildPropRow(p, n);
  });
  html += '</div>';

  // Action section
  html += buildActionSection(n);

  html += `<button class="prop-delete" id="prop-delete-btn">Delete node</button>`;
  propsBody.innerHTML = html;

  // Wire up all inputs
  propsBody.querySelectorAll('input, select, textarea').forEach(el => {
    el.addEventListener('input', () => { applyPropChange(el, n); });
  });
  document.getElementById('prop-delete-btn')?.addEventListener('click', deleteSelected);
  wireActionSection(n);
}

function buildPropRow(p, n) {
  const v = n[p];
  if (p === 'color') return propRow(p, 'color', v || '#ffffff', 'Color');
  if (p === 'alpha') return propRow(p, 'number', v ?? 210, 'Alpha', '0','255','1');
  if (p === 'doubleSided' || p === 'shadow' || p === 'seeThrough') return propCheckRow(p, v, label(p));
  if (p === 'alignment') return propSelectRow(p, v, ['LEFT','CENTER','RIGHT'], 'Align H');
  if (p === 'verticalAlignment') return propSelectRow(p, v, ['TOP','MIDDLE','BOTTOM'], 'Align V');
  if (p === 'transform') return propSelectRow(p, v, ['FIXED','HEAD','GUI','GROUND'], 'Transform');
  if (p === 'text') return propTextareaRow(p, v, 'Text');
  // Number fields
  if (typeof v === 'number') {
    const isInt = Number.isInteger(v);
    return propRow(p, 'number', v, label(p), null, null, isInt ? '1' : '0.5');
  }
  return propRow(p, 'text', v ?? '', label(p));
}

function propRow(key, type, val, lbl, min, max, step) {
  const mAttr = min != null ? `min="${min}"` : '';
  const xAttr = max != null ? `max="${max}"` : '';
  const sAttr = step != null ? `step="${step}"` : '';
  return `<div class="prop-row"><label>${lbl}</label><input type="${type}" data-key="${key}" value="${val}" ${mAttr} ${xAttr} ${sAttr} /></div>`;
}
function propCheckRow(key, val, lbl) {
  const chk = val ? 'checked' : '';
  return `<div class="prop-row"><label>${lbl}</label><input type="checkbox" data-key="${key}" ${chk} /></div>`;
}
function propSelectRow(key, val, opts, lbl) {
  const options = opts.map(o => `<option value="${o}" ${o===val?'selected':''}>${o}</option>`).join('');
  return `<div class="prop-row"><label>${lbl}</label><select data-key="${key}">${options}</select></div>`;
}
function propTextareaRow(key, val, lbl) {
  return `<div class="prop-row" style="align-items:flex-start"><label style="padding-top:4px">${lbl}</label><textarea data-key="${key}" rows="3">${val}</textarea></div>`;
}

function label(p) {
  const map = { x:'X', y:'Y', x1:'X1', y1:'Y1', x2:'X2', y2:'Y2',
    width:'Width', height:'Height', depth:'Depth', scale:'Scale',
    boxX:'Box X', boxY:'Box Y', fontSize:'Font Sz', contentWidth:'Content W',
    leftOffset:'Off L', rightOffset:'Off R', verticalOffset:'Off V',
    thickness:'Thickness', material:'Material', alpha:'Alpha',
    shadow:'Shadow', seeThrough:'See-thru', doubleSided:'2-sided' };
  return map[p] ?? p;
}

function applyPropChange(el, n) {
  const key = el.dataset.key;
  let val = el.type === 'checkbox' ? el.checked : el.value;
  if (el.type === 'number' || el.type === 'range') val = parseFloat(val);
  n[key] = val;
  render();
  updateLayersList();
}

function refreshPropsValues(n) {
  propsBody.querySelectorAll('[data-key]').forEach(el => {
    const v = n[el.dataset.key];
    if (el.type === 'checkbox') el.checked = !!v;
    else el.value = v ?? '';
  });
}

/* ── Action section ─────────────────────────────────────── */
function buildActionSection(n) {
  const btn = n._button || {};
  const actionType = btn.action?.type || 'NONE';
  const actionVal  = btn.action?.value || '';
  const desc = btn.description || '';
  const needsValue = actionType !== 'NONE';

  return `
  <div class="prop-group action-section">
    <div class="prop-group-title">Button Action</div>
    ${propRow('_desc', 'text', desc, 'Tooltip')}
    <div class="prop-row">
      <label>Action</label>
      <select id="action-type-sel">
        <option value="NONE" ${actionType==='NONE'?'selected':''}>None</option>
        <option value="OPEN_URL" ${actionType==='OPEN_URL'?'selected':''}>Open URL</option>
        <option value="PLAYER_COMMAND" ${actionType==='PLAYER_COMMAND'?'selected':''}>Player Command</option>
        <option value="CONSOLE_COMMAND" ${actionType==='CONSOLE_COMMAND'?'selected':''}>Console Command</option>
        <option value="SUGGEST_COMMAND" ${actionType==='SUGGEST_COMMAND'?'selected':''}>Suggest Command</option>
      </select>
    </div>
    <div class="prop-row" id="action-value-row" style="display:${needsValue?'flex':'none'}">
      <label>Value</label>
      <input type="text" id="action-val-input" value="${actionVal}" placeholder="URL or command..." />
    </div>
  </div>`;
}

function wireActionSection(n) {
  const sel = document.getElementById('action-type-sel');
  const valRow = document.getElementById('action-value-row');
  const valInput = document.getElementById('action-val-input');
  const descInput = propsBody.querySelector('[data-key="_desc"]');

  const sync = () => {
    const type = sel.value;
    valRow.style.display = type !== 'NONE' ? 'flex' : 'none';
    if (!n._button) n._button = { id: n.id + '_btn', nodeId: n.id, description: '', action: { type: 'NONE', value: '' } };
    n._button.action = { type, value: valInput?.value || '' };
    n._button.description = descInput?.value || '';
  };

  sel?.addEventListener('change', sync);
  valInput?.addEventListener('input', sync);
  descInput?.addEventListener('input', sync);
}

/* ── Delete ─────────────────────────────────────────────── */
function deleteSelected() {
  if (state.selectedIdx < 0) return;
  pushUndo();
  state.nodes.splice(state.selectedIdx, 1);
  state.selectedIdx = -1;
  buildPropsPanel(); render(); updateLayersList();
}

/* ── Layers panel ───────────────────────────────────────── */
const layersPanel = document.getElementById('layers-panel');
document.getElementById('btn-layers').addEventListener('click', () => layersPanel.classList.toggle('hidden'));
document.getElementById('layers-close').addEventListener('click', () => layersPanel.classList.add('hidden'));

function updateLayersList() {
  const list = document.getElementById('layers-list');
  list.innerHTML = '';
  [...state.nodes].reverse().forEach((n, ri) => {
    const i = state.nodes.length - 1 - ri;
    const li = document.createElement('li');
    li.className = 'layer-item' + (i === state.selectedIdx ? ' selected' : '');
    li.innerHTML = `<span>${NodeDefs[n.type]?.label(n) ?? n.type}</span>
      <span class="layer-type">${n.type}</span>
      <button class="layer-up" title="Move up">↑</button>
      <button class="layer-down" title="Move down">↓</button>
      <button class="layer-del" title="Delete">✕</button>`;
    li.addEventListener('click', e => {
      if (e.target.tagName === 'BUTTON') return;
      state.selectedIdx = i; buildPropsPanel(); render(); updateLayersList();
    });
    li.querySelector('.layer-up').addEventListener('click', () => moveNode(i, 1));
    li.querySelector('.layer-down').addEventListener('click', () => moveNode(i, -1));
    li.querySelector('.layer-del').addEventListener('click', () => { state.selectedIdx = i; deleteSelected(); });
    list.appendChild(li);
  });
}

function moveNode(idx, dir) {
  const t = idx + dir;
  if (t < 0 || t >= state.nodes.length) return;
  pushUndo();
  [state.nodes[idx], state.nodes[t]] = [state.nodes[t], state.nodes[idx]];
  state.selectedIdx = t;
  render(); updateLayersList();
}

/* ── Toolbar controls ───────────────────────────────────── */
const canvasWInput = document.getElementById('canvas-w');
const canvasHInput = document.getElementById('canvas-h');
const zoomRange = document.getElementById('zoom-range');
const zoomLabel = document.getElementById('zoom-label');
const gridToggle = document.getElementById('grid-toggle');

canvasWInput.addEventListener('change', () => { state.canvasW = parseInt(canvasWInput.value); resizeCanvas(); });
canvasHInput.addEventListener('change', () => { state.canvasH = parseInt(canvasHInput.value); resizeCanvas(); });
zoomRange.addEventListener('input', () => {
  state.zoom = parseFloat(zoomRange.value);
  zoomLabel.textContent = state.zoom.toFixed(1) + '×';
  resizeCanvas();
});
gridToggle.addEventListener('change', () => { state.grid = gridToggle.checked; render(); });

/* ── Export ─────────────────────────────────────────────── */
document.getElementById('btn-export').addEventListener('click', () => {
  const json = Serializer.toJson({ ...state });
  const blob = new Blob([json], { type: 'application/json' });
  const a = document.createElement('a');
  a.href = URL.createObjectURL(blob);
  a.download = (state.name || 'panel') + '.hhdui.json';
  a.click();
});

/* ── Import ─────────────────────────────────────────────── */
document.getElementById('btn-import').addEventListener('click', () => {
  document.getElementById('import-file').click();
});
document.getElementById('import-file').addEventListener('change', e => {
  const file = e.target.files[0]; if (!file) return;
  const reader = new FileReader();
  reader.onload = ev => {
    try {
      const s = Serializer.fromJson(ev.target.result);
      state.nodes = s.nodes;
      state.canvasW = s.canvasW; state.canvasH = s.canvasH;
      state.name = s.name;
      state.selectedIdx = -1;
      canvasWInput.value = s.canvasW;
      canvasHInput.value = s.canvasH;
      resizeCanvas(); buildPropsPanel(); updateLayersList();
    } catch(err) { alert('Import failed: ' + err.message); }
  };
  reader.readAsText(file);
  e.target.value = '';
});

/* ── Clear ──────────────────────────────────────────────── */
document.getElementById('btn-clear').addEventListener('click', () => {
  if (!state.nodes.length || confirm('Clear all nodes?')) {
    pushUndo(); state.nodes = []; state.selectedIdx = -1;
    buildPropsPanel(); render(); updateLayersList();
  }
});

/* ── Boot ───────────────────────────────────────────────── */
resizeCanvas();
buildPropsPanel();
updateLayersList();
