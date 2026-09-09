/**
 * editor.js — Main editor: canvas, selection, drag-resize, undo, Pickr color pickers,
 *             drag-drop layer reordering, context-sensitive cursor, preview canvas.
 */

/* ── State ────────────────────────────────────────────────── */
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
  dragType: null,           // 'move' | 'resize-se' | 'resize-nw' | ... | 'line-p1' | 'line-p2'
  dragStartMx: 0, dragStartMy: 0, // canvas-space mouse at drag start
  dragStartNode: null,      // deep-copy of node values at drag start
};

/* ── Undo stack ──────────────────────────────────────────── */
const _undoStack = [];
function pushUndo() {
  _undoStack.push(JSON.stringify(state.nodes));
  if (_undoStack.length > 80) _undoStack.shift();
}
function undo() {
  if (!_undoStack.length) return;
  state.nodes = JSON.parse(_undoStack.pop());
  state.selectedIdx = -1;
  buildPropsPanel(); render(); updateLayersList();
}

/* ── localStorage persistence ────────────────────────────── */
const STORAGE_KEY = 'hhdui_builder_v1';
let _saveTimer = null;

function scheduleSave() {
  clearTimeout(_saveTimer);
  _saveTimer = setTimeout(_persistState, 800);
  _showSaveIndicator('pending');
}

function _persistState() {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify({
      v: 1,
      nodes:   state.nodes,
      canvasW: state.canvasW,
      canvasH: state.canvasH,
      name:    state.name,
      zoom:    state.zoom,
      grid:    state.grid,
    }));
    _showSaveIndicator('saved');
  } catch (e) {
    console.warn('[HaoHan Builder] localStorage write failed', e);
  }
}

function _restoreState() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return false;
    const s = JSON.parse(raw);
    if (!s || s.v !== 1) return false;
    state.nodes   = s.nodes   ?? [];
    state.canvasW = s.canvasW ?? 256;
    state.canvasH = s.canvasH ?? 192;
    state.name    = s.name    ?? 'untitled';
    state.zoom    = s.zoom    ?? 2;
    state.grid    = s.grid    ?? true;
    return true;
  } catch { return false; }
}

function _showSaveIndicator(state) {
  const el = document.getElementById('save-indicator');
  if (!el) return;
  el.dataset.state = state;
  el.title = state === 'saved' ? 'All changes saved' : 'Saving…';
}

/* ── DOM refs ────────────────────────────────────────────── */
const canvas     = document.getElementById('canvas');
const ctx        = canvas.getContext('2d');
const propsBody  = document.getElementById('props-body');
const prevCanvas = document.getElementById('preview-canvas');
const prevCtx    = prevCanvas.getContext('2d');

/* Expose render to nodes.js texture loader */
window.__renderFrame = () => render();

/* ── Canvas sizing ───────────────────────────────────────── */
function resizeCanvas() {
  canvas.width  = state.canvasW * state.zoom;
  canvas.height = state.canvasH * state.zoom;
  canvas.style.width  = canvas.width + 'px';
  canvas.style.height = canvas.height + 'px';
  document.getElementById('canvas-dims').textContent = `${state.canvasW}×${state.canvasH}`;
  render();
}

/* ── Render ──────────────────────────────────────────────── */
function render() {
  const z = state.zoom;
  ctx.clearRect(0, 0, canvas.width, canvas.height);

  // Canvas background
  ctx.fillStyle = '#0a0e18';
  ctx.fillRect(0, 0, canvas.width, canvas.height);

  // Grid
  if (state.grid) {
    const step = 8 * z;
    ctx.save();
    ctx.strokeStyle = 'rgba(255,255,255,0.035)';
    ctx.lineWidth = 1;
    for (let x = 0; x <= canvas.width; x += step) { ctx.beginPath(); ctx.moveTo(x, 0); ctx.lineTo(x, canvas.height); ctx.stroke(); }
    for (let y = 0; y <= canvas.height; y += step) { ctx.beginPath(); ctx.moveTo(0, y); ctx.lineTo(canvas.width, y); ctx.stroke(); }
    ctx.restore();
  }

  // Nodes (back to front)
  state.nodes.forEach((n, i) => {
    NodeDefs[n.type]?.render(ctx, n, z);
    if (i === state.selectedIdx) renderSelection(n);
  });

  updatePreview();
  scheduleSave();
}

/* ── Resize helpers ──────────────────────────────────── */

const HANDLE_CURSORS = {
  nw: 'nw-resize', n: 'n-resize', ne: 'ne-resize',
  e:  'e-resize',  se: 'se-resize',
  s:  's-resize',  sw: 'sw-resize', w: 'w-resize',
};

/** Get bounding rect {x,y,w,h} for rect-type nodes (canvas coords). */
function getNodeRect(n) {
  if (n.type === 'background' || n.type === 'block') return { x: n.x, y: n.y, w: n.width, h: n.height };
  if (n.type === 'text') return { x: n.boxX, y: n.boxY, w: n.width, h: n.height };
  return null;
}

/** Write bounding rect back onto the node. */
function setNodeRect(n, x, y, w, h) {
  if (n.type === 'background' || n.type === 'block') { n.x = x; n.y = y; n.width = w; n.height = h; }
  else if (n.type === 'text') { n.boxX = x; n.boxY = y; n.width = w; n.height = h; }
}

/** Return [{x,y,type}] handle array in canvas-pixels. */
function getNodeHandles(n) {
  const z = state.zoom;
  const r = getNodeRect(n);
  if (!r) return [];
  const x1 = r.x * z, y1 = r.y * z;
  const x2 = (r.x + r.w) * z, y2 = (r.y + r.h) * z;
  const mx = (x1 + x2) / 2,   my = (y1 + y2) / 2;
  return [
    { x: x1, y: y1, type: 'nw' },
    { x: mx, y: y1, type: 'n'  },
    { x: x2, y: y1, type: 'ne' },
    { x: x2, y: my, type: 'e'  },
    { x: x2, y: y2, type: 'se' },
    { x: mx, y: y2, type: 's'  },
    { x: x1, y: y2, type: 'sw' },
    { x: x1, y: my, type: 'w'  },
  ];
}

/**
 * Recompute node rect from drag-start snapshot + current mouse delta.
 * @param {object} orig  - deep-copy of the node at drag-start
 * @param {string} handle - 'nw'|'n'|'ne'|'e'|'se'|'s'|'sw'|'w'
 * @param {number} dx     - node-space delta X
 * @param {number} dy     - node-space delta Y
 * @returns {{x,y,w,h}}
 */
function calcResizeRect(orig, handle, dx, dy) {
  const r = getNodeRect(orig);
  if (!r) return null;
  const MIN_W = orig.type === 'text' ? 20 : 8;
  const MIN_H = 8;
  let { x, y, w, h } = r;

  if (handle.includes('e')) { w = Math.max(MIN_W, w + dx); }
  if (handle.includes('s')) { h = Math.max(MIN_H, h + dy); }
  if (handle.includes('w')) {
    const nw = Math.max(MIN_W, w - dx);
    x = x + w - nw; w = nw;
  }
  if (handle.includes('n')) {
    const nh = Math.max(MIN_H, h - dy);
    y = y + h - nh; h = nh;
  }
  return { x: Math.round(x), y: Math.round(y), w: Math.round(w), h: Math.round(h) };
}

/* ── Selection overlay ────────────────────────────────────── */
function renderSelection(n) {
  const z = state.zoom;
  ctx.save();

  const drawHandleAt = (hx, hy, corner) => {
    const hs = corner ? 5 : 4; // corners slightly larger
    ctx.setLineDash([]);
    ctx.fillStyle = '#4f8ef7';
    ctx.strokeStyle = 'rgba(255,255,255,.65)';
    ctx.lineWidth = 1;
    ctx.fillRect(hx - hs, hy - hs, hs * 2, hs * 2);
    ctx.strokeRect(hx - hs, hy - hs, hs * 2, hs * 2);
  };

  const handles = getNodeHandles(n);
  if (handles.length) {
    const r = getNodeRect(n);
    // Dashed bounding box
    ctx.strokeStyle = '#4f8ef7';
    ctx.lineWidth = 1.5;
    ctx.setLineDash([4, 3]);
    ctx.strokeRect(r.x * z - 1, r.y * z - 1, r.w * z + 2, r.h * z + 2);
    // Handles
    const CORNERS = new Set(['nw','ne','se','sw']);
    handles.forEach(h => drawHandleAt(h.x, h.y, CORNERS.has(h.type)));
  } else if (n.type === 'item') {
    const hs = 8 * n.scale * z;
    ctx.strokeStyle = '#4f8ef7';
    ctx.lineWidth = 1.5;
    ctx.setLineDash([4, 3]);
    ctx.strokeRect(n.x * z - hs - 2, n.y * z - hs - 2, hs * 2 + 4, hs * 2 + 4);
  } else if (n.type === 'line') {
    ctx.strokeStyle = '#4f8ef7';
    ctx.lineWidth = 1.5;
    ctx.setLineDash([]);
    ctx.beginPath();
    ctx.moveTo(n.x1 * z, n.y1 * z);
    ctx.lineTo(n.x2 * z, n.y2 * z);
    ctx.stroke();
    // Round endpoint handles
    [[n.x1 * z, n.y1 * z], [n.x2 * z, n.y2 * z]].forEach(([hx, hy]) => {
      ctx.setLineDash([]);
      ctx.fillStyle = '#4f8ef7';
      ctx.strokeStyle = 'rgba(255,255,255,.65)';
      ctx.lineWidth = 1;
      ctx.beginPath(); ctx.arc(hx, hy, 5, 0, Math.PI * 2); ctx.fill(); ctx.stroke();
    });
  }
  ctx.restore();
}

/* ── Preview canvas ──────────────────────────────────────── */
function updatePreview() {
  const parent = prevCanvas.parentElement;
  const maxW = parent.clientWidth - 20;
  const ratio = state.canvasH / state.canvasW;
  const pw = Math.min(maxW, 230);
  const ph = Math.round(pw * ratio);
  if (prevCanvas.width !== pw || prevCanvas.height !== ph) {
    prevCanvas.width = pw;
    prevCanvas.height = ph;
  }
  const pz = pw / state.canvasW;
  prevCtx.fillStyle = '#0a0e18';
  prevCtx.fillRect(0, 0, pw, ph);
  state.nodes.forEach(n => NodeDefs[n.type]?.render(prevCtx, n, pz));
}

/* ── Hit testing ─────────────────────────────────────────── */
const SLOP = 5;

function hitTest(mx, my) {
  const z = state.zoom;
  for (let i = state.nodes.length - 1; i >= 0; i--) {
    if (hitsNode(state.nodes[i], mx / z, my / z)) return i;
  }
  return -1;
}

function hitsNode(n, px, py) {
  switch (n.type) {
    case 'background':
      return inRect(px, py, n.x, n.y, n.width, n.height);
    case 'text':
      return inRect(px, py, n.boxX, n.boxY, n.width, n.height);
    case 'block':
      return inRect(px, py, n.x, n.y, n.width, n.height);
    case 'item': {
      const hs = 8 * n.scale;
      return Math.abs(px - n.x) <= hs + SLOP && Math.abs(py - n.y) <= hs + SLOP;
    }
    case 'line': {
      const dx = n.x2 - n.x1, dy = n.y2 - n.y1;
      const len2 = dx * dx + dy * dy;
      if (len2 === 0) return dist2(px, py, n.x1, n.y1) <= SLOP;
      const t = Math.max(0, Math.min(1, ((px - n.x1) * dx + (py - n.y1) * dy) / len2));
      return dist2(px, py, n.x1 + t * dx, n.y1 + t * dy) <= n.thickness / 2 + SLOP;
    }
    default: return false;
  }
}

function inRect(px, py, rx, ry, rw, rh) {
  return px >= rx - SLOP && px <= rx + rw + SLOP && py >= ry - SLOP && py <= ry + rh + SLOP;
}
function dist2(ax, ay, bx, by) { return Math.sqrt((ax - bx) ** 2 + (ay - by) ** 2); }

function hitsResizeHandle(n, mx, my) {
  const handles = getNodeHandles(n);
  for (const h of handles) {
    if (Math.abs(mx - h.x) <= 8 && Math.abs(my - h.y) <= 8) return h.type;
  }
  return null;
}

function hitsLineEndpoint(n, mx, my, ep /* 1 or 2 */) {
  if (n.type !== 'line') return false;
  const z = state.zoom;
  const ex = (ep === 1 ? n.x1 : n.x2) * z;
  const ey = (ep === 1 ? n.y1 : n.y2) * z;
  return Math.abs(mx - ex) <= 8 && Math.abs(my - ey) <= 8;
}

/* ── Context-sensitive cursor ──────────────────────────────── */
function updateCursor(mx, my) {
  if (state.dragging) return;
  const sel = state.nodes[state.selectedIdx];
  if (sel) {
    const handle = hitsResizeHandle(sel, mx, my);
    if (handle) { canvas.style.cursor = HANDLE_CURSORS[handle]; return; }
    if (hitsLineEndpoint(sel, mx, my, 1) || hitsLineEndpoint(sel, mx, my, 2)) { canvas.style.cursor = 'crosshair'; return; }
  }
  const idx = hitTest(mx, my);
  canvas.style.cursor = idx !== -1 ? 'move' : 'default';
}

/* ── Mouse events ────────────────────────────────────────── */
canvas.addEventListener('mousemove', e => {
  const r = canvas.getBoundingClientRect();
  const mx = e.clientX - r.left, my = e.clientY - r.top;
  const z = state.zoom;

  // Cursor position display
  document.getElementById('cursor-pos').textContent = `${Math.round(mx / z)}, ${Math.round(my / z)}`;

  if (state.dragging) {
    const n = state.nodes[state.selectedIdx];
    if (!n) return;

    if (state.dragType === 'move') {
      const nx = snap(mx / z - state.dragOffX);
      const ny = snap(my / z - state.dragOffY);
      if (n.type === 'text') { n.boxX = nx; n.boxY = ny; }
      else if (n.type === 'line') {
        const ddx = nx - n.x1, ddy = ny - n.y1;
        n.x1 = nx; n.y1 = ny; n.x2 += ddx; n.y2 += ddy;
      } else { n.x = nx; n.y = ny; }

    } else if (state.dragType.startsWith('resize-')) {
      const handle = state.dragType.slice(7); // 'se', 'nw', etc.
      const dx = snap((mx - state.dragStartMx) / z);
      const dy = snap((my - state.dragStartMy) / z);
      const res = calcResizeRect(state.dragStartNode, handle, dx, dy);
      if (res) setNodeRect(n, res.x, res.y, res.w, res.h);

    } else if (state.dragType === 'line-p1') {
      n.x1 = snap(mx / z); n.y1 = snap(my / z);
    } else if (state.dragType === 'line-p2') {
      n.x2 = snap(mx / z); n.y2 = snap(my / z);
    }
    refreshPropsInputs(n);
    render(); updateLayersList();
    return;
  }

  updateCursor(mx, my);
});

canvas.addEventListener('mousedown', e => {
  const r = canvas.getBoundingClientRect();
  const mx = e.clientX - r.left, my = e.clientY - r.top;
  const z = state.zoom;
  const sel = state.nodes[state.selectedIdx];

  if (sel) {
    const handleType = hitsResizeHandle(sel, mx, my);
    if (handleType) {
      pushUndo();
      state.dragging = true;
      state.dragType = `resize-${handleType}`;
      state.dragStartMx = mx; state.dragStartMy = my;
      state.dragStartNode = JSON.parse(JSON.stringify(sel));
      canvas.style.cursor = HANDLE_CURSORS[handleType];
      return;
    }
    if (hitsLineEndpoint(sel, mx, my, 1)) {
      pushUndo();
      state.dragging = true; state.dragType = 'line-p1';
      canvas.style.cursor = 'grabbing';
      return;
    }
    if (hitsLineEndpoint(sel, mx, my, 2)) {
      pushUndo();
      state.dragging = true; state.dragType = 'line-p2';
      canvas.style.cursor = 'grabbing';
      return;
    }
  }

  const idx = hitTest(mx, my);
  if (idx !== -1) {
    if (idx !== state.selectedIdx) {
      state.selectedIdx = idx;
      buildPropsPanel();
    }
    const n = state.nodes[idx];
    const ox = n.type === 'text' ? n.boxX : (n.type === 'line' ? n.x1 : n.x);
    const oy = n.type === 'text' ? n.boxY : (n.type === 'line' ? n.y1 : n.y);
    pushUndo();
    state.dragging = true; state.dragType = 'move';
    state.dragOffX = mx / z - ox; state.dragOffY = my / z - oy;
    canvas.style.cursor = 'grabbing';
    render(); updateLayersList();
  } else {
    state.selectedIdx = -1;
    buildPropsPanel(); render(); updateLayersList();
  }
});

window.addEventListener('mouseup', () => {
  state.dragging = false;
  const r = canvas.getBoundingClientRect();
  // Reset cursor via position check
  canvas.style.cursor = 'default';
});

function snap(v) { return Math.round(v / 2) * 2; }

/* ── Keyboard shortcuts ──────────────────────────────────── */
window.addEventListener('keydown', e => {
  if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA' || e.target.tagName === 'SELECT') return;

  if ((e.ctrlKey || e.metaKey) && e.key === 'z') { e.preventDefault(); undo(); return; }
  if ((e.ctrlKey || e.metaKey) && e.key === 'd') { e.preventDefault(); duplicateSelected(); return; }

  if (e.key === 'Delete' || e.key === 'Backspace') { deleteSelected(); return; }

  // Nudge with arrow keys
  const step = e.shiftKey ? 8 : 1;
  if (e.key === 'ArrowUp')    { nudge( 0, -step); e.preventDefault(); }
  if (e.key === 'ArrowDown')  { nudge( 0,  step); e.preventDefault(); }
  if (e.key === 'ArrowLeft')  { nudge(-step, 0);  e.preventDefault(); }
  if (e.key === 'ArrowRight') { nudge( step, 0);  e.preventDefault(); }

  // Layer reordering: Ctrl+[ / Ctrl+]
  if ((e.ctrlKey || e.metaKey) && e.key === '[') { moveNode(state.selectedIdx, -1); e.preventDefault(); }
  if ((e.ctrlKey || e.metaKey) && e.key === ']') { moveNode(state.selectedIdx,  1); e.preventDefault(); }
});

function nudge(dx, dy) {
  const n = state.nodes[state.selectedIdx];
  if (!n) return;
  if (n.type === 'text') { n.boxX += dx; n.boxY += dy; }
  else if (n.type === 'line') { n.x1 += dx; n.y1 += dy; n.x2 += dx; n.y2 += dy; }
  else { n.x += dx; n.y += dy; }
  refreshPropsInputs(n); render();
}

/* ── Add nodes ───────────────────────────────────────────── */
document.querySelectorAll('.tool-btn[data-type]').forEach(btn => {
  btn.addEventListener('click', () => {
    const type = btn.dataset.type;
    const def = NodeDefs[type]; if (!def) return;
    pushUndo();
    const node = def.defaults();
    // Place roughly centred
    const cx = Math.round(state.canvasW / 2);
    const cy = Math.round(state.canvasH / 2);
    if (type === 'text') { node.boxX = cx - 60; node.boxY = cy - 10; }
    else if (type === 'line') { node.x1 = cx - 40; node.y1 = cy; node.x2 = cx + 40; node.y2 = cy; }
    else { node.x = cx - (node.width ?? 16) / 2; node.y = cy - (node.height ?? 16) / 2; }
    state.nodes.push(node);
    state.selectedIdx = state.nodes.length - 1;
    buildPropsPanel(); render(); updateLayersList();
  });
});

/* ── Duplicate & delete ──────────────────────────────────── */
function duplicateSelected() {
  if (state.selectedIdx < 0) return;
  pushUndo();
  const copy = JSON.parse(JSON.stringify(state.nodes[state.selectedIdx]));
  copy.id = copy.id + '_copy';
  if (copy.type === 'line') { copy.x1 += 8; copy.y1 += 8; copy.x2 += 8; copy.y2 += 8; }
  else if (copy.type === 'text') { copy.boxX += 8; copy.boxY += 8; }
  else { copy.x += 8; copy.y += 8; }
  state.nodes.push(copy);
  state.selectedIdx = state.nodes.length - 1;
  buildPropsPanel(); render(); updateLayersList();
}

function deleteSelected() {
  if (state.selectedIdx < 0) return;
  pushUndo();
  state.nodes.splice(state.selectedIdx, 1);
  state.selectedIdx = -1;
  buildPropsPanel(); render(); updateLayersList();
}

function moveNode(idx, dir) {
  const to = idx + dir;
  if (to < 0 || to >= state.nodes.length) return;
  pushUndo();
  [state.nodes[idx], state.nodes[to]] = [state.nodes[to], state.nodes[idx]];
  state.selectedIdx = to;
  render(); updateLayersList();
}

function reorderNodes(fromIdx, toIdx) {
  if (fromIdx === toIdx) return;
  pushUndo();
  const node = state.nodes.splice(fromIdx, 1)[0];
  const adj = fromIdx < toIdx ? toIdx - 1 : toIdx;
  state.nodes.splice(adj, 0, node);
  if (state.selectedIdx === fromIdx) state.selectedIdx = adj;
  render(); updateLayersList();
}

/* ── Pickr color picker management ──────────────────────── */
let _activePickrs = [];

function destroyPickrs() {
  _activePickrs.forEach(p => { try { p.destroyAndRemove(); } catch (_) {} });
  _activePickrs = [];
}

function initColorPicker(mountEl, initialHex, alpha, onChangeHex, onChangeAlpha) {
  const hasAlpha = onChangeAlpha != null;
  const defaultVal = hexWithAlpha(initialHex, hasAlpha ? (alpha ?? 255) : 255);

  const pickr = Pickr.create({
    el: mountEl,
    theme: 'nano',
    default: defaultVal,
    components: {
      preview: true,
      opacity: hasAlpha,
      hue: true,
      interaction: { hex: true, rgba: true, input: true, save: true },
    },
  });

  pickr.on('change', color => {
    const rgba = color.toRGBA();
    const hex = '#' + [rgba[0], rgba[1], rgba[2]]
      .map(v => Math.round(v).toString(16).padStart(2, '0')).join('');
    onChangeHex(hex);
    if (hasAlpha) onChangeAlpha(Math.round(rgba[3] * 255));
    render();
  });

  _activePickrs.push(pickr);
}

/* ── Properties panel ────────────────────────────────────── */
const PROP_LABELS = {
  x: 'X', y: 'Y', x1: 'X1', y1: 'Y1', x2: 'X2', y2: 'Y2',
  boxX: 'Box X', boxY: 'Box Y', width: 'Width', height: 'Height',
  depth: 'Depth', scale: 'Scale', fontSize: 'Font size',
  contentWidth: 'Content W', leftOffset: 'Offset L', rightOffset: 'Offset R',
  verticalOffset: 'Offset V', thickness: 'Thickness', material: 'Material',
  transform: 'Transform', alignment: 'Align H', verticalAlignment: 'Align V',
  shadow: 'Shadow', seeThrough: 'See-through', doubleSided: 'Double sided',
};

function buildPropsPanel() {
  destroyPickrs();
  const n = state.nodes[state.selectedIdx];

  // Update header
  const icon = document.getElementById('props-type-icon');
  const title = document.getElementById('props-title-text');
  const idBadge = document.getElementById('props-node-id-badge');

  if (!n) {
    icon.textContent = '';
    title.textContent = 'Properties';
    idBadge.textContent = '';
    propsBody.innerHTML = '<div class="empty-state"><div class="empty-icon"><svg viewBox="0 0 40 40" fill="none" stroke="currentColor" stroke-width="1.5" opacity=".35"><circle cx="20" cy="20" r="16"/><path d="M20 13v7l5 3"/></svg></div><div>Select a node to edit its properties</div></div>';
    return;
  }

  const def = NodeDefs[n.type];
  icon.textContent = def.icon ?? '';
  title.textContent = n.type.toUpperCase();
  idBadge.textContent = n.id;

  // Build HTML
  let html = '';

  // Node-specific props
  html += `<div class="prop-section"><div class="prop-section-title">Layout</div>`;
  def.props.forEach(p => { html += buildPropRowHtml(p, n); });
  html += `</div>`;

  // Button/Action section
  html += buildActionHtml(n);

  html += `<button class="prop-delete" id="prop-del-btn">Delete node</button>`;

  propsBody.innerHTML = html;

  // Wire up regular inputs
  propsBody.querySelectorAll('[data-key]').forEach(el => {
    el.addEventListener('input', () => applyInput(el, n));
    el.addEventListener('change', () => applyInput(el, n));
  });

  // Init Pickr on color mounts
  propsBody.querySelectorAll('.pickr-mount').forEach(el => {
    const key = el.dataset.key;
    const alphaKey = el.dataset.alphaKey || null;
    initColorPicker(
      el,
      n[key] || '#000000',
      alphaKey ? (n[alphaKey] ?? 255) : 255,
      hex => { n[key] = hex; },
      alphaKey ? a => { n[alphaKey] = a; } : null,
    );
  });

  // Wire material picker button
  propsBody.querySelector('.material-pick-btn')?.addEventListener('click', () => {
    ItemPicker.open(n.material, (namespace) => {
      n.material = namespace;
      const inp = propsBody.querySelector('[data-key="material"]');
      if (inp) inp.value = namespace;
      const thumb = propsBody.querySelector('.material-thumb');
      if (thumb) { thumb.src = _matThumbUrl(namespace); thumb.style.opacity = '1'; }
      render();
    });
  });

  // Action wiring
  wireAction(n);

  document.getElementById('prop-del-btn')?.addEventListener('click', deleteSelected);
}

function buildPropRowHtml(p, n) {
  // Virtual props for color pickers
  if (p === '_colorAlpha') {
    return `<div class="prop-row prop-color-row"><label>Color</label>
      <div class="pickr-wrap"><div class="pickr-mount" data-key="color" data-alpha-key="alpha"></div></div></div>`;
  }
  if (p === '_color') {
    return `<div class="prop-row prop-color-row"><label>Color</label>
      <div class="pickr-wrap"><div class="pickr-mount" data-key="color"></div></div></div>`;
  }
  if (p === 'material') return _buildMaterialRow(n);

  const v = n[p];
  const lbl = PROP_LABELS[p] ?? p;

  // Pair x/y and x1/y1 x2/y2
  if (p === 'x' && n.y !== undefined && n.type !== 'line') return propPairRow('x', 'y', n, 'X', 'Y');
  if (p === 'y' && n.x !== undefined && n.type !== 'line') return '';
  if (p === 'boxX') return propPairRow('boxX', 'boxY', n, 'X', 'Y');
  if (p === 'boxY') return '';
  if (p === 'x1') return propPairRow('x1', 'y1', n, 'X1', 'Y1');
  if (p === 'y1') return '';
  if (p === 'x2') return propPairRow('x2', 'y2', n, 'X2', 'Y2');
  if (p === 'y2') return '';
  if (p === 'width' && n.height !== undefined) return propPairRow('width', 'height', n, 'W', 'H');
  if (p === 'height' && n.width !== undefined) return '';

  if (p === 'alignment') return propSelectRow(p, v, ['LEFT', 'CENTER', 'RIGHT'], lbl);
  if (p === 'verticalAlignment') return propSelectRow(p, v, ['TOP', 'MIDDLE', 'BOTTOM'], lbl);
  if (p === 'transform') return propSelectRow(p, v, ['FIXED', 'HEAD', 'GUI', 'GROUND'], lbl);

  if (typeof v === 'boolean') return propCheckRow(p, v, lbl);

  if (p === 'text') return `<div class="prop-row prop-row-top"><label>${lbl}</label><textarea data-key="${p}" rows="3">${escHtml(v)}</textarea></div>`;

  const step = Number.isInteger(v) ? 1 : 0.5;
  return propInputRow(p, 'number', v, lbl, step);
}

function propInputRow(key, type, val, lbl, step) {
  const s = step != null ? `step="${step}"` : '';
  return `<div class="prop-row"><label>${lbl}</label><input type="${type}" data-key="${key}" value="${escHtml(String(val ?? ''))}" ${s} /></div>`;
}
function propCheckRow(key, val, lbl) {
  return `<div class="prop-row"><label>${lbl}</label><input type="checkbox" data-key="${key}" ${val ? 'checked' : ''} /></div>`;
}
function propSelectRow(key, val, opts, lbl) {
  const options = opts.map(o => `<option value="${o}" ${o === val ? 'selected' : ''}>${o}</option>`).join('');
  return `<div class="prop-row"><label>${lbl}</label><select data-key="${key}">${options}</select></div>`;
}
function propPairRow(k1, k2, n, l1, l2) {
  const v1 = n[k1] ?? 0, v2 = n[k2] ?? 0;
  return `
    <div class="prop-pair-row">
      <div class="prop-pair-field">
        <span class="pair-key">${l1}</span>
        <input type="number" data-key="${k1}" value="${v1}" step="1" />
      </div>
      <div class="prop-pair-field">
        <span class="pair-key">${l2}</span>
        <input type="number" data-key="${k2}" value="${v2}" step="1" />
      </div>
    </div>`;
}

function _matThumbUrl(ns) {
  const name = (ns || '').replace('minecraft:', '');
  return `https://raw.githubusercontent.com/InventivetalentDev/minecraft-assets/1.21.4/assets/minecraft/textures/item/${name}.png`;
}

function _buildMaterialRow(n) {
  const ns = n.material || '';
  const thumbUrl = _matThumbUrl(ns);
  return `
    <div class="prop-row material-picker-row">
      <label>Material</label>
      <div class="material-input-wrap">
        <img class="material-thumb" src="${thumbUrl}"
             onerror="this.src='https://raw.githubusercontent.com/InventivetalentDev/minecraft-assets/1.21.4/assets/minecraft/textures/block/${(ns||'').replace('minecraft:','')+'.png'}';this.onerror=function(){this.style.opacity='.15'}" />
        <input type="text" data-key="material" value="${ns}" placeholder="minecraft:stone" class="material-text-input" />
        <button class="material-pick-btn" title="Browse all items">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/></svg>
        </button>
      </div>
    </div>`;
}

function buildActionHtml(n) {
  const btn = n._button || {};
  const t = btn.action?.type || 'NONE';
  const v = btn.action?.value || '';
  const desc = btn.description || '';
  const needsValue = t !== 'NONE';
  return `<div class="prop-section"><div class="prop-section-title">Button Action</div>
    ${propInputRow('_desc', 'text', desc, 'Tooltip')}
    <div class="prop-row"><label>Action</label><select id="action-type-sel">
      <option value="NONE" ${t === 'NONE' ? 'selected' : ''}>None</option>
      <option value="OPEN_URL" ${t === 'OPEN_URL' ? 'selected' : ''}>Open URL</option>
      <option value="PLAYER_COMMAND" ${t === 'PLAYER_COMMAND' ? 'selected' : ''}>Player Command</option>
      <option value="CONSOLE_COMMAND" ${t === 'CONSOLE_COMMAND' ? 'selected' : ''}>Console Command</option>
      <option value="SUGGEST_COMMAND" ${t === 'SUGGEST_COMMAND' ? 'selected' : ''}>Suggest Command</option>
    </select></div>
    <div class="prop-row action-row-value ${needsValue ? 'visible' : ''}" id="action-val-row">
      <label>Value</label><input type="text" id="action-val-input" value="${escHtml(v)}" placeholder="URL or /command…" />
    </div>
  </div>`;
}

function wireAction(n) {
  const typeSel = document.getElementById('action-type-sel');
  const valRow  = document.getElementById('action-val-row');
  const valInp  = document.getElementById('action-val-input');
  const descInp = propsBody.querySelector('[data-key="_desc"]');

  const sync = () => {
    const type = typeSel.value;
    valRow.classList.toggle('visible', type !== 'NONE');
    if (!n._button) n._button = { id: n.id + '_btn', nodeId: n.id, description: '', action: { type: 'NONE', value: '' } };
    n._button.action = { type, value: valInp?.value || '' };
    n._button.description = descInp?.value || '';
  };

  typeSel?.addEventListener('change', sync);
  valInp?.addEventListener('input', sync);
  descInp?.addEventListener('input', sync);
}

function applyInput(el, n) {
  const key = el.dataset.key;
  if (!key || key === '_desc') return;
  let val = el.type === 'checkbox' ? el.checked : el.value;
  if (el.type === 'number') val = parseFloat(val);
  n[key] = val;
  render(); updateLayersList();
}

function refreshPropsInputs(n) {
  propsBody.querySelectorAll('[data-key]').forEach(el => {
    const key = el.dataset.key;
    if (!key || key === '_desc' || !el.matches('input, select, textarea')) return;
    if (el.type === 'checkbox') el.checked = !!n[key];
    else if (n[key] !== undefined) el.value = n[key];
  });
}

function escHtml(s) { return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;'); }

/* ── Layers list ─────────────────────────────────────────── */
let _dragFromIdx = -1;

function updateLayersList() {
  const list = document.getElementById('layers-list');
  const empty = document.getElementById('layers-empty');
  const count = document.getElementById('layer-count');
  count.textContent = state.nodes.length;
  empty.style.display = state.nodes.length ? 'none' : 'block';

  list.innerHTML = '';
  [...state.nodes].reverse().forEach((n, ri) => {
    const realIdx = state.nodes.length - 1 - ri;
    const def = NodeDefs[n.type];
    const li = document.createElement('li');
    li.className = 'layer-item' + (realIdx === state.selectedIdx ? ' selected' : '');
    li.draggable = true;
    li.dataset.realIdx = realIdx;

    li.innerHTML = `
      <span class="layer-drag" title="Drag to reorder">⠿</span>
      <span class="layer-icon">${def?.icon ?? '?'}</span>
      <span class="layer-name">${def?.label(n) ?? n.type}</span>
      <div class="layer-actions">
        <button class="layer-act" data-act="up"  title="Move up (Ctrl+])">↑</button>
        <button class="layer-act" data-act="down" title="Move down (Ctrl+[)">↓</button>
        <button class="layer-act del" data-act="del" title="Delete">✕</button>
      </div>`;

    // Select on click (not on action buttons)
    li.addEventListener('click', e => {
      if (e.target.closest('.layer-actions') || e.target.classList.contains('layer-drag')) return;
      state.selectedIdx = realIdx;
      buildPropsPanel(); render(); updateLayersList();
    });

    // Action buttons
    li.querySelectorAll('.layer-act').forEach(btn => {
      btn.addEventListener('click', e => {
        e.stopPropagation();
        const act = btn.dataset.act;
        if (act === 'up')  moveNode(realIdx, 1);
        if (act === 'down') moveNode(realIdx, -1);
        if (act === 'del') { state.selectedIdx = realIdx; deleteSelected(); }
      });
    });

    // ── Drag-and-drop reorder ──────────────────────────────
    li.addEventListener('dragstart', e => {
      _dragFromIdx = realIdx;
      e.dataTransfer.effectAllowed = 'move';
      e.dataTransfer.setData('text/plain', realIdx);
      setTimeout(() => li.classList.add('dragging'), 0);
    });
    li.addEventListener('dragend', () => {
      li.classList.remove('dragging');
      list.querySelectorAll('.drop-above, .drop-below').forEach(el => el.classList.remove('drop-above', 'drop-below'));
    });
    li.addEventListener('dragover', e => {
      e.preventDefault();
      e.dataTransfer.dropEffect = 'move';
      list.querySelectorAll('.drop-above, .drop-below').forEach(el => el.classList.remove('drop-above', 'drop-below'));
      const rect = li.getBoundingClientRect();
      const mid = rect.top + rect.height / 2;
      li.classList.add(e.clientY < mid ? 'drop-above' : 'drop-below');
    });
    li.addEventListener('dragleave', () => li.classList.remove('drop-above', 'drop-below'));
    li.addEventListener('drop', e => {
      e.preventDefault();
      li.classList.remove('drop-above', 'drop-below');
      const from = _dragFromIdx;
      const to   = realIdx;
      if (from !== to && from >= 0) reorderNodes(from, to);
    });

    list.appendChild(li);
  });
}

/* ── Toolbar controls ────────────────────────────────────── */
const cwInput    = document.getElementById('canvas-w');
const chInput    = document.getElementById('canvas-h');
const zoomRange  = document.getElementById('zoom-range');
const zoomLabel  = document.getElementById('zoom-label');
const gridToggle = document.getElementById('grid-toggle');

cwInput.addEventListener('change', () => { state.canvasW = Math.max(64, parseInt(cwInput.value)); resizeCanvas(); });
chInput.addEventListener('change', () => { state.canvasH = Math.max(64, parseInt(chInput.value)); resizeCanvas(); });
zoomRange.addEventListener('input', () => {
  state.zoom = parseFloat(zoomRange.value);
  zoomLabel.textContent = state.zoom.toFixed(1) + '×';
  resizeCanvas();
});
gridToggle.addEventListener('change', () => { state.grid = gridToggle.checked; render(); });
document.getElementById('btn-undo').addEventListener('click', undo);

/* ── Export ──────────────────────────────────────────────── */
document.getElementById('btn-export').addEventListener('click', () => {
  const json = Serializer.toJson({ ...state });
  const blob = new Blob([json], { type: 'application/json' });
  const a = document.createElement('a');
  a.href = URL.createObjectURL(blob);
  a.download = (state.name || 'panel') + '.hhdui.json';
  a.click();
});

/* ── Import ──────────────────────────────────────────────── */
document.getElementById('btn-import').addEventListener('click', () => document.getElementById('import-file').click());
document.getElementById('import-file').addEventListener('change', e => {
  const file = e.target.files[0]; if (!file) return;
  const reader = new FileReader();
  reader.onload = ev => {
    try {
      const s = Serializer.fromJson(ev.target.result);
      Object.assign(state, { nodes: s.nodes, canvasW: s.canvasW, canvasH: s.canvasH, name: s.name, selectedIdx: -1 });
      cwInput.value = s.canvasW; chInput.value = s.canvasH;
      resizeCanvas(); buildPropsPanel(); updateLayersList();
    } catch (err) { alert('Import failed: ' + err.message); }
  };
  reader.readAsText(file);
  e.target.value = '';
});

/* ── Clear ───────────────────────────────────────────────── */
document.getElementById('btn-clear').addEventListener('click', () => {
  if (!state.nodes.length || confirm('Clear all nodes?')) {
    pushUndo(); state.nodes = []; state.selectedIdx = -1;
    buildPropsPanel(); render(); updateLayersList();
  }
});

/* ── Boot ────────────────────────────────────────────────── */
const _hadSaved = _restoreState();

// Sync controls with restored state
zoomRange.value = state.zoom;
zoomLabel.textContent = state.zoom.toFixed(1) + '×';
cwInput.value = state.canvasW;
chInput.value = state.canvasH;
gridToggle.checked = state.grid;

resizeCanvas();
buildPropsPanel();
updateLayersList();

if (_hadSaved && state.nodes.length) {
  _showSaveIndicator('saved');
}
