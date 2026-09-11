/**
 * editor.js — Main editor: canvas, multi-selection, marquee drag-select, zoom-to-cursor,
 *             spacebar pan, shape toolbars, undo, Pickr color pickers, preview canvas.
 */

/* ── State ────────────────────────────────────────────────── */
const state = {
  nodes: [],
  selectedIdx: -1,
  selectedIdxs: [],         // Array of selected node indices for multi-selection
  canvasW: 256,
  canvasH: 192,
  zoom: 2,
  grid: true,
  name: 'untitled',
  dragging: false,
  dragOffX: 0, dragOffY: 0,
  dragType: null,           // 'move' | 'resize-se' | 'resize-nw' | ... | 'line-p1' | 'line-p2'
  dragStartMx: 0, dragStartMy: 0, // canvas-space mouse at drag start
  dragStartNodes: [],       // snapshot of all selected nodes at drag start
  dragStartNode: null,      // deep-copy of primary node at drag start
  isPanning: false,
  panStartClientX: 0, panStartClientY: 0,
  panStartScrollLeft: 0, panStartScrollTop: 0,
  isSpacePressed: false,
  isBoxSelecting: false,
  boxStartCanvasX: 0, boxStartCanvasY: 0,
  boxCurrCanvasX: 0, boxCurrCanvasY: 0,
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
  state.selectedIdxs = [];
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
    if (state.selectedIdxs.includes(i)) {
      renderSelection(n, state.selectedIdxs.length === 1);
    }
  });

  // Marquee box selection overlay
  if (state.isBoxSelecting) {
    const bx = Math.min(state.boxStartCanvasX, state.boxCurrCanvasX) * z;
    const by = Math.min(state.boxStartCanvasY, state.boxCurrCanvasY) * z;
    const bw = Math.abs(state.boxCurrCanvasX - state.boxStartCanvasX) * z;
    const bh = Math.abs(state.boxCurrCanvasY - state.boxStartCanvasY) * z;
    ctx.save();
    ctx.fillStyle = 'rgba(79, 142, 247, 0.16)';
    ctx.strokeStyle = '#4f8ef7';
    ctx.lineWidth = 1.2;
    ctx.setLineDash([4, 3]);
    ctx.fillRect(bx, by, bw, bh);
    ctx.strokeRect(bx, by, bw, bh);
    ctx.restore();
  }

  // Ghost Drag Preview overlay for multi/single selection move
  if (state.dragging && state.dragType === 'move' && state.selectedIdxs.length > 0) {
    let gMinX = Infinity, gMinY = Infinity, gMaxX = -Infinity, gMaxY = -Infinity;

    ctx.save();
    ctx.fillStyle = 'rgba(79, 142, 247, 0.18)';
    ctx.strokeStyle = '#4f8ef7';
    ctx.lineWidth = 1.5;
    ctx.setLineDash([4, 3]);

    state.selectedIdxs.forEach(idx => {
      const n = state.nodes[idx];
      if (!n) return;
      const r = getNodeBoundingBox(n);
      if (r && r.w > 0 && r.h > 0) {
        ctx.fillRect(r.x * z, r.y * z, r.w * z, r.h * z);
        ctx.strokeRect(r.x * z, r.y * z, r.w * z, r.h * z);
        gMinX = Math.min(gMinX, r.x);
        gMinY = Math.min(gMinY, r.y);
        gMaxX = Math.max(gMaxX, r.x + r.w);
        gMaxY = Math.max(gMaxY, r.y + r.h);
      }
    });

    if (state.selectedIdxs.length > 1 && gMinX < Infinity) {
      // Outer group boundary with position badge
      ctx.strokeStyle = 'rgba(255, 255, 255, 0.85)';
      ctx.lineWidth = 1;
      ctx.setLineDash([2, 2]);
      const gw = (gMaxX - gMinX) * z;
      const gh = (gMaxY - gMinY) * z;
      ctx.strokeRect(gMinX * z - 3, gMinY * z - 3, gw + 6, gh + 6);

      // Floating coordinate badge
      ctx.fillStyle = '#4f8ef7';
      ctx.setLineDash([]);
      ctx.font = 'bold 10px "JetBrains Mono", monospace';
      const badgeText = `X: ${Math.round(gMinX)}, Y: ${Math.round(gMinY)} (${Math.round(gMaxX - gMinX)}×${Math.round(gMaxY - gMinY)})`;
      const badgeW = ctx.measureText(badgeText).width + 12;
      ctx.fillRect(gMinX * z - 3, gMinY * z - 19, badgeW, 16);
      ctx.fillStyle = '#ffffff';
      ctx.textBaseline = 'middle';
      ctx.textAlign = 'left';
      ctx.fillText(badgeText, gMinX * z + 3, gMinY * z - 11);
    }
    ctx.restore();
  }

  updatePreview();
  scheduleSave();
}

/* ── Resize helpers ──────────────────────────────────── */

const HANDLE_CURSORS = {
  nw: 'nw-resize', n: 'n-resize', ne: 'ne-resize',
  e:  'e-resize',  se: 'se-resize',
  s:  's-resize',  sw: 'sw-resize', w: 'w-resize',
  rot: 'crosshair',
};

/** Get bounding rect {x,y,w,h} for rect/shape nodes (canvas coords). */
function getNodeRect(n) {
  if (n.type === 'shape' || n.type === 'background' || n.type === 'block') {
    return { x: n.x, y: n.y, w: n.width, h: n.height };
  }
  if (n.type === 'text') return { x: n.boxX, y: n.boxY, w: n.width, h: n.height };
  return null;
}

function getNodeBoundingBox(n) {
  if (n.type === 'shape' || n.type === 'background' || n.type === 'block') {
    return { x: n.x, y: n.y, w: n.width, h: n.height };
  }
  if (n.type === 'text') {
    return { x: n.boxX, y: n.boxY, w: n.width, h: n.height };
  }
  if (n.type === 'item') {
    const sz = 16 * (n.scale || 0.8);
    return { x: n.x - sz / 2, y: n.y - sz / 2, w: sz, h: sz };
  }
  if (n.type === 'line') {
    return {
      x: Math.min(n.x1, n.x2),
      y: Math.min(n.y1, n.y2),
      w: Math.max(4, Math.abs(n.x2 - n.x1)),
      h: Math.max(4, Math.abs(n.y2 - n.y1)),
    };
  }
  return { x: 0, y: 0, w: 0, h: 0 };
}

function rectsIntersect(r1x, r1y, r1w, r1h, r2x, r2y, r2w, r2h) {
  return !(r2x > r1x + r1w || r2x + r2w < r1x || r2y > r1y + r1h || r2y + r2h < r1y);
}

/** Write bounding rect back onto the node. */
function setNodeRect(n, x, y, w, h) {
  if (n.type === 'shape' || n.type === 'background' || n.type === 'block') {
    n.x = x; n.y = y; n.width = w; n.height = h;
  } else if (n.type === 'text') {
    n.boxX = x; n.boxY = y; n.width = w; n.height = h;
  }
}

/** Return [{x,y,type}] handle array in canvas-pixels. */
function getNodeHandles(n) {
  const z = state.zoom;
  const r = getNodeRect(n);
  if (!r) return [];
  const x1 = r.x * z, y1 = r.y * z;
  const x2 = (r.x + r.w) * z, y2 = (r.y + r.h) * z;
  const mx = (x1 + x2) / 2,   my = (y1 + y2) / 2;
  const rotDist = 22;

  let rawHandles = [
    { x: x1, y: y1, type: 'nw' },
    { x: mx, y: y1, type: 'n'  },
    { x: x2, y: y1, type: 'ne' },
    { x: x2, y: my, type: 'e'  },
    { x: x2, y: y2, type: 'se' },
    { x: mx, y: y2, type: 's'  },
    { x: x1, y: y2, type: 'sw' },
    { x: x1, y: my, type: 'w'  },
  ];

  if (n.type === 'shape') {
    rawHandles.push({ x: mx, y: y1 - rotDist, type: 'rot' });
  }

  const rot = n.rotation || 0;
  if (rot !== 0) {
    const rad = (rot * Math.PI) / 180;
    const cos = Math.cos(rad);
    const sin = Math.sin(rad);
    return rawHandles.map(h => {
      const dx = h.x - mx;
      const dy = h.y - my;
      return {
        x: mx + dx * cos - dy * sin,
        y: my + dx * sin + dy * cos,
        type: h.type,
      };
    });
  }

  return rawHandles;
}

/**
 * Recompute node rect from drag-start snapshot + current mouse delta.
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
function renderSelection(n, showHandles = true) {
  const z = state.zoom;
  ctx.save();

  const drawHandleAt = (hx, hy, type) => {
    ctx.setLineDash([]);
    if (type === 'rot') {
      ctx.fillStyle = '#4f8ef7';
      ctx.strokeStyle = '#ffffff';
      ctx.lineWidth = 1.5;
      ctx.beginPath();
      ctx.arc(hx, hy, 5, 0, Math.PI * 2);
      ctx.fill();
      ctx.stroke();
      return;
    }
    const corner = ['nw','ne','se','sw'].includes(type);
    const hs = corner ? 5 : 4;
    ctx.fillStyle = '#4f8ef7';
    ctx.strokeStyle = 'rgba(255,255,255,.85)';
    ctx.lineWidth = 1;
    ctx.fillRect(hx - hs, hy - hs, hs * 2, hs * 2);
    ctx.strokeRect(hx - hs, hy - hs, hs * 2, hs * 2);
  };

  const handles = getNodeHandles(n);
  if (handles.length) {
    const r = getNodeRect(n);
    const rot = n.rotation || 0;

    ctx.save();
    if (rot !== 0) {
      const cx = (r.x + r.w / 2) * z;
      const cy = (r.y + r.h / 2) * z;
      ctx.translate(cx, cy);
      ctx.rotate((rot * Math.PI) / 180);
      ctx.translate(-cx, -cy);
    }
    ctx.strokeStyle = '#4f8ef7';
    ctx.lineWidth = 1.5;
    ctx.setLineDash([4, 3]);
    ctx.strokeRect(r.x * z - 1, r.y * z - 1, r.w * z + 2, r.h * z + 2);

    if (showHandles && n.type === 'shape') {
      const mx = (r.x + r.w / 2) * z;
      const my = r.y * z;
      ctx.beginPath();
      ctx.setLineDash([2, 2]);
      ctx.moveTo(mx, my);
      ctx.lineTo(mx, my - 22);
      ctx.stroke();
    }
    ctx.restore();

    if (showHandles) {
      handles.forEach(h => drawHandleAt(h.x, h.y, h.type));
    }
  } else if (n.type === 'item') {
    const hs = 8 * (n.scale || 0.8) * z;
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
    if (showHandles) {
      [[n.x1 * z, n.y1 * z], [n.x2 * z, n.y2 * z]].forEach(([hx, hy]) => {
        ctx.setLineDash([]);
        ctx.fillStyle = '#4f8ef7';
        ctx.strokeStyle = 'rgba(255,255,255,.75)';
        ctx.lineWidth = 1;
        ctx.beginPath(); ctx.arc(hx, hy, 5, 0, Math.PI * 2); ctx.fill(); ctx.stroke();
      });
    }
  }
  ctx.restore();
}

/* ── Preview canvas ──────────────────────────────────────── */
function updatePreview() {
  const parent = prevCanvas.parentElement;
  if (!parent) return;
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
  if (n.type === 'shape' && (n.rotation || 0) !== 0) {
    const cx = n.x + n.width / 2;
    const cy = n.y + n.height / 2;
    const rad = (-n.rotation * Math.PI) / 180;
    const cos = Math.cos(rad);
    const sin = Math.sin(rad);
    const dx = px - cx;
    const dy = py - cy;
    const rx = cx + dx * cos - dy * sin;
    const ry = cy + dx * sin + dy * cos;
    return inRect(rx, ry, n.x, n.y, n.width, n.height);
  }
  switch (n.type) {
    case 'shape':
    case 'background':
      return inRect(px, py, n.x, n.y, n.width, n.height);
    case 'text':
      return inRect(px, py, n.boxX, n.boxY, n.width, n.height);
    case 'block':
      return inRect(px, py, n.x, n.y, n.width, n.height);
    case 'item': {
      const hs = 8 * (n.scale || 0.8);
      return Math.abs(px - n.x) <= hs + SLOP && Math.abs(py - n.y) <= hs + SLOP;
    }
    case 'line': {
      const dx = n.x2 - n.x1, dy = n.y2 - n.y1;
      const len2 = dx * dx + dy * dy;
      if (len2 === 0) return dist2(px, py, n.x1, n.y1) <= SLOP;
      const t = Math.max(0, Math.min(1, ((px - n.x1) * dx + (py - n.y1) * dy) / len2));
      return dist2(px, py, n.x1 + t * dx, n.y1 + t * dy) <= (n.thickness || 2) / 2 + SLOP;
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
  if (state.dragging || state.isPanning) return;
  if (state.isSpacePressed) { canvas.style.cursor = 'grab'; return; }

  const sel = state.nodes[state.selectedIdx];
  if (sel && state.selectedIdxs.length === 1) {
    const handle = hitsResizeHandle(sel, mx, my);
    if (handle) { canvas.style.cursor = HANDLE_CURSORS[handle]; return; }
    if (hitsLineEndpoint(sel, mx, my, 1) || hitsLineEndpoint(sel, mx, my, 2)) { canvas.style.cursor = 'crosshair'; return; }
  }
  const idx = hitTest(mx, my);
  canvas.style.cursor = idx !== -1 ? 'move' : 'default';
}

/* ── Zoom toward cursor ────────────────────────────────────── */
function zoomAt(targetZoom, clientX, clientY) {
  const scrollEl = document.getElementById('canvas-scroll');
  const ZOOM_MIN = 0.1, ZOOM_MAX = 20;
  const newZoom = Math.min(ZOOM_MAX, Math.max(ZOOM_MIN, Math.round(targetZoom * 100) / 100));
  if (newZoom === state.zoom) return;

  const oldZoom = state.zoom;
  const rect = canvas.getBoundingClientRect();
  const mouseCanvasX = (clientX - rect.left) / oldZoom;
  const mouseCanvasY = (clientY - rect.top) / oldZoom;

  state.zoom = newZoom;
  zoomRange.value = state.zoom;
  zoomLabel.textContent = state.zoom.toFixed(2) + '×';
  resizeCanvas();

  // Anchor mouse position
  const newRect = canvas.getBoundingClientRect();
  scrollEl.scrollLeft += (newRect.left + mouseCanvasX * newZoom) - clientX;
  scrollEl.scrollTop  += (newRect.top  + mouseCanvasY * newZoom) - clientY;
}

/* ── Mouse events ────────────────────────────────────────── */
canvas.addEventListener('mousemove', e => {
  const r = canvas.getBoundingClientRect();
  const mx = e.clientX - r.left, my = e.clientY - r.top;
  const z = state.zoom;
  const scrollEl = document.getElementById('canvas-scroll');

  // Cursor position display
  document.getElementById('cursor-pos').textContent = `${Math.round(mx / z)}, ${Math.round(my / z)}`;

  // Viewport Pan
  if (state.isPanning) {
    scrollEl.scrollLeft = state.panStartScrollLeft - (e.clientX - state.panStartClientX);
    scrollEl.scrollTop  = state.panStartScrollTop  - (e.clientY - state.panStartClientY);
    return;
  }

  // Box (Marquee) Selection
  if (state.isBoxSelecting) {
    state.boxCurrCanvasX = mx / z;
    state.boxCurrCanvasY = my / z;

    const bx = Math.min(state.boxStartCanvasX, state.boxCurrCanvasX);
    const by = Math.min(state.boxStartCanvasY, state.boxCurrCanvasY);
    const bw = Math.abs(state.boxCurrCanvasX - state.boxStartCanvasX);
    const bh = Math.abs(state.boxCurrCanvasY - state.boxStartCanvasY);

    if (bw > 2 || bh > 2) {
      const hits = [];
      state.nodes.forEach((n, idx) => {
        const bb = getNodeBoundingBox(n);
        if (rectsIntersect(bx, by, bw, bh, bb.x, bb.y, bb.w, bb.h)) {
          hits.push(idx);
        }
      });
      state.selectedIdxs = hits;
      state.selectedIdx = hits.length > 0 ? hits[0] : -1;
      buildPropsPanel(); updateLayersList();
    }
    render();
    return;
  }

  // Multi-node or Single-node Dragging
  if (state.dragging) {
    if (state.dragType === 'move') {
      const ddx = snap(mx / z - state.dragStartMx);
      const ddy = snap(my / z - state.dragStartMy);

      state.dragStartNodes.forEach(item => {
        const n = state.nodes[item.idx];
        const orig = item.orig;
        if (!n || !orig) return;

        if (n.type === 'text') {
          n.boxX = orig.boxX + ddx;
          n.boxY = orig.boxY + ddy;
        } else if (n.type === 'line') {
          n.x1 = orig.x1 + ddx;
          n.y1 = orig.y1 + ddy;
          n.x2 = orig.x2 + ddx;
          n.y2 = orig.y2 + ddy;
        } else {
          n.x = orig.x + ddx;
          n.y = orig.y + ddy;
        }
      });

      const primary = state.nodes[state.selectedIdx];
      if (primary) refreshPropsInputs(primary);
      render(); updateLayersList();
      return;

    } else if (state.dragType === 'rotate') {
      const n = state.nodes[state.selectedIdx];
      if (!n) return;
      const cx = (n.x + n.width / 2) * z;
      const cy = (n.y + n.height / 2) * z;
      let deg = Math.round(Math.atan2(my - cy, mx - cx) * (180 / Math.PI) + 90);
      deg = (deg % 360 + 360) % 360;
      if (e.shiftKey) {
        deg = Math.round(deg / 15) * 15;
      }
      n.rotation = deg;
      refreshPropsInputs(n);
      render(); updateLayersList();
      return;

    } else if (state.dragType.startsWith('resize-')) {
      const n = state.nodes[state.selectedIdx];
      if (!n) return;
      const handle = state.dragType.slice(7);
      const dx = snap((mx - state.dragStartMx) / z);
      const dy = snap((my - state.dragStartMy) / z);
      const res = calcResizeRect(state.dragStartNode, handle, dx, dy);
      if (res) setNodeRect(n, res.x, res.y, res.w, res.h);
      refreshPropsInputs(n);
      render(); updateLayersList();
      return;

    } else if (state.dragType === 'line-p1') {
      const n = state.nodes[state.selectedIdx];
      if (n) { n.x1 = snap(mx / z); n.y1 = snap(my / z); refreshPropsInputs(n); render(); updateLayersList(); }
      return;
    } else if (state.dragType === 'line-p2') {
      const n = state.nodes[state.selectedIdx];
      if (n) { n.x2 = snap(mx / z); n.y2 = snap(my / z); refreshPropsInputs(n); render(); updateLayersList(); }
      return;
    }
  }

  updateCursor(mx, my);
});

canvas.addEventListener('mousedown', e => {
  const r = canvas.getBoundingClientRect();
  const mx = e.clientX - r.left, my = e.clientY - r.top;
  const z = state.zoom;
  const scrollEl = document.getElementById('canvas-scroll');

  // Space held or Middle Mouse Click -> Pan viewport
  if (e.button === 1 || state.isSpacePressed) {
    state.isPanning = true;
    state.panStartClientX = e.clientX;
    state.panStartClientY = e.clientY;
    state.panStartScrollLeft = scrollEl.scrollLeft;
    state.panStartScrollTop = scrollEl.scrollTop;
    canvas.style.cursor = 'grabbing';
    e.preventDefault();
    return;
  }

  // Check resize/rotation handles for single selected node
  const sel = state.nodes[state.selectedIdx];
  if (sel && state.selectedIdxs.length === 1) {
    const handleType = hitsResizeHandle(sel, mx, my);
    if (handleType === 'rot') {
      pushUndo();
      state.dragging = true;
      state.dragType = 'rotate';
      state.dragStartMx = mx; state.dragStartMy = my;
      state.dragStartNode = JSON.parse(JSON.stringify(sel));
      canvas.style.cursor = 'crosshair';
      return;
    }
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
    pushUndo();
    if (e.shiftKey || e.ctrlKey) {
      // Toggle node in multi-selection
      const pos = state.selectedIdxs.indexOf(idx);
      if (pos !== -1) {
        state.selectedIdxs.splice(pos, 1);
        state.selectedIdx = state.selectedIdxs[0] ?? -1;
      } else {
        state.selectedIdxs.push(idx);
        state.selectedIdx = idx;
      }
    } else {
      if (!state.selectedIdxs.includes(idx)) {
        state.selectedIdxs = [idx];
      }
      state.selectedIdx = idx;
    }

    // Prepare multi-drag
    state.dragging = true;
    state.dragType = 'move';
    state.dragStartMx = mx / z;
    state.dragStartMy = my / z;
    state.dragStartNodes = state.selectedIdxs.map(i => ({
      idx: i,
      orig: JSON.parse(JSON.stringify(state.nodes[i])),
    }));

    canvas.style.cursor = 'grabbing';
    buildPropsPanel(); render(); updateLayersList();
  } else {
    // Clicked empty area -> Box selection (PowerPoint marquee)
    if (!e.shiftKey && !e.ctrlKey) {
      state.selectedIdxs = [];
      state.selectedIdx = -1;
    }
    state.isBoxSelecting = true;
    state.boxStartCanvasX = mx / z;
    state.boxStartCanvasY = my / z;
    state.boxCurrCanvasX = mx / z;
    state.boxCurrCanvasY = my / z;

    buildPropsPanel(); render(); updateLayersList();
  }
});

window.addEventListener('mouseup', () => {
  state.isPanning = false;
  state.isBoxSelecting = false;
  state.dragging = false;
  canvas.style.cursor = state.isSpacePressed ? 'grab' : 'default';
  render();
});

function snap(v) { return Math.round(v / 2) * 2; }

/* ── Keyboard shortcuts ──────────────────────────────────── */
window.addEventListener('keydown', e => {
  if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA' || e.target.tagName === 'SELECT') return;

  // Spacebar hold to pan
  if (e.code === 'Space' && !state.isSpacePressed) {
    state.isSpacePressed = true;
    canvas.style.cursor = 'grab';
    e.preventDefault();
    return;
  }

  if ((e.ctrlKey || e.metaKey) && e.key === 'z') { e.preventDefault(); undo(); return; }
  if ((e.ctrlKey || e.metaKey) && e.key === 'd') { e.preventDefault(); duplicateSelected(); return; }

  if (e.key === 'Delete' || e.key === 'Backspace') { deleteSelected(); return; }

  // Select all: Ctrl+A
  if ((e.ctrlKey || e.metaKey) && e.key === 'a') {
    e.preventDefault();
    state.selectedIdxs = state.nodes.map((_, i) => i);
    state.selectedIdx = state.nodes.length > 0 ? 0 : -1;
    buildPropsPanel(); render(); updateLayersList();
    return;
  }

  // Nudge with arrow keys
  const step = e.shiftKey ? 8 : 1;
  if (e.key === 'ArrowUp')    { nudge( 0, -step); e.preventDefault(); }
  if (e.key === 'ArrowDown')  { nudge( 0,  step); e.preventDefault(); }
  if (e.key === 'ArrowLeft')  { nudge(-step, 0);  e.preventDefault(); }
  if (e.key === 'ArrowRight') { nudge( step, 0);  e.preventDefault(); }

  // Layer reordering: Ctrl+[ / Ctrl+]
  if ((e.ctrlKey || e.metaKey) && e.key === '[') { if (state.selectedIdx >= 0) moveNode(state.selectedIdx, -1); e.preventDefault(); }
  if ((e.ctrlKey || e.metaKey) && e.key === ']') { if (state.selectedIdx >= 0) moveNode(state.selectedIdx,  1); e.preventDefault(); }
});

window.addEventListener('keyup', e => {
  if (e.code === 'Space') {
    state.isSpacePressed = false;
    if (!state.isPanning) canvas.style.cursor = 'default';
  }
});

function nudge(dx, dy) {
  if (!state.selectedIdxs.length) return;
  state.selectedIdxs.forEach(idx => {
    const n = state.nodes[idx];
    if (!n) return;
    if (n.type === 'text') { n.boxX += dx; n.boxY += dy; }
    else if (n.type === 'line') { n.x1 += dx; n.y1 += dy; n.x2 += dx; n.y2 += dy; }
    else { n.x = (n.x ?? 0) + dx; n.y = (n.y ?? 0) + dy; }
  });
  if (state.selectedIdx >= 0) refreshPropsInputs(state.nodes[state.selectedIdx]);
  render();
}

/* ── Add nodes ───────────────────────────────────────────── */
document.querySelectorAll('.tool-btn[data-type]').forEach(btn => {
  btn.addEventListener('click', () => {
    const type = btn.dataset.type;
    const def = NodeDefs[type]; if (!def) return;
    pushUndo();
    const node = def.defaults();
    if (type === 'shape' || type === 'background') {
      node.shapeType = activeShapeConfig.shapeType;
      node.outline = activeShapeConfig.brushSize > 0;
      node.outlineThickness = activeShapeConfig.brushSize;
      node.outlineStyle = activeShapeConfig.outlineStyle;
      if (activeShapeConfig.fillMode === 'none') {
        node.alpha = 0;
      }
    }
    // Place roughly centred
    const cx = Math.round(state.canvasW / 2);
    const cy = Math.round(state.canvasH / 2);
    if (type === 'text') { node.boxX = cx - 60; node.boxY = cy - 10; }
    else if (type === 'line') { node.x1 = cx - 40; node.y1 = cy; node.x2 = cx + 40; node.y2 = cy; }
    else { node.x = cx - (node.width ?? 16) / 2; node.y = cy - (node.height ?? 16) / 2; }
    state.nodes.push(node);
    state.selectedIdx = state.nodes.length - 1;
    state.selectedIdxs = [state.selectedIdx];
    buildPropsPanel(); render(); updateLayersList();
  });
});

/* ── Duplicate & delete (multi-select supported) ─────────── */
function duplicateSelected() {
  if (!state.selectedIdxs.length) return;
  pushUndo();
  const newIdxs = [];
  const copies = [];
  state.selectedIdxs.forEach(idx => {
    const node = state.nodes[idx];
    if (!node) return;
    const copy = JSON.parse(JSON.stringify(node));
    copy.id = copy.id + '_copy';
    if (copy.type === 'line') { copy.x1 += 8; copy.y1 += 8; copy.x2 += 8; copy.y2 += 8; }
    else if (copy.type === 'text') { copy.boxX += 8; copy.boxY += 8; }
    else { copy.x = (copy.x ?? 0) + 8; copy.y = (copy.y ?? 0) + 8; }
    copies.push(copy);
  });
  copies.forEach(copy => {
    state.nodes.push(copy);
    newIdxs.push(state.nodes.length - 1);
  });
  state.selectedIdxs = newIdxs;
  state.selectedIdx = newIdxs[0] ?? -1;
  buildPropsPanel(); render(); updateLayersList();
}

function deleteSelected() {
  if (!state.selectedIdxs.length) return;
  pushUndo();
  const sorted = [...state.selectedIdxs].sort((a, b) => b - a);
  sorted.forEach(i => {
    state.nodes.splice(i, 1);
  });
  state.selectedIdxs = [];
  state.selectedIdx = -1;
  buildPropsPanel(); render(); updateLayersList();
}

function moveNode(idx, dir) {
  const to = idx + dir;
  if (to < 0 || to >= state.nodes.length) return;
  pushUndo();
  [state.nodes[idx], state.nodes[to]] = [state.nodes[to], state.nodes[idx]];
  state.selectedIdx = to;
  state.selectedIdxs = [to];
  render(); updateLayersList();
}

function reorderNodes(fromIdx, toIdx) {
  if (fromIdx === toIdx) return;
  pushUndo();
  const node = state.nodes.splice(fromIdx, 1)[0];
  const adj = fromIdx < toIdx ? toIdx - 1 : toIdx;
  state.nodes.splice(adj, 0, node);
  if (state.selectedIdx === fromIdx) {
    state.selectedIdx = adj;
    state.selectedIdxs = [adj];
  }
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
  shapeType: 'Shape',
  x: 'X', y: 'Y', x1: 'X1', y1: 'Y1', x2: 'X2', y2: 'Y2',
  boxX: 'Box X', boxY: 'Box Y', width: 'Width', height: 'Height',
  depth: 'Depth', scale: 'Scale', fontSize: 'Font size',
  contentWidth: 'Content W', leftOffset: 'Offset L', rightOffset: 'Offset R',
  verticalOffset: 'Offset V', thickness: 'Thickness', material: 'Material',
  transform: 'Transform', alignment: 'Align H', verticalAlignment: 'Align V',
  shadow: 'Shadow', seeThrough: 'See-through', doubleSided: 'Double sided',
  cornerRadius: 'Corner Radius', rotation: 'Rotation (°)',
  outline: 'Enable Outline', outlineThickness: 'Outline Thickness',
  outlineStyle: 'Outline Style',
};

function buildPropsPanel() {
  destroyPickrs();
  const n = state.nodes[state.selectedIdx];

  // Update header
  const icon = document.getElementById('props-type-icon');
  const title = document.getElementById('props-title-text');
  const idBadge = document.getElementById('props-node-id-badge');

  if (!n || state.selectedIdxs.length === 0) {
    icon.textContent = '';
    title.textContent = 'Properties';
    idBadge.textContent = '';
    propsBody.innerHTML = '<div class="empty-state"><div class="empty-icon"><svg viewBox="0 0 40 40" fill="none" stroke="currentColor" stroke-width="1.5" opacity=".35"><circle cx="20" cy="20" r="16"/><path d="M20 13v7l5 3"/></svg></div><div>Select one or more nodes to edit properties</div></div>';
    return;
  }

  if (state.selectedIdxs.length > 1) {
    icon.textContent = '☵';
    title.textContent = `${state.selectedIdxs.length} NODES SELECTED`;
    idBadge.textContent = 'Multi';
    propsBody.innerHTML = `
      <div class="prop-section">
        <div class="prop-section-title">Multiple Selection</div>
        <div style="font-size:11px;color:var(--text-dim);margin-bottom:12px;">
          ${state.selectedIdxs.length} items selected.<br/>
          Drag to move all together, press <b>Delete</b> to remove all, or <b>Ctrl+D</b> to duplicate.
        </div>
        <button class="prop-delete" id="prop-del-btn">Delete ${state.selectedIdxs.length} nodes</button>
      </div>`;
    document.getElementById('prop-del-btn')?.addEventListener('click', deleteSelected);
    return;
  }

  const def = NodeDefs[n.type];
  icon.textContent = def.icon ?? '';
  title.textContent = n.type.toUpperCase();
  idBadge.textContent = n.id;

  // Build HTML
  let html = '';

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
  if (p === '_colorAlpha') {
    return `<div class="prop-row prop-color-row"><label>Fill Color</label>
      <div class="pickr-wrap"><div class="pickr-mount" data-key="color" data-alpha-key="alpha"></div></div></div>`;
  }
  if (p === '_color') {
    return `<div class="prop-row prop-color-row"><label>Color</label>
      <div class="pickr-wrap"><div class="pickr-mount" data-key="color"></div></div></div>`;
  }
  if (p === '_outlineColorAlpha') {
    if (!n.outline) return '';
    return `<div class="prop-row prop-color-row"><label>Outline Color</label>
      <div class="pickr-wrap"><div class="pickr-mount" data-key="outlineColor" data-alpha-key="outlineAlpha"></div></div></div>`;
  }
  if (p === 'outlineThickness' || p === 'outlineStyle') {
    if (!n.outline) return '';
  }
  if (p === 'cornerRadius' && n.shapeType !== 'rounded_rect') {
    return '';
  }
  if (p === 'shapeType') {
    let optsHtml = '';
    SHAPE_CATEGORIES.forEach(cat => {
      optsHtml += `<optgroup label="${cat.name}">`;
      cat.shapes.forEach(s => {
        optsHtml += `<option value="${s.id}" ${(n.shapeType || 'rect') === s.id ? 'selected' : ''}>${s.name}</option>`;
      });
      optsHtml += `</optgroup>`;
    });
    return `<div class="prop-row"><label>Shape Type</label><select data-key="shapeType">${optsHtml}</select></div>`;
  }
  if (p === 'outlineStyle') {
    return propSelectRow('outlineStyle', n.outlineStyle || 'solid', ['solid', 'dashed', 'dotted'], 'Outline Style');
  }
  if (p === 'rotation') {
    const rot = Math.round((((n.rotation || 0) % 360) + 360) % 360);
    return `
      <div class="prop-row prop-rotation-row">
        <label>Rotation (°)</label>
        <div style="display:flex;align-items:center;gap:6px;flex:1;">
          <input type="range" min="0" max="360" step="1" value="${rot}" data-key="rotation" style="flex:1;cursor:pointer;accent-color:#4f8ef7;" />
          <input type="number" min="0" max="360" step="1" value="${rot}" data-key="rotation" style="width:52px;text-align:center;" />
        </div>
      </div>`;
  }
  if (p === 'material') return _buildMaterialRow(n);

  const v = n[p];
  const lbl = PROP_LABELS[p] ?? p;

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
  if (el.type === 'number' || el.type === 'range') val = parseFloat(val) || 0;
  n[key] = val;
  if (key === 'outline' || key === 'shapeType') {
    buildPropsPanel();
  } else if (key === 'rotation') {
    refreshPropsInputs(n);
  }
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
    const isSelected = state.selectedIdxs.includes(realIdx);
    const li = document.createElement('li');
    li.className = 'layer-item' + (isSelected ? ' selected' : '');
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
      if (e.shiftKey || e.ctrlKey) {
        const pos = state.selectedIdxs.indexOf(realIdx);
        if (pos !== -1) state.selectedIdxs.splice(pos, 1);
        else state.selectedIdxs.push(realIdx);
        state.selectedIdx = state.selectedIdxs[0] ?? -1;
      } else {
        state.selectedIdx = realIdx;
        state.selectedIdxs = [realIdx];
      }
      buildPropsPanel(); render(); updateLayersList();
    });

    // Action buttons
    li.querySelectorAll('.layer-act').forEach(btn => {
      btn.addEventListener('click', e => {
        e.stopPropagation();
        const act = btn.dataset.act;
        if (act === 'up')  moveNode(realIdx, 1);
        if (act === 'down') moveNode(realIdx, -1);
        if (act === 'del') {
          state.selectedIdxs = [realIdx];
          state.selectedIdx = realIdx;
          deleteSelected();
        }
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

/* ── Active Shape Toolbar Configuration ──────────────────── */
const activeShapeConfig = {
  shapeType: 'rect',
  brushSize: 2,
  outlineStyle: 'solid',
  fillMode: 'solid',
};

(function initShapeToolbar() {
  const pickerBtn  = document.getElementById('btn-shape-picker');
  const dropdown   = document.getElementById('shape-picker-dropdown');
  const iconSpan   = document.getElementById('shape-picker-icon');
  const nameSpan   = document.getElementById('shape-picker-name');
  const brushInput = document.getElementById('shape-brush-size');
  const styleSelect= document.getElementById('shape-outline-style');
  const fillSelect = document.getElementById('shape-fill-mode');
  const addBtn     = document.getElementById('btn-add-active-shape');
  const decBtn     = document.getElementById('shape-brush-dec');
  const incBtn     = document.getElementById('shape-brush-inc');

  if (!pickerBtn || !dropdown) return;

  // Build dropdown categories HTML matching reference image
  let dHtml = '';
  SHAPE_CATEGORIES.forEach(cat => {
    dHtml += `<div class="shape-cat-section">`;
    dHtml += `<div class="shape-cat-title">${cat.name}</div>`;
    dHtml += `<div class="shape-cat-grid">`;
    cat.shapes.forEach(s => {
      const activeCls = s.id === activeShapeConfig.shapeType ? ' active' : '';
      dHtml += `
        <div class="shape-item${activeCls}" data-shape-id="${s.id}" title="${s.name}">
          <span class="shape-item-icon"><svg viewBox="0 0 20 20">${s.icon}</svg></span>
          <span class="shape-item-name">${s.name}</span>
        </div>`;
    });
    dHtml += `</div></div>`;
  });
  dropdown.innerHTML = dHtml;

  // Toggle dropdown
  pickerBtn.addEventListener('click', e => {
    e.stopPropagation();
    const isOpen = !dropdown.hidden;
    dropdown.hidden = isOpen;
    pickerBtn.classList.toggle('open', !isOpen);
  });

  document.addEventListener('click', e => {
    if (!dropdown.hidden && !dropdown.contains(e.target) && e.target !== pickerBtn) {
      dropdown.hidden = true;
      pickerBtn.classList.remove('open');
    }
  });

  // Pick shape from popover
  dropdown.querySelectorAll('.shape-item').forEach(item => {
    item.addEventListener('click', () => {
      const shapeId = item.dataset.shapeId;
      activeShapeConfig.shapeType = shapeId;

      dropdown.querySelectorAll('.shape-item').forEach(el => el.classList.toggle('active', el.dataset.shapeId === shapeId));
      const def = getShapeDef(shapeId);
      iconSpan.innerHTML = `<svg viewBox="0 0 20 20" fill="currentColor">${def.icon}</svg>`;
      nameSpan.textContent = def.name;
      dropdown.hidden = true;
      pickerBtn.classList.remove('open');

      // If a shape node is selected, change its shape
      if (state.selectedIdx >= 0) {
        const sel = state.nodes[state.selectedIdx];
        if (sel && (sel.type === 'shape' || sel.type === 'background')) {
          pushUndo();
          sel.shapeType = shapeId;
          buildPropsPanel(); render(); updateLayersList();
        }
      }
    });
  });

  // Brush size stepper & input
  brushInput?.addEventListener('input', () => {
    activeShapeConfig.brushSize = Math.max(0, parseInt(brushInput.value) || 0);
  });
  decBtn?.addEventListener('click', () => {
    activeShapeConfig.brushSize = Math.max(0, (parseInt(brushInput.value) || 2) - 1);
    brushInput.value = activeShapeConfig.brushSize;
  });
  incBtn?.addEventListener('click', () => {
    activeShapeConfig.brushSize = (parseInt(brushInput.value) || 2) + 1;
    brushInput.value = activeShapeConfig.brushSize;
  });

  styleSelect?.addEventListener('change', () => {
    activeShapeConfig.outlineStyle = styleSelect.value;
  });
  fillSelect?.addEventListener('change', () => {
    activeShapeConfig.fillMode = fillSelect.value;
  });

  // Add Active Shape Button
  addBtn?.addEventListener('click', () => {
    pushUndo();
    const node = NodeDefs.shape.defaults();
    node.shapeType = activeShapeConfig.shapeType;
    node.outline = activeShapeConfig.brushSize > 0;
    node.outlineThickness = activeShapeConfig.brushSize;
    node.outlineStyle = activeShapeConfig.outlineStyle;
    if (activeShapeConfig.fillMode === 'none') {
      node.alpha = 0;
    }
    const cx = Math.round(state.canvasW / 2);
    const cy = Math.round(state.canvasH / 2);
    node.x = cx - node.width / 2;
    node.y = cy - node.height / 2;
    state.nodes.push(node);
    state.selectedIdx = state.nodes.length - 1;
    state.selectedIdxs = [state.selectedIdx];
    buildPropsPanel(); render(); updateLayersList();
  });
})();

/* ── Toolbar controls ────────────────────────────────────── */
const cwInput    = document.getElementById('canvas-w');
const chInput    = document.getElementById('canvas-h');
const zoomRange  = document.getElementById('zoom-range');
const zoomLabel  = document.getElementById('zoom-label');
const gridToggle = document.getElementById('grid-toggle');

cwInput.addEventListener('change', () => { state.canvasW = Math.max(0, parseInt(cwInput.value) || 0); resizeCanvas(); });
chInput.addEventListener('change', () => { state.canvasH = Math.max(0, parseInt(chInput.value) || 0); resizeCanvas(); });
zoomRange.addEventListener('input', () => {
  state.zoom = parseFloat(zoomRange.value);
  zoomLabel.textContent = state.zoom.toFixed(1) + '×';
  resizeCanvas();
});
gridToggle.addEventListener('change', () => { state.grid = gridToggle.checked; render(); });
document.getElementById('btn-undo').addEventListener('click', undo);

/* ── Accurate cursor-anchored zoom ───────────────────────── */
function zoomAt(targetZoom, clientX, clientY) {
  const newZoom = Math.min(10.0, Math.max(0.2, targetZoom));
  if (Math.abs(newZoom - state.zoom) < 0.001) return;

  const wrapper = document.getElementById('canvas-scroll');
  const rect = canvas.getBoundingClientRect();

  // If clientX/clientY not specified, anchor at center of canvas-scroll viewport
  let cx = clientX, cy = clientY;
  if (cx === undefined || cy === undefined) {
    const wrapRect = wrapper.getBoundingClientRect();
    cx = wrapRect.left + wrapRect.width / 2;
    cy = wrapRect.top + wrapRect.height / 2;
  }

  // Exact point on canvas in unscaled logical coordinates before zoom
  const canvasX = (cx - rect.left) / state.zoom;
  const canvasY = (cy - rect.top) / state.zoom;

  // Apply new zoom factor
  state.zoom = newZoom;
  zoomRange.value = newZoom;
  zoomLabel.textContent = newZoom.toFixed(1) + '×';
  resizeCanvas();

  // Re-anchor to keep (canvasX, canvasY) fixed precisely under (cx, cy)
  const newRect = canvas.getBoundingClientRect();
  const targetScreenX = newRect.left + canvasX * newZoom;
  const targetScreenY = newRect.top + canvasY * newZoom;
  wrapper.scrollLeft += (targetScreenX - cx);
  wrapper.scrollTop += (targetScreenY - cy);
}

// Zoom toolbar buttons
document.getElementById('btn-zoom-in')?.addEventListener('click', () => zoomAt(state.zoom + 0.4));
document.getElementById('btn-zoom-out')?.addEventListener('click', () => zoomAt(state.zoom - 0.4));
document.getElementById('btn-zoom-reset')?.addEventListener('click', () => zoomAt(1.0));

/* ── Mouse wheel zoom to cursor position ─────────────────── */
(function initScrollZoom() {
  const wrapper = document.getElementById('canvas-scroll');
  const ZOOM_SPEED = 0.0015;

  wrapper.addEventListener('wheel', e => {
    e.preventDefault();
    const delta = -e.deltaY * ZOOM_SPEED * state.zoom;
    zoomAt(state.zoom + delta, e.clientX, e.clientY);
  }, { passive: false });
})();

/* ── Theme Switcher (Dark / Light) ───────────────────────── */
const THEME_KEY = 'hhdui_theme';
function applyTheme(theme) {
  if (theme === 'light') {
    document.body.dataset.theme = 'light';
  } else {
    delete document.body.dataset.theme;
  }
  try { localStorage.setItem(THEME_KEY, theme); } catch {}
}

const savedTheme = localStorage.getItem(THEME_KEY) || 'dark';
applyTheme(savedTheme);

document.getElementById('btn-theme-toggle')?.addEventListener('click', () => {
  const isLight = document.body.dataset.theme === 'light';
  applyTheme(isLight ? 'dark' : 'light');
});

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

/* ── Scene Panel ─────────────────────────────────────────────
   Blender/Blockbench-style environment & viewport controls.
   Lets you test UI layout against different Minecraft backgrounds,
   upload custom images, adjust opacity, blur, and brightness.
────────────────────────────────────────────────────────────── */
const SCENE_STORAGE_KEY = 'hhdui_scene_v1';

const sceneState = {
  env: 'dark',
  customBg: '#1a1a2e',
  imageSrc: '',
  imageName: 'Ảnh của tôi',
  imageFit: 'cover',   // 'cover' | 'contain' | 'center'
  sceneOpacity: 100,   // 0 - 100%
  sceneBlur: 0,        // 0 - 20px
  sceneBrightness: 100,// 10 - 100%
  gridStyle: 'dots',   // 'dots' | 'lines' | 'cross'
  gridSize: 4,
  gridColor: 'white',  // 'white' | 'cyan' | 'red'
  canvasOpacity: 100,
  checkerboard: false,
};

function saveSceneState() {
  try {
    const clone = { ...sceneState };
    // If base64 image is too large (>2MB), avoid breaking localStorage quota
    if (clone.imageSrc && clone.imageSrc.length > 2500000) {
      clone.imageSrc = ''; // Keep other settings
    }
    localStorage.setItem(SCENE_STORAGE_KEY, JSON.stringify(clone));
  } catch (e) {
    console.warn('[HaoHan Builder] scene localStorage write failed', e);
  }
}

function restoreSceneState() {
  try {
    const raw = localStorage.getItem(SCENE_STORAGE_KEY);
    if (!raw) return false;
    const s = JSON.parse(raw);
    if (!s) return false;
    Object.assign(sceneState, s);
    return true;
  } catch { return false; }
}

const ENV_LABELS = {
  dark: '',
  overworld_day: 'Overworld · Ngày',
  overworld_night: 'Overworld · Đêm',
  nether: 'Nether',
  end: 'The End',
  custom: 'Màu nền tùy chọn',
  image: 'Ảnh tùy chọn',
};

const ENV_BG_CLASS = {
  dark: '',
  overworld_day: 'env-overworld-day',
  overworld_night: 'env-overworld-night',
  nether: 'env-nether',
  end: 'env-end',
  custom: 'env-custom',
  image: 'env-image',
};

const ENV_CANVAS_BG = {
  dark: '#0a0e18',
  overworld_day: 'rgba(0,0,0,0)',
  overworld_night: 'rgba(0,0,0,0)',
  nether: 'rgba(0,0,0,0)',
  end: 'rgba(0,0,0,0)',
  custom: 'rgba(0,0,0,0)',
  image: 'rgba(0,0,0,0)',
};

const GRID_COLORS = {
  white: 'rgba(255,255,255,0.06)',
  cyan:  'rgba(0,255,255,0.08)',
  red:   'rgba(255,80,80,0.07)',
};

/* Render background on canvas */
function _renderSceneBackground(ctx2d, w, h) {
  const opacity = sceneState.canvasOpacity / 100;
  if (sceneState.checkerboard) {
    const cs = 8;
    for (let row = 0; row * cs < h; row++) {
      for (let col = 0; col * cs < w; col++) {
        ctx2d.fillStyle = (row + col) % 2 === 0 ? 'rgba(80,80,80,0.35)' : 'rgba(120,120,120,0.35)';
        ctx2d.fillRect(col * cs, row * cs, cs, cs);
      }
    }
  }
  const bg = ENV_CANVAS_BG[sceneState.env] || '#0a0e18';
  ctx2d.globalAlpha = opacity;
  ctx2d.fillStyle = bg === 'rgba(0,0,0,0)' ? 'rgba(10,14,24,0.35)' : bg;
  ctx2d.fillRect(0, 0, w, h);
  ctx2d.globalAlpha = 1;
}

function _renderSceneGrid(ctx2d, w, h, z) {
  if (!state.grid) return;
  const step = sceneState.gridSize * z;
  const color = GRID_COLORS[sceneState.gridColor] || GRID_COLORS.white;
  ctx2d.save();
  ctx2d.strokeStyle = color;
  ctx2d.fillStyle = color;
  ctx2d.lineWidth = 1;

  if (sceneState.gridStyle === 'dots') {
    for (let x = 0; x <= w; x += step) {
      for (let y = 0; y <= h; y += step) {
        ctx2d.beginPath();
        ctx2d.arc(x, y, 0.8, 0, Math.PI * 2);
        ctx2d.fill();
      }
    }
  } else if (sceneState.gridStyle === 'lines') {
    for (let x = 0; x <= w; x += step) { ctx2d.beginPath(); ctx2d.moveTo(x,0); ctx2d.lineTo(x,h); ctx2d.stroke(); }
    for (let y = 0; y <= h; y += step) { ctx2d.beginPath(); ctx2d.moveTo(0,y); ctx2d.lineTo(w,y); ctx2d.stroke(); }
  } else if (sceneState.gridStyle === 'cross') {
    const hs = 2.5;
    for (let x = step; x < w; x += step) {
      for (let y = step; y < h; y += step) {
        ctx2d.beginPath();
        ctx2d.moveTo(x - hs, y); ctx2d.lineTo(x + hs, y);
        ctx2d.moveTo(x, y - hs); ctx2d.lineTo(x, y + hs);
        ctx2d.stroke();
      }
    }
  }
  ctx2d.restore();
}

/* Monkey-patch render() to use scene settings */
(function patchRender() {
  window.render = function renderPatched() {
    const z = state.zoom;
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    _renderSceneBackground(ctx, canvas.width, canvas.height);
    _renderSceneGrid(ctx, canvas.width, canvas.height, z);
    // Nodes (back to front)
    state.nodes.forEach((n, i) => {
      NodeDefs[n.type]?.render(ctx, n, z);
      if (i === state.selectedIdx) renderSelection(n);
    });
    // Update preview directly
    _updateScenePreview();
    scheduleSave();
  };
})();

function _updateScenePreview() {
  const parent = prevCanvas.parentElement;
  if (!parent) return;
  const maxW = parent.clientWidth - 20;
  const ratio = state.canvasH / state.canvasW;
  const pw = Math.min(maxW, 230);
  const ph = Math.round(pw * ratio);
  if (prevCanvas.width !== pw || prevCanvas.height !== ph) {
    prevCanvas.width = pw;
    prevCanvas.height = ph;
  }
  const pz = pw / state.canvasW;
  _renderSceneBackground(prevCtx, pw, ph);
  _renderSceneGrid(prevCtx, pw, ph, pz);
  state.nodes.forEach(n => NodeDefs[n.type]?.render(prevCtx, n, pz));
}

/* Update visual filters on the scene environment layer */
function _updateSceneFilters() {
  const envBg = document.getElementById('scene-env-bg');
  if (!envBg) return;
  envBg.style.setProperty('--scene-opacity', (sceneState.sceneOpacity / 100).toString());
  envBg.style.setProperty('--scene-blur', `${sceneState.sceneBlur}px`);
  envBg.style.setProperty('--scene-brightness', (sceneState.sceneBrightness / 100).toString());
}

/* Apply environment change */
function applyEnvironment(env) {
  sceneState.env = env;
  const envBg = document.getElementById('scene-env-bg');
  const envLabel = document.getElementById('scene-env-label');
  const imageRow = document.getElementById('sp-image-row');

  // Reset classes & inline background
  envBg.className = '';
  envBg.style.backgroundImage = '';
  envBg.style.background = '';

  const cls = ENV_BG_CLASS[env];
  if (cls) {
    envBg.classList.add('visible', cls);
  }

  // Custom color env
  if (env === 'custom') {
    envBg.style.background = sceneState.customBg;
    envBg.classList.add('visible');
  }

  // Custom image env
  if (env === 'image') {
    if (sceneState.imageSrc) {
      envBg.classList.add('visible', 'env-image');
      envBg.style.backgroundImage = `url("${sceneState.imageSrc}")`;
      envBg.style.backgroundSize = sceneState.imageFit || 'cover';
      envBg.style.backgroundPosition = 'center';
    }
    if (imageRow) imageRow.style.display = 'flex';
  } else {
    if (imageRow) imageRow.style.display = 'none';
  }

  if (envLabel) envLabel.textContent = ENV_LABELS[env] || '';
  _updateSceneFilters();
  saveSceneState();
  window.render();
}

function _applyImageEnv(src, name = 'Ảnh của tôi') {
  sceneState.imageSrc = src;
  sceneState.imageName = name;
  sceneState.env = 'image';

  // Update thumbnail in button
  const preview = document.getElementById('sp-image-preview');
  if (preview) {
    preview.style.backgroundImage = `url("${src}")`;
    preview.style.backgroundSize = 'cover';
    preview.style.backgroundPosition = 'center';
    preview.innerHTML = '';
  }

  // Update image info block in scene panel
  const info = document.getElementById('sp-image-info');
  const thumb = document.getElementById('sp-current-thumb');
  const nameEl = document.getElementById('sp-image-name');
  if (info && thumb && nameEl) {
    thumb.style.backgroundImage = `url("${src}")`;
    nameEl.textContent = name;
    info.style.display = 'flex';
  }

  _setEnvActive('image');
  applyEnvironment('image');
}

function _clearImageEnv() {
  sceneState.imageSrc = '';
  sceneState.imageName = 'Ảnh của tôi';
  const preview = document.getElementById('sp-image-preview');
  if (preview) {
    preview.style.backgroundImage = '';
    preview.innerHTML = `<svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.5" style="width:14px;height:14px;opacity:.8"><rect x="2" y="4" width="16" height="12" rx="2"/><circle cx="7" cy="8" r="1.5"/><polyline points="2,14 6,10 10,13 13,10 18,14"/></svg>`;
  }
  const info = document.getElementById('sp-image-info');
  if (info) info.style.display = 'none';
  const input = document.getElementById('sp-image-url');
  if (input) input.value = '';

  applyEnvironment('image');
}

/* Update active env button */
function _setEnvActive(env) {
  document.querySelectorAll('.sp-env-btn').forEach(b => b.classList.toggle('active', b.dataset.env === env));
}

/* Update active seg-btn in a group */
function _setSegActive(groupId, key, val) {
  document.querySelectorAll(`#${groupId} .sp-seg-btn`).forEach(b =>
    b.classList.toggle('active', (b.dataset[key] || b.dataset.size || b.dataset.style || b.dataset.color) === String(val))
  );
}

/* ── Wire up Scene Panel controls ─────────────────────────── */
(function initScenePanel() {
  restoreSceneState();

  const btn   = document.getElementById('scene-panel-btn');
  const panel = document.getElementById('scene-panel');
  const canvasWrap = document.getElementById('canvas-wrapper');

  /* Toggle open/close */
  btn.addEventListener('click', e => {
    e.stopPropagation();
    const isOpen = !panel.hidden;
    panel.hidden = isOpen;
    btn.classList.toggle('open', !isOpen);
  });

  /* Close on outside click */
  document.addEventListener('click', e => {
    if (!panel.hidden && !panel.contains(e.target) && e.target !== btn) {
      panel.hidden = true;
      btn.classList.remove('open');
    }
  });

  /* ── Environment buttons ─────────────────────── */
  document.querySelectorAll('.sp-env-btn').forEach(b => {
    b.addEventListener('click', () => {
      const env = b.dataset.env;
      if (env === 'custom') {
        document.getElementById('sp-bg-color-input').click();
        return;
      }
      _setEnvActive(env);
      applyEnvironment(env);
    });
  });

  /* Custom Image dropzone & file upload */
  const dropzone = document.getElementById('sp-dropzone');
  const fileInput = document.getElementById('sp-image-upload');

  if (dropzone && fileInput) {
    dropzone.addEventListener('click', e => {
      if (e.target.tagName !== 'LABEL') fileInput.click();
    });

    dropzone.addEventListener('dragover', e => {
      e.preventDefault();
      dropzone.classList.add('dragover');
    });
    dropzone.addEventListener('dragleave', () => dropzone.classList.remove('dragover'));
    dropzone.addEventListener('drop', e => {
      e.preventDefault();
      dropzone.classList.remove('dragover');
      const file = e.dataTransfer.files[0];
      if (file && file.type.startsWith('image/')) {
        const reader = new FileReader();
        reader.onload = ev => _applyImageEnv(ev.target.result, file.name);
        reader.readAsDataURL(file);
      }
    });

    fileInput.addEventListener('change', e => {
      const file = e.target.files[0];
      if (!file) return;
      const reader = new FileReader();
      reader.onload = ev => _applyImageEnv(ev.target.result, file.name);
      reader.readAsDataURL(file);
      e.target.value = '';
    });
  }

  /* Global drag & drop on canvas area for images */
  if (canvasWrap) {
    canvasWrap.addEventListener('dragover', e => {
      if (e.dataTransfer.types.includes('Files')) {
        e.preventDefault();
        canvasWrap.classList.add('drag-active');
      }
    });
    canvasWrap.addEventListener('dragleave', e => {
      if (!canvasWrap.contains(e.relatedTarget)) {
        canvasWrap.classList.remove('drag-active');
      }
    });
    canvasWrap.addEventListener('drop', e => {
      canvasWrap.classList.remove('drag-active');
      const file = e.dataTransfer.files[0];
      if (file && file.type.startsWith('image/')) {
        e.preventDefault();
        const reader = new FileReader();
        reader.onload = ev => _applyImageEnv(ev.target.result, file.name);
        reader.readAsDataURL(file);
      }
    });
  }

  /* Clipboard paste support for images (Ctrl+V) */
  window.addEventListener('paste', e => {
    if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') return;
    const items = e.clipboardData?.items;
    if (!items) return;
    for (let i = 0; i < items.length; i++) {
      if (items[i].type.indexOf('image') !== -1) {
        const file = items[i].getAsFile();
        if (file) {
          const reader = new FileReader();
          reader.onload = ev => _applyImageEnv(ev.target.result, 'Clipboard Image');
          reader.readAsDataURL(file);
          break;
        }
      }
    }
  });

  /* Image URL apply */
  const urlInput = document.getElementById('sp-image-url');
  const urlApply = document.getElementById('sp-image-url-apply');
  if (urlApply && urlInput) {
    urlApply.addEventListener('click', () => {
      const url = urlInput.value.trim();
      if (url) _applyImageEnv(url, 'URL Image');
    });
    urlInput.addEventListener('keydown', e => {
      if (e.key === 'Enter') {
        const url = e.target.value.trim();
        if (url) _applyImageEnv(url, 'URL Image');
      }
    });
  }

  /* Image fit toggle button (cover -> contain -> center) */
  const fitBtn = document.getElementById('sp-image-fit-btn');
  if (fitBtn) {
    fitBtn.textContent = (sceneState.imageFit || 'cover').toUpperCase();
    fitBtn.addEventListener('click', () => {
      const fits = ['cover', 'contain', 'center'];
      const nextIdx = (fits.indexOf(sceneState.imageFit) + 1) % fits.length;
      sceneState.imageFit = fits[nextIdx];
      fitBtn.textContent = sceneState.imageFit.toUpperCase();
      const envBg = document.getElementById('scene-env-bg');
      if (envBg) envBg.style.backgroundSize = sceneState.imageFit;
      saveSceneState();
    });
  }

  /* Image clear button */
  const clearBtn = document.getElementById('sp-image-clear-btn');
  if (clearBtn) {
    clearBtn.addEventListener('click', _clearImageEnv);
  }

  /* Custom color picker */
  const colorInput = document.getElementById('sp-bg-color-input');
  const customPreview = document.getElementById('sp-custom-preview');
  if (colorInput) {
    colorInput.value = sceneState.customBg || '#0a0e18';
    if (customPreview) customPreview.style.background = colorInput.value;
    const updateCustom = () => {
      sceneState.customBg = colorInput.value;
      if (customPreview) customPreview.style.background = colorInput.value;
      _setEnvActive('custom');
      applyEnvironment('custom');
    };
    colorInput.addEventListener('input', updateCustom);
    colorInput.addEventListener('change', updateCustom);
  }

  /* ── Scene Opacity Slider ─────────────────────── */
  const sceneOpacitySlider = document.getElementById('sp-scene-opacity');
  const sceneOpacityVal = document.getElementById('sp-scene-opacity-val');
  if (sceneOpacitySlider) {
    sceneOpacitySlider.value = sceneState.sceneOpacity ?? 100;
    if (sceneOpacityVal) sceneOpacityVal.textContent = sceneOpacitySlider.value + '%';
    sceneOpacitySlider.addEventListener('input', () => {
      sceneState.sceneOpacity = parseInt(sceneOpacitySlider.value);
      if (sceneOpacityVal) sceneOpacityVal.textContent = sceneState.sceneOpacity + '%';
      _updateSceneFilters();
      saveSceneState();
    });
  }

  /* ── Scene Blur Slider ────────────────────────── */
  const sceneBlurSlider = document.getElementById('sp-scene-blur');
  const sceneBlurVal = document.getElementById('sp-scene-blur-val');
  if (sceneBlurSlider) {
    sceneBlurSlider.value = sceneState.sceneBlur ?? 0;
    if (sceneBlurVal) sceneBlurVal.textContent = sceneBlurSlider.value + 'px';
    sceneBlurSlider.addEventListener('input', () => {
      sceneState.sceneBlur = parseInt(sceneBlurSlider.value);
      if (sceneBlurVal) sceneBlurVal.textContent = sceneState.sceneBlur + 'px';
      _updateSceneFilters();
      saveSceneState();
    });
  }

  /* ── Scene Brightness Slider ──────────────────── */
  const sceneBrightSlider = document.getElementById('sp-scene-brightness');
  const sceneBrightVal = document.getElementById('sp-scene-brightness-val');
  if (sceneBrightSlider) {
    sceneBrightSlider.value = sceneState.sceneBrightness ?? 100;
    if (sceneBrightVal) sceneBrightVal.textContent = sceneBrightSlider.value + '%';
    sceneBrightSlider.addEventListener('input', () => {
      sceneState.sceneBrightness = parseInt(sceneBrightSlider.value);
      if (sceneBrightVal) sceneBrightVal.textContent = sceneState.sceneBrightness + '%';
      _updateSceneFilters();
      saveSceneState();
    });
  }

  /* ── Grid visible toggle ─────────────────────── */
  const gridVisibleCheckbox = document.getElementById('sp-grid-visible');
  if (gridVisibleCheckbox) {
    gridVisibleCheckbox.checked = state.grid;
    gridVisibleCheckbox.addEventListener('change', e => {
      state.grid = e.target.checked;
      document.getElementById('grid-toggle').checked = state.grid;
      window.render();
    });
  }
  document.getElementById('grid-toggle').addEventListener('change', () => {
    if (gridVisibleCheckbox) gridVisibleCheckbox.checked = state.grid;
  });

  /* ── Grid style ──────────────────────────────── */
  _setSegActive('sp-grid-style', 'style', sceneState.gridStyle);
  document.querySelectorAll('#sp-grid-style .sp-seg-btn').forEach(b => {
    b.addEventListener('click', () => {
      sceneState.gridStyle = b.dataset.style;
      _setSegActive('sp-grid-style', 'style', sceneState.gridStyle);
      saveSceneState();
      window.render();
    });
  });

  /* ── Grid size ───────────────────────────────── */
  _setSegActive('sp-grid-size', 'size', sceneState.gridSize);
  document.querySelectorAll('#sp-grid-size .sp-seg-btn').forEach(b => {
    b.addEventListener('click', () => {
      sceneState.gridSize = parseInt(b.dataset.size);
      _setSegActive('sp-grid-size', 'size', sceneState.gridSize);
      saveSceneState();
      window.render();
    });
  });

  /* ── Grid color ──────────────────────────────── */
  _setSegActive('sp-grid-color', 'color', sceneState.gridColor);
  document.querySelectorAll('#sp-grid-color .sp-seg-btn').forEach(b => {
    b.addEventListener('click', () => {
      sceneState.gridColor = b.dataset.color;
      _setSegActive('sp-grid-color', 'color', sceneState.gridColor);
      saveSceneState();
      window.render();
    });
  });

  /* ── Canvas fill opacity ─────────────────────── */
  const opacitySlider = document.getElementById('sp-canvas-opacity');
  const opacityVal    = document.getElementById('sp-canvas-opacity-val');
  if (opacitySlider) {
    opacitySlider.value = sceneState.canvasOpacity ?? 100;
    if (opacityVal) opacityVal.textContent = opacitySlider.value + '%';
    opacitySlider.addEventListener('input', () => {
      sceneState.canvasOpacity = parseInt(opacitySlider.value);
      if (opacityVal) opacityVal.textContent = sceneState.canvasOpacity + '%';
      saveSceneState();
      window.render();
    });
  }

  /* ── Checkerboard ────────────────────────────── */
  const checkerbox = document.getElementById('sp-checkerboard');
  if (checkerbox) {
    checkerbox.checked = sceneState.checkerboard ?? false;
    checkerbox.addEventListener('change', e => {
      sceneState.checkerboard = e.target.checked;
      saveSceneState();
      window.render();
    });
  }

  // Restore image thumbnail if present
  if (sceneState.imageSrc) {
    _applyImageEnv(sceneState.imageSrc, sceneState.imageName || 'Ảnh của tôi');
  }

  // Set initial environment
  _setEnvActive(sceneState.env || 'dark');
  applyEnvironment(sceneState.env || 'dark');
})();
