/**
 * nodes.js — HaoHan Display UI Node Definitions & Geometry Rendering Engine
 * Hardened:
 * - Line system with Butt/Square caps (no rounded caps), Arrowheads, Elbow & Curved connectors, Freeform
 * - SkewX, SkewY, ScaleX, ScaleY, Rotation transforms for all nodes
 * - Text Solid + Linear Gradient + Continuous Non-Restarting Gradient Animation
 * - Grouped Shape Categories with individual custom SVG icons
 * - Safe normalization & backwards compatibility
 */

let _uid = 1;
function uniqueId(prefix = 'node') {
  return `${prefix}_${_uid++}`;
}

const _texCache = {};
let _mcVersion = '1.21.4';

function setMcVersion(v) {
  if (v && v !== _mcVersion) {
    _mcVersion = v;
    for (const k in _texCache) delete _texCache[k];
    window.__renderFrame?.();
  }
}

function getMcVersion() {
  return _mcVersion;
}

function getMcTexture(type, material, version = _mcVersion) {
  if (!material) return null;
  const name = material.toLowerCase().replace('minecraft:', '').replace(/\s+/g, '_');
  const key = `${version}:${type}:${name}`;
  if (key in _texCache) return _texCache[key];

  const img = new Image();
  img.crossOrigin = 'anonymous';
  img.src = `https://raw.githubusercontent.com/InventivetalentDev/minecraft-assets/${version}/assets/minecraft/textures/${type}/${name}.png`;

  img.onload = () => {
    _texCache[key] = img;
    window.__renderFrame?.();
  };
  img.onerror = () => {
    if (type === 'item') {
      const blockKey = `${version}:block:${name}`;
      if (blockKey in _texCache) {
        _texCache[key] = _texCache[blockKey];
      } else {
        const bImg = new Image();
        bImg.crossOrigin = 'anonymous';
        bImg.src = `https://raw.githubusercontent.com/InventivetalentDev/minecraft-assets/${version}/assets/minecraft/textures/block/${name}.png`;
        bImg.onload = () => {
          _texCache[blockKey] = bImg;
          _texCache[key] = bImg;
          window.__renderFrame?.();
        };
        bImg.onerror = () => {
          _texCache[key] = null;
          window.__renderFrame?.();
        };
        _texCache[key] = bImg;
        return;
      }
    }
    _texCache[key] = null;
    window.__renderFrame?.();
  };

  _texCache[key] = img;
  return img;
}

function syncUid(nodes) {
  if (!Array.isArray(nodes)) return;
  nodes.forEach(n => {
    if (!n) return;
    if (n.id) {
      const match = String(n.id).match(/_(\d+)$/);
      if (match) {
        const num = parseInt(match[1], 10);
        if (num >= _uid) _uid = num + 1;
      }
    }
    if (n._button && n._button.id) {
      const match = String(n._button.id).match(/_(\d+)/);
      if (match) {
        const num = parseInt(match[1], 10);
        if (num >= _uid) _uid = num + 1;
      }
    }
  });
}

function stripFormatting(text) {
  if (!text) return '';
  return text.replace(/<[^>]+>/g, '').replace(/&[0-9a-fklmnorA-FKLMNOR]/g, '').trim();
}

function hexToRgba(hex, alpha) {
  if (!hex || hex.length < 7) return `rgba(40,50,80,${(alpha ?? 255) / 255})`;
  const r = parseInt(hex.slice(1, 3), 16);
  const g = parseInt(hex.slice(3, 5), 16);
  const b = parseInt(hex.slice(5, 7), 16);
  return `rgba(${r},${g},${b},${(alpha ?? 255) / 255})`;
}

function hexWithAlpha(hex, alpha) {
  const a = Math.round(Math.max(0, Math.min(255, alpha ?? 255))).toString(16).padStart(2, '0');
  return (hex || '#000000') + a;
}

function drawPlaceholder(ctx, px, py, w, h, label) {
  ctx.save();
  ctx.fillStyle = '#1a2438';
  ctx.strokeStyle = '#2e4070';
  ctx.lineWidth = 1;
  ctx.fillRect(px, py, w, h);
  ctx.strokeRect(px, py, w, h);
  if (w > 16 && h > 10) {
    ctx.fillStyle = '#4a6090';
    ctx.font = `${Math.max(7, Math.min(w, h) * 0.22)}px "JetBrains Mono", monospace`;
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText((label || '').slice(0, 6), px + w / 2, py + h / 2);
  }
  ctx.restore();
}

/* ── Unified Transform Matrix Helper ──────────────────────── */
function applyNodeTransform(ctx, cx, cy, n) {
  ctx.translate(cx, cy);
  if (n.rotation) ctx.rotate((n.rotation * Math.PI) / 180);
  if (n.skewX || n.skewY) {
    const sx = Math.tan(((n.skewX || 0) * Math.PI) / 180);
    const sy = Math.tan(((n.skewY || 0) * Math.PI) / 180);
    ctx.transform(1, sy, sx, 1, 0, 0);
  }
  const scX = (n.scaleX ?? 1) * (n.flipX ? -1 : 1);
  const scY = (n.scaleY ?? 1) * (n.flipY ? -1 : 1);
  if (scX !== 1 || scY !== 1) {
    ctx.scale(scX, scY);
  }
  ctx.translate(-cx, -cy);
}

/* ── Shapes Catalog & SVG Icons ───────────────────────────── */
const SHAPE_CATEGORIES = [
  {
    name: 'Lines & Connectors',
    shapes: [
      { id: 'line:straight', name: 'Straight Line', icon: '<line x1="2" y1="18" x2="18" y2="2" stroke="currentColor" stroke-width="2.5" stroke-linecap="butt"/>' },
      { id: 'line:arrow', name: 'Line Arrow', icon: '<line x1="2" y1="18" x2="14" y2="6" stroke="currentColor" stroke-width="2" stroke-linecap="butt"/><polygon points="11,3 18,2 17,9" fill="currentColor"/>' },
      { id: 'line:double_arrow', name: 'Double Arrow', icon: '<line x1="5" y1="15" x2="15" y2="5" stroke="currentColor" stroke-width="2"/><polygon points="3,11 2,18 9,17" fill="currentColor"/><polygon points="11,3 18,2 17,9" fill="currentColor"/>' },
      { id: 'line:elbow', name: 'Elbow Connector', icon: '<polyline points="3,5 10,5 10,16 17,16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="butt"/>' },
      { id: 'line:elbow_arrow', name: 'Elbow Arrow', icon: '<polyline points="3,5 10,5 10,16 14,16" fill="none" stroke="currentColor" stroke-width="2"/><polygon points="13,13 18,16 13,19" fill="currentColor"/>' },
      { id: 'line:curve', name: 'Curved Line', icon: '<path d="M3,16 Q 10,4, 17,16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="butt"/>' },
      { id: 'line:curved_arrow', name: 'Curved Arrow', icon: '<path d="M3,16 Q 10,4, 14,14" fill="none" stroke="currentColor" stroke-width="2"/><polygon points="11,12 18,15 14,18" fill="currentColor"/>' },
    ],
  },
  {
    name: 'Basic',
    shapes: [
      { id: 'rect', name: 'Rectangle', icon: '<rect x="2" y="4" width="16" height="12" rx="0"/>' },
      { id: 'rounded_rect', name: 'Rounded Rectangle', icon: '<rect x="2" y="4" width="16" height="12" rx="3"/>' },
      { id: 'circle', name: 'Circle', icon: '<circle cx="10" cy="10" r="7"/>' },
      { id: 'ellipse', name: 'Ellipse', icon: '<ellipse cx="10" cy="10" rx="8" ry="5"/>' },
      { id: 'diamond', name: 'Diamond', icon: '<polygon points="10,2 18,10 10,18 2,10"/>' },
      { id: 'trapezoid', name: 'Trapezoid', icon: '<polygon points="5,4 15,4 18,16 2,16"/>' },
      { id: 'parallelogram', name: 'Parallelogram', icon: '<polygon points="6,4 18,4 14,16 2,16"/>' },
    ],
  },
  {
    name: 'Triangles',
    shapes: [
      { id: 'triangle', name: 'Triangle', icon: '<polygon points="10,3 18,17 2,17"/>' },
      { id: 'right_triangle', name: 'Right Triangle', icon: '<polygon points="3,3 17,17 3,17"/>' },
    ],
  },
  {
    name: 'Polygons',
    shapes: [
      { id: 'pentagon', name: 'Pentagon (5)', icon: '<polygon points="10,2 18,8 15,17 5,17 2,8"/>' },
      { id: 'hexagon', name: 'Hexagon (6)', icon: '<polygon points="10,2 17,6 17,14 10,18 3,14 3,6"/>' },
      { id: 'heptagon', name: 'Heptagon (7)', icon: '<polygon points="10,2 17,5 18,13 14,18 6,18 2,13 3,5"/>' },
      { id: 'octagon', name: 'Octagon (8)', icon: '<polygon points="6,2 14,2 18,6 18,14 14,18 6,18 2,14 2,6"/>' },
    ],
  },
  {
    name: 'Stars',
    shapes: [
      { id: 'star3', name: '3-Point Star', icon: '<polygon points="10,2 12,8 18,15 10,12 2,15 8,8"/>' },
      { id: 'star4', name: '4-Point Star', icon: '<polygon points="10,2 12,8 18,10 12,12 10,18 8,12 2,10 8,8"/>' },
      { id: 'star5', name: '5-Point Star', icon: '<polygon points="10,2 12.5,7.5 18,8 14,12 15.5,17.5 10,14.5 4.5,17.5 6,12 2,8 7.5,7.5"/>' },
      { id: 'star6', name: '6-Point Star', icon: '<polygon points="10,2 12,6 17,6 13,10 15,15 10,12 5,15 7,10 3,6 8,6"/>' },
    ],
  },
  {
    name: 'Arrows',
    shapes: [
      { id: 'arrow_right', name: 'Right Arrow', icon: '<polygon points="2,7 11,7 11,3 18,10 11,17 11,13 2,13"/>' },
      { id: 'arrow_left', name: 'Left Arrow', icon: '<polygon points="18,7 9,7 9,3 2,10 9,17 9,13 18,13"/>' },
      { id: 'arrow_up', name: 'Up Arrow', icon: '<polygon points="7,18 7,9 3,9 10,2 17,9 13,9 13,18"/>' },
      { id: 'arrow_down', name: 'Down Arrow', icon: '<polygon points="7,2 7,11 3,11 10,18 17,11 13,11 13,2"/>' },
      { id: 'chevron_right', name: 'Notched Chevron', icon: '<polygon points="2,3 11,10 2,17 8,17 17,10 8,3"/>' },
      { id: 'double_arrow', name: 'Double Arrow', icon: '<polygon points="5,7 15,7 15,3 20,10 15,17 15,13 5,13 5,17 0,10 5,3"/>' },
    ],
  },
  {
    name: 'Symbols & Callouts',
    shapes: [
      { id: 'heart', name: 'Heart', icon: '<path d="M10,17 C4,13 2,9 2,6 A4,4 0 0,1 10,4.5 A4,4 0 0,1 18,6 C18,9 16,13 10,17 Z"/>' },
      { id: 'cross', name: 'Cross', icon: '<polygon points="7,2 13,2 13,7 18,7 18,13 13,13 13,18 7,18 7,13 2,13 2,7 7,7"/>' },
      { id: 'speech_bubble', name: 'Speech Bubble', icon: '<path d="M2,3 H18 V13 H12 L8,17 V13 H2 Z"/>' },
    ],
  },
];

function getShapeDef(id) {
  for (const cat of SHAPE_CATEGORIES) {
    const s = cat.shapes.find(x => x.id === id);
    if (s) return s;
  }
  return { id: 'rect', name: 'Rectangle', icon: '<rect x="2" y="4" width="16" height="12" rx="0"/>' };
}

/* ── Geometric Path Generator ────────────────────────────── */
function drawShapePath(ctx, shapeType, x, y, w, h, radius = 6) {
  ctx.beginPath();
  const cx = x + w / 2;
  const cy = y + h / 2;
  const rx = w / 2;
  const ry = h / 2;

  switch (shapeType) {
    case 'rounded_rect': {
      const r = Math.max(0, Math.min(radius, Math.abs(w) / 2, Math.abs(h) / 2));
      if (ctx.roundRect) {
        ctx.roundRect(x, y, w, h, r);
      } else {
        ctx.moveTo(x + r, y);
        ctx.lineTo(x + w - r, y); ctx.arcTo(x + w, y, x + w, y + r, r);
        ctx.lineTo(x + w, y + h - r); ctx.arcTo(x + w, y + h, x + w - r, y + h, r);
        ctx.lineTo(x + r, y + h); ctx.arcTo(x, y + h, x, y + h - r, r);
        ctx.lineTo(x, y + r); ctx.arcTo(x, y, x + r, y, r);
      }
      break;
    }
    case 'circle':
    case 'ellipse':
      ctx.ellipse(cx, cy, Math.max(0.1, Math.abs(rx)), Math.max(0.1, Math.abs(ry)), 0, 0, Math.PI * 2);
      break;
    case 'diamond':
      ctx.moveTo(cx, y);
      ctx.lineTo(x + w, cy);
      ctx.lineTo(cx, y + h);
      ctx.lineTo(x, cy);
      ctx.closePath();
      break;
    case 'trapezoid': {
      const ins = w * 0.22;
      ctx.moveTo(x + ins, y);
      ctx.lineTo(x + w - ins, y);
      ctx.lineTo(x + w, y + h);
      ctx.lineTo(x, y + h);
      ctx.closePath();
      break;
    }
    case 'parallelogram': {
      const sl = w * 0.22;
      ctx.moveTo(x + sl, y);
      ctx.lineTo(x + w, y);
      ctx.lineTo(x + w - sl, y + h);
      ctx.lineTo(x, y + h);
      ctx.closePath();
      break;
    }
    case 'triangle':
      ctx.moveTo(cx, y);
      ctx.lineTo(x + w, y + h);
      ctx.lineTo(x, y + h);
      ctx.closePath();
      break;
    case 'right_triangle':
      ctx.moveTo(x, y);
      ctx.lineTo(x + w, y + h);
      ctx.lineTo(x, y + h);
      ctx.closePath();
      break;
    case 'pentagon':
      _drawPolygon(ctx, cx, cy, rx, ry, 5, -Math.PI / 2);
      break;
    case 'hexagon':
      _drawPolygon(ctx, cx, cy, rx, ry, 6, 0);
      break;
    case 'heptagon':
      _drawPolygon(ctx, cx, cy, rx, ry, 7, -Math.PI / 2);
      break;
    case 'octagon':
      _drawPolygon(ctx, cx, cy, rx, ry, 8, Math.PI / 8);
      break;
    case 'star3':
      _drawStar(ctx, cx, cy, rx, ry, 3, 0.45, -Math.PI / 2);
      break;
    case 'star4':
      _drawStar(ctx, cx, cy, rx, ry, 4, 0.4, -Math.PI / 2);
      break;
    case 'star5':
      _drawStar(ctx, cx, cy, rx, ry, 5, 0.42, -Math.PI / 2);
      break;
    case 'star6':
      _drawStar(ctx, cx, cy, rx, ry, 6, 0.48, -Math.PI / 2);
      break;
    case 'arrow_right': {
      const aw = w * 0.45, ah = h * 0.26;
      ctx.moveTo(x, y + ah);
      ctx.lineTo(x + w - aw, y + ah);
      ctx.lineTo(x + w - aw, y);
      ctx.lineTo(x + w, cy);
      ctx.lineTo(x + w - aw, y + h);
      ctx.lineTo(x + w - aw, y + h - ah);
      ctx.lineTo(x, y + h - ah);
      ctx.closePath();
      break;
    }
    case 'arrow_left': {
      const aw = w * 0.45, ah = h * 0.26;
      ctx.moveTo(x + w, y + ah);
      ctx.lineTo(x + aw, y + ah);
      ctx.lineTo(x + aw, y);
      ctx.lineTo(x, cy);
      ctx.lineTo(x + aw, y + h);
      ctx.lineTo(x + aw, y + h - ah);
      ctx.lineTo(x + w, y + h - ah);
      ctx.closePath();
      break;
    }
    case 'arrow_up': {
      const aw = w * 0.26, ah = h * 0.45;
      ctx.moveTo(x + aw, y + h);
      ctx.lineTo(x + aw, y + ah);
      ctx.lineTo(x, y + ah);
      ctx.lineTo(cx, y);
      ctx.lineTo(x + w, y + ah);
      ctx.lineTo(x + w - aw, y + ah);
      ctx.lineTo(x + w - aw, y + h);
      ctx.closePath();
      break;
    }
    case 'arrow_down': {
      const aw = w * 0.26, ah = h * 0.45;
      ctx.moveTo(x + aw, y);
      ctx.lineTo(x + aw, y + h - ah);
      ctx.lineTo(x, y + h - ah);
      ctx.lineTo(cx, y + h);
      ctx.lineTo(x + w, y + h - ah);
      ctx.lineTo(x + w - aw, y + h - ah);
      ctx.lineTo(x + w - aw, y);
      ctx.closePath();
      break;
    }
    case 'chevron_right':
      ctx.moveTo(x, y);
      ctx.lineTo(x + w * 0.55, cy);
      ctx.lineTo(x, y + h);
      ctx.lineTo(x + w * 0.45, y + h);
      ctx.lineTo(x + w, cy);
      ctx.lineTo(x + w * 0.45, y);
      ctx.closePath();
      break;
    case 'double_arrow': {
      const aw = w * 0.3, ah = h * 0.26;
      ctx.moveTo(x + aw, y);
      ctx.lineTo(x + aw, y + ah);
      ctx.lineTo(x + w - aw, y + ah);
      ctx.lineTo(x + w, cy);
      ctx.lineTo(x + w - aw, y + h);
      ctx.lineTo(x + w - aw, y + h - ah);
      ctx.lineTo(x + aw, y + h - ah);
      ctx.lineTo(x + aw, y + h);
      ctx.closePath();
      break;
    }
    case 'heart': {
      const topCurveH = h * 0.3;
      ctx.moveTo(cx, y + h);
      ctx.bezierCurveTo(x, y + h * 0.7, x, y + topCurveH, x + w * 0.25, y + topCurveH);
      ctx.bezierCurveTo(x + w * 0.45, y + topCurveH, cx, y + topCurveH * 1.5, cx, y + h * 0.55);
      ctx.bezierCurveTo(cx, y + topCurveH * 1.5, x + w * 0.55, y + topCurveH, x + w * 0.75, y + topCurveH);
      ctx.bezierCurveTo(x + w, y + topCurveH, x + w, y + h * 0.7, cx, y + h);
      ctx.closePath();
      break;
    }
    case 'cross': {
      const t = Math.min(w, h) * 0.33;
      const th = Math.min(w, h) * 0.33;
      ctx.moveTo(cx - t / 2, y);
      ctx.lineTo(cx + t / 2, y);
      ctx.lineTo(cx + t / 2, cy - th / 2);
      ctx.lineTo(x + w, cy - th / 2);
      ctx.lineTo(x + w, cy + th / 2);
      ctx.lineTo(cx + t / 2, cy + th / 2);
      ctx.lineTo(cx + t / 2, y + h);
      ctx.lineTo(cx - t / 2, y + h);
      ctx.lineTo(cx - t / 2, cy + th / 2);
      ctx.lineTo(x, cy + th / 2);
      ctx.lineTo(x, cy - th / 2);
      ctx.lineTo(cx - t / 2, cy - th / 2);
      ctx.closePath();
      break;
    }
    case 'speech_bubble': {
      const r = Math.min(6, w / 6, h / 6);
      const bH = h * 0.78;
      ctx.moveTo(x + r, y);
      ctx.lineTo(x + w - r, y); ctx.arcTo(x + w, y, x + w, y + r, r);
      ctx.lineTo(x + w, y + bH - r); ctx.arcTo(x + w, y + bH, x + w - r, y + bH, r);
      ctx.lineTo(x + w * 0.45, y + bH);
      ctx.lineTo(x + w * 0.25, y + h);
      ctx.lineTo(x + w * 0.28, y + bH);
      ctx.lineTo(x + r, y + bH); ctx.arcTo(x, y + bH, x, y + bH - r, r);
      ctx.lineTo(x, y + r); ctx.arcTo(x, y, x + r, y, r);
      ctx.closePath();
      break;
    }
    case 'rect':
    default:
      ctx.rect(x, y, w, h);
      break;
  }
}

function _drawPolygon(ctx, cx, cy, rx, ry, sides, startAngle) {
  for (let i = 0; i < sides; i++) {
    const a = startAngle + (i * 2 * Math.PI) / sides;
    const px = cx + rx * Math.cos(a);
    const py = cy + ry * Math.sin(a);
    if (i === 0) ctx.moveTo(px, py);
    else ctx.lineTo(px, py);
  }
  ctx.closePath();
}

function _drawStar(ctx, cx, cy, rx, ry, points, innerRatio, startAngle) {
  const step = Math.PI / points;
  for (let i = 0; i < points * 2; i++) {
    const a = startAngle + i * step;
    const rX = i % 2 === 0 ? rx : rx * innerRatio;
    const rY = i % 2 === 0 ? ry : ry * innerRatio;
    const px = cx + rX * Math.cos(a);
    const py = cy + rY * Math.sin(a);
    if (i === 0) ctx.moveTo(px, py);
    else ctx.lineTo(px, py);
  }
  ctx.closePath();
}

/* ── Render Shape with Solid or Line Outline ────────────────── */
function renderShape(ctx, n, zoom) {
  const shapeType = n.shapeType || 'rect';
  const alpha = n.alpha ?? 210;
  const isOpaque = alpha === 255;
  const hasOutline = Boolean(n.outline && (n.outlineThickness ?? 2) > 0);
  const ot = Math.max(1, n.outlineThickness ?? 2);
  const rad = n.cornerRadius ?? 6;

  ctx.save();
  const cx = (n.x + n.width / 2) * zoom;
  const cy = (n.y + n.height / 2) * zoom;
  applyNodeTransform(ctx, cx, cy, n);

  const outAlpha = n.outlineAlpha ?? 255;
  const outColor = hexToRgba(n.outlineColor || '#4f8ef7', outAlpha);

  const applyDash = () => {
    if (n.outlineStyle === 'dashed') ctx.setLineDash([5 * zoom, 3 * zoom]);
    else if (n.outlineStyle === 'dotted') ctx.setLineDash([2 * zoom, 3 * zoom]);
    else if (n.outlineStyle === 'dash_dot') ctx.setLineDash([6 * zoom, 3 * zoom, 2 * zoom, 3 * zoom]);
    else ctx.setLineDash([]);
  };

  if (hasOutline && isOpaque && n.color !== 'transparent') {
    ctx.strokeStyle = outColor;
    ctx.lineWidth = ot * 2 * zoom;
    applyDash();
    drawShapePath(ctx, shapeType, n.x * zoom, n.y * zoom, n.width * zoom, n.height * zoom, rad * zoom);
    ctx.stroke();

    ctx.fillStyle = hexToRgba(n.color, 255);
    drawShapePath(ctx, shapeType, n.x * zoom, n.y * zoom, n.width * zoom, n.height * zoom, rad * zoom);
    ctx.fill();
  } else {
    if (alpha > 0 && n.color !== 'transparent') {
      ctx.fillStyle = hexToRgba(n.color, alpha);
      drawShapePath(ctx, shapeType, n.x * zoom, n.y * zoom, n.width * zoom, n.height * zoom, rad * zoom);
      ctx.fill();
    }
    if (hasOutline) {
      ctx.strokeStyle = outColor;
      ctx.lineWidth = ot * zoom;
      applyDash();
      drawShapePath(ctx, shapeType, n.x * zoom, n.y * zoom, n.width * zoom, n.height * zoom, rad * zoom);
      ctx.stroke();
    }
  }

  ctx.restore();
}

/* ── Arrowhead Helper for Line Connectors ─────────────────── */
function drawArrowhead(ctx, px, py, theta, len, wid, color, alpha) {
  ctx.save();
  ctx.translate(px, py);
  ctx.rotate(theta);
  ctx.fillStyle = hexToRgba(color, alpha);
  ctx.beginPath();
  ctx.moveTo(0, 0);
  ctx.lineTo(-len, -wid / 2);
  ctx.lineTo(-len * 0.75, 0);
  ctx.lineTo(-len, wid / 2);
  ctx.closePath();
  ctx.fill();
  ctx.restore();
}

/* ── Render Line (PowerPoint-style Connectors & Butt Caps) ── */
function renderLine(ctx, n, zoom) {
  ctx.save();

  const x1 = n.x1 * zoom, y1 = n.y1 * zoom;
  const x2 = n.x2 * zoom, y2 = n.y2 * zoom;
  const cx = (x1 + x2) / 2;
  const cy = (y1 + y2) / 2;

  applyNodeTransform(ctx, cx, cy, n);

  const col = n.color || n.outlineColor || '#4a90d9';
  const alpha = n.alpha ?? 255;
  n.color = col;
  n.outlineColor = col;
  ctx.strokeStyle = hexToRgba(col, alpha);
  ctx.lineWidth = Math.max(1, (n.thickness ?? 2) * zoom);
  // Default cap is BUTT (square ends, never rounded!)
  ctx.lineCap = n.cap === 'square' ? 'square' : 'butt';
  ctx.lineJoin = 'miter';

  // Dash Style
  if (n.dash === 'dashed') ctx.setLineDash([8 * zoom, 4 * zoom]);
  else if (n.dash === 'dotted') ctx.setLineDash([2 * zoom, 3 * zoom]);
  else if (n.dash === 'dash_dot') ctx.setLineDash([8 * zoom, 3 * zoom, 2 * zoom, 3 * zoom]);
  else ctx.setLineDash([]);

  const lineType = n.lineType || 'straight';
  const hasArrowStart = Boolean(n.arrowStart || lineType === 'double_arrow');
  const hasArrowEnd = Boolean(n.arrowEnd || lineType === 'arrow' || lineType === 'double_arrow' || lineType === 'elbow_arrow' || lineType === 'curved_arrow');
  const arrowLen = Math.max(4, (n.arrowLength || 10) * zoom);
  const arrowWid = Math.max(4, (n.arrowWidth || 8) * zoom);

  ctx.beginPath();

  if (lineType === 'elbow' || lineType === 'elbow_arrow') {
    // Right-angle connector
    const midX = (n.elbowMidX != null ? n.elbowMidX * zoom : (x1 + x2) / 2);
    ctx.moveTo(x1, y1);
    ctx.lineTo(midX, y1);
    ctx.lineTo(midX, y2);
    ctx.lineTo(x2, y2);
    ctx.stroke();

    if (hasArrowStart) {
      const theta = x1 < midX ? Math.PI : 0;
      drawArrowhead(ctx, x1, y1, theta, arrowLen, arrowWid, col, alpha);
    }
    if (hasArrowEnd) {
      const theta = x2 > midX ? 0 : Math.PI;
      drawArrowhead(ctx, x2, y2, theta, arrowLen, arrowWid, col, alpha);
    }
  } else if (lineType === 'curve' || lineType === 'curved_arrow') {
    // Quadratic Bézier curve
    const ctrlX = n.curveCtrlX != null ? n.curveCtrlX * zoom : (x1 + x2) / 2;
    const ctrlY = n.curveCtrlY != null ? n.curveCtrlY * zoom : (y1 + y2) / 2 - 25 * zoom;

    ctx.moveTo(x1, y1);
    ctx.quadraticCurveTo(ctrlX, ctrlY, x2, y2);
    ctx.stroke();

    if (hasArrowStart) {
      const theta = Math.atan2(y1 - ctrlY, x1 - ctrlX);
      drawArrowhead(ctx, x1, y1, theta, arrowLen, arrowWid, col, alpha);
    }
    if (hasArrowEnd) {
      const theta = Math.atan2(y2 - ctrlY, x2 - ctrlX);
      drawArrowhead(ctx, x2, y2, theta, arrowLen, arrowWid, col, alpha);
    }
  } else if (lineType === 'freeform' && Array.isArray(n.points) && n.points.length > 1) {
    // Freeform path
    ctx.moveTo(n.points[0].x * zoom, n.points[0].y * zoom);
    for (let i = 1; i < n.points.length; i++) {
      ctx.lineTo(n.points[i].x * zoom, n.points[i].y * zoom);
    }
    ctx.stroke();
  } else {
    // Straight line
    ctx.moveTo(x1, y1);
    ctx.lineTo(x2, y2);
    ctx.stroke();

    const theta = Math.atan2(y2 - y1, x2 - x1);
    if (hasArrowStart) {
      drawArrowhead(ctx, x1, y1, theta + Math.PI, arrowLen, arrowWid, col, alpha);
    }
    if (hasArrowEnd) {
      drawArrowhead(ctx, x2, y2, theta, arrowLen, arrowWid, col, alpha);
    }
  }

  ctx.restore();
}

/* ── Normalizer ───────────────────────────────────────────── */
function normalizeNode(n) {
  if (!n || typeof n !== 'object') return null;
  const rawType = String(n.type || 'shape').toLowerCase().trim();
  const type = rawType === 'background' ? 'shape' : (NodeDefs[rawType] ? rawType : 'shape');
  const def = NodeDefs[type] || NodeDefs.shape;
  const base = def.defaults();
  const res = { ...base, ...n, type };

  res.id = String(res.id || uniqueId(type));
  const rawRot = Number(res.rotation);
  res.rotation = !isNaN(rawRot) ? Math.round(((rawRot % 360) + 360) % 360) : 0;
  res.scaleX = Number(res.scaleX) || 1.0;
  res.scaleY = Number(res.scaleY) || 1.0;
  res.skewX = Number(res.skewX) || 0.0;
  res.skewY = Number(res.skewY) || 0.0;
  res.flipX = Boolean(res.flipX);
  res.flipY = Boolean(res.flipY);
  res.visible = res.visible !== false;
  res.locked = Boolean(res.locked);
  res.depth = typeof res.depth === 'number' && !isNaN(res.depth) ? res.depth : 0.001;

  const _r3 = v => Number(Number(v || 0).toFixed(3));

  if (type === 'shape') {
    res.shapeType = res.shapeType || 'rect';
    res.x = _r3(res.x);
    res.y = _r3(res.y);
    res.width = Math.max(4, _r3(res.width || 100));
    res.height = Math.max(4, _r3(res.height || 60));
    res.alpha = Math.max(0, Math.min(255, Number(res.alpha) ?? 210));
    res.color = res.color || '#1a2035';
    res.outline = Boolean(res.outline);
    res.outlineThickness = Math.max(1, Math.round(Number(res.outlineThickness) || 2));
    res.outlineAlpha = Math.max(0, Math.min(255, Number(res.outlineAlpha) ?? 255));
    res.outlineColor = res.outlineColor || '#4f8ef7';
    res.outlineStyle = ['solid', 'dashed', 'dotted', 'dash_dot'].includes(res.outlineStyle) ? res.outlineStyle : 'solid';
    res.cornerRadius = Math.max(0, Math.round(Number(res.cornerRadius) || 0));
  } else if (type === 'text') {
    res.boxX = _r3(res.boxX);
    res.boxY = _r3(res.boxY);
    res.width = Math.max(12, _r3(res.width || 120));
    res.height = Math.max(8, _r3(res.height || 20));
    res.fontSize = Math.max(1, Number(res.fontSize) || 8.0);
    res.text = res.text != null ? String(res.text) : 'Text';
    res.alignment = ['LEFT', 'CENTER', 'RIGHT'].includes(res.alignment) ? res.alignment : 'CENTER';
    res.verticalAlignment = ['TOP', 'MIDDLE', 'BOTTOM'].includes(res.verticalAlignment) ? res.verticalAlignment : 'MIDDLE';
    res.fillType = ['solid', 'gradient'].includes(res.fillType) ? res.fillType : 'solid';
    res.color = res.color || '#ffffff';
    res.alpha = Math.max(0, Math.min(255, Number(res.alpha) ?? 255));
    res.gradient = res.gradient || {
      type: 'linear',
      angle: 0,
      stops: [
        { offset: 0, color: '#ff4d4f', opacity: 1 },
        { offset: 1, color: '#4f8ef7', opacity: 1 },
      ],
    };
    res.gradientAnimation = res.gradientAnimation || {
      enabled: false,
      speed: 1.0,
      direction: 'forward',
      mode: 'scroll',
    };
    res.shadow = res.shadow !== false;
  } else if (type === 'item') {
    res.x = _r3(res.x);
    res.y = _r3(res.y);
    res.scale = Math.max(0.1, Number(res.scale) || 0.8);
    res.material = String(res.material || 'DIAMOND_SWORD').toUpperCase();
    res.transform = ['FIXED', 'HEAD', 'GUI', 'GROUND'].includes(res.transform) ? res.transform : 'FIXED';
  } else if (type === 'block') {
    res.x = _r3(res.x);
    res.y = _r3(res.y);
    res.width = Math.max(4, _r3(res.width || 24));
    res.height = Math.max(4, _r3(res.height || 24));
    res.thickness = Math.max(0.1, Number(res.thickness) || 1.0);
    res.material = String(res.material || 'STONE').toUpperCase();
  } else if (type === 'line') {
    res.lineType = ['straight', 'arrow', 'double_arrow', 'elbow', 'elbow_arrow', 'curve', 'curved_arrow', 'freeform'].includes(res.lineType) ? res.lineType : 'straight';
    res.x1 = _r3(res.x1);
    res.y1 = _r3(res.y1);
    res.x2 = _r3(res.x2 != null ? res.x2 : 80);
    res.y2 = _r3(res.y2 != null ? res.y2 : 20);
    res.thickness = Math.max(1, Number(res.thickness) || 2);
    res.color = res.color || res.outlineColor || '#4a90d9';
    res.outlineColor = res.color;
    res.alpha = Math.max(0, Math.min(255, Number(res.alpha) ?? 255));
    res.dash = ['solid', 'dashed', 'dotted', 'dash_dot'].includes(res.dash) ? res.dash : 'solid';
    res.cap = ['butt', 'square'].includes(res.cap) ? res.cap : 'butt';
    res.arrowStart = Boolean(res.arrowStart || res.lineType === 'double_arrow');
    res.arrowEnd = Boolean(res.arrowEnd || res.lineType === 'arrow' || res.lineType === 'double_arrow' || res.lineType === 'elbow_arrow' || res.lineType === 'curved_arrow');
    res.arrowLength = Math.max(4, Number(res.arrowLength) || 10);
    res.arrowWidth = Math.max(4, Number(res.arrowWidth) || 8);
    res.curveCtrlX = res.curveCtrlX != null ? _r3(res.curveCtrlX) : _r3((res.x1 + res.x2) / 2);
    res.curveCtrlY = res.curveCtrlY != null ? _r3(res.curveCtrlY) : _r3((res.y1 + res.y2) / 2 - 20);
    res.elbowMidX = res.elbowMidX != null ? _r3(res.elbowMidX) : _r3((res.x1 + res.x2) / 2);
    res.elbowMidY = res.elbowMidY != null ? _r3(res.elbowMidY) : _r3((res.y1 + res.y2) / 2);
    res.points = Array.isArray(res.points) ? res.points : [];
  }

  return res;
}

/* ── Rich Text Parser ────────────────────────────────────────
 * Tags: <bold>, <italic>, <color:#hex> (self-contained, nestable)
 * Gradient mode ignores per-segment colors (gradient overrides all).
 */
function parseRichText(text) {
  // Segments: [{text, bold, italic, color}]
  const segments = [];
  let bold = false, italic = false, color = null;
  // Simple stack-less parser: walk the string finding <tag> and </tag>
  const re = /<(\/?)([a-zA-Z]+(?::[^>]*)?)>/g;
  let last = 0;
  let m;
  while ((m = re.exec(text)) !== null) {
    if (m.index > last) {
      segments.push({ text: text.slice(last, m.index), bold, italic, color });
    }
    const closing = m[1] === '/';
    const tag = m[2].toLowerCase();
    if (!closing) {
      if (tag === 'bold') bold = true;
      else if (tag === 'italic') italic = true;
      else if (tag.startsWith('color:')) color = tag.slice(6) || null;
    } else {
      if (tag === 'bold') bold = false;
      else if (tag === 'italic') italic = false;
      else if (tag === 'color') color = null;
    }
    last = re.lastIndex;
  }
  if (last < text.length) segments.push({ text: text.slice(last), bold, italic, color });
  return segments.filter(s => s.text.length > 0);
}

/* ── Node definitions ─────────────────────────────────────── */
const NodeDefs = {

  /* ── Shape ───────────────────────────────────────────────── */
  shape: {
    defaults: () => ({
      type: 'shape',
      shapeType: 'rect',
      id: uniqueId('shape'),
      x: 10, y: 10, depth: 0.001,
      width: 100, height: 60,
      color: '#1a2035', alpha: 210,
      cornerRadius: 6,
      rotation: 0,
      scaleX: 1.0, scaleY: 1.0,
      skewX: 0, skewY: 0,
      flipX: false, flipY: false,
      outline: false,
      outlineColor: '#4f8ef7',
      outlineAlpha: 255,
      outlineThickness: 2,
      outlineStyle: 'solid',
      doubleSided: false,
      visible: true,
      locked: false,
    }),
    render(ctx, n, zoom) {
      renderShape(ctx, n, zoom);
    },
    label: n => `${getShapeDef(n.shapeType || 'rect').name} ${Math.round(n.width)}×${Math.round(n.height)}`,
    icon: '⬚',
    props: ['shapeType', 'x', 'y', 'depth', 'width', 'height', 'rotation', 'scaleX', 'scaleY', 'skewX', 'skewY', 'flipX', 'flipY', '_colorAlpha', 'cornerRadius', 'outline', '_outlineColorAlpha', 'outlineThickness', 'outlineStyle', 'doubleSided'],
  },

  /* ── Background (Alias to shape for backward compatibility) ─ */
  background: {
    defaults: () => ({
      type: 'shape',
      shapeType: 'rect',
      id: uniqueId('bg'),
      x: 10, y: 10, depth: 0.001,
      width: 100, height: 60,
      color: '#1a2035', alpha: 210,
      cornerRadius: 6,
      rotation: 0,
      scaleX: 1.0, scaleY: 1.0,
      skewX: 0, skewY: 0,
      flipX: false, flipY: false,
      outline: false,
      outlineColor: '#4f8ef7',
      outlineAlpha: 255,
      outlineThickness: 2,
      outlineStyle: 'solid',
      doubleSided: false,
      visible: true,
      locked: false,
    }),
    render(ctx, n, zoom) {
      renderShape(ctx, n, zoom);
    },
    label: n => `${getShapeDef(n.shapeType || 'rect').name} ${Math.round(n.width)}×${Math.round(n.height)}`,
    icon: '▭',
    props: ['shapeType', 'x', 'y', 'depth', 'width', 'height', 'rotation', 'scaleX', 'scaleY', 'skewX', 'skewY', 'flipX', 'flipY', '_colorAlpha', 'cornerRadius', 'outline', '_outlineColorAlpha', 'outlineThickness', 'outlineStyle', 'doubleSided'],
  },

  /* ── Text with Solid & Linear Animated Gradient ─────────── */
  text: {
    defaults: () => ({
      type: 'text',
      id: uniqueId('txt'),
      text: 'Hello World',
      boxX: 10, boxY: 10, depth: 0.002,
      width: 120, height: 20,
      fontSize: 8.0, contentWidth: 114,
      alignment: 'CENTER',
      verticalAlignment: 'MIDDLE',
      leftOffset: 0, rightOffset: 0, verticalOffset: 0,
      fillType: 'solid',
      color: '#ffffff',
      alpha: 255,
      gradient: {
        type: 'linear',
        angle: 0,
        stops: [
          { offset: 0, color: '#ff4d4f', opacity: 1 },
          { offset: 1, color: '#4f8ef7', opacity: 1 },
        ],
      },
      gradientAnimation: {
        enabled: false,
        speed: 1.0,
        direction: 'forward',
        mode: 'scroll',
      },
      rotation: 0,
      scaleX: 1.0, scaleY: 1.0,
      skewX: 0, skewY: 0,
      flipX: false, flipY: false,
      shadow: true, seeThrough: false, doubleSided: false,
      visible: true,
      locked: false,
    }),
    render(ctx, n, zoom) {
      ctx.save();
      const cx = (n.boxX + n.width / 2) * zoom;
      const cy = (n.boxY + n.height / 2) * zoom;
      applyNodeTransform(ctx, cx, cy, n);

      // Bounding guide
      ctx.strokeStyle = 'rgba(79,142,247,0.3)';
      ctx.lineWidth = 1;
      ctx.setLineDash([3, 3]);
      ctx.strokeRect(n.boxX * zoom, n.boxY * zoom, n.width * zoom, n.height * zoom);

      const raw = stripFormatting(n.text);
      const fsz = Math.max(7, n.fontSize * zoom * 0.9);
      const baseFont = `${fsz}px "Minecraft", "Minecraftia", "VT323", "JetBrains Mono", monospace`;
      ctx.textBaseline = 'middle';

      // Compute anchor X for alignment
      let anchorX;
      if (n.alignment === 'CENTER') {
        ctx.textAlign = 'center';
        anchorX = (n.boxX + n.width / 2) * zoom;
      } else if (n.alignment === 'RIGHT') {
        ctx.textAlign = 'right';
        anchorX = (n.boxX + n.width) * zoom - 3;
      } else {
        ctx.textAlign = 'left';
        anchorX = (n.boxX + 3) * zoom;
      }

      // Shadow (always drawn on plain text)
      if (n.shadow) {
        const sOff = Math.max(1, Math.round(zoom * 0.8));
        ctx.font = baseFont;
        ctx.fillStyle = '#3f3f3f';
        ctx.fillText(raw, anchorX + sOff, cy + sOff);
      }

      // ── Gradient mode: full-text gradient fill (ignore per-segment colors) ──
      if (n.fillType === 'gradient' && n.gradient?.stops?.length >= 2) {
        const tw = n.width * zoom;
        const th = n.height * zoom;
        const tcx = n.boxX * zoom + tw / 2;
        const tcy = n.boxY * zoom + th / 2;
        const diag = Math.sqrt(tw * tw + th * th) / 2;

        const animTime = window.__animClock || performance.now();
        const animCfg = n.gradientAnimation || {};
        const isAnim = Boolean(animCfg.enabled);
        const spd = Number(animCfg.speed) || 1.0;
        const dir = animCfg.direction === 'reverse' ? -1 : 1;
        const mode = animCfg.mode || 'scroll';

        let angleDeg = n.gradient.angle || 0;
        if (isAnim && mode === 'rotate') {
          angleDeg = ((angleDeg + animTime * 0.05 * spd * dir) % 360 + 360) % 360;
        }
        const rad = (angleDeg * Math.PI) / 180;
        const x0 = tcx - Math.cos(rad) * diag;
        const y0 = tcy - Math.sin(rad) * diag;
        const x1 = tcx + Math.cos(rad) * diag;
        const y1 = tcy + Math.sin(rad) * diag;
        const grad = ctx.createLinearGradient(x0, y0, x1, y1);

        if (isAnim && (mode === 'scroll' || mode === 'shift')) {
          const shift = ((animTime * 0.0003 * spd * dir) % 1 + 1) % 1;
          const baseStops = n.gradient.stops;
          const sorted = [...baseStops].sort((a, b) => a.offset - b.offset);

          // Lerp two gradient stops by color + opacity
          const lerpSt = (lo, hi, t) => {
            const p = h => parseInt((h || '#ffffff').replace('#', ''), 16);
            const lp = p(lo.color), hp = p(hi.color);
            const hex2 = v => Math.max(0, Math.min(255, Math.round(v))).toString(16).padStart(2, '0');
            return {
              color: '#' + hex2(((lp >> 16) & 0xff) + (((hp >> 16) & 0xff) - ((lp >> 16) & 0xff)) * t)
                + hex2(((lp >> 8) & 0xff) + (((hp >> 8) & 0xff) - ((lp >> 8) & 0xff)) * t)
                + hex2((lp & 0xff) + ((hp & 0xff) - (lp & 0xff)) * t),
              opacity: (lo.opacity ?? 1) + ((hi.opacity ?? 1) - (lo.opacity ?? 1)) * t,
            };
          };

          // Interpolate the BASE gradient at offset t ∈ [0,1]
          const baseAt = t => {
            const clamped = Math.max(0, Math.min(1, t));
            let lo = sorted[0], hi = sorted[sorted.length - 1];
            for (let i = 0; i < sorted.length - 1; i++) {
              if (clamped >= sorted[i].offset && clamped <= sorted[i + 1].offset) {
                lo = sorted[i]; hi = sorted[i + 1]; break;
              }
            }
            const span = hi.offset - lo.offset;
            return span < 1e-9 ? { color: lo.color, opacity: lo.opacity ?? 1 }
              : lerpSt(lo, hi, (clamped - lo.offset) / span);
          };

          // Seam = display-position where the tile wraps (base pos crosses 1→0)
          const seam = 1 - shift;
          const fs = [];

          // display=0 → base=shift
          fs.push({ pos: 0, ...baseAt(shift) });

          // Section 1: display [0, seam] maps base [shift → 1]
          sorted.forEach(s => {
            if (s.offset > shift + 1e-5 && s.offset < 1 - 1e-5 && seam > 1e-5) {
              fs.push({ pos: (s.offset - shift) / (1 - shift) * seam, color: s.color, opacity: s.opacity ?? 1 });
            }
          });

          // Seam blend: 3% wide lerp from base@1 → base@0 (smooth loop connect)
          const BW = 0.015;
          const seamLo = Math.max(0.001, seam - BW);
          const seamHi = Math.min(0.999, seam + BW);
          fs.push({ pos: seamLo, ...baseAt(1) });
          fs.push({ pos: seamHi, ...baseAt(0) });

          // Section 2: display [seam, 1] maps base [0 → shift]
          sorted.forEach(s => {
            if (s.offset > 1e-5 && s.offset < shift - 1e-5 && (1 - seam) > 1e-5) {
              fs.push({ pos: seam + (s.offset / shift) * (1 - seam), color: s.color, opacity: s.opacity ?? 1 });
            }
          });

          // display=1 → base=shift (same as display=0, seamless)
          fs.push({ pos: 1, ...baseAt(shift) });

          fs.sort((a, b) => a.pos - b.pos);
          fs.forEach(st => {
            grad.addColorStop(Math.max(0, Math.min(1, st.pos)), hexToRgba(st.color || '#ffffff', Math.round((st.opacity ?? 1) * (n.alpha ?? 255))));
          });

        } else {
          n.gradient.stops.forEach(st => {
            grad.addColorStop(Math.max(0, Math.min(1, st.offset)), hexToRgba(st.color || '#ffffff', Math.round((st.opacity ?? 1) * (n.alpha ?? 255))));
          });
        }
        ctx.font = baseFont;
        ctx.fillStyle = grad;
        ctx.fillText(raw, anchorX, cy);

      } else {
        // ── Solid mode: render per-segment with individual color/bold/italic ──
        const segments = parseRichText(n.text);
        const defaultColor = hexToRgba(n.color || '#ffffff', n.alpha ?? 255);

        if (segments.length <= 1 && !segments[0]?.bold && !segments[0]?.italic && !segments[0]?.color) {
          // Trivial: just one plain segment, fast path
          ctx.font = baseFont;
          ctx.fillStyle = defaultColor;
          ctx.fillText(raw, anchorX, cy);
        } else {
          // Measure with base font (skew/bold don't change advance significantly)
          ctx.font = baseFont;
          let totalW = 0;
          const segWidths = segments.map(seg => {
            // Bold adds ~1px, italic slightly widens apparent text
            const w = ctx.measureText(seg.text).width + (seg.bold ? 1 : 0);
            totalW += w;
            return w;
          });

          let startX;
          if (n.alignment === 'CENTER') startX = anchorX - totalW / 2;
          else if (n.alignment === 'RIGHT') startX = anchorX - totalW;
          else startX = anchorX;

          // Helper: draw one segment with fake-bold and/or fake-italic
          const ITALIC_SKEW = -0.25; // tan(-14°) ≈ horizontal skew factor
          let curX = startX;
          segments.forEach((seg, i) => {
            ctx.font = baseFont;
            ctx.textAlign = 'left';
            ctx.fillStyle = seg.color ? hexToRgba(seg.color, n.alpha ?? 255) : defaultColor;

            if (seg.italic) {
              // Fake italic: skew the canvas horizontally around the text baseline
              ctx.save();
              // setTransform(a,b,c,d,e,f): c = horizontal skew
              // We want skew centered at (curX, cy) so we translate, skew, translate back
              ctx.transform(1, 0, ITALIC_SKEW, 1, -ITALIC_SKEW * cy, 0);
              if (seg.bold) {
                ctx.fillText(seg.text, curX + 1, cy);
              }
              ctx.fillText(seg.text, curX, cy);
              ctx.restore();
            } else if (seg.bold) {
              // Fake bold: draw twice, shifted 1px right
              ctx.fillText(seg.text, curX + 1, cy);
              ctx.fillText(seg.text, curX, cy);
            } else {
              ctx.fillText(seg.text, curX, cy);
            }
            curX += segWidths[i];
          });
        }
      }

      ctx.restore();
    },
    label: n => `"${stripFormatting(n.text).slice(0, 20) || '(empty)'}"`,
    icon: 'T',
    props: ['text', 'boxX', 'boxY', 'depth', 'width', 'height', 'rotation', 'scaleX', 'scaleY', 'skewX', 'skewY', 'flipX', 'flipY', 'fontSize', 'alignment', 'verticalAlignment', 'shadow', 'seeThrough', 'doubleSided'],
  },

  /* ── Item ────────────────────────────────────────────────── */
  item: {
    defaults: () => ({
      type: 'item',
      id: uniqueId('item'),
      material: 'DIAMOND_SWORD',
      x: 20, y: 20, depth: 0.003,
      scale: 0.8,
      rotation: 0,
      scaleX: 1.0, scaleY: 1.0,
      skewX: 0, skewY: 0,
      flipX: false, flipY: false,
      transform: 'FIXED',
      doubleSided: false,
      visible: true,
      locked: false,
    }),
    render(ctx, n, zoom) {
      const sz = 16 * (n.scale || 0.8) * zoom;
      const px = n.x * zoom - sz / 2;
      const py = n.y * zoom - sz / 2;
      const cx = n.x * zoom;
      const cy = n.y * zoom;

      ctx.save();
      applyNodeTransform(ctx, cx, cy, n);

      ctx.imageSmoothingEnabled = false;
      const img = getMcTexture('item', n.material);
      if (img && img.complete && img.naturalWidth > 0) {
        ctx.drawImage(img, px, py, sz, sz);
      } else {
        drawPlaceholder(ctx, px, py, sz, sz, n.material);
      }
      ctx.restore();
    },
    label: n => `${(n.material || 'item').toLowerCase().replace(/_/g, ' ')}`,
    icon: '⬡',
    props: ['material', 'x', 'y', 'depth', 'scale', 'rotation', 'scaleX', 'scaleY', 'skewX', 'skewY', 'flipX', 'flipY', 'transform', 'doubleSided'],
  },

  /* ── Block ───────────────────────────────────────────────── */
  block: {
    defaults: () => ({
      type: 'block',
      id: uniqueId('blk'),
      material: 'STONE',
      x: 30, y: 30, depth: 0.004,
      width: 24, height: 24, thickness: 1.0,
      rotation: 0,
      scaleX: 1.0, scaleY: 1.0,
      skewX: 0, skewY: 0,
      flipX: false, flipY: false,
      doubleSided: false,
      visible: true,
      locked: false,
    }),
    render(ctx, n, zoom) {
      const px = n.x * zoom, py = n.y * zoom;
      const w = n.width * zoom, h = n.height * zoom;
      const cx = px + w / 2;
      const cy = py + h / 2;

      ctx.save();
      applyNodeTransform(ctx, cx, cy, n);

      ctx.imageSmoothingEnabled = false;
      const img = getMcTexture('block', n.material);
      if (img && img.complete && img.naturalWidth > 0) {
        ctx.drawImage(img, px, py, w, h);
      } else {
        drawPlaceholder(ctx, px, py, w, h, n.material);
      }
      ctx.restore();
    },
    label: n => `${(n.material || 'block').toLowerCase().replace(/_/g, ' ')} block`,
    icon: '▪',
    props: ['material', 'x', 'y', 'depth', 'width', 'height', 'rotation', 'scaleX', 'scaleY', 'skewX', 'skewY', 'flipX', 'flipY', 'thickness', 'doubleSided'],
  },

  /* ── Line / Connector (Square/Butt Caps, Arrows, Elbow, Curves) */
  line: {
    defaults: () => ({
      type: 'line',
      lineType: 'straight',
      id: uniqueId('line'),
      x1: 10, y1: 10, x2: 80, y2: 20,
      thickness: 2.0,
      color: '#4a90d9',
      alpha: 255,
      dash: 'solid',
      cap: 'butt',
      arrowStart: false,
      arrowEnd: false,
      arrowLength: 10,
      arrowWidth: 8,
      curveCtrlX: 45,
      curveCtrlY: 0,
      elbowMidX: 45,
      elbowMidY: 15,
      points: [],
      rotation: 0,
      scaleX: 1.0, scaleY: 1.0,
      skewX: 0, skewY: 0,
      flipX: false, flipY: false,
      visible: true,
      locked: false,
    }),
    render(ctx, n, zoom) {
      renderLine(ctx, n, zoom);
    },
    label: n => {
      const t = (n.lineType || 'line').replace(/_/g, ' ');
      return `${t.charAt(0).toUpperCase() + t.slice(1)} (${Math.round(n.x1)},${Math.round(n.y1)})`;
    },
    icon: '╱',
    props: ['lineType', 'x1', 'y1', 'x2', 'y2', 'thickness', 'color', 'alpha', 'dash', 'cap', 'arrowStart', 'arrowEnd', 'rotation', 'scaleX', 'scaleY', 'skewX', 'skewY', 'flipX', 'flipY'],
  },
};
