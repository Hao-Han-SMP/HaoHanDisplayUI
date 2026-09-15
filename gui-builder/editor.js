/**
 * editor.js — HaoHan Display UI Visual Builder
 * Hardened Architecture:
 * - Decoupled Document Space, Viewport Space, and Camera Space
 * - True cursor-anchored zoom with zero drift and unconstrained pan
 * - Continuous, decoupled preview animation & text gradient engine
 * - Line connectors (Butt/Square caps, Arrows, Elbow, Curves, Skew, Scale)
 * - Grouped Shape Popover Picker with Custom SVG Icons
 * - Text Solid + Animated Linear Gradient with Pickr
 * - Restored Scene Background with Upload/URL, Presets, Filters (Brightness, Contrast, Saturation, Blur, Opacity)
 * - Quick Color actions in Ribbon
 */

/* ── SVG Icon Library (Uniform 24x24, Stroke 2, Round Caps) ── */
const SVG_ICONS = {
  select: `<rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="9" cy="9" r="1.5"/><polyline points="21 15 16 10 5 21"/>`,
  shape: `<rect x="3" y="3" width="18" height="18" rx="2"/>`,
  text: `<polyline points="4 7 4 4 20 4 20 7"/><line x1="12" y1="4" x2="12" y2="20"/><line x1="9" y1="20" x2="15" y2="20"/>`,
  item: `<polygon points="12 2 21 7 21 17 12 22 3 17 3 7"/>`,
  block: `<path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/>`,
  line: `<line x1="4" y1="20" x2="20" y2="4"/>`,
  arrow: `<line x1="3" y1="12" x2="19" y2="12"/><polyline points="14 7 19 12 14 17"/>`,
  doubleArrow: `<line x1="5" y1="12" x2="19" y2="12"/><polyline points="9 7 4 12 9 17"/><polyline points="15 7 20 12 15 17"/>`,
  elbow: `<polyline points="4 6 12 6 12 18 20 18"/>`,
  curve: `<path d="M4 18 Q 12 4, 20 18"/>`,
  freeform: `<path d="M4 16 Q 8 6, 12 14 T 20 8"/>`,
  undo: `<path d="M3 7v6h6"/><path d="M3 13A9 9 0 1 0 6 6.3L3 9"/>`,
  redo: `<path d="M21 7v6h-6"/><path d="M21 13A9 9 0 1 1 18 6.3L21 9"/>`,
  copy: `<rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>`,
  paste: `<path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/><rect x="8" y="2" width="8" height="4" rx="1"/>`,
  duplicate: `<rect x="8" y="8" width="12" height="12" rx="2"/><path d="M16 8V6a2 2 0 0 0-2-2H6a2 2 0 0 0-2 2v8a2 2 0 0 0 2 2h2"/>`,
  delete: `<polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>`,
  bringForward: `<rect x="8" y="8" width="12" height="12" rx="1"/><path d="M4 16V4h12"/>`,
  sendBackward: `<rect x="4" y="4" width="12" height="12" rx="1"/><path d="M8 20h12V8"/>`,
  flipH: `<line x1="12" y1="3" x2="12" y2="21" stroke-dasharray="3 3"/><polyline points="8 8 3 12 8 16"/><polyline points="16 8 21 12 16 16"/>`,
  flipV: `<line x1="3" y1="12" x2="21" y2="12" stroke-dasharray="3 3"/><polyline points="8 8 12 3 16 8"/><polyline points="8 16 12 21 16 16"/>`,
  zoomIn: `<circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/><line x1="11" y1="8" x2="11" y2="14"/><line x1="8" y1="11" x2="14" y2="11"/>`,
  zoomOut: `<circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/><line x1="8" y1="11" x2="14" y2="11"/>`,
  grid: `<rect x="3" y="3" width="18" height="18" rx="2"/><line x1="3" y1="9" x2="21" y2="9"/><line x1="3" y1="15" x2="21" y2="15"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/>`,
  snap: `<circle cx="12" cy="12" r="2"/><line x1="12" y1="2" x2="12" y2="6"/><line x1="12" y1="18" x2="12" y2="22"/><line x1="2" y1="12" x2="6" y2="12"/><line x1="18" y1="12" x2="22" y2="12"/>`,
  eye: `<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>`,
  eyeOff: `<path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/>`,
  lock: `<rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/>`,
  unlock: `<rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 9.9-1"/>`,
  chevronDown: `<polyline points="6 9 12 15 18 9"/>`,
  chevronUp: `<polyline points="18 15 12 9 6 15"/>`,
  play: `<polygon points="6 3 20 12 6 21 6 3" fill="currentColor"/>`,
  pause: `<rect x="6" y="4" width="4" height="16"/><rect x="14" y="4" width="4" height="16"/>`,
  replay: `<polyline points="1 4 1 10 7 10"/><path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10"/>`,
};

function getSvgIcon(name, extraClass = '') {
  const content = SVG_ICONS[name] || SVG_ICONS.select;
  return `<svg class="svg-icon ${extraClass}" viewBox="0 0 24 24">${content}</svg>`;
}

/* ── 1. State Separation ──────────────────────────────────── */

// DOCUMENT STATE (Persisted and Serialized)
const state = {
  nodes: [],
  canvasW: 256,
  canvasH: 192,
  name: 'untitled',
  boardColor: '#0a0e18',
  boardAlpha: 100, // 0-100 percent
};

// CAMERA STATE (Viewport & Zoom Space)
const camera = {
  x: 0,              // screen pixels offset
  y: 0,
  zoom: 1.0,         // 1.0 = 100%, 0.10 to 16.0
};

// EDITOR INTERACTION STATE (Transient, not in exported document)
const editor = {
  selectedIdx: -1,
  selectedIdxs: [],
  grid: true,
  snap: true,
  snapGridSize: 4,

  // Tool
  activeTool: 'select',

  // Mouse & Panning
  isPanning: false,
  panStartX: 0,
  panStartY: 0,
  panStartCamX: 0,
  panStartCamY: 0,
  isSpacePressed: false,

  // Transform / Drag
  dragging: false,
  dragType: null,    // 'move' | 'nw' | 'n' | 'ne' | 'e' | 'se' | 's' | 'sw' | 'w' | 'rot' | 'line-p1' | 'line-p2' | 'line-ctrl' | 'line-mid'
  dragStartScreenX: 0,
  dragStartScreenY: 0,
  dragStartWorldX: 0,
  dragStartWorldY: 0,
  dragStartNodeSnapshots: [],
  preDragHistoryJson: null,

  // Marquee Selection Box (in World Space)
  isBoxSelecting: false,
  boxStartWorldX: 0,
  boxStartWorldY: 0,
  boxCurrWorldX: 0,
  boxCurrWorldY: 0,

  // Collapsible inspector section states
  collapsedSections: {
    transform: false,
    appearance: false,
    text: false,
    item: false,
    block: false,
    interaction: false,
    lineGeometry: false,
    lineStyle: false,
    lineArrow: false,
  },
};

// ANIMATION STATE (Decoupled preview clock)
const previewAnim = {
  type: 'none',
  isPlaying: false,
  progress: 1.0,     // 0.0 to 1.0
  speed: 1.0,        // 1.0 or 2.0
  duration: 800,     // ms
  lastTimestamp: null,
  rafId: null,
};

// Global continuous animation clock for gradient & preview
let _globalAnimRafId = null;
function startGlobalAnimLoop() {
  const tick = (now) => {
    window.__animClock = now;
    // If preview animation is playing, advance it
    if (previewAnim.isPlaying) {
      if (!previewAnim.lastTimestamp) previewAnim.lastTimestamp = now;
      const dt = now - previewAnim.lastTimestamp;
      previewAnim.lastTimestamp = now;
      const step = (dt * previewAnim.speed) / previewAnim.duration;
      previewAnim.progress = Math.min(1.0, previewAnim.progress + step);
      if (previewAnim.progress >= 1.0) {
        previewAnim.isPlaying = false;
        _syncPreviewPlayBtn();
      }
      updatePreview();
    }

    // If any text node has gradient animation enabled, redraw canvas smoothly
    const hasActiveGradient = state.nodes.some(n => n.visible !== false && n.type === 'text' && n.fillType === 'gradient' && n.gradientAnimation?.enabled);
    if (hasActiveGradient) {
      render();
      updatePreview();
    }

    _globalAnimRafId = requestAnimationFrame(tick);
  };
  _globalAnimRafId = requestAnimationFrame(tick);
}

/* ── Clipboard & History ─────────────────────────────────── */
let _clipboard = null;
const _undoStack = [];
const _redoStack = [];

function pushUndo() {
  _undoStack.push(JSON.stringify(state.nodes));
  if (_undoStack.length > 80) _undoStack.shift();
  _redoStack.length = 0;
  _updateUndoRedoButtons();
}

function undo() {
  if (!_undoStack.length) return;
  _redoStack.push(JSON.stringify(state.nodes));
  state.nodes = JSON.parse(_undoStack.pop()).map(n => normalizeNode(n)).filter(Boolean);
  syncUid(state.nodes);
  _syncSelectionAfterDataChange();
  buildPropsPanel();
  render();
  updateLayersList();
  scheduleSave();
  _updateUndoRedoButtons();
  showToast('Undo', 'info');
}

function redo() {
  if (!_redoStack.length) return;
  _undoStack.push(JSON.stringify(state.nodes));
  state.nodes = JSON.parse(_redoStack.pop()).map(n => normalizeNode(n)).filter(Boolean);
  syncUid(state.nodes);
  _syncSelectionAfterDataChange();
  buildPropsPanel();
  render();
  updateLayersList();
  scheduleSave();
  _updateUndoRedoButtons();
  showToast('Redo', 'info');
}

function _updateUndoRedoButtons() {
  const uBtn = document.getElementById('btn-undo');
  const rBtn = document.getElementById('btn-redo');
  if (uBtn) uBtn.style.opacity = _undoStack.length ? '1' : '0.4';
  if (rBtn) rBtn.style.opacity = _redoStack.length ? '1' : '0.4';
}

function _syncSelectionAfterDataChange() {
  editor.selectedIdxs = editor.selectedIdxs.filter(i => i >= 0 && i < state.nodes.length);
  editor.selectedIdx = editor.selectedIdxs.length ? editor.selectedIdxs[editor.selectedIdxs.length - 1] : -1;
  updateStatusSelection();
  updateQuickColorSwatches();
}

/* ── Toast Notifications ─────────────────────────────────── */
function showToast(message, type = 'info') {
  const container = document.getElementById('toast-container');
  if (!container) return;
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.textContent = message;
  container.appendChild(toast);
  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(10px)';
    toast.style.transition = 'all .2s';
    setTimeout(() => toast.remove(), 200);
  }, 2200);
}

/* ── localStorage Persistence ────────────────────────────── */
const STORAGE_KEY = 'hhdui_builder_v1';
let _saveTimer = null;

function scheduleSave() {
  clearTimeout(_saveTimer);
  _showSaveIndicator('pending');
  _saveTimer = setTimeout(_persistState, 800);
}

function _persistState() {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify({
      v: 1,
      nodes:      state.nodes,
      canvasW:    state.canvasW,
      canvasH:    state.canvasH,
      name:       state.name,
      boardColor: state.boardColor,
      boardAlpha: state.boardAlpha,
      camera:     { zoom: camera.zoom, x: camera.x, y: camera.y },
      grid:       editor.grid,
      snap:       editor.snap,
    }));
    _showSaveIndicator('saved');
  } catch (e) {
    console.warn('[HaoHan Builder] localStorage write failed', e);
    _showSaveIndicator('error');
  }
}

function _restoreState() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return false;
    const s = JSON.parse(raw);
    if (!s || s.v !== 1) return false;
    state.nodes   = Array.isArray(s.nodes) ? s.nodes.map(n => normalizeNode(n)).filter(Boolean) : [];
    syncUid(state.nodes);
    state.canvasW = Math.max(16, Math.min(4096, s.canvasW ?? 256));
    state.canvasH = Math.max(16, Math.min(4096, s.canvasH ?? 192));
    state.name    = s.name ?? 'untitled';
    if (s.boardColor) state.boardColor = s.boardColor;
    if (s.boardAlpha !== undefined) state.boardAlpha = Math.max(0, Math.min(100, Number(s.boardAlpha)));
    if (s.camera) {
      camera.zoom = Math.max(0.1, Math.min(16.0, s.camera.zoom ?? 1.0));
      camera.x    = Number(s.camera.x) || 0;
      camera.y    = Number(s.camera.y) || 0;
    }
    if (s.grid !== undefined) editor.grid = Boolean(s.grid);
    if (s.snap !== undefined) editor.snap = Boolean(s.snap);
    return true;
  } catch {
    return false;
  }
}

function _showSaveIndicator(status) {
  const el = document.getElementById('save-indicator');
  if (!el) return;
  el.dataset.state = status;
  const textEl = el.querySelector('.save-text');
  if (status === 'saved') {
    el.title = 'All changes saved';
    if (textEl) textEl.textContent = 'Saved';
  } else if (status === 'pending') {
    el.title = 'Saving…';
    if (textEl) textEl.textContent = 'Saving…';
  } else if (status === 'error') {
    el.title = 'Save Error';
    if (textEl) textEl.textContent = 'Save Error';
  }
}

/* ── DOM References ───────────────────────────────────────── */
const canvasViewport = document.getElementById('canvas-viewport');
const sceneBgLayer   = document.getElementById('scene-bg-layer');
const canvas         = document.getElementById('canvas');
const ctx            = canvas.getContext('2d');
const propsBody      = document.getElementById('props-body');
const prevCanvas     = document.getElementById('preview-canvas');
const prevCtx        = prevCanvas.getContext('2d');

/* Expose render to nodes.js texture loader */
window.__renderFrame = () => render();

/* ── 2. CAMERA COORDINATE TRANSFORMS ──────────────────────── */

function screenToWorld(screenX, screenY) {
  return {
    x: (screenX - camera.x) / camera.zoom,
    y: (screenY - camera.y) / camera.zoom,
  };
}

function worldToScreen(worldX, worldY) {
  return {
    x: worldX * camera.zoom + camera.x,
    y: worldY * camera.zoom + camera.y,
  };
}

function zoomAt(screenX, screenY, newZoom) {
  const clampedZoom = Math.max(0.10, Math.min(16.0, Math.round(newZoom * 100) / 100));
  if (Math.abs(clampedZoom - camera.zoom) < 0.001) return;

  const world = screenToWorld(screenX, screenY);
  camera.zoom = clampedZoom;
  camera.x = screenX - world.x * camera.zoom;
  camera.y = screenY - world.y * camera.zoom;

  updateZoomUI();
  render();
}

function resetZoom() {
  const vpRect = canvasViewport.getBoundingClientRect();
  camera.zoom = 1.0;
  camera.x = Math.round((vpRect.width - state.canvasW * camera.zoom) / 2);
  camera.y = Math.round((vpRect.height - state.canvasH * camera.zoom) / 2);
  updateZoomUI();
  render();
}

function fitCanvas() {
  const vpRect = canvasViewport.getBoundingClientRect();
  const padding = 40;
  const availW = Math.max(60, vpRect.width - padding * 2);
  const availH = Math.max(60, vpRect.height - padding * 2);
  const fitZoom = Math.min(availW / state.canvasW, availH / state.canvasH);
  const clampedZoom = Math.max(0.25, Math.min(8.0, Math.floor(fitZoom * 4) / 4 || fitZoom));

  camera.zoom = clampedZoom;
  camera.x = Math.round((vpRect.width - state.canvasW * camera.zoom) / 2);
  camera.y = Math.round((vpRect.height - state.canvasH * camera.zoom) / 2);

  updateZoomUI();
  render();
}

function updateZoomUI() {
  const zPct = Math.round(camera.zoom * 100);
  const zSel = document.getElementById('zoom-select');
  if (zSel) {
    let matched = false;
    for (let i = 0; i < zSel.options.length; i++) {
      if (Math.abs(parseFloat(zSel.options[i].value) - camera.zoom) < 0.05) {
        zSel.selectedIndex = i;
        matched = true;
        break;
      }
    }
    if (!matched) zSel.value = camera.zoom.toFixed(2);
  }
  const statusZoom = document.getElementById('status-zoom');
  if (statusZoom) statusZoom.textContent = `${zPct}%`;
}

/* ── 3. Canvas Viewport Sizing ────────────────────────────── */
function resizeCanvas() {
  const rect = canvasViewport.getBoundingClientRect();
  const dpr = window.devicePixelRatio || 1;
  const w = Math.max(10, Math.round(rect.width));
  const h = Math.max(10, Math.round(rect.height));

  if (canvas.width !== w * dpr || canvas.height !== h * dpr) {
    canvas.width = w * dpr;
    canvas.height = h * dpr;
    canvas.style.width = w + 'px';
    canvas.style.height = h + 'px';
  }
  render();
}

window.addEventListener('resize', resizeCanvas);
if (window.ResizeObserver) {
  new ResizeObserver(() => resizeCanvas()).observe(canvasViewport);
}

/* ── 4. Unified Canvas Render ─────────────────────────────── */
function render() {
  const dpr = window.devicePixelRatio || 1;
  const vpW = canvas.width / dpr;
  const vpH = canvas.height / dpr;

  ctx.save();
  ctx.scale(dpr, dpr);
  ctx.clearRect(0, 0, vpW, vpH);

  // 1. Camera Transform Space (World Document Rendering)
  ctx.save();
  ctx.translate(camera.x, camera.y);

  // Document paper boundary & drop shadow
  const docW = state.canvasW * camera.zoom;
  const docH = state.canvasH * camera.zoom;

let _checkerboardPattern = null;
function getCheckerboardPattern(ctx2d) {
  if (_checkerboardPattern) return _checkerboardPattern;
  const pCanvas = document.createElement('canvas');
  pCanvas.width = 16;
  pCanvas.height = 16;
  const pCtx = pCanvas.getContext('2d');
  pCtx.fillStyle = '#0d121c';
  pCtx.fillRect(0, 0, 16, 16);
  pCtx.fillStyle = '#151c2a';
  pCtx.fillRect(0, 0, 8, 8);
  pCtx.fillRect(8, 8, 8, 8);
  _checkerboardPattern = ctx2d.createPattern(pCanvas, 'repeat');
  return _checkerboardPattern;
}

  const _bColor = state.boardColor || '#0a0e18';
  const _bAlpha = (state.boardAlpha ?? 100) / 100;
  ctx.save();
  ctx.shadowColor = 'rgba(0, 0, 0, 0.65)';
  ctx.shadowBlur = 24 * Math.min(2, camera.zoom);
  ctx.shadowOffsetY = 6 * Math.min(2, camera.zoom);
  if (sceneState.checkerboard) {
    // Always draw checkerboard as base
    ctx.fillStyle = getCheckerboardPattern(ctx);
    ctx.fillRect(0, 0, docW, docH);
    ctx.restore();
    // Overlay board color on top with alpha
    if (_bAlpha > 0) {
      ctx.save();
      ctx.globalAlpha = _bAlpha;
      ctx.fillStyle = _bColor;
      ctx.fillRect(0, 0, docW, docH);
      ctx.restore();
    }
  } else {
    ctx.globalAlpha = _bAlpha;
    ctx.fillStyle = _bColor;
    ctx.fillRect(0, 0, docW, docH);
    ctx.restore();
  }

  ctx.strokeStyle = '#2b374e';
  ctx.lineWidth = 1;
  ctx.strokeRect(0, 0, docW, docH);

  // Document Space Grid
  _renderSceneGrid(ctx, docW, docH, camera.zoom);

  // Render All Nodes (back to front)
  state.nodes.forEach(n => {
    if (n.visible === false) return;
    NodeDefs[n.type]?.render(ctx, n, camera.zoom);
  });

  // Drag Move Ghost
  if (editor.dragging && editor.dragType === 'move' && editor.selectedIdxs.length > 0) {
    _renderDragGhostAndGuides(ctx, camera.zoom);
  }

  ctx.restore(); // Exit Camera Space

  // 2. Screen Overlays (Selection handles & Marquee)
  _renderSelectionOverlays(ctx);

  if (editor.isBoxSelecting) {
    const s1 = worldToScreen(editor.boxStartWorldX, editor.boxStartWorldY);
    const s2 = worldToScreen(editor.boxCurrWorldX, editor.boxCurrWorldY);
    const bx = Math.min(s1.x, s2.x);
    const by = Math.min(s1.y, s2.y);
    const bw = Math.abs(s2.x - s1.x);
    const bh = Math.abs(s2.y - s1.y);

    ctx.save();
    ctx.fillStyle = 'rgba(79, 142, 247, 0.14)';
    ctx.strokeStyle = '#4f8ef7';
    ctx.lineWidth = 1;
    ctx.setLineDash([4, 3]);
    ctx.fillRect(bx, by, bw, bh);
    ctx.strokeRect(bx, by, bw, bh);
    ctx.restore();
  }

  ctx.restore(); // Exit DPR scale

  updatePreview();
}

function _renderDragGhostAndGuides(ctx2d, z) {
  let gMinX = Infinity, gMinY = Infinity;
  ctx2d.save();
  ctx2d.fillStyle = 'rgba(79, 142, 247, 0.12)';
  ctx2d.strokeStyle = '#4f8ef7';
  ctx2d.lineWidth = 1.2;
  ctx2d.setLineDash([4, 3]);

  editor.selectedIdxs.forEach(idx => {
    const n = state.nodes[idx];
    if (!n || n.visible === false) return;
    const r = getNodeBoundingBox(n);
    if (r && r.w > 0 && r.h > 0) {
      ctx2d.fillRect(r.x * z, r.y * z, r.w * z, r.h * z);
      ctx2d.strokeRect(r.x * z, r.y * z, r.w * z, r.h * z);
      gMinX = Math.min(gMinX, r.x);
      gMinY = Math.min(gMinY, r.y);
    }
  });

  if (gMinX < Infinity) {
    ctx2d.fillStyle = '#4f8ef7';
    ctx2d.setLineDash([]);
    ctx2d.font = 'bold 10px "JetBrains Mono", monospace';
    const badgeText = `X: ${Math.round(gMinX)}, Y: ${Math.round(gMinY)}`;
    const badgeW = ctx2d.measureText(badgeText).width + 10;
    ctx2d.fillRect(gMinX * z, Math.max(0, gMinY * z - 18), badgeW, 16);
    ctx2d.fillStyle = '#ffffff';
    ctx2d.textBaseline = 'middle';
    ctx2d.fillText(badgeText, gMinX * z + 5, Math.max(8, gMinY * z - 10));
  }
  ctx2d.restore();
}

function _renderSelectionOverlays(ctx2d) {
  const isMulti = editor.selectedIdxs.length > 1;

  editor.selectedIdxs.forEach(idx => {
    const n = state.nodes[idx];
    if (!n || n.visible === false) return;
    const showHandles = !isMulti;
    renderSelectionForNode(ctx2d, n, showHandles);
  });
}

function renderSelectionForNode(ctx2d, n, showHandles = true) {
  const z = camera.zoom;
  const r = getNodeBoundingBox(n);
  if (!r) return;

  const p1 = worldToScreen(r.x, r.y);
  const w = r.w * z;
  const h = r.h * z;
  const cx = p1.x + w / 2;

  ctx2d.save();
  ctx2d.strokeStyle = '#4f8ef7';
  ctx2d.lineWidth = 1.5;
  ctx2d.setLineDash([4, 3]);
  ctx2d.strokeRect(p1.x - 1, p1.y - 1, w + 2, h + 2);

  // Rotation stalk
  if (showHandles && (n.type === 'shape' || n.type === 'block' || n.type === 'text' || n.type === 'line')) {
    ctx2d.beginPath();
    ctx2d.setLineDash([2, 2]);
    ctx2d.moveTo(cx, p1.y);
    ctx2d.lineTo(cx, p1.y - 20);
    ctx2d.stroke();
  }
  ctx2d.restore();

  if (showHandles) {
    const handles = getScreenHandles(n);
    handles.forEach(hd => {
      ctx2d.setLineDash([]);
      if (hd.type === 'rot') {
        ctx2d.fillStyle = '#4f8ef7';
        ctx2d.strokeStyle = '#ffffff';
        ctx2d.lineWidth = 1.5;
        ctx2d.beginPath();
        ctx2d.arc(hd.x, hd.y, 5, 0, Math.PI * 2);
        ctx2d.fill();
        ctx2d.stroke();
      } else {
        const isControl = hd.type.startsWith('line-');
        const sz = isControl ? 7 : 5;
        ctx2d.fillStyle = isControl ? '#34d399' : '#4f8ef7';
        ctx2d.strokeStyle = '#ffffff';
        ctx2d.lineWidth = 1;
        ctx2d.fillRect(hd.x - sz / 2, hd.y - sz / 2, sz, sz);
        ctx2d.strokeRect(hd.x - sz / 2, hd.y - sz / 2, sz, sz);
      }
    });
  }
}

function getScreenHandles(n) {
  const z = camera.zoom;
  const r = getNodeBoundingBox(n);
  if (!r) return [];

  const p1 = worldToScreen(r.x, r.y);
  const p2 = worldToScreen(r.x + r.w, r.y + r.h);
  const mx = (p1.x + p2.x) / 2;
  const my = (p1.y + p2.y) / 2;
  const rotDist = 20;

  if (n.type === 'line') {
    const pt1 = worldToScreen(n.x1, n.y1);
    const pt2 = worldToScreen(n.x2, n.y2);
    const lineHandles = [
      { x: pt1.x, y: pt1.y, type: 'line-p1' },
      { x: pt2.x, y: pt2.y, type: 'line-p2' },
      { x: mx,    y: p1.y - rotDist, type: 'rot' },
    ];
    if (n.lineType === 'curve' || n.lineType === 'curved_arrow') {
      const ctrl = worldToScreen(n.curveCtrlX ?? (n.x1 + n.x2) / 2, n.curveCtrlY ?? (n.y1 + n.y2) / 2 - 20);
      lineHandles.push({ x: ctrl.x, y: ctrl.y, type: 'line-ctrl' });
    } else if (n.lineType === 'elbow' || n.lineType === 'elbow_arrow') {
      const mid = worldToScreen(n.elbowMidX ?? (n.x1 + n.x2) / 2, n.elbowMidY ?? (n.y1 + n.y2) / 2);
      lineHandles.push({ x: mid.x, y: mid.y, type: 'line-mid' });
    }
    return lineHandles;
  }

  const rawHandles = [
    { x: p1.x, y: p1.y, type: 'nw' },
    { x: mx,   y: p1.y, type: 'n'  },
    { x: p2.x, y: p1.y, type: 'ne' },
    { x: p2.x, y: my,   type: 'e'  },
    { x: p2.x, y: p2.y, type: 'se' },
    { x: mx,   y: p2.y, type: 's'  },
    { x: p1.x, y: p2.y, type: 'sw' },
    { x: p1.x, y: my,   type: 'w'  },
    { x: mx,   y: p1.y - rotDist, type: 'rot' },
  ];

  return rawHandles;
}

/* ── 5. Transformed Bounding Boxes & Geometry ─────────────── */
function getNodeBoundingBox(n) {
  if (!n) return { x: 0, y: 0, w: 0, h: 0 };

  if (n.type === 'line') {
    let pts = [{ x: n.x1, y: n.y1 }, { x: n.x2, y: n.y2 }];
    if (n.lineType === 'curve' || n.lineType === 'curved_arrow') {
      pts.push({ x: n.curveCtrlX ?? (n.x1 + n.x2) / 2, y: n.curveCtrlY ?? (n.y1 + n.y2) / 2 - 20 });
    } else if (n.lineType === 'elbow' || n.lineType === 'elbow_arrow') {
      pts.push({ x: n.elbowMidX ?? (n.x1 + n.x2) / 2, y: n.elbowMidY ?? (n.y1 + n.y2) / 2 });
    } else if (n.lineType === 'freeform' && Array.isArray(n.points)) {
      pts = pts.concat(n.points);
    }
    const cx = (n.x1 + n.x2) / 2;
    const cy = (n.y1 + n.y2) / 2;
    const rot = n.rotation || 0;
    const skX = n.skewX || 0;
    const skY = n.skewY || 0;
    const scX = (n.scaleX ?? 1) * (n.flipX ? -1 : 1);
    const scY = (n.scaleY ?? 1) * (n.flipY ? -1 : 1);

    if (rot !== 0 || skX !== 0 || skY !== 0 || scX !== 1 || scY !== 1) {
      const rad = (rot * Math.PI) / 180;
      const cos = Math.cos(rad);
      const sin = Math.sin(rad);
      const tanX = Math.tan((skX * Math.PI) / 180);
      const tanY = Math.tan((skY * Math.PI) / 180);
      pts = pts.map(pt => {
        let dx = pt.x - cx;
        let dy = pt.y - cy;
        let rx = dx * cos - dy * sin;
        let ry = dx * sin + dy * cos;
        let sx = rx + ry * tanX;
        let sy = ry + rx * tanY;
        let fx = sx * scX;
        let fy = sy * scY;
        return { x: cx + fx, y: cy + fy };
      });
    }

    const pad = Math.max(4, (n.thickness || 2) / 2 + 2);
    const minX = Math.min(...pts.map(p => p.x)) - pad;
    const minY = Math.min(...pts.map(p => p.y)) - pad;
    const maxX = Math.max(...pts.map(p => p.x)) + pad;
    const maxY = Math.max(...pts.map(p => p.y)) + pad;
    return { x: Math.round(minX), y: Math.round(minY), w: Math.max(8, Math.round(maxX - minX)), h: Math.max(8, Math.round(maxY - minY)) };
  }

  let x = n.x, y = n.y, w = n.width, h = n.height;
  if (n.type === 'text') {
    x = n.boxX; y = n.boxY;
  } else if (n.type === 'item') {
    const sz = 16 * (n.scale || 0.8);
    x = n.x - sz / 2; y = n.y - sz / 2; w = sz; h = sz;
  }

  const rot = n.rotation || 0;
  const skX = n.skewX || 0;
  const skY = n.skewY || 0;
  const scX = (n.scaleX ?? 1) * (n.flipX ? -1 : 1);
  const scY = (n.scaleY ?? 1) * (n.flipY ? -1 : 1);

  if (rot === 0 && skX === 0 && skY === 0 && scX === 1 && scY === 1) {
    return { x: Math.round(x), y: Math.round(y), w: Math.round(w), h: Math.round(h) };
  }

  const cx = x + w / 2;
  const cy = y + h / 2;
  const rad = (rot * Math.PI) / 180;
  const cos = Math.cos(rad);
  const sin = Math.sin(rad);
  const tanX = Math.tan((skX * Math.PI) / 180);
  const tanY = Math.tan((skY * Math.PI) / 180);

  const corners = [
    { x: x, y: y },
    { x: x + w, y: y },
    { x: x + w, y: y + h },
    { x: x, y: y + h },
  ];

  const transformed = corners.map(pt => {
    let dx = pt.x - cx;
    let dy = pt.y - cy;
    let rx = dx * cos - dy * sin;
    let ry = dx * sin + dy * cos;
    let sx = rx + ry * tanX;
    let sy = ry + rx * tanY;
    let fx = sx * scX;
    let fy = sy * scY;
    return { x: cx + fx, y: cy + fy };
  });

  const minX = Math.min(...transformed.map(p => p.x));
  const minY = Math.min(...transformed.map(p => p.y));
  const maxX = Math.max(...transformed.map(p => p.x));
  const maxY = Math.max(...transformed.map(p => p.y));

  return {
    x: Math.round(minX),
    y: Math.round(minY),
    w: Math.max(4, Math.round(maxX - minX)),
    h: Math.max(4, Math.round(maxY - minY)),
  };
}

function getNodeCenter(n) {
  if (!n) return { x: 0, y: 0 };
  if (n.type === 'line') return { x: (n.x1 + n.x2) / 2, y: (n.y1 + n.y2) / 2 };
  if (n.type === 'text') return { x: n.boxX + n.width / 2, y: n.boxY + n.height / 2 };
  if (n.type === 'item') return { x: n.x, y: n.y };
  return { x: (n.x ?? 0) + (n.width ?? 0) / 2, y: (n.y ?? 0) + (n.height ?? 0) / 2 };
}

function rectsIntersect(r1x, r1y, r1w, r1h, r2x, r2y, r2w, r2h) {
  return !(r2x > r1x + r1w || r2x + r2w < r1x || r2y > r1y + r1h || r2y + r2h < r1y);
}

/* ── 6. Hit Testing ───────────────────────────────────────── */
const SLOP = 5;

function hitTestWorld(wx, wy) {
  for (let i = state.nodes.length - 1; i >= 0; i--) {
    const n = state.nodes[i];
    if (n.visible === false || n.locked) continue;
    if (hitsNode(n, wx, wy)) return i;
  }
  return -1;
}

function hitsNode(n, px, py) {
  if (n.type === 'line') {
    let localX = px, localY = py;
    const cx = (n.x1 + n.x2) / 2;
    const cy = (n.y1 + n.y2) / 2;
    if (n.rotation || n.skewX || n.skewY || n.flipX || n.flipY) {
      const rad = (-(n.rotation || 0) * Math.PI) / 180;
      const dx = px - cx;
      const dy = py - cy;
      const cos = Math.cos(rad);
      const sin = Math.sin(rad);
      const rx = dx * cos - dy * sin;
      const ry = dx * sin + dy * cos;
      localX = cx + rx;
      localY = cy + ry;
    }
    const dx = n.x2 - n.x1, dy = n.y2 - n.y1;
    const len2 = dx * dx + dy * dy;
    const thresh = Math.max(6, (n.thickness || 2) / 2 + SLOP);
    if (len2 === 0) return dist2(localX, localY, n.x1, n.y1) <= thresh;
    const t = Math.max(0, Math.min(1, ((localX - n.x1) * dx + (localY - n.y1) * dy) / len2));
    return dist2(localX, localY, n.x1 + t * dx, n.y1 + t * dy) <= thresh;
  }

  const bb = getNodeBoundingBox(n);
  return px >= bb.x - SLOP && px <= bb.x + bb.w + SLOP && py >= bb.y - SLOP && py <= bb.y + bb.h + SLOP;
}

function dist2(ax, ay, bx, by) {
  return Math.sqrt((ax - bx) ** 2 + (ay - by) ** 2);
}

function hitsScreenHandle(n, screenX, screenY) {
  const handles = getScreenHandles(n);
  for (const h of handles) {
    if (Math.abs(screenX - h.x) <= 8 && Math.abs(screenY - h.y) <= 8) {
      return h.type;
    }
  }
  return null;
}

/* ── 7. Pointer & Mouse Event Listeners ───────────────────── */
const HANDLE_CURSORS = {
  nw: 'nw-resize', n: 'n-resize', ne: 'ne-resize',
  e:  'e-resize',  se: 'se-resize',
  s:  's-resize',  sw: 'sw-resize', w: 'w-resize',
  rot: 'crosshair',
  'line-p1': 'crosshair',
  'line-p2': 'crosshair',
  'line-ctrl': 'crosshair',
  'line-mid': 'crosshair',
};

canvasViewport.addEventListener('mousedown', onPointerDown);
window.addEventListener('mousemove', onPointerMove);
window.addEventListener('mouseup', onPointerUp);

function onPointerDown(e) {
  const rect = canvasViewport.getBoundingClientRect();
  const screenX = e.clientX - rect.left;
  const screenY = e.clientY - rect.top;
  const world = screenToWorld(screenX, screenY);

  if (e.button === 1 || (e.button === 0 && editor.isSpacePressed)) {
    e.preventDefault();
    editor.isPanning = true;
    editor.panStartX = screenX;
    editor.panStartY = screenY;
    editor.panStartCamX = camera.x;
    editor.panStartCamY = camera.y;
    canvasViewport.classList.add('panning');
    return;
  }

  if (e.button !== 0) return;

  if (editor.selectedIdx >= 0 && editor.selectedIdxs.length === 1) {
    const selNode = state.nodes[editor.selectedIdx];
    const handleHit = hitsScreenHandle(selNode, screenX, screenY);
    if (handleHit) {
      editor.dragging = true;
      editor.dragType = handleHit;
      editor.dragStartScreenX = screenX;
      editor.dragStartScreenY = screenY;
      editor.dragStartWorldX = world.x;
      editor.dragStartWorldY = world.y;
      editor.dragStartNodeSnapshots = [JSON.parse(JSON.stringify(selNode))];
      editor.preDragHistoryJson = JSON.stringify(state.nodes);
      return;
    }
  }

  const hitIdx = hitTestWorld(world.x, world.y);

  if (hitIdx !== -1) {
    if (e.shiftKey || e.ctrlKey) {
      const pos = editor.selectedIdxs.indexOf(hitIdx);
      if (pos !== -1) editor.selectedIdxs.splice(pos, 1);
      else editor.selectedIdxs.push(hitIdx);
      editor.selectedIdx = editor.selectedIdxs.length ? editor.selectedIdxs[editor.selectedIdxs.length - 1] : -1;
    } else {
      if (!editor.selectedIdxs.includes(hitIdx)) {
        editor.selectedIdx = hitIdx;
        editor.selectedIdxs = [hitIdx];
      }
    }

    editor.dragging = true;
    editor.dragType = 'move';
    editor.dragStartScreenX = screenX;
    editor.dragStartScreenY = screenY;
    editor.dragStartWorldX = world.x;
    editor.dragStartWorldY = world.y;
    editor.dragStartNodeSnapshots = editor.selectedIdxs.map(i => JSON.parse(JSON.stringify(state.nodes[i])));
    editor.preDragHistoryJson = JSON.stringify(state.nodes);

    buildPropsPanel();
    updateLayersList();
    updateStatusSelection();
    updateQuickColorSwatches();
    render();
  } else {
    if (!e.shiftKey && !e.ctrlKey) {
      editor.selectedIdxs = [];
      editor.selectedIdx = -1;
      buildPropsPanel();
      updateLayersList();
      updateStatusSelection();
      updateQuickColorSwatches();
    }
    editor.isBoxSelecting = true;
    editor.boxStartWorldX = world.x;
    editor.boxStartWorldY = world.y;
    editor.boxCurrWorldX = world.x;
    editor.boxCurrWorldY = world.y;
    render();
  }
}

function onPointerMove(e) {
  const rect = canvasViewport.getBoundingClientRect();
  const screenX = e.clientX - rect.left;
  const screenY = e.clientY - rect.top;
  const world = screenToWorld(screenX, screenY);

  updateStatusCursor(world.x, world.y, screenX >= 0 && screenX <= rect.width && screenY >= 0 && screenY <= rect.height);

  if (editor.isPanning) {
    const dx = screenX - editor.panStartX;
    const dy = screenY - editor.panStartY;
    camera.x = editor.panStartCamX + dx;
    camera.y = editor.panStartCamY + dy;
    render();
    return;
  }

  if (editor.isBoxSelecting) {
    editor.boxCurrWorldX = world.x;
    editor.boxCurrWorldY = world.y;

    const bx = Math.min(editor.boxStartWorldX, editor.boxCurrWorldX);
    const by = Math.min(editor.boxStartWorldY, editor.boxCurrWorldY);
    const bw = Math.abs(editor.boxCurrWorldX - editor.boxStartWorldX);
    const bh = Math.abs(editor.boxCurrWorldY - editor.boxStartWorldY);

    if (bw > 2 || bh > 2) {
      const inside = [];
      state.nodes.forEach((n, idx) => {
        if (n.visible === false || n.locked) return;
        const nr = getNodeBoundingBox(n);
        if (rectsIntersect(bx, by, bw, bh, nr.x, nr.y, nr.w, nr.h)) {
          inside.push(idx);
        }
      });
      editor.selectedIdxs = inside;
      editor.selectedIdx = inside.length ? inside[inside.length - 1] : -1;
      updateStatusSelection();
      updateQuickColorSwatches();
    }
    render();
    return;
  }

  if (editor.dragging) {
    let dx = world.x - editor.dragStartWorldX;
    let dy = world.y - editor.dragStartWorldY;

    if (editor.snap) {
      const gs = editor.snapGridSize;
      dx = Math.round(dx / gs) * gs;
      dy = Math.round(dy / gs) * gs;
    }

    if (editor.dragType === 'move') {
      editor.selectedIdxs.forEach((idx, i) => {
        const orig = editor.dragStartNodeSnapshots[i];
        const n = state.nodes[idx];
        if (!orig || !n || n.locked) return;
        _applyNodeDelta(n, orig, dx, dy);
      });
      if (editor.selectedIdx >= 0) refreshPropsInputs(state.nodes[editor.selectedIdx]);
    } else if (editor.dragType === 'rot') {
      const orig = editor.dragStartNodeSnapshots[0];
      const n = state.nodes[editor.selectedIdx];
      if (orig && n) {
        const c = getNodeCenter(orig);
        const rad = Math.atan2(world.y - c.y, world.x - c.x);
        let deg = Math.round((rad * 180) / Math.PI) + 90;
        deg = (((deg % 360) + 360) % 360);
        if (e.shiftKey) {
          deg = (Math.round(deg / 45) * 45) % 360;
        } else {
          for (let snap = 0; snap < 360; snap += 45) {
            if (Math.abs(deg - snap) <= 3 || Math.abs(deg - snap) >= 357) {
              deg = snap % 360;
              break;
            }
          }
        }
        n.rotation = deg;
        refreshPropsInputs(n);
      }
    } else if (editor.dragType === 'line-p1') {
      const orig = editor.dragStartNodeSnapshots[0];
      const n = state.nodes[editor.selectedIdx];
      if (orig && n) {
        n.x1 = Math.round(orig.x1 + dx);
        n.y1 = Math.round(orig.y1 + dy);
        refreshPropsInputs(n);
      }
    } else if (editor.dragType === 'line-p2') {
      const orig = editor.dragStartNodeSnapshots[0];
      const n = state.nodes[editor.selectedIdx];
      if (orig && n) {
        n.x2 = Math.round(orig.x2 + dx);
        n.y2 = Math.round(orig.y2 + dy);
        refreshPropsInputs(n);
      }
    } else if (editor.dragType === 'line-ctrl') {
      const orig = editor.dragStartNodeSnapshots[0];
      const n = state.nodes[editor.selectedIdx];
      if (orig && n) {
        n.curveCtrlX = Math.round((orig.curveCtrlX ?? (orig.x1 + orig.x2) / 2) + dx);
        n.curveCtrlY = Math.round((orig.curveCtrlY ?? (orig.y1 + orig.y2) / 2 - 20) + dy);
        refreshPropsInputs(n);
      }
    } else if (editor.dragType === 'line-mid') {
      const orig = editor.dragStartNodeSnapshots[0];
      const n = state.nodes[editor.selectedIdx];
      if (orig && n) {
        n.elbowMidX = Math.round((orig.elbowMidX ?? (orig.x1 + orig.x2) / 2) + dx);
        n.elbowMidY = Math.round((orig.elbowMidY ?? (orig.y1 + orig.y2) / 2) + dy);
        refreshPropsInputs(n);
      }
    } else {
      const orig = editor.dragStartNodeSnapshots[0];
      const n = state.nodes[editor.selectedIdx];
      if (orig && n) {
        const newRect = calcResizeRect(orig, editor.dragType, dx, dy);
        if (newRect) {
          if (n.type === 'text') {
            n.boxX = newRect.x; n.boxY = newRect.y; n.width = newRect.w; n.height = newRect.h;
          } else {
            n.x = newRect.x; n.y = newRect.y; n.width = newRect.w; n.height = newRect.h;
          }
          n.flipX = newRect.flipX;
          n.flipY = newRect.flipY;
          refreshPropsInputs(n);
        }
      }
    }

    render();
    return;
  }

  updateHoverCursor(screenX, screenY, world.x, world.y);
}

function onPointerUp() {
  if (editor.isPanning) {
    editor.isPanning = false;
    canvasViewport.classList.remove('panning');
  }

  if (editor.isBoxSelecting) {
    editor.isBoxSelecting = false;
    buildPropsPanel();
    updateLayersList();
    render();
  }

  if (editor.dragging) {
    editor.dragging = false;
    if (editor.preDragHistoryJson && JSON.stringify(state.nodes) !== editor.preDragHistoryJson) {
      _undoStack.push(editor.preDragHistoryJson);
      if (_undoStack.length > 80) _undoStack.shift();
      _redoStack.length = 0;
      _updateUndoRedoButtons();
      scheduleSave();
      updateLayersList();
    }
    editor.preDragHistoryJson = null;
    render();
  }
}

function updateHoverCursor(screenX, screenY, wx, wy) {
  if (editor.isSpacePressed) {
    canvasViewport.style.cursor = 'grab';
    return;
  }
  if (editor.selectedIdx >= 0 && editor.selectedIdxs.length === 1) {
    const selNode = state.nodes[editor.selectedIdx];
    const handleHit = hitsScreenHandle(selNode, screenX, screenY);
    if (handleHit) {
      canvasViewport.style.cursor = HANDLE_CURSORS[handleHit] || 'pointer';
      return;
    }
  }
  const hitIdx = hitTestWorld(wx, wy);
  canvasViewport.style.cursor = hitIdx !== -1 ? 'move' : 'default';
}

function _applyNodeDelta(n, orig, dx, dy) {
  if (n.type === 'text') {
    n.boxX = orig.boxX + dx;
    n.boxY = orig.boxY + dy;
  } else if (n.type === 'item') {
    n.x = orig.x + dx;
    n.y = orig.y + dy;
  } else if (n.type === 'line') {
    n.x1 = orig.x1 + dx;
    n.y1 = orig.y1 + dy;
    n.x2 = orig.x2 + dx;
    n.y2 = orig.y2 + dy;
    if (orig.curveCtrlX != null) n.curveCtrlX = orig.curveCtrlX + dx;
    if (orig.curveCtrlY != null) n.curveCtrlY = orig.curveCtrlY + dy;
    if (orig.elbowMidX != null) n.elbowMidX = orig.elbowMidX + dx;
    if (orig.elbowMidY != null) n.elbowMidY = orig.elbowMidY + dy;
  } else {
    n.x = orig.x + dx;
    n.y = orig.y + dy;
  }
}

function calcResizeRect(orig, handle, dx, dy) {
  let ox = orig.x ?? 0;
  let oy = orig.y ?? 0;
  let ow = orig.width ?? orig.w ?? 20;
  let oh = orig.height ?? orig.h ?? 20;
  if (orig.type === 'text') {
    ox = orig.boxX ?? ox;
    oy = orig.boxY ?? oy;
  }

  const rot = orig.rotation || 0;
  const rad = (rot * Math.PI) / 180;
  const cos = Math.cos(rad);
  const sin = Math.sin(rad);

  // Project world delta (dx, dy) into object's local unrotated coordinate space
  const ldx = dx * cos + dy * sin;
  const ldy = -dx * sin + dy * cos;

  let localLeft = 0;
  let localRight = ow;
  let localTop = 0;
  let localBottom = oh;

  let flipX = Boolean(orig.flipX);
  let flipY = Boolean(orig.flipY);

  if (handle.includes('e')) {
    localRight = ow + ldx;
  } else if (handle.includes('w')) {
    localLeft = ldx;
  }

  if (handle.includes('s')) {
    localBottom = oh + ldy;
  } else if (handle.includes('n')) {
    localTop = ldy;
  }

  // Crossing detection & flip in X
  let newW = localRight - localLeft;
  let xOffset = localLeft;
  if (newW < 0) {
    newW = Math.abs(newW);
    flipX = !flipX;
    xOffset = localRight;
  }

  // Crossing detection & flip in Y
  let newH = localBottom - localTop;
  let yOffset = localTop;
  if (newH < 0) {
    newH = Math.abs(newH);
    flipY = !flipY;
    yOffset = localBottom;
  }

  const MIN_W = 1;
  const MIN_H = 1;
  newW = Math.max(MIN_W, newW);
  newH = Math.max(MIN_H, newH);

  // Original unrotated local center
  const origLocalCenterX = ow / 2;
  const origLocalCenterY = oh / 2;

  // New unrotated local center relative to original top-left
  const newLocalCenterX = xOffset + newW / 2;
  const newLocalCenterY = yOffset + newH / 2;

  // Local center displacement
  const deltaLocalCenterX = newLocalCenterX - origLocalCenterX;
  const deltaLocalCenterY = newLocalCenterY - origLocalCenterY;

  // Rotate local center displacement back to world space
  const deltaWorldCenterX = deltaLocalCenterX * cos - deltaLocalCenterY * sin;
  const deltaWorldCenterY = deltaLocalCenterX * sin + deltaLocalCenterY * cos;

  // Original world center
  const origWorldCenterX = ox + ow / 2;
  const origWorldCenterY = oy + oh / 2;

  // New world center
  const newWorldCenterX = origWorldCenterX + deltaWorldCenterX;
  const newWorldCenterY = origWorldCenterY + deltaWorldCenterY;

  // Final unrotated top-left position
  const finalX = Math.round(newWorldCenterX - newW / 2);
  const finalY = Math.round(newWorldCenterY - newH / 2);
  const finalW = Math.round(newW);
  const finalH = Math.round(newH);

  return {
    x: finalX,
    y: finalY,
    w: finalW,
    h: finalH,
    flipX,
    flipY,
  };
}

/* ── Mouse Wheel Zoom (Cursor Anchored) ───────────────────── */
canvasViewport.addEventListener('wheel', e => {
  e.preventDefault();
  const rect = canvasViewport.getBoundingClientRect();
  const screenX = e.clientX - rect.left;
  const screenY = e.clientY - rect.top;

  const zoomFactor = e.deltaY < 0 ? 1.15 : 0.85;
  zoomAt(screenX, screenY, camera.zoom * zoomFactor);
}, { passive: false });

/* ── 8. Status Bar Updates ────────────────────────────────── */
function updateStatusCursor(wx, wy, isInside) {
  const el = document.getElementById('status-cursor');
  if (!el) return;
  if (!isInside) el.textContent = 'X: -, Y: -';
  else el.textContent = `X: ${Math.round(wx)}, Y: ${Math.round(wy)}`;
}

function updateStatusSelection() {
  const el = document.getElementById('status-selection');
  if (!el) return;
  const count = editor.selectedIdxs.length;
  if (count === 0) el.textContent = 'Ready';
  else if (count === 1) {
    const n = state.nodes[editor.selectedIdx];
    el.textContent = `${(n?.type || 'Object').toUpperCase()} selected`;
  } else {
    el.textContent = `${count} objects selected`;
  }
}

function updateStatusDims() {
  const el = document.getElementById('status-dims');
  if (el) el.textContent = `${state.canvasW} × ${state.canvasH} px`;
}

function updateStatusSnap() {
  const el = document.getElementById('status-snap');
  if (el) el.textContent = `Snap: ${editor.snap ? 'ON' : 'OFF'}`;
}

function updateStatusGrid() {
  const el = document.getElementById('status-grid');
  if (el) el.textContent = `Grid: ${editor.grid ? 'ON' : 'OFF'}`;
}

function updateQuickColorSwatches() {
  const fillGroup = document.getElementById('ribbon-quick-color-group');
  const fillBtn = document.getElementById('btn-quick-fill');
  const outlineBtn = document.getElementById('btn-quick-outline');
  const fillLabel = document.getElementById('quick-fill-label');
  const outlineLabel = document.getElementById('quick-outline-label');
  const fillSwatch = document.getElementById('quick-fill-swatch');
  const outlineSwatch = document.getElementById('quick-outline-swatch');

  const sel = editor.selectedIdx >= 0 ? state.nodes[editor.selectedIdx] : null;

  if (!fillGroup) return;

  if (sel && (sel.type === 'item' || sel.type === 'block')) {
    fillGroup.style.display = 'none';
    return;
  }
  fillGroup.style.display = 'flex';

  if (sel && sel.type === 'shape') {
    if (fillLabel) fillLabel.textContent = 'Fill';
    if (outlineLabel) outlineLabel.textContent = 'Outline';
    if (fillBtn) {
      fillBtn.title = 'Fill: Thay đổi màu/nội dung bên trong shape';
      fillBtn.classList.toggle('active', Boolean(sel.color && sel.color !== 'transparent'));
    }
    if (outlineBtn) {
      outlineBtn.title = 'Outline: Thay đổi đường viền bên ngoài shape';
      outlineBtn.classList.toggle('active', Boolean(sel.outline));
    }
    if (fillSwatch) fillSwatch.style.background = sel.color || '#1a2035';
    if (outlineSwatch) {
      outlineSwatch.style.background = 'transparent';
      outlineSwatch.style.borderColor = (sel.outline && sel.outlineColor) ? sel.outlineColor : '#4f8ef7';
    }
  } else if (sel && sel.type === 'text') {
    if (fillLabel) fillLabel.textContent = 'Text Color';
    if (outlineLabel) outlineLabel.textContent = 'Gradient';
    if (fillBtn) {
      fillBtn.title = 'Text Color: Thay đổi màu sắc chữ';
      fillBtn.classList.toggle('active', sel.fillType !== 'gradient');
    }
    if (outlineBtn) {
      outlineBtn.title = 'Gradient: Chuyển sang hiệu ứng gradient chữ';
      outlineBtn.classList.toggle('active', sel.fillType === 'gradient');
    }
    if (fillSwatch) {
      fillSwatch.style.background = sel.fillType === 'gradient' ? 'linear-gradient(to right, #ff4d4f, #4f8ef7)' : (sel.color || '#ffffff');
    }
    if (outlineSwatch) {
      outlineSwatch.style.background = 'linear-gradient(to right, #ff4d4f, #4f8ef7)';
      outlineSwatch.style.borderColor = 'transparent';
    }
  } else if (sel && sel.type === 'line') {
    if (fillLabel) fillLabel.textContent = 'Line Color';
    if (outlineLabel) outlineLabel.textContent = 'Dash';
    if (fillBtn) {
      fillBtn.title = 'Line Color: Thay đổi màu sắc nét vẽ';
      fillBtn.classList.add('active');
    }
    if (outlineBtn) {
      outlineBtn.title = 'Dash: Bật/tắt nét đứt nét liền';
      outlineBtn.classList.toggle('active', Boolean(sel.dash && sel.dash !== 'solid'));
    }
    if (fillSwatch) fillSwatch.style.background = sel.color || '#4a90d9';
    if (outlineSwatch) {
      outlineSwatch.style.background = 'transparent';
      outlineSwatch.style.borderColor = sel.color || '#4a90d9';
    }
  } else {
    // Default when no selection
    if (fillLabel) fillLabel.textContent = 'Fill';
    if (outlineLabel) outlineLabel.textContent = 'Outline';
    if (fillBtn) {
      fillBtn.title = 'Fill: Thay đổi màu/nội dung bên trong shape';
      fillBtn.classList.remove('active');
    }
    if (outlineBtn) {
      outlineBtn.title = 'Outline: Thay đổi đường viền bên ngoài shape';
      outlineBtn.classList.remove('active');
    }
    if (fillSwatch) fillSwatch.style.background = '#1a2035';
    if (outlineSwatch) {
      outlineSwatch.style.background = 'transparent';
      outlineSwatch.style.borderColor = '#4f8ef7';
    }
  }
}

/* ── 9. Ribbon Commands & Top Bar ─────────────────────────── */
function initRibbonCommands() {
  const docNameInput = document.getElementById('doc-name-input');
  docNameInput?.addEventListener('input', () => {
    state.name = docNameInput.value.trim() || 'untitled';
    scheduleSave();
  });

  const cwInput = document.getElementById('canvas-w');
  const chInput = document.getElementById('canvas-h');
  cwInput?.addEventListener('change', () => {
    pushUndo();
    state.canvasW = Math.max(16, Math.min(4096, parseInt(cwInput.value) || 256));
    cwInput.value = state.canvasW;
    updateStatusDims();
    render();
    scheduleSave();
  });
  chInput?.addEventListener('change', () => {
    pushUndo();
    state.canvasH = Math.max(16, Math.min(4096, parseInt(chInput.value) || 192));
    chInput.value = state.canvasH;
    updateStatusDims();
    render();
    scheduleSave();
  });

  document.getElementById('btn-theme-toggle')?.addEventListener('click', () => {
    const isLight = document.body.dataset.theme === 'light';
    if (isLight) delete document.body.dataset.theme;
    else document.body.dataset.theme = 'light';
    try { localStorage.setItem('hhdui_theme', isLight ? 'dark' : 'light'); } catch {}
  });
  const savedTheme = localStorage.getItem('hhdui_theme') || 'dark';
  if (savedTheme === 'light') document.body.dataset.theme = 'light';

  document.getElementById('btn-clear')?.addEventListener('click', () => {
    if (!state.nodes.length) return;
    if (confirm('Clear all objects on canvas?')) {
      pushUndo();
      state.nodes = [];
      editor.selectedIdxs = [];
      editor.selectedIdx = -1;
      buildPropsPanel();
      updateLayersList();
      updateStatusSelection();
      render();
      scheduleSave();
      showToast('Canvas cleared', 'info');
    }
  });

  document.getElementById('btn-export')?.addEventListener('click', () => {
    try {
      state.scene = sceneState;
      const json = Serializer.toJson(state);
      const blob = new Blob([json], { type: 'application/json' });
      const a = document.createElement('a');
      a.href = URL.createObjectURL(blob);
      const filename = (state.name || 'panel').replace(/[/\\?%*:|"<>]/g, '_');
      a.download = `${filename}.hhdui.json`;
      a.click();
      showToast(`Exported ${filename}.hhdui.json`, 'success');
    } catch (err) {
      showToast('Export error: ' + err.message, 'error');
    }
  });

  const importFileInput = document.getElementById('import-file');
  document.getElementById('btn-import')?.addEventListener('click', () => importFileInput?.click());
  importFileInput?.addEventListener('change', e => {
    const file = e.target.files[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = ev => {
      try {
        const s = Serializer.fromJson(ev.target.result);
        pushUndo();
        state.nodes = s.nodes;
        state.canvasW = s.canvasW;
        state.canvasH = s.canvasH;
        state.name = s.name;
        editor.selectedIdxs = [];
        editor.selectedIdx = -1;

        if (s.scene) {
          Object.assign(sceneState, s.scene);
          applySceneFilters();
          saveSceneState();
        }

        if (cwInput) cwInput.value = s.canvasW;
        if (chInput) chInput.value = s.canvasH;
        if (docNameInput) docNameInput.value = s.name;

        updateStatusDims();
        fitCanvas();
        buildPropsPanel();
        updateLayersList();
        updateStatusSelection();
        scheduleSave();
        showToast(`Imported ${file.name} successfully`, 'success');
      } catch (err) {
        showToast('Import error: ' + err.message, 'error');
      }
    };
    reader.readAsText(file);
    e.target.value = '';
  });

  // Tools: Text, Item, Block
  document.querySelectorAll('.ribbon-tool-btn[data-type]').forEach(btn => {
    btn.addEventListener('click', () => {
      const type = btn.dataset.type;
      const def = NodeDefs[type];
      if (!def) return;
      pushUndo();
      const node = normalizeNode(def.defaults());
      const cx = Math.round(state.canvasW / 2);
      const cy = Math.round(state.canvasH / 2);

      if (type === 'text') {
        node.boxX = cx - (node.width ?? 120) / 2;
        node.boxY = cy - (node.height ?? 20) / 2;
      } else {
        node.x = cx - (node.width ?? 24) / 2;
        node.y = cy - (node.height ?? 24) / 2;
      }

      state.nodes.push(node);
      editor.selectedIdx = state.nodes.length - 1;
      editor.selectedIdxs = [editor.selectedIdx];
      buildPropsPanel();
      updateLayersList();
      updateStatusSelection();
      updateQuickColorSwatches();
      render();
      scheduleSave();
    });
  });

  // Line Connector Dropdown
  const lineBtn = document.getElementById('btn-line-picker');
  const lineDropdown = document.getElementById('line-picker-dropdown');
  lineBtn?.addEventListener('click', e => {
    e.stopPropagation();
    const isOpen = !lineDropdown.hidden;
    lineDropdown.hidden = isOpen;
    lineBtn.classList.toggle('open', !isOpen);
  });
  document.addEventListener('click', e => {
    if (lineDropdown && !lineDropdown.hidden && !lineDropdown.contains(e.target) && e.target !== lineBtn) {
      lineDropdown.hidden = true;
      lineBtn.classList.remove('open');
    }
  });
  lineDropdown?.querySelectorAll('.line-item').forEach(item => {
    item.addEventListener('click', () => {
      const lt = item.dataset.lineType || 'straight';
      lineDropdown.hidden = true;
      lineBtn.classList.remove('open');
      pushUndo();
      const node = normalizeNode(NodeDefs.line.defaults());
      node.lineType = lt;
      const cx = Math.round(state.canvasW / 2);
      const cy = Math.round(state.canvasH / 2);
      node.x1 = cx - 50; node.y1 = cy - 20;
      node.x2 = cx + 50; node.y2 = cy + 20;
      if (lt === 'curve' || lt === 'curved_arrow') {
        node.curveCtrlX = cx;
        node.curveCtrlY = cy - 40;
      } else if (lt === 'elbow' || lt === 'elbow_arrow') {
        node.elbowMidX = cx;
        node.elbowMidY = cy;
      }
      state.nodes.push(node);
      editor.selectedIdx = state.nodes.length - 1;
      editor.selectedIdxs = [editor.selectedIdx];
      buildPropsPanel();
      updateLayersList();
      updateStatusSelection();
      updateQuickColorSwatches();
      render();
      scheduleSave();
    });
  });

  // History buttons
  document.getElementById('btn-undo')?.addEventListener('click', undo);
  document.getElementById('btn-redo')?.addEventListener('click', redo);

  // Clipboard buttons
  document.getElementById('btn-copy')?.addEventListener('click', copySelected);
  document.getElementById('btn-paste')?.addEventListener('click', pasteClipboard);
  document.getElementById('btn-duplicate')?.addEventListener('click', duplicateSelected);
  document.getElementById('btn-delete')?.addEventListener('click', deleteSelected);

  // Arrange & Flip buttons
  document.getElementById('btn-bring-forward')?.addEventListener('click', () => {
    if (editor.selectedIdx >= 0) moveNode(editor.selectedIdx, 1);
  });
  document.getElementById('btn-send-backward')?.addEventListener('click', () => {
    if (editor.selectedIdx >= 0) moveNode(editor.selectedIdx, -1);
  });
  document.getElementById('btn-flip-h')?.addEventListener('click', () => toggleFlip('flipX'));
  document.getElementById('btn-flip-v')?.addEventListener('click', () => toggleFlip('flipY'));

  // Quick Color buttons
  document.getElementById('btn-quick-fill')?.addEventListener('click', () => {
    if (editor.selectedIdx >= 0) {
      const pcrMount = propsBody.querySelector('.pickr-mount[data-key="color"] button');
      if (pcrMount) pcrMount.click();
    }
  });
  document.getElementById('btn-quick-outline')?.addEventListener('click', () => {
    if (editor.selectedIdx >= 0) {
      const sel = state.nodes[editor.selectedIdx];
      if (sel && sel.type === 'shape') {
        pushUndo();
        sel.outline = !sel.outline;
        buildPropsPanel();
        render();
        scheduleSave();
        updateQuickColorSwatches();
      } else if (sel && sel.type === 'text') {
        pushUndo();
        sel.fillType = sel.fillType === 'gradient' ? 'solid' : 'gradient';
        buildPropsPanel();
        render();
        scheduleSave();
        updateQuickColorSwatches();
      } else if (sel && sel.type === 'line') {
        pushUndo();
        sel.dash = sel.dash === 'dashed' ? 'solid' : 'dashed';
        buildPropsPanel();
        render();
        scheduleSave();
        updateQuickColorSwatches();
      }
    }
  });

  // Alignment buttons
  document.querySelectorAll('#ribbon-align-group .ribbon-icon-btn[data-align]').forEach(btn => {
    btn.addEventListener('click', () => alignSelected(btn.dataset.align));
  });

  // Camera & View buttons
  document.getElementById('btn-zoom-in')?.addEventListener('click', () => {
    const vpRect = canvasViewport.getBoundingClientRect();
    zoomAt(vpRect.width / 2, vpRect.height / 2, camera.zoom * 1.25);
  });
  document.getElementById('btn-zoom-out')?.addEventListener('click', () => {
    const vpRect = canvasViewport.getBoundingClientRect();
    zoomAt(vpRect.width / 2, vpRect.height / 2, camera.zoom / 1.25);
  });
  document.getElementById('btn-zoom-reset')?.addEventListener('click', resetZoom);
  document.getElementById('btn-zoom-fit')?.addEventListener('click', fitCanvas);

  const zoomSelect = document.getElementById('zoom-select');
  zoomSelect?.addEventListener('change', () => {
    const vpRect = canvasViewport.getBoundingClientRect();
    zoomAt(vpRect.width / 2, vpRect.height / 2, parseFloat(zoomSelect.value));
  });

  const gridToggle = document.getElementById('btn-grid-toggle');
  gridToggle?.addEventListener('click', () => {
    editor.grid = !editor.grid;
    gridToggle.classList.toggle('active', editor.grid);
    updateStatusGrid();
    render();
    scheduleSave();
  });

  const snapToggle = document.getElementById('btn-snap-toggle');
  snapToggle?.addEventListener('click', () => {
    editor.snap = !editor.snap;
    snapToggle.classList.toggle('active', editor.snap);
    updateStatusSnap();
    scheduleSave();
  });

  document.getElementById('status-zoom')?.addEventListener('click', resetZoom);
}

/* ── 10. Clipboard Actions ────────────────────────────────── */
function copySelected() {
  if (!editor.selectedIdxs.length) return;
  _clipboard = editor.selectedIdxs.map(i => JSON.parse(JSON.stringify(state.nodes[i]))).filter(Boolean);
  showToast(`Copied ${_clipboard.length} object(s)`, 'info');
}

function cutSelected() {
  if (!editor.selectedIdxs.length) return;
  copySelected();
  deleteSelected();
}

function pasteClipboard() {
  if (!_clipboard || !_clipboard.length) return;
  pushUndo();
  const newIdxs = [];
  _clipboard.forEach(orig => {
    const copy = JSON.parse(JSON.stringify(orig));
    copy.id = uniqueId(copy.type || 'node');
    if (copy._button) copy._button.nodeId = copy.id;
    if (copy.type === 'line') {
      copy.x1 += 10; copy.y1 += 10; copy.x2 += 10; copy.y2 += 10;
      if (copy.curveCtrlX != null) copy.curveCtrlX += 10;
      if (copy.curveCtrlY != null) copy.curveCtrlY += 10;
      if (copy.elbowMidX != null) copy.elbowMidX += 10;
      if (copy.elbowMidY != null) copy.elbowMidY += 10;
    } else if (copy.type === 'text') {
      copy.boxX += 10; copy.boxY += 10;
    } else {
      copy.x = (copy.x ?? 0) + 10; copy.y = (copy.y ?? 0) + 10;
    }
    state.nodes.push(normalizeNode(copy));
    newIdxs.push(state.nodes.length - 1);
  });
  editor.selectedIdxs = newIdxs;
  editor.selectedIdx = newIdxs[newIdxs.length - 1];
  buildPropsPanel();
  updateLayersList();
  updateStatusSelection();
  updateQuickColorSwatches();
  render();
  scheduleSave();
  showToast(`Pasted ${newIdxs.length} object(s)`, 'success');
}

function duplicateSelected() {
  if (!editor.selectedIdxs.length) return;
  copySelected();
  pasteClipboard();
}

function deleteSelected() {
  if (!editor.selectedIdxs.length) return;
  pushUndo();
  const sorted = [...editor.selectedIdxs].sort((a, b) => b - a);
  sorted.forEach(i => state.nodes.splice(i, 1));
  editor.selectedIdxs = [];
  editor.selectedIdx = -1;
  buildPropsPanel();
  updateLayersList();
  updateStatusSelection();
  updateQuickColorSwatches();
  render();
  scheduleSave();
}

function selectAll() {
  editor.selectedIdxs = state.nodes.map((_, i) => i).filter(i => !state.nodes[i].locked);
  editor.selectedIdx = editor.selectedIdxs.length ? editor.selectedIdxs[editor.selectedIdxs.length - 1] : -1;
  buildPropsPanel();
  updateLayersList();
  updateStatusSelection();
  updateQuickColorSwatches();
  render();
}

/* ── 11. Mirror / Flip & Alignment ────────────────────────── */
function toggleFlip(prop) {
  if (!editor.selectedIdxs.length) return;
  pushUndo();
  editor.selectedIdxs.forEach(idx => {
    const n = state.nodes[idx];
    if (n) n[prop] = !n[prop];
  });
  if (editor.selectedIdx >= 0) refreshPropsInputs(state.nodes[editor.selectedIdx]);
  render();
  scheduleSave();
}

function alignSelected(type) {
  if (editor.selectedIdxs.length < 2) return;
  pushUndo();
  const boxes = editor.selectedIdxs.map(idx => ({
    node: state.nodes[idx],
    box: getNodeBoundingBox(state.nodes[idx]),
  }));

  if (type === 'left') {
    const minX = Math.min(...boxes.map(b => b.box.x));
    boxes.forEach(b => _setNodePos(b.node, minX, b.box.y));
  } else if (type === 'center') {
    const minX = Math.min(...boxes.map(b => b.box.x));
    const maxX = Math.max(...boxes.map(b => b.box.x + b.box.w));
    const midX = (minX + maxX) / 2;
    boxes.forEach(b => _setNodePos(b.node, midX - b.box.w / 2, b.box.y));
  } else if (type === 'right') {
    const maxX = Math.max(...boxes.map(b => b.box.x + b.box.w));
    boxes.forEach(b => _setNodePos(b.node, maxX - b.box.w, b.box.y));
  } else if (type === 'top') {
    const minY = Math.min(...boxes.map(b => b.box.y));
    boxes.forEach(b => _setNodePos(b.node, b.box.x, minY));
  } else if (type === 'middle') {
    const minY = Math.min(...boxes.map(b => b.box.y));
    const maxY = Math.max(...boxes.map(b => b.box.y + b.box.h));
    const midY = (minY + maxY) / 2;
    boxes.forEach(b => _setNodePos(b.node, b.box.x, midY - b.box.h / 2));
  } else if (type === 'bottom') {
    const maxY = Math.max(...boxes.map(b => b.box.y + b.box.h));
    boxes.forEach(b => _setNodePos(b.node, b.box.x, maxY - b.box.h));
  }

  if (editor.selectedIdx >= 0) refreshPropsInputs(state.nodes[editor.selectedIdx]);
  render();
  scheduleSave();
}

function _setNodePos(n, x, y) {
  x = Math.round(x);
  y = Math.round(y);
  if (n.type === 'text') {
    n.boxX = x; n.boxY = y;
  } else if (n.type === 'item') {
    const sz = 16 * (n.scale || 0.8);
    n.x = x + sz / 2; n.y = y + sz / 2;
  } else if (n.type === 'line') {
    const dx = x - Math.min(n.x1, n.x2);
    const dy = y - Math.min(n.y1, n.y2);
    n.x1 += dx; n.y1 += dy; n.x2 += dx; n.y2 += dy;
    if (n.curveCtrlX != null) n.curveCtrlX += dx;
    if (n.curveCtrlY != null) n.curveCtrlY += dy;
    if (n.elbowMidX != null) n.elbowMidX += dx;
    if (n.elbowMidY != null) n.elbowMidY += dy;
  } else {
    n.x = x; n.y = y;
  }
}

/* ── 12. Layers Panel ─────────────────────────────────────── */
function updateLayersList() {
  const list = document.getElementById('layers-list');
  const empty = document.getElementById('layers-empty');
  const count = document.getElementById('layer-count');
  if (!list) return;

  if (count) count.textContent = state.nodes.length;
  if (empty) empty.style.display = state.nodes.length ? 'none' : 'block';

  list.innerHTML = '';
  [...state.nodes].reverse().forEach((n, ri) => {
    const realIdx = state.nodes.length - 1 - ri;
    const def = NodeDefs[n.type];
    const isSelected = editor.selectedIdxs.includes(realIdx);
    const li = document.createElement('li');

    li.className = 'layer-item' +
      (isSelected ? ' selected' : '') +
      (n.visible === false ? ' hidden-layer' : '') +
      (n.locked ? ' locked-layer' : '');
    li.draggable = true;
    li.dataset.realIdx = realIdx;

    li.innerHTML = `
      <button class="layer-vis-btn ${n.visible !== false ? 'active' : ''}" data-act="toggle-vis" title="${n.visible !== false ? 'Hide' : 'Show'}">
        ${getSvgIcon(n.visible !== false ? 'eye' : 'eyeOff')}
      </button>
      <button class="layer-lock-btn ${n.locked ? 'active' : ''}" data-act="toggle-lock" title="${n.locked ? 'Unlock' : 'Lock'}">
        ${getSvgIcon(n.locked ? 'lock' : 'unlock')}
      </button>
      <span class="layer-icon">${getSvgIcon(n.type === 'shape' ? 'shape' : n.type === 'text' ? 'text' : n.type === 'item' ? 'item' : n.type === 'block' ? 'block' : 'line')}</span>
      <span class="layer-name">${escHtml(def?.label(n) ?? n.type)}</span>
      <button class="layer-reorder-btn" data-act="up" title="Bring Forward">▲</button>
      <button class="layer-reorder-btn" data-act="down" title="Send Backward">▼</button>
    `;

    li.addEventListener('click', e => {
      if (e.target.closest('.layer-vis-btn') || e.target.closest('.layer-lock-btn') || e.target.closest('.layer-reorder-btn')) return;
      if (e.shiftKey || e.ctrlKey) {
        const pos = editor.selectedIdxs.indexOf(realIdx);
        if (pos !== -1) editor.selectedIdxs.splice(pos, 1);
        else editor.selectedIdxs.push(realIdx);
        editor.selectedIdx = editor.selectedIdxs.length ? editor.selectedIdxs[editor.selectedIdxs.length - 1] : -1;
      } else {
        editor.selectedIdx = realIdx;
        editor.selectedIdxs = [realIdx];
      }
      buildPropsPanel();
      updateStatusSelection();
      updateQuickColorSwatches();
      updateLayersList();
      render();
    });

    li.querySelectorAll('[data-act]').forEach(btn => {
      btn.addEventListener('click', ev => {
        ev.stopPropagation();
        const act = btn.dataset.act;
        if (act === 'toggle-vis') {
          n.visible = n.visible === false ? true : false;
          updateLayersList();
          render();
          scheduleSave();
        } else if (act === 'toggle-lock') {
          n.locked = !n.locked;
          updateLayersList();
          render();
          scheduleSave();
        } else if (act === 'up') {
          moveNode(realIdx, 1);
        } else if (act === 'down') {
          moveNode(realIdx, -1);
        }
      });
    });

    list.appendChild(li);
  });
}

function moveNode(idx, dir) {
  const to = idx + dir;
  if (to < 0 || to >= state.nodes.length) return;
  pushUndo();
  [state.nodes[idx], state.nodes[to]] = [state.nodes[to], state.nodes[idx]];
  editor.selectedIdx = to;
  editor.selectedIdxs = [to];
  buildPropsPanel();
  updateLayersList();
  render();
  scheduleSave();
}

/* ── 13. Inspector (Properties Panel) ─────────────────────── */
let _activePickrs = [];

function destroyPickrs() {
  _activePickrs.forEach(p => { try { p.destroyAndRemove(); } catch (_) {} });
  _activePickrs = [];
}

function buildPropsPanel() {
  destroyPickrs();
  const iconSpan = document.getElementById('props-type-icon');
  const titleText = document.getElementById('props-title-text');
  const idBadge = document.getElementById('props-node-id-badge');

  if (editor.selectedIdxs.length === 0 || editor.selectedIdx < 0) {
    if (iconSpan) iconSpan.innerHTML = '';
    if (titleText) titleText.textContent = 'PROPERTIES';
    if (idBadge) idBadge.textContent = '';
    propsBody.innerHTML = `
      <div class="empty-state">
        <div class="empty-icon">${getSvgIcon('select')}</div>
        <div>Select an object on canvas or layers to inspect properties</div>
      </div>`;
    return;
  }

  // Multi-selection panel
  if (editor.selectedIdxs.length > 1) {
    if (iconSpan) iconSpan.innerHTML = getSvgIcon('select');
    if (titleText) titleText.textContent = `${editor.selectedIdxs.length} OBJECTS`;
    if (idBadge) idBadge.textContent = 'Multi';

    propsBody.innerHTML = `
      <div class="inspector-section">
        <div class="inspector-header">
          <span class="inspector-title">ALIGNMENT</span>
        </div>
        <div class="inspector-content">
          <div style="display:grid;grid-template-columns:repeat(6,1fr);gap:4px;">
            <button class="cmd-btn" data-align="left" title="Align Left">L</button>
            <button class="cmd-btn" data-align="center" title="Align Center">C</button>
            <button class="cmd-btn" data-align="right" title="Align Right">R</button>
            <button class="cmd-btn" data-align="top" title="Align Top">T</button>
            <button class="cmd-btn" data-align="middle" title="Align Middle">M</button>
            <button class="cmd-btn" data-align="bottom" title="Align Bottom">B</button>
          </div>
        </div>
      </div>

      <div class="inspector-section">
        <div class="inspector-header">
          <span class="inspector-title">TRANSFORM</span>
        </div>
        <div class="inspector-content">
          <div style="display:flex;gap:6px;">
            <button id="multi-flip-x" class="cmd-btn" style="flex:1;">Flip H</button>
            <button id="multi-flip-y" class="cmd-btn" style="flex:1;">Flip V</button>
          </div>
        </div>
      </div>

      <button id="prop-del-btn" class="prop-delete">Delete ${editor.selectedIdxs.length} objects</button>
    `;

    propsBody.querySelectorAll('[data-align]').forEach(btn => {
      btn.addEventListener('click', () => alignSelected(btn.dataset.align));
    });
    document.getElementById('multi-flip-x')?.addEventListener('click', () => toggleFlip('flipX'));
    document.getElementById('multi-flip-y')?.addEventListener('click', () => toggleFlip('flipY'));
    document.getElementById('prop-del-btn')?.addEventListener('click', deleteSelected);
    return;
  }

  // Single Selection Inspector
  const n = state.nodes[editor.selectedIdx];
  if (!n) return;
  const def = NodeDefs[n.type] || NodeDefs.shape;

  if (iconSpan) iconSpan.innerHTML = getSvgIcon(n.type === 'shape' ? 'shape' : n.type === 'text' ? 'text' : n.type === 'item' ? 'item' : n.type === 'block' ? 'block' : 'line');
  if (titleText) titleText.textContent = n.type.toUpperCase();
  if (idBadge) idBadge.textContent = n.id;

  let html = '';

  // 1. TRANSFORM Section (All nodes)
  html += `
    <div class="inspector-section ${editor.collapsedSections.transform ? 'collapsed' : ''}" data-sec="transform">
      <div class="inspector-header">
        <span class="inspector-title">TRANSFORM</span>
        <span class="inspector-chevron">${getSvgIcon('chevronDown')}</span>
      </div>
      <div class="inspector-content">
  `;

  if (n.type === 'line') {
    html += `
      <div class="inspector-pair-grid">
        <div class="inspector-pair"><span class="inspector-pair-label">X1</span><input type="number" class="inspector-pair-input" data-key="x1" value="${n.x1}" /></div>
        <div class="inspector-pair"><span class="inspector-pair-label">Y1</span><input type="number" class="inspector-pair-input" data-key="y1" value="${n.y1}" /></div>
      </div>
      <div class="inspector-pair-grid">
        <div class="inspector-pair"><span class="inspector-pair-label">X2</span><input type="number" class="inspector-pair-input" data-key="x2" value="${n.x2}" /></div>
        <div class="inspector-pair"><span class="inspector-pair-label">Y2</span><input type="number" class="inspector-pair-input" data-key="y2" value="${n.y2}" /></div>
      </div>
    `;
    if (n.lineType === 'curve' || n.lineType === 'curved_arrow') {
      html += `
        <div class="inspector-pair-grid">
          <div class="inspector-pair"><span class="inspector-pair-label">CX</span><input type="number" class="inspector-pair-input" data-key="curveCtrlX" value="${n.curveCtrlX ?? Math.round((n.x1 + n.x2) / 2)}" /></div>
          <div class="inspector-pair"><span class="inspector-pair-label">CY</span><input type="number" class="inspector-pair-input" data-key="curveCtrlY" value="${n.curveCtrlY ?? Math.round((n.y1 + n.y2) / 2 - 20)}" /></div>
        </div>
      `;
    } else if (n.lineType === 'elbow' || n.lineType === 'elbow_arrow') {
      html += `
        <div class="inspector-pair-grid">
          <div class="inspector-pair"><span class="inspector-pair-label">MX</span><input type="number" class="inspector-pair-input" data-key="elbowMidX" value="${n.elbowMidX ?? Math.round((n.x1 + n.x2) / 2)}" /></div>
          <div class="inspector-pair"><span class="inspector-pair-label">MY</span><input type="number" class="inspector-pair-input" data-key="elbowMidY" value="${n.elbowMidY ?? Math.round((n.y1 + n.y2) / 2)}" /></div>
        </div>
      `;
    }
  } else if (n.type === 'text') {
    html += `
      <div class="inspector-pair-grid">
        <div class="inspector-pair"><span class="inspector-pair-label">X</span><input type="number" class="inspector-pair-input" data-key="boxX" value="${n.boxX}" /></div>
        <div class="inspector-pair"><span class="inspector-pair-label">Y</span><input type="number" class="inspector-pair-input" data-key="boxY" value="${n.boxY}" /></div>
      </div>
      <div class="inspector-pair-grid">
        <div class="inspector-pair"><span class="inspector-pair-label">W</span><input type="number" class="inspector-pair-input" data-key="width" value="${n.width}" /></div>
        <div class="inspector-pair"><span class="inspector-pair-label">H</span><input type="number" class="inspector-pair-input" data-key="height" value="${n.height}" /></div>
      </div>
    `;
  } else {
    html += `
      <div class="inspector-pair-grid">
        <div class="inspector-pair"><span class="inspector-pair-label">X</span><input type="number" class="inspector-pair-input" data-key="x" value="${n.x}" /></div>
        <div class="inspector-pair"><span class="inspector-pair-label">Y</span><input type="number" class="inspector-pair-input" data-key="y" value="${n.y}" /></div>
      </div>
    `;
    if (n.type !== 'item') {
      html += `
        <div class="inspector-pair-grid">
          <div class="inspector-pair"><span class="inspector-pair-label">W</span><input type="number" class="inspector-pair-input" data-key="width" value="${n.width}" /></div>
          <div class="inspector-pair"><span class="inspector-pair-label">H</span><input type="number" class="inspector-pair-input" data-key="height" value="${n.height}" /></div>
        </div>
      `;
    }
  }

  // Rotation, Scale & Skew
  const rot = Math.round((((n.rotation || 0) % 360) + 360) % 360);
  html += `
    <div class="inspector-row">
      <span class="inspector-label">Rotation</span>
      <input type="range" min="0" max="360" step="1" value="${rot}" data-key="rotation" style="flex:1;accent-color:#4f8ef7;" />
      <div class="inspector-unit-wrap">
        <input type="number" min="0" max="360" step="1" value="${rot}" data-key="rotation" class="inspector-unit-input" />
        <span class="inspector-unit-label">°</span>
      </div>
    </div>
    <div class="inspector-pair-grid">
      <div class="inspector-pair"><span class="inspector-pair-label">SX</span><input type="number" class="inspector-pair-input" data-key="scaleX" value="${n.scaleX ?? 1.0}" step="0.1" /></div>
      <div class="inspector-pair"><span class="inspector-pair-label">SY</span><input type="number" class="inspector-pair-input" data-key="scaleY" value="${n.scaleY ?? 1.0}" step="0.1" /></div>
    </div>
    <div class="inspector-pair-grid">
      <div class="inspector-pair"><span class="inspector-pair-label">KX</span><input type="number" class="inspector-pair-input" data-key="skewX" value="${n.skewX ?? 0}" step="1" min="-80" max="80" /></div>
      <div class="inspector-pair"><span class="inspector-pair-label">KY</span><input type="number" class="inspector-pair-input" data-key="skewY" value="${n.skewY ?? 0}" step="1" min="-80" max="80" /></div>
    </div>
    <div class="inspector-row">
      <span class="inspector-label">Mirror</span>
      <div style="display:flex;gap:4px;flex:1;">
        <button id="btn-prop-flip-x" class="cmd-btn ${n.flipX ? 'cmd-btn-primary' : ''}" style="flex:1;">Flip H</button>
        <button id="btn-prop-flip-y" class="cmd-btn ${n.flipY ? 'cmd-btn-primary' : ''}" style="flex:1;">Flip V</button>
      </div>
    </div>
  </div></div>`;

  // 2. LINE SPECIFIC INSPECTOR
  if (n.type === 'line') {
    html += `
      <div class="inspector-section ${editor.collapsedSections.lineStyle ? 'collapsed' : ''}" data-sec="lineStyle">
        <div class="inspector-header">
          <span class="inspector-title">LINE STYLE</span>
          <span class="inspector-chevron">${getSvgIcon('chevronDown')}</span>
        </div>
        <div class="inspector-content">
          <div class="inspector-row">
            <span class="inspector-label">Type</span>
            <select class="inspector-select" data-key="lineType">
              <option value="straight" ${n.lineType === 'straight' ? 'selected' : ''}>Straight Line</option>
              <option value="arrow" ${n.lineType === 'arrow' ? 'selected' : ''}>Line Arrow</option>
              <option value="double_arrow" ${n.lineType === 'double_arrow' ? 'selected' : ''}>Double Arrow</option>
              <option value="elbow" ${n.lineType === 'elbow' ? 'selected' : ''}>Elbow Connector</option>
              <option value="elbow_arrow" ${n.lineType === 'elbow_arrow' ? 'selected' : ''}>Elbow Arrow</option>
              <option value="curve" ${n.lineType === 'curve' ? 'selected' : ''}>Curved Line</option>
              <option value="curved_arrow" ${n.lineType === 'curved_arrow' ? 'selected' : ''}>Curved Arrow</option>
            </select>
          </div>
          <div class="inspector-row">
            <span class="inspector-label">Color</span>
            <div class="pickr-mount" data-key="color" data-alpha-key="alpha"></div>
          </div>
          <div class="inspector-row">
            <span class="inspector-label">Thickness</span>
            <input type="number" class="inspector-input" data-key="thickness" value="${n.thickness ?? 2}" min="1" max="48" />
          </div>
          <div class="inspector-row">
            <span class="inspector-label">Dash</span>
            <select class="inspector-select" data-key="dash">
              <option value="solid" ${n.dash === 'solid' ? 'selected' : ''}>Solid</option>
              <option value="dashed" ${n.dash === 'dashed' ? 'selected' : ''}>Dashed</option>
              <option value="dotted" ${n.dash === 'dotted' ? 'selected' : ''}>Dotted</option>
              <option value="dash_dot" ${n.dash === 'dash_dot' ? 'selected' : ''}>Dash Dot</option>
            </select>
          </div>
          <div class="inspector-row">
            <span class="inspector-label">End Cap</span>
            <select class="inspector-select" data-key="cap">
              <option value="butt" ${(n.cap || 'butt') === 'butt' ? 'selected' : ''}>Square (Butt)</option>
              <option value="square" ${n.cap === 'square' ? 'selected' : ''}>Square Ext</option>
            </select>
          </div>
        </div>
      </div>

      <div class="inspector-section ${editor.collapsedSections.lineArrow ? 'collapsed' : ''}" data-sec="lineArrow">
        <div class="inspector-header">
          <span class="inspector-title">ARROWHEADS</span>
          <span class="inspector-chevron">${getSvgIcon('chevronDown')}</span>
        </div>
        <div class="inspector-content">
          <div class="inspector-checkbox-row">
            <span class="inspector-label">Start Arrow</span>
            <input type="checkbox" class="inspector-checkbox" data-key="arrowStart" ${n.arrowStart ? 'checked' : ''} />
          </div>
          <div class="inspector-checkbox-row">
            <span class="inspector-label">End Arrow</span>
            <input type="checkbox" class="inspector-checkbox" data-key="arrowEnd" ${n.arrowEnd ? 'checked' : ''} />
          </div>
          <div class="inspector-pair-grid">
            <div class="inspector-pair"><span class="inspector-pair-label">Len</span><input type="number" class="inspector-pair-input" data-key="arrowLength" value="${n.arrowLength ?? 10}" min="4" max="40" /></div>
            <div class="inspector-pair"><span class="inspector-pair-label">Wid</span><input type="number" class="inspector-pair-input" data-key="arrowWidth" value="${n.arrowWidth ?? 8}" min="4" max="40" /></div>
          </div>
        </div>
      </div>
    `;
  }

  // 3. SHAPE APPEARANCE Section
  if (n.type === 'shape' || n.type === 'background') {
    const shapeDef = getShapeDef(n.shapeType || 'rect');
    html += `
      <div class="inspector-section ${editor.collapsedSections.appearance ? 'collapsed' : ''}" data-sec="appearance">
        <div class="inspector-header">
          <span class="inspector-title">APPEARANCE</span>
          <span class="inspector-chevron">${getSvgIcon('chevronDown')}</span>
        </div>
        <div class="inspector-content">
          <div class="inspector-row">
            <span class="inspector-label">Shape</span>
            <button id="btn-inspector-shape-picker" class="shape-picker-btn" style="flex:1;" type="button">
              <span class="shape-picker-icon"><svg viewBox="0 0 20 20" fill="currentColor">${shapeDef.icon}</svg></span>
              <span>${shapeDef.name}</span>
              <svg class="svg-icon shape-caret" viewBox="0 0 24 24" style="margin-left:auto;"><polyline points="6 9 12 15 18 9"/></svg>
            </button>
          </div>
          <div class="inspector-row">
            <span class="inspector-label">Fill Color</span>
            <div class="pickr-mount" data-key="color" data-alpha-key="alpha"></div>
          </div>
          ${n.shapeType === 'rounded_rect' ? `
            <div class="inspector-row">
              <span class="inspector-label">Radius</span>
              <input type="number" class="inspector-input" data-key="cornerRadius" value="${n.cornerRadius ?? 6}" min="0" max="64" />
            </div>
          ` : ''}
          <div class="inspector-checkbox-row">
            <span class="inspector-label">Outline</span>
            <input type="checkbox" class="inspector-checkbox" data-key="outline" ${n.outline ? 'checked' : ''} />
          </div>
          ${n.outline ? `
            <div class="inspector-row">
              <span class="inspector-label">Color</span>
              <div class="pickr-mount" data-key="outlineColor" data-alpha-key="outlineAlpha"></div>
            </div>
            <div class="inspector-row">
              <span class="inspector-label">Thickness</span>
              <input type="number" class="inspector-input" data-key="outlineThickness" value="${n.outlineThickness ?? 2}" min="1" max="32" />
            </div>
            <div class="inspector-row">
              <span class="inspector-label">Style</span>
              <select class="inspector-select" data-key="outlineStyle">
                <option value="solid" ${(n.outlineStyle || 'solid') === 'solid' ? 'selected' : ''}>Solid</option>
                <option value="dashed" ${n.outlineStyle === 'dashed' ? 'selected' : ''}>Dashed</option>
                <option value="dotted" ${n.outlineStyle === 'dotted' ? 'selected' : ''}>Dotted</option>
                <option value="dash_dot" ${n.outlineStyle === 'dash_dot' ? 'selected' : ''}>Dash Dot</option>
              </select>
            </div>
          ` : ''}
        </div>
      </div>
    `;
  }

  // 4. TEXT Section (Solid / Gradient)
  if (n.type === 'text') {
    const isGrad = n.fillType === 'gradient';
    const grad = n.gradient || { type: 'linear', angle: 0, stops: [{ offset: 0, color: '#ff4d4f' }, { offset: 1, color: '#4f8ef7' }] };
    const anim = n.gradientAnimation || { enabled: false, speed: 1.0, direction: 'forward', mode: 'scroll' };

    html += `
      <div class="inspector-section ${editor.collapsedSections.text ? 'collapsed' : ''}" data-sec="text">
        <div class="inspector-header">
          <span class="inspector-title">TEXT</span>
          <span class="inspector-chevron">${getSvgIcon('chevronDown')}</span>
        </div>
        <div class="inspector-content">
          <div class="inspector-row">
            <textarea class="inspector-textarea" data-key="text" rows="3">${escHtml(n.text)}</textarea>
          </div>
          <div class="inspector-row">
            <span class="inspector-label">Font Size</span>
            <input type="number" class="inspector-input" data-key="fontSize" value="${n.fontSize ?? 8}" min="4" max="64" step="0.5" />
          </div>
          <div class="inspector-row">
            <span class="inspector-label">Align H</span>
            <select class="inspector-select" data-key="alignment">
              <option value="LEFT" ${n.alignment === 'LEFT' ? 'selected' : ''}>LEFT</option>
              <option value="CENTER" ${n.alignment === 'CENTER' ? 'selected' : ''}>CENTER</option>
              <option value="RIGHT" ${n.alignment === 'RIGHT' ? 'selected' : ''}>RIGHT</option>
            </select>
          </div>
          <div class="inspector-checkbox-row">
            <span class="inspector-label">Shadow</span>
            <input type="checkbox" class="inspector-checkbox" data-key="shadow" ${n.shadow ? 'checked' : ''} />
          </div>
        </div>
      </div>

      <div class="inspector-section" data-sec="textFill">
        <div class="inspector-header">
          <span class="inspector-title">FILL & COLOR</span>
          <span class="inspector-chevron">${getSvgIcon('chevronDown')}</span>
        </div>
        <div class="inspector-content">
          <div class="fill-type-seg">
            <button class="fill-type-btn ${!isGrad ? 'active' : ''}" id="btn-fill-solid" type="button">Solid</button>
            <button class="fill-type-btn ${isGrad ? 'active' : ''}" id="btn-fill-gradient" type="button">Gradient</button>
          </div>

          ${!isGrad ? `
            <div class="inspector-row" style="margin-top:6px;">
              <span class="inspector-label">Color</span>
              <div class="pickr-mount" data-key="color" data-alpha-key="alpha"></div>
            </div>
          ` : `
            <div style="margin-top:8px;">
              <div class="gradient-bar-preview" id="grad-preview-bar"></div>
              <div class="inspector-row">
                <span class="inspector-label">Angle</span>
                <input type="range" min="0" max="360" step="1" value="${grad.angle || 0}" id="grad-angle-slider" style="flex:1;accent-color:#4f8ef7;" />
                <span style="font-size:10px;font-family:var(--mono);width:32px;text-align:right;" id="grad-angle-val">${grad.angle || 0}°</span>
              </div>
              <div class="gradient-stops-list" id="grad-stops-container"></div>
              <button id="btn-add-grad-stop" class="cmd-btn" style="width:100%;margin-top:4px;" type="button">+ Add Color Stop</button>
            </div>

            <div style="margin-top:10px;border-top:1px solid var(--border2);padding-top:8px;">
              <div class="inspector-checkbox-row">
                <span class="inspector-label">Animation</span>
                <input type="checkbox" class="inspector-checkbox" id="grad-anim-enable" ${anim.enabled ? 'checked' : ''} />
              </div>
              <div class="inspector-row">
                <span class="inspector-label">Speed</span>
                <input type="range" min="0.2" max="4.0" step="0.1" value="${anim.speed || 1.0}" id="grad-anim-speed" style="flex:1;accent-color:#4f8ef7;" />
                <span style="font-size:10px;font-family:var(--mono);width:28px;text-align:right;" id="grad-anim-speed-val">${anim.speed || 1.0}×</span>
              </div>
              <div class="inspector-row">
                <span class="inspector-label">Direction</span>
                <select class="inspector-select" id="grad-anim-dir">
                  <option value="forward" ${anim.direction === 'forward' ? 'selected' : ''}>Forward</option>
                  <option value="reverse" ${anim.direction === 'reverse' ? 'selected' : ''}>Reverse</option>
                </select>
              </div>
            </div>
          `}
        </div>
      </div>
    `;
  }

  // 5. ITEM / BLOCK Section
  if (n.type === 'item' || n.type === 'block') {
    html += `
      <div class="inspector-section ${editor.collapsedSections.item ? 'collapsed' : ''}" data-sec="item">
        <div class="inspector-header">
          <span class="inspector-title">${n.type.toUpperCase()}</span>
          <span class="inspector-chevron">${getSvgIcon('chevronDown')}</span>
        </div>
        <div class="inspector-content">
          <div class="inspector-row">
            <span class="inspector-label">Material</span>
            <div style="display:flex;gap:4px;flex:1;">
              <input type="text" class="inspector-input" data-key="material" value="${n.material || 'STONE'}" />
              <button id="btn-browse-material" class="cmd-btn" title="Browse Items">...</button>
            </div>
          </div>
          ${n.type === 'item' ? `
            <div class="inspector-row">
              <span class="inspector-label">Scale</span>
              <input type="number" class="inspector-input" data-key="scale" value="${n.scale ?? 0.8}" min="0.1" max="10" step="0.1" />
            </div>
          ` : `
            <div class="inspector-row">
              <span class="inspector-label">Thickness</span>
              <input type="number" class="inspector-input" data-key="thickness" value="${n.thickness ?? 1.0}" min="0.1" max="10" step="0.1" />
            </div>
          `}
        </div>
      </div>
    `;
  }

  // 6. BUTTON INTERACTION Section
  const btn = n._button || {};
  const t = btn.action?.type || 'NONE';
  const v = btn.action?.value || '';
  const desc = btn.description || '';

  html += `
    <div class="inspector-section ${editor.collapsedSections.interaction ? 'collapsed' : ''}" data-sec="interaction">
      <div class="inspector-header">
        <span class="inspector-title">INTERACTION</span>
        <span class="inspector-chevron">${getSvgIcon('chevronDown')}</span>
      </div>
      <div class="inspector-content">
        <div class="inspector-row">
          <span class="inspector-label">Action</span>
          <select id="action-type-sel" class="inspector-select">
            <option value="NONE" ${t === 'NONE' ? 'selected' : ''}>None</option>
            <option value="OPEN_URL" ${t === 'OPEN_URL' ? 'selected' : ''}>Open URL</option>
            <option value="PLAYER_COMMAND" ${t === 'PLAYER_COMMAND' ? 'selected' : ''}>Player Command</option>
            <option value="CONSOLE_COMMAND" ${t === 'CONSOLE_COMMAND' ? 'selected' : ''}>Console Command</option>
            <option value="SUGGEST_COMMAND" ${t === 'SUGGEST_COMMAND' ? 'selected' : ''}>Suggest Command</option>
          </select>
        </div>
        <div class="inspector-row" id="action-val-row" style="display:${t !== 'NONE' ? 'flex' : 'none'};">
          <span class="inspector-label">Value</span>
          <input type="text" id="action-val-input" class="inspector-input" value="${escHtml(v)}" placeholder="URL or /command…" />
        </div>
        <div class="inspector-row">
          <span class="inspector-label">Tooltip</span>
          <input type="text" id="action-desc-input" class="inspector-input" value="${escHtml(desc)}" placeholder="Hover tooltip…" />
        </div>
      </div>
    </div>
  `;

  html += `<button id="prop-del-btn" class="prop-delete">Delete object</button>`;

  propsBody.innerHTML = html;

  // Wire Collapsible Headers
  propsBody.querySelectorAll('.inspector-header').forEach(hdr => {
    hdr.addEventListener('click', () => {
      const sec = hdr.closest('.inspector-section');
      const secName = sec?.dataset.sec;
      sec.classList.toggle('collapsed');
      if (secName) editor.collapsedSections[secName] = sec.classList.contains('collapsed');
    });
  });

  // Wire Inputs
  propsBody.querySelectorAll('[data-key]').forEach(el => {
    el.addEventListener('input', () => applyInspectorInput(el, n, false));
    el.addEventListener('change', () => applyInspectorInput(el, n, true));
  });

  // Wire Flip Buttons
  document.getElementById('btn-prop-flip-x')?.addEventListener('click', () => toggleFlip('flipX'));
  document.getElementById('btn-prop-flip-y')?.addEventListener('click', () => toggleFlip('flipY'));

  // Wire Shape Picker in Inspector
  document.getElementById('btn-inspector-shape-picker')?.addEventListener('click', e => {
    e.stopPropagation();
    const dropdown = document.getElementById('shape-picker-dropdown');
    if (dropdown) {
      dropdown.hidden = false;
      const rect = e.currentTarget.getBoundingClientRect();
      dropdown.style.position = 'fixed';
      dropdown.style.top = `${rect.bottom + 4}px`;
      dropdown.style.right = `${window.innerWidth - rect.right}px`;
    }
  });

  // Wire Text Fill Type (Solid / Gradient)
  document.getElementById('btn-fill-solid')?.addEventListener('click', () => {
    pushUndo();
    n.fillType = 'solid';
    buildPropsPanel();
    render();
    scheduleSave();
  });
  document.getElementById('btn-fill-gradient')?.addEventListener('click', () => {
    pushUndo();
    n.fillType = 'gradient';
    buildPropsPanel();
    render();
    scheduleSave();
  });

  // Wire Gradient Controls
  if (n.type === 'text' && n.fillType === 'gradient') {
    wireGradientControls(n);
  }

  // Wire Pickr instances
  propsBody.querySelectorAll('.pickr-mount').forEach(el => {
    const key = el.dataset.key;
    const alphaKey = el.dataset.alphaKey || null;
    initColorPicker(
      el,
      n[key] || '#000000',
      alphaKey ? (n[alphaKey] ?? 255) : 255,
      hex => { n[key] = hex; updateQuickColorSwatches(); },
      alphaKey ? a => { n[alphaKey] = a; } : null,
    );
  });

  // Wire Item Picker
  document.getElementById('btn-browse-material')?.addEventListener('click', () => {
    ItemPicker.open(n.material, namespace => {
      n.material = namespace.replace('minecraft:', '').toUpperCase();
      const inp = propsBody.querySelector('[data-key="material"]');
      if (inp) inp.value = n.material;
      render();
      scheduleSave();
    });
  });

  wireButtonAction(n);
  document.getElementById('prop-del-btn')?.addEventListener('click', deleteSelected);
}

function wireGradientControls(n) {
  const grad = n.gradient;
  const bar = document.getElementById('grad-preview-bar');
  const angleSlider = document.getElementById('grad-angle-slider');
  const angleVal = document.getElementById('grad-angle-val');
  const stopsCont = document.getElementById('grad-stops-container');

  const updateGradBar = () => {
    if (!bar) return;
    const str = grad.stops.map(s => `${s.color || '#fff'} ${Math.round((s.offset ?? 0) * 100)}%`).join(', ');
    bar.style.background = `linear-gradient(to right, ${str})`;
  };
  updateGradBar();

  angleSlider?.addEventListener('input', () => {
    grad.angle = parseInt(angleSlider.value, 10) || 0;
    if (angleVal) angleVal.textContent = `${grad.angle}°`;
    render();
  });
  angleSlider?.addEventListener('change', () => {
    pushUndo();
    scheduleSave();
  });

  // Render stops
  if (stopsCont) {
    stopsCont.innerHTML = '';
    grad.stops.forEach((st, idx) => {
      const row = document.createElement('div');
      row.className = 'gradient-stop-row';
      row.innerHTML = `
        <span class="gradient-stop-num">#${idx + 1}</span>
        <div class="gradient-stop-swatch" id="grad-swatch-${idx}" style="background:${st.color || '#fff'};"></div>
        <input type="number" class="gradient-stop-pos" value="${Math.round((st.offset ?? 0) * 100)}" min="0" max="100" />
        <span style="font-size:10px;color:var(--text-dim);">%</span>
        ${grad.stops.length > 2 ? `<button class="gradient-stop-del" title="Remove stop">✕</button>` : ''}
      `;

      // Pos change
      const posInp = row.querySelector('.gradient-stop-pos');
      posInp.addEventListener('change', () => {
        pushUndo();
        st.offset = Math.max(0, Math.min(100, parseInt(posInp.value, 10) || 0)) / 100;
        updateGradBar();
        render();
        scheduleSave();
      });

      // Del stop
      row.querySelector('.gradient-stop-del')?.addEventListener('click', () => {
        pushUndo();
        grad.stops.splice(idx, 1);
        wireGradientControls(n);
        updateGradBar();
        render();
        scheduleSave();
      });

      // Swatch color pickr
      const swatch = row.querySelector(`#grad-swatch-${idx}`);
      initColorPicker(
        swatch,
        st.color || '#ffffff',
        Math.round((st.opacity ?? 1) * 255),
        hex => {
          st.color = hex;
          swatch.style.background = hex;
          updateGradBar();
          render();
        },
        alpha => {
          st.opacity = alpha / 255;
          render();
        }
      );

      stopsCont.appendChild(row);
    });
  }

  // Add stop button
  document.getElementById('btn-add-grad-stop')?.addEventListener('click', () => {
    pushUndo();
    const lastOffset = grad.stops.length ? grad.stops[grad.stops.length - 1].offset : 0.5;
    grad.stops.push({ offset: Math.min(1.0, lastOffset + 0.1), color: '#34d399', opacity: 1 });
    wireGradientControls(n);
    updateGradBar();
    render();
    scheduleSave();
  });

  // Animation settings
  const animEnable = document.getElementById('grad-anim-enable');
  const animSpeed = document.getElementById('grad-anim-speed');
  const animSpeedVal = document.getElementById('grad-anim-speed-val');
  const animDir = document.getElementById('grad-anim-dir');

  animEnable?.addEventListener('change', () => {
    n.gradientAnimation.enabled = animEnable.checked;
    scheduleSave();
    render();
  });
  animSpeed?.addEventListener('input', () => {
    n.gradientAnimation.speed = parseFloat(animSpeed.value) || 1.0;
    if (animSpeedVal) animSpeedVal.textContent = `${n.gradientAnimation.speed.toFixed(1)}×`;
  });
  animSpeed?.addEventListener('change', () => {
    scheduleSave();
  });
  animDir?.addEventListener('change', () => {
    n.gradientAnimation.direction = animDir.value;
    scheduleSave();
  });
}

function applyInspectorInput(el, n, isChange) {
  const key = el.dataset.key;
  if (!key) return;
  if (isChange) pushUndo();

  let val = el.type === 'checkbox' ? el.checked : el.value;
  if (el.type === 'number' || el.type === 'range') {
    val = parseFloat(val);
    if (isNaN(val)) val = 0;
  }
  n[key] = val;

  if (key === 'outline' || key === 'shapeType' || key === 'lineType') {
    buildPropsPanel();
  } else if (key === 'rotation') {
    refreshPropsInputs(n);
  }

  render();
  updateLayersList();
  scheduleSave();
}

function refreshPropsInputs(n) {
  propsBody.querySelectorAll('[data-key]').forEach(el => {
    const key = el.dataset.key;
    if (!key || el === document.activeElement) return;
    if (el.type === 'checkbox') el.checked = Boolean(n[key]);
    else if (n[key] !== undefined) el.value = n[key];
  });
}

function wireButtonAction(n) {
  const typeSel = document.getElementById('action-type-sel');
  const valRow  = document.getElementById('action-val-row');
  const valInp  = document.getElementById('action-val-input');
  const descInp = document.getElementById('action-desc-input');

  const sync = () => {
    const type = typeSel?.value || 'NONE';
    if (valRow) valRow.style.display = type !== 'NONE' ? 'flex' : 'none';
    if (!n._button) {
      n._button = {
        id: uniqueId('btn'),
        nodeId: n.id,
        description: '',
        action: { type: 'NONE', value: '' },
      };
    }
    n._button.action = { type, value: valInp?.value || '' };
    n._button.description = descInp?.value || '';
    scheduleSave();
  };

  typeSel?.addEventListener('change', sync);
  valInp?.addEventListener('input', sync);
  descInp?.addEventListener('input', sync);
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
    scheduleSave();
  });

  _activePickrs.push(pickr);
}

function escHtml(s) {
  return String(s || '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

/* ── 14. Decoupled Animation & Preview Engine ─────────────── */
function updatePreview() {
  if (!prevCanvas) return;
  const parent = prevCanvas.parentElement;
  if (!parent) return;
  const maxW = parent.clientWidth - 10;
  const ratio = state.canvasH / state.canvasW;
  const pw = Math.max(80, Math.min(maxW, 240));
  const ph = Math.round(pw * ratio);
  if (prevCanvas.width !== pw || prevCanvas.height !== ph) {
    prevCanvas.width = pw;
    prevCanvas.height = ph;
  }
  const pz = pw / state.canvasW;

  prevCtx.clearRect(0, 0, pw, ph);
  const _pbColor = state.boardColor || '#0a0e18';
  const _pbAlpha = (state.boardAlpha ?? 100) / 100;
  if (sceneState.checkerboard) {
    prevCtx.fillStyle = getCheckerboardPattern(prevCtx);
    prevCtx.fillRect(0, 0, pw, ph);
    if (_pbAlpha > 0) {
      prevCtx.save();
      prevCtx.globalAlpha = _pbAlpha;
      prevCtx.fillStyle = _pbColor;
      prevCtx.fillRect(0, 0, pw, ph);
      prevCtx.restore();
    }
  } else {
    prevCtx.save();
    prevCtx.globalAlpha = _pbAlpha;
    prevCtx.fillStyle = _pbColor;
    prevCtx.fillRect(0, 0, pw, ph);
    prevCtx.restore();
  }
  _renderSceneGrid(prevCtx, pw, ph, pz);

  const t = previewAnim.type;
  const prog = previewAnim.progress;

  state.nodes.forEach(n => {
    if (n.visible === false) return;
    prevCtx.save();

    if (t !== 'none' && prog < 1.0) {
      const ease = 1 - Math.pow(1 - prog, 3);
      if (t === 'fadeIn') {
        prevCtx.globalAlpha = Math.max(0, Math.min(1, ease));
      } else if (t === 'scaleIn') {
        const s = 0.5 + 0.5 * ease;
        prevCtx.globalAlpha = Math.max(0, Math.min(1, ease));
        const cx = pw / 2, cy = ph / 2;
        prevCtx.translate(cx, cy);
        prevCtx.scale(s, s);
        prevCtx.translate(-cx, -cy);
      } else if (t === 'slideDown') {
        prevCtx.globalAlpha = Math.max(0, Math.min(1, ease));
        prevCtx.translate(0, (1 - ease) * -20);
      } else if (t === 'slideUp') {
        prevCtx.globalAlpha = Math.max(0, Math.min(1, ease));
        prevCtx.translate(0, (1 - ease) * 20);
      }
    }

    NodeDefs[n.type]?.render(prevCtx, n, pz);
    prevCtx.restore();
  });
}

function _syncPreviewPlayBtn() {
  const btn = document.getElementById('preview-btn-play');
  if (btn) btn.innerHTML = getSvgIcon(previewAnim.isPlaying ? 'pause' : 'play');
}

function initPreviewControls() {
  const typeSelect = document.getElementById('preview-anim-type');
  const playBtn    = document.getElementById('preview-btn-play');
  const restartBtn = document.getElementById('preview-btn-restart');
  const speedBtn   = document.getElementById('preview-btn-speed');

  typeSelect?.addEventListener('change', e => {
    previewAnim.type = e.target.value;
    if (previewAnim.type !== 'none') {
      previewAnim.progress = 0.0;
      previewAnim.isPlaying = true;
      previewAnim.lastTimestamp = null;
    } else {
      previewAnim.progress = 1.0;
      previewAnim.isPlaying = false;
      updatePreview();
    }
    _syncPreviewPlayBtn();
  });

  playBtn?.addEventListener('click', () => {
    if (previewAnim.type === 'none') {
      previewAnim.type = 'fadeIn';
      if (typeSelect) typeSelect.value = 'fadeIn';
    }
    previewAnim.isPlaying = !previewAnim.isPlaying;
    if (previewAnim.isPlaying) {
      if (previewAnim.progress >= 1.0) previewAnim.progress = 0.0;
      previewAnim.lastTimestamp = null;
    }
    _syncPreviewPlayBtn();
  });

  restartBtn?.addEventListener('click', () => {
    if (previewAnim.type === 'none') {
      previewAnim.type = 'fadeIn';
      if (typeSelect) typeSelect.value = 'fadeIn';
    }
    previewAnim.progress = 0.0;
    previewAnim.isPlaying = true;
    previewAnim.lastTimestamp = null;
    _syncPreviewPlayBtn();
  });

  speedBtn?.addEventListener('click', () => {
    previewAnim.speed = previewAnim.speed === 1.0 ? 2.0 : 1.0;
    speedBtn.textContent = `${previewAnim.speed}×`;
  });
}

/* ── 15. Context Menu & Keyboard Shortcuts ────────────────── */
const contextMenuEl = document.getElementById('canvas-context-menu');

canvasViewport.addEventListener('contextmenu', e => {
  e.preventDefault();
  const rect = canvasViewport.getBoundingClientRect();
  const world = screenToWorld(e.clientX - rect.left, e.clientY - rect.top);
  const hitIdx = hitTestWorld(world.x, world.y);

  if (hitIdx !== -1) {
    if (!editor.selectedIdxs.includes(hitIdx)) {
      editor.selectedIdxs = [hitIdx];
      editor.selectedIdx = hitIdx;
      buildPropsPanel();
      updateLayersList();
      updateStatusSelection();
      updateQuickColorSwatches();
      render();
    }
    showContextMenu(e.clientX, e.clientY, true);
  } else {
    showContextMenu(e.clientX, e.clientY, false);
  }
});

function showContextMenu(x, y, hasSelection) {
  if (!contextMenuEl) return;
  let html = '';
  if (hasSelection) {
    html += `
      <div class="context-menu-item" data-act="duplicate"><span>Duplicate</span><span class="context-menu-shortcut">Ctrl+D</span></div>
      <div class="context-menu-item" data-act="copy"><span>Copy</span><span class="context-menu-shortcut">Ctrl+C</span></div>
      <div class="context-menu-item" data-act="cut"><span>Cut</span><span class="context-menu-shortcut">Ctrl+X</span></div>
      <div class="context-menu-sep"></div>
      <div class="context-menu-item" data-act="flipX"><span>Flip Horizontal</span></div>
      <div class="context-menu-item" data-act="flipY"><span>Flip Vertical</span></div>
      <div class="context-menu-sep"></div>
      <div class="context-menu-item" data-act="forward"><span>Bring Forward</span><span class="context-menu-shortcut">Ctrl+]</span></div>
      <div class="context-menu-item" data-act="backward"><span>Send Backward</span><span class="context-menu-shortcut">Ctrl+[</span></div>
      <div class="context-menu-sep"></div>
      <div class="context-menu-item danger" data-act="delete"><span>Delete</span><span class="context-menu-shortcut">Del</span></div>
    `;
  } else {
    html += `
      <div class="context-menu-item" data-act="paste"><span>Paste</span><span class="context-menu-shortcut">Ctrl+V</span></div>
      <div class="context-menu-item" data-act="selectAll"><span>Select All</span><span class="context-menu-shortcut">Ctrl+A</span></div>
      <div class="context-menu-sep"></div>
      <div class="context-menu-item" data-act="fit"><span>Fit Canvas</span><span class="context-menu-shortcut">0</span></div>
      <div class="context-menu-item" data-act="resetZoom"><span>Reset 100%</span><span class="context-menu-shortcut">1</span></div>
    `;
  }

  contextMenuEl.innerHTML = html;
  contextMenuEl.hidden = false;
  contextMenuEl.style.left = Math.min(window.innerWidth - 180, x) + 'px';
  contextMenuEl.style.top  = Math.min(window.innerHeight - 240, y) + 'px';

  contextMenuEl.querySelectorAll('.context-menu-item').forEach(item => {
    item.addEventListener('click', () => {
      const act = item.dataset.act;
      hideContextMenu();
      if (act === 'duplicate') duplicateSelected();
      if (act === 'copy') copySelected();
      if (act === 'cut') cutSelected();
      if (act === 'paste') pasteClipboard();
      if (act === 'selectAll') selectAll();
      if (act === 'flipX') toggleFlip('flipX');
      if (act === 'flipY') toggleFlip('flipY');
      if (act === 'forward') { if (editor.selectedIdx >= 0) moveNode(editor.selectedIdx, 1); }
      if (act === 'backward') { if (editor.selectedIdx >= 0) moveNode(editor.selectedIdx, -1); }
      if (act === 'delete') deleteSelected();
      if (act === 'fit') fitCanvas();
      if (act === 'resetZoom') resetZoom();
    });
  });
}

function hideContextMenu() {
  if (contextMenuEl) contextMenuEl.hidden = true;
}

document.addEventListener('click', e => {
  if (contextMenuEl && !contextMenuEl.hidden && !contextMenuEl.contains(e.target)) {
    hideContextMenu();
  }
});

// Global Keyboard Shortcuts
window.addEventListener('keydown', e => {
  if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA' || e.target.tagName === 'SELECT') return;

  if (e.code === 'Space' && !editor.isSpacePressed) {
    editor.isSpacePressed = true;
    canvasViewport.style.cursor = 'grab';
    e.preventDefault();
    return;
  }

  if ((e.ctrlKey || e.metaKey) && !e.shiftKey && e.key.toLowerCase() === 'z') { e.preventDefault(); undo(); return; }
  if (((e.ctrlKey || e.metaKey) && e.shiftKey && e.key.toLowerCase() === 'z') || ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'y')) { e.preventDefault(); redo(); return; }
  if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'd') { e.preventDefault(); duplicateSelected(); return; }
  if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'c') { e.preventDefault(); copySelected(); return; }
  if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'x') { e.preventDefault(); cutSelected(); return; }
  if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'v') { e.preventDefault(); pasteClipboard(); return; }
  if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'a') { e.preventDefault(); selectAll(); return; }
  if (e.key === 'Delete' || e.key === 'Backspace') { e.preventDefault(); deleteSelected(); return; }

  if (e.key === 'Escape') {
    hideContextMenu();
    if (editor.isPanning) {
      editor.isPanning = false;
      canvasViewport.classList.remove('panning');
    }
    if (editor.selectedIdxs.length) {
      editor.selectedIdxs = [];
      editor.selectedIdx = -1;
      buildPropsPanel();
      updateLayersList();
      updateStatusSelection();
      updateQuickColorSwatches();
      render();
    }
    return;
  }

  const step = e.shiftKey ? 8 : 1;
  if (e.key === 'ArrowUp')    { nudge( 0, -step); e.preventDefault(); }
  if (e.key === 'ArrowDown')  { nudge( 0,  step); e.preventDefault(); }
  if (e.key === 'ArrowLeft')  { nudge(-step, 0);  e.preventDefault(); }
  if (e.key === 'ArrowRight') { nudge( step, 0);  e.preventDefault(); }

  if (e.key === '0') { e.preventDefault(); fitCanvas(); return; }
  if (e.key === '1') { e.preventDefault(); resetZoom(); return; }
  if (e.key === '+' || e.key === '=') {
    const vpRect = canvasViewport.getBoundingClientRect();
    zoomAt(vpRect.width / 2, vpRect.height / 2, camera.zoom * 1.25);
    e.preventDefault();
    return;
  }
  if (e.key === '-' || e.key === '_') {
    const vpRect = canvasViewport.getBoundingClientRect();
    zoomAt(vpRect.width / 2, vpRect.height / 2, camera.zoom / 1.25);
    e.preventDefault();
    return;
  }
  if (e.key.toLowerCase() === 'g') {
    editor.grid = !editor.grid;
    document.getElementById('btn-grid-toggle')?.classList.toggle('active', editor.grid);
    updateStatusGrid();
    render();
    scheduleSave();
    return;
  }
  if (e.key.toLowerCase() === 's') {
    editor.snap = !editor.snap;
    document.getElementById('btn-snap-toggle')?.classList.toggle('active', editor.snap);
    updateStatusSnap();
    scheduleSave();
    return;
  }
  if ((e.ctrlKey || e.metaKey) && e.key === '[') {
    if (editor.selectedIdx >= 0) moveNode(editor.selectedIdx, -1);
    e.preventDefault();
  }
  if ((e.ctrlKey || e.metaKey) && e.key === ']') {
    if (editor.selectedIdx >= 0) moveNode(editor.selectedIdx, 1);
    e.preventDefault();
  }
});

window.addEventListener('keyup', e => {
  if (e.code === 'Space') {
    editor.isSpacePressed = false;
    if (!editor.isPanning) canvasViewport.style.cursor = 'default';
  }
});

function nudge(dx, dy) {
  if (!editor.selectedIdxs.length) return;
  pushUndo();
  editor.selectedIdxs.forEach(idx => {
    const n = state.nodes[idx];
    if (!n || n.locked) return;
    if (n.type === 'text') { n.boxX += dx; n.boxY += dy; }
    else if (n.type === 'line') {
      n.x1 += dx; n.y1 += dy; n.x2 += dx; n.y2 += dy;
      if (n.curveCtrlX != null) n.curveCtrlX += dx;
      if (n.curveCtrlY != null) n.curveCtrlY += dy;
      if (n.elbowMidX != null) n.elbowMidX += dx;
      if (n.elbowMidY != null) n.elbowMidY += dy;
    }
    else { n.x = (n.x ?? 0) + dx; n.y = (n.y ?? 0) + dy; }
  });
  if (editor.selectedIdx >= 0) refreshPropsInputs(state.nodes[editor.selectedIdx]);
  render();
  scheduleSave();
}

/* ── 16. Shape Popover Picker ─────────────────────────────── */
const activeShapeConfig = {
  shapeType: 'rect',
};

(function initShapePopover() {
  const pickerBtn  = document.getElementById('btn-shape-picker');
  const dropdown   = document.getElementById('shape-picker-dropdown');
  const iconSpan   = document.getElementById('shape-picker-icon');
  const nameSpan   = document.getElementById('shape-picker-name');

  if (!pickerBtn || !dropdown) return;

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

  dropdown.querySelectorAll('.shape-item').forEach(item => {
    item.addEventListener('click', () => {
      const shapeId = item.dataset.shapeId;
      if (shapeId.startsWith('line:')) {
        const lt = shapeId.replace('line:', '');
        dropdown.hidden = true;
        pickerBtn.classList.remove('open');
        pushUndo();
        const node = normalizeNode(NodeDefs.line.defaults());
        node.lineType = lt;
        const cx = Math.round(state.canvasW / 2);
        const cy = Math.round(state.canvasH / 2);
        node.x1 = cx - 50; node.y1 = cy - 20;
        node.x2 = cx + 50; node.y2 = cy + 20;
        if (lt === 'curve' || lt === 'curved_arrow') {
          node.curveCtrlX = cx;
          node.curveCtrlY = cy - 40;
        } else if (lt === 'elbow' || lt === 'elbow_arrow') {
          node.elbowMidX = cx;
          node.elbowMidY = cy;
        }
        state.nodes.push(node);
        editor.selectedIdx = state.nodes.length - 1;
        editor.selectedIdxs = [editor.selectedIdx];
        buildPropsPanel();
        updateLayersList();
        updateStatusSelection();
        updateQuickColorSwatches();
        render();
        scheduleSave();
        return;
      }
      activeShapeConfig.shapeType = shapeId;

      dropdown.querySelectorAll('.shape-item').forEach(el => el.classList.toggle('active', el.dataset.shapeId === shapeId));
      const def = getShapeDef(shapeId);
      if (iconSpan) iconSpan.innerHTML = `<svg viewBox="0 0 20 20" fill="currentColor">${def.icon}</svg>`;
      if (nameSpan) nameSpan.textContent = def.name;
      dropdown.hidden = true;
      pickerBtn.classList.remove('open');

      // If shape node is currently selected, update its shapeType
      if (editor.selectedIdx >= 0) {
        const sel = state.nodes[editor.selectedIdx];
        if (sel && (sel.type === 'shape' || sel.type === 'background')) {
          pushUndo();
          sel.shapeType = shapeId;
          buildPropsPanel();
          updateLayersList();
          render();
          scheduleSave();
        }
      } else {
        // Add new shape if none selected
        pushUndo();
        const node = normalizeNode(NodeDefs.shape.defaults());
        node.shapeType = shapeId;
        const cx = Math.round(state.canvasW / 2);
        const cy = Math.round(state.canvasH / 2);
        node.x = cx - node.width / 2;
        node.y = cy - node.height / 2;
        state.nodes.push(node);
        editor.selectedIdx = state.nodes.length - 1;
        editor.selectedIdxs = [editor.selectedIdx];
        buildPropsPanel();
        updateLayersList();
        updateStatusSelection();
        updateQuickColorSwatches();
        render();
        scheduleSave();
      }
    });
  });
})();

/* ── 17. Scene Environment Panel (Image, Presets, Filters) ─── */
const SCENE_STORAGE_KEY = 'hhdui_scene_v1';
const sceneState = {
  bgMode: 'color',
  env: 'dark',
  customBg: '#080b11',
  backgroundColor: '#080b11',
  imageSrc: '',
  imageName: 'Custom Image',
  imageFit: 'cover',
  imagePos: 'center',
  brightness: 100,
  contrast: 100,
  saturation: 100,
  blur: 0,
  opacity: 100,
  gridStyle: 'dots',
  gridSize: 4,
  checkerboard: false,
};

function saveSceneState() {
  try {
    const clone = { ...sceneState };
    if (clone.imageSrc && clone.imageSrc.length > 2500000) clone.imageSrc = '';
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

const PRESET_BG_IMAGES = {
  overworld_day: 'assets/image/daytime.png',
  overworld_night: 'assets/image/nighttime.png',
  nether: 'assets/image/nether.png',
  end: 'assets/image/theend.png',
};

let _sceneImg = null;
let _lastSceneImgSrc = '';

function getSceneImage() {
  const isImageMode = (sceneState.bgMode === 'image') || (sceneState.env === 'image');
  let src = '';
  if (isImageMode && sceneState.imageSrc) src = sceneState.imageSrc;
  else if (!isImageMode && PRESET_BG_IMAGES[sceneState.env]) src = PRESET_BG_IMAGES[sceneState.env];
  if (!src) return null;
  if (_sceneImg && _lastSceneImgSrc === src) return _sceneImg;
  _lastSceneImgSrc = src;
  const img = new Image();
  img.crossOrigin = 'anonymous';
  img.src = src;
  img.onload = () => {
    _sceneImg = img;
    applySceneFilters();
  };
  img.onerror = () => {
    console.warn('[HaoHan Builder] Scene image failed to load:', src);
  };
  _sceneImg = img;
  return img;
}

function applySceneFilters() {
  if (!sceneBgLayer) return;
  const isImageMode = (sceneState.bgMode === 'image') || (sceneState.env === 'image');
  let bgImg = 'none';
  let bgColor = sceneState.backgroundColor || sceneState.customBg || '#080b11';

  if (isImageMode && sceneState.imageSrc) {
    bgImg = `url("${sceneState.imageSrc}")`;
  } else if (!isImageMode && PRESET_BG_IMAGES[sceneState.env]) {
    bgImg = `url("${PRESET_BG_IMAGES[sceneState.env]}")`;
  }

  sceneBgLayer.style.backgroundColor = bgColor;
  sceneBgLayer.style.backgroundImage = bgImg;
  sceneBgLayer.style.backgroundSize = sceneState.imageFit || 'cover';
  sceneBgLayer.style.backgroundPosition = sceneState.imagePos || 'center';
  sceneBgLayer.style.backgroundRepeat = 'no-repeat';
  sceneBgLayer.style.filter = `brightness(${sceneState.brightness}%) contrast(${sceneState.contrast}%) saturate(${sceneState.saturation}%) blur(${sceneState.blur}px)`;
  sceneBgLayer.style.opacity = (Math.max(0, Math.min(100, sceneState.opacity)) / 100).toString();

  const swatchBox = document.getElementById('sp-color-swatch-box');
  const hexText = document.getElementById('sp-color-hex-text');
  if (swatchBox) swatchBox.style.background = bgColor;
  if (hexText) hexText.textContent = bgColor;
}

function _renderSceneGrid(ctx2d, w, h, z) {
  if (!editor.grid) return;
  const step = sceneState.gridSize * z;
  if (step < 3) return;
  const color = 'rgba(255, 255, 255, 0.08)';
  ctx2d.save();
  ctx2d.strokeStyle = color;
  ctx2d.fillStyle = color;
  ctx2d.lineWidth = 1;

  if (sceneState.gridStyle === 'dots') {
    ctx2d.beginPath();
    for (let x = 0; x <= w; x += step) {
      const rx = Math.round(x);
      for (let y = 0; y <= h; y += step) {
        ctx2d.rect(rx - 0.5, Math.round(y) - 0.5, 1, 1);
      }
    }
    ctx2d.fill();
  } else if (sceneState.gridStyle === 'lines') {
    ctx2d.beginPath();
    for (let x = 0; x <= w; x += step) {
      const rx = Math.round(x) - 0.5;
      ctx2d.moveTo(rx, 0); ctx2d.lineTo(rx, h);
    }
    for (let y = 0; y <= h; y += step) {
      const ry = Math.round(y) - 0.5;
      ctx2d.moveTo(0, ry); ctx2d.lineTo(w, ry);
    }
    ctx2d.stroke();
  } else if (sceneState.gridStyle === 'cross') {
    const hs = 2;
    ctx2d.beginPath();
    for (let x = step; x < w; x += step) {
      const rx = Math.round(x);
      for (let y = step; y < h; y += step) {
        const ry = Math.round(y);
        ctx2d.moveTo(rx - hs, ry); ctx2d.lineTo(rx + hs, ry);
        ctx2d.moveTo(rx, ry - hs); ctx2d.lineTo(rx, ry + hs);
      }
    }
    ctx2d.stroke();
  }
  ctx2d.restore();
}

(function initScenePanel() {
  restoreSceneState();

  const btn = document.getElementById('scene-panel-btn');
  const panel = document.getElementById('scene-panel');

  btn?.addEventListener('click', e => {
    e.stopPropagation();
    const isOpen = !panel.hidden;
    panel.hidden = isOpen;
    btn.classList.toggle('open', !isOpen);
  });

  document.addEventListener('click', e => {
    if (panel && !panel.hidden && !panel.contains(e.target) && e.target !== btn) {
      panel.hidden = true;
      btn.classList.remove('open');
    }
  });

  // Background Mode Toggle (Color vs Image)
  const modeColorBtn = document.getElementById('sp-mode-color-btn');
  const modeImageBtn = document.getElementById('sp-mode-image-btn');
  const colorSection = document.getElementById('sp-color-section');
  const imgRow = document.getElementById('sp-image-row');

  const setBgMode = (mode) => {
    sceneState.bgMode = mode;
    modeColorBtn?.classList.toggle('active', mode === 'color');
    modeImageBtn?.classList.toggle('active', mode === 'image');
    if (colorSection) colorSection.style.display = mode === 'color' ? 'block' : 'none';
    if (imgRow) imgRow.style.display = mode === 'image' ? 'flex' : 'none';
    applySceneFilters();
    saveSceneState();
  };

  modeColorBtn?.addEventListener('click', () => setBgMode('color'));
  modeImageBtn?.addEventListener('click', () => setBgMode('image'));

  // Scene Color Button & Picker
  const colorBtn = document.getElementById('sp-color-btn');
  const colorInput = document.getElementById('sp-bg-color-input');
  colorBtn?.addEventListener('click', () => colorInput?.click());
  colorInput?.addEventListener('input', () => {
    sceneState.backgroundColor = colorInput.value;
    sceneState.customBg = colorInput.value;
    sceneState.env = 'custom';
    document.querySelectorAll('.sp-env-grid .sp-env-btn').forEach(el => el.classList.remove('active'));
    applySceneFilters();
    saveSceneState();
  });

  // Preset Buttons
  document.querySelectorAll('.sp-env-grid .sp-env-btn').forEach(b => {
    b.addEventListener('click', () => {
      document.querySelectorAll('.sp-env-grid .sp-env-btn').forEach(el => el.classList.remove('active'));
      b.classList.add('active');
      const env = b.dataset.env;
      sceneState.env = env;
      if (env === 'dark') {
        sceneState.customBg = '#080b11';
        sceneState.backgroundColor = '#080b11';
      } else if (env === 'overworld_day') {
        sceneState.customBg = '#2d5a27';
        sceneState.backgroundColor = '#2d5a27';
      } else if (env === 'overworld_night') {
        sceneState.customBg = '#090e1c';
        sceneState.backgroundColor = '#090e1c';
      } else if (env === 'nether') {
        sceneState.customBg = '#380a06';
        sceneState.backgroundColor = '#380a06';
      } else if (env === 'end') {
        sceneState.customBg = '#161329';
        sceneState.backgroundColor = '#161329';
      }
      applySceneFilters();
      saveSceneState();
    });
  });

  // Image upload
  const dropzone = document.getElementById('sp-dropzone');
  const fileInput = document.getElementById('sp-image-upload');
  const thumb = document.getElementById('sp-current-thumb');
  const imgInfo = document.getElementById('sp-image-info');
  const imgName = document.getElementById('sp-image-name');

  const applyImg = (dataUrl, name) => {
    sceneState.imageSrc = dataUrl;
    sceneState.imageName = name;
    sceneState.bgMode = 'image';
    setBgMode('image');
    if (thumb) thumb.style.backgroundImage = `url("${dataUrl}")`;
    if (imgName) imgName.textContent = name;
    if (imgInfo) imgInfo.style.display = 'flex';
    applySceneFilters();
    saveSceneState();
  };

  dropzone?.addEventListener('click', e => {
    if (e.target.tagName !== 'LABEL') fileInput?.click();
  });
  dropzone?.addEventListener('dragover', e => { e.preventDefault(); dropzone.classList.add('dragover'); });
  dropzone?.addEventListener('dragleave', () => dropzone.classList.remove('dragover'));
  dropzone?.addEventListener('drop', e => {
    e.preventDefault();
    dropzone.classList.remove('dragover');
    const f = e.dataTransfer.files[0];
    if (f && f.type.startsWith('image/')) {
      const reader = new FileReader();
      reader.onload = ev => applyImg(ev.target.result, f.name);
      reader.readAsDataURL(f);
    }
  });
  fileInput?.addEventListener('change', e => {
    const f = e.target.files[0];
    if (f) {
      const reader = new FileReader();
      reader.onload = ev => applyImg(ev.target.result, f.name);
      reader.readAsDataURL(f);
      e.target.value = '';
    }
  });

  const urlInp = document.getElementById('sp-image-url');
  document.getElementById('sp-image-url-apply')?.addEventListener('click', () => {
    const url = urlInp?.value.trim();
    if (url) applyImg(url, 'URL Image');
  });

  document.getElementById('sp-image-clear-btn')?.addEventListener('click', () => {
    sceneState.imageSrc = '';
    if (imgInfo) imgInfo.style.display = 'none';
    if (urlInp) urlInp.value = '';
    applySceneFilters();
    saveSceneState();
  });

  const fitBtn = document.getElementById('sp-image-fit-btn');
  fitBtn?.addEventListener('click', () => {
    const fits = ['cover', 'contain', 'stretch'];
    const curIdx = fits.indexOf(sceneState.imageFit || 'cover');
    sceneState.imageFit = fits[(curIdx + 1) % fits.length];
    fitBtn.textContent = sceneState.imageFit.toUpperCase();
    applySceneFilters();
    saveSceneState();
  });

  const posBtn = document.getElementById('sp-image-pos-btn');
  posBtn?.addEventListener('click', () => {
    const positions = ['center', 'top', 'bottom', 'left', 'right'];
    const curIdx = positions.indexOf(sceneState.imagePos || 'center');
    sceneState.imagePos = positions[(curIdx + 1) % positions.length];
    posBtn.textContent = sceneState.imagePos.charAt(0).toUpperCase() + sceneState.imagePos.slice(1);
    applySceneFilters();
    saveSceneState();
  });

  // Synced Realtime Sliders + Number Inputs
  const setupSyncSlider = (sliderId, numId, key, min, max) => {
    const slider = document.getElementById(sliderId);
    const numInput = document.getElementById(numId);
    if (!slider || !numInput) return;

    slider.value = sceneState[key] ?? 100;
    numInput.value = sceneState[key] ?? 100;

    slider.addEventListener('input', () => {
      const val = parseInt(slider.value, 10);
      numInput.value = val;
      sceneState[key] = val;
      applySceneFilters();
    });

    slider.addEventListener('change', () => {
      saveSceneState();
    });

    numInput.addEventListener('input', () => {
      const val = parseFloat(numInput.value);
      if (!isNaN(val)) {
        slider.value = Math.max(min, Math.min(max, val));
        sceneState[key] = parseInt(slider.value, 10);
        applySceneFilters();
      }
    });

    numInput.addEventListener('blur', () => {
      let val = parseFloat(numInput.value);
      if (isNaN(val)) val = sceneState[key] ?? 100;
      val = Math.max(min, Math.min(max, val));
      numInput.value = val;
      slider.value = val;
      sceneState[key] = val;
      applySceneFilters();
      saveSceneState();
    });

    numInput.addEventListener('keydown', (e) => {
      if (e.key === 'Enter') numInput.blur();
    });
  };

  setupSyncSlider('sp-scene-brightness', 'sp-scene-brightness-num', 'brightness', 0, 200);
  setupSyncSlider('sp-scene-contrast', 'sp-scene-contrast-num', 'contrast', 0, 200);
  setupSyncSlider('sp-scene-saturation', 'sp-scene-saturation-num', 'saturation', 0, 200);
  setupSyncSlider('sp-scene-blur', 'sp-scene-blur-num', 'blur', 0, 50);
  setupSyncSlider('sp-scene-opacity', 'sp-scene-opacity-num', 'opacity', 0, 100);

  document.querySelectorAll('#sp-grid-style .sp-seg-btn').forEach(b => {
    b.addEventListener('click', () => {
      document.querySelectorAll('#sp-grid-style .sp-seg-btn').forEach(el => el.classList.remove('active'));
      b.classList.add('active');
      sceneState.gridStyle = b.dataset.style;
      saveSceneState();
      render();
    });
  });

  document.querySelectorAll('#sp-grid-size .sp-seg-btn').forEach(b => {
    b.addEventListener('click', () => {
      document.querySelectorAll('#sp-grid-size .sp-seg-btn').forEach(el => el.classList.remove('active'));
      b.classList.add('active');
      sceneState.gridSize = parseInt(b.dataset.size, 10);
      editor.snapGridSize = sceneState.gridSize;
      saveSceneState();
      render();
    });
  });

  const checkerbox = document.getElementById('sp-checkerboard');
  if (checkerbox) {
    checkerbox.checked = Boolean(sceneState.checkerboard);
    checkerbox.addEventListener('change', e => {
      sceneState.checkerboard = e.target.checked;
      render();
      updatePreview();
      saveSceneState();
    });
  }

  setBgMode(sceneState.bgMode || (sceneState.env === 'image' ? 'image' : 'color'));
  applySceneFilters();

  // ── Document Board Color & Opacity Controls ───────────────
  const boardColorBtn   = document.getElementById('sp-board-color-btn');
  const boardColorInput = document.getElementById('sp-board-color-input');
  const boardSwatchBox  = document.getElementById('sp-board-swatch-box');
  const boardHexText    = document.getElementById('sp-board-hex-text');
  const boardAlphaRange = document.getElementById('sp-board-alpha');
  const boardAlphaNum   = document.getElementById('sp-board-alpha-num');

  function _syncBoardSwatchUI() {
    const hex = state.boardColor || '#0a0e18';
    if (boardSwatchBox) boardSwatchBox.style.background = hex;
    if (boardHexText)   boardHexText.textContent = hex;
    if (boardColorInput) boardColorInput.value = hex;
    const pct = Math.round(state.boardAlpha ?? 100);
    if (boardAlphaRange) boardAlphaRange.value = pct;
    if (boardAlphaNum)   boardAlphaNum.value  = pct;
  }

  // Sync initial values
  _syncBoardSwatchUI();

  boardColorBtn?.addEventListener('click', () => boardColorInput?.click());

  boardColorInput?.addEventListener('input', () => {
    state.boardColor = boardColorInput.value;
    if (boardSwatchBox) boardSwatchBox.style.background = boardColorInput.value;
    if (boardHexText)   boardHexText.textContent = boardColorInput.value;
    render();
    updatePreview();
    scheduleSave();
  });

  boardAlphaRange?.addEventListener('input', () => {
    const val = parseInt(boardAlphaRange.value, 10);
    state.boardAlpha = val;
    if (boardAlphaNum) boardAlphaNum.value = val;
    render();
    updatePreview();
  });
  boardAlphaRange?.addEventListener('change', () => scheduleSave());

  boardAlphaNum?.addEventListener('input', () => {
    let val = parseInt(boardAlphaNum.value, 10);
    if (isNaN(val)) return;
    val = Math.max(0, Math.min(100, val));
    state.boardAlpha = val;
    if (boardAlphaRange) boardAlphaRange.value = val;
    render();
    updatePreview();
  });
  boardAlphaNum?.addEventListener('blur', () => {
    let val = parseInt(boardAlphaNum.value, 10);
    if (isNaN(val)) val = state.boardAlpha ?? 100;
    val = Math.max(0, Math.min(100, val));
    state.boardAlpha = val;
    boardAlphaNum.value = val;
    if (boardAlphaRange) boardAlphaRange.value = val;
    render();
    updatePreview();
    scheduleSave();
  });
  boardAlphaNum?.addEventListener('keydown', e => { if (e.key === 'Enter') boardAlphaNum.blur(); });
})();

/* ── 18. Boot Initialization ──────────────────────────────── */
const _hadSaved = _restoreState();

const cwInput = document.getElementById('canvas-w');
const chInput = document.getElementById('canvas-h');
const docNameInput = document.getElementById('doc-name-input');
if (cwInput) cwInput.value = state.canvasW;
if (chInput) chInput.value = state.canvasH;
if (docNameInput) docNameInput.value = state.name;

initRibbonCommands();
initPreviewControls();
resizeCanvas();

if (!_hadSaved || (camera.x === 0 && camera.y === 0)) {
  fitCanvas();
} else {
  updateZoomUI();
}

updateStatusDims();
updateStatusSnap();
updateStatusGrid();
updateStatusSelection();
updateQuickColorSwatches();
buildPropsPanel();
updateLayersList();
_updateUndoRedoButtons();
startGlobalAnimLoop();

if (_hadSaved && state.nodes.length) {
  _showSaveIndicator('saved');
}
