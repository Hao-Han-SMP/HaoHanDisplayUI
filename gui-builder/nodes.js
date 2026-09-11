/**
 * nodes.js — Node type definitions, canvas renderers, and Minecraft texture loading.
 * Textures sourced from: InventivetalentDev/minecraft-assets (MIT license, public CDN)
 */

/* ── Texture cache ──────────────────────────────────────── */
const _texCache = {};

/**
 * Loads and returns a cached HTMLImageElement for a Minecraft material texture.
 * On successful load, calls window.__renderFrame() to refresh the canvas.
 */
function getMcTexture(type, material) {
  const name = material.toLowerCase().replace(/\s+/g, '_');
  const key = `${type}:${name}`;
  if (key in _texCache) return _texCache[key];

  const img = new Image();
  img.crossOrigin = 'anonymous';
  img.src = `https://raw.githubusercontent.com/InventivetalentDev/minecraft-assets/1.21.4/assets/minecraft/textures/${type}/${name}.png`;

  img.onload = () => {
    _texCache[key] = img;
    window.__renderFrame?.();
  };
  img.onerror = () => {
    if (type === 'item') {
      // Try block fallback (some items share block textures)
      _texCache[key] = getMcTexture('block', material);
    } else {
      _texCache[key] = null;
    }
    window.__renderFrame?.();
  };

  _texCache[key] = img;
  return img;
}

/* ── Helpers ─────────────────────────────────────────────── */
let _uid = 1;
function uniqueId(prefix) { return `${prefix}_${_uid++}`; }

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

/** Combine hex color + 0-255 alpha into 8-char hex string (for Pickr default) */
function hexWithAlpha(hex, alpha) {
  const a = Math.round(Math.max(0, Math.min(255, alpha ?? 255))).toString(16).padStart(2, '0');
  return (hex || '#000000') + a;
}

/* ── Canvas drawing utilities ────────────────────────────── */
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
    ctx.fillText(label.slice(0, 6), px + w / 2, py + h / 2);
  }
  ctx.restore();
}

/* ── Shapes Catalog ───────────────────────────────────────── */
const SHAPE_CATEGORIES = [
  {
    name: 'Basic',
    shapes: [
      { id: 'rect', name: 'Rectangle', icon: '<rect x="3" y="4" width="14" height="12" rx="0"/>' },
      { id: 'rounded_rect', name: 'Rounded Rectangle', icon: '<rect x="3" y="4" width="14" height="12" rx="3"/>' },
      { id: 'circle', name: 'Circle / Ellipse', icon: '<circle cx="10" cy="10" r="7"/>' },
      { id: 'diamond', name: 'Diamond', icon: '<polygon points="10,2 18,10 10,18 2,10"/>' },
      { id: 'trapezoid', name: 'Trapezoid', icon: '<polygon points="5,4 15,4 18,16 2,16"/>' },
      { id: 'parallelogram', name: 'Parallelogram', icon: '<polygon points="6,4 18,4 14,16 2,16"/>' },
      { id: 'triangle', name: 'Triangle', icon: '<polygon points="10,3 18,17 2,17"/>' },
      { id: 'right_triangle', name: 'Right Triangle', icon: '<polygon points="3,3 17,17 3,17"/>' },
    ],
  },
  {
    name: 'Polygons and Stars',
    shapes: [
      { id: 'pentagon', name: 'Pentagon (5)', icon: '<polygon points="10,2 18,8 15,17 5,17 2,8"/>' },
      { id: 'hexagon', name: 'Hexagon (6)', icon: '<polygon points="10,2 17,6 17,14 10,18 3,14 3,6"/>' },
      { id: 'heptagon', name: 'Heptagon (7)', icon: '<polygon points="10,2 17,5 18,13 14,18 6,18 2,13 3,5"/>' },
      { id: 'octagon', name: 'Octagon (8)', icon: '<polygon points="6,2 14,2 18,6 18,14 14,18 6,18 2,14 2,6"/>' },
      { id: 'star3', name: '3-Point Star', icon: '<polygon points="10,2 12,8 18,15 10,12 2,15 8,8"/>' },
      { id: 'star4', name: '4-Point Star', icon: '<polygon points="10,2 12,8 18,10 12,12 10,18 8,12 2,10 8,8"/>' },
      { id: 'star5', name: '5-Point Star', icon: '<polygon points="10,2 12.5,7.5 18,8 14,12 15.5,17.5 10,14.5 4.5,17.5 6,12 2,8 7.5,7.5"/>' },
      { id: 'star6', name: '6-Point Star', icon: '<polygon points="10,2 12,6 17,6 13,10 15,15 10,12 5,15 7,10 3,6 8,6"/>' },
    ],
  },
  {
    name: 'Arrows & Symbols',
    shapes: [
      { id: 'arrow_right', name: 'Right Arrow', icon: '<polygon points="2,7 11,7 11,3 18,10 11,17 11,13 2,13"/>' },
      { id: 'arrow_left', name: 'Left Arrow', icon: '<polygon points="18,7 9,7 9,3 2,10 9,17 9,13 18,13"/>' },
      { id: 'chevron_right', name: 'Notched Chevron', icon: '<polygon points="2,3 11,10 2,17 8,17 17,10 8,3"/>' },
      { id: 'double_arrow', name: 'Double Arrow', icon: '<polygon points="5,7 15,7 15,3 20,10 15,17 15,13 5,13 5,17 0,10 5,3"/>' },
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
  return { id: 'rect', name: 'Rectangle', icon: '<rect x="3" y="4" width="14" height="12" rx="0"/>' };
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
      const r = Math.max(0, Math.min(radius, w / 2, h / 2));
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
      ctx.ellipse(cx, cy, Math.max(0.1, rx), Math.max(0.1, ry), 0, 0, Math.PI * 2);
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
      ctx.lineTo(x + w - aw, y);
      ctx.lineTo(x + w, cy);
      ctx.lineTo(x + w - aw, y + h);
      ctx.lineTo(x + w - aw, y + h - ah);
      ctx.lineTo(x + aw, y + h - ah);
      ctx.lineTo(x + aw, y + h);
      ctx.lineTo(x, cy);
      ctx.closePath();
      break;
    }
    case 'heart': {
      const topCurveH = h * 0.3;
      ctx.moveTo(cx, y + h);
      ctx.bezierCurveTo(x, cy + h * 0.2, x, y + topCurveH, cx - rx * 0.5, y);
      ctx.bezierCurveTo(cx, y, cx, y + topCurveH, cx, y + topCurveH);
      ctx.bezierCurveTo(cx, y + topCurveH, cx, y, cx + rx * 0.5, y);
      ctx.bezierCurveTo(x + w, y + topCurveH, x + w, cy + h * 0.2, cx, y + h);
      ctx.closePath();
      break;
    }
    case 'cross': {
      const t = w * 0.3, th = h * 0.3;
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
  const ot = (n.outlineThickness ?? 2);
  const rad = n.cornerRadius ?? 6;
  const rot = n.rotation || 0;

  ctx.save();

  if (rot !== 0) {
    const cx = (n.x + n.width / 2) * zoom;
    const cy = (n.y + n.height / 2) * zoom;
    ctx.translate(cx, cy);
    ctx.rotate((rot * Math.PI) / 180);
    ctx.translate(-cx, -cy);
  }

  /* 
   * Trường hợp alpha = 255: Vẽ 1 shape đằng sau với offset dày hơn làm outline
   * Trường hợp alpha < 255: Vẽ fill trước, sau đó vẽ line viền bao quanh
   */
  if (hasOutline && isOpaque) {
    // Solid fill: Backing duplicate shape with expanded offset
    const outAlpha = n.outlineAlpha ?? 255;
    ctx.fillStyle = hexToRgba(n.outlineColor || '#4f8ef7', outAlpha);
    drawShapePath(
      ctx,
      shapeType,
      (n.x - ot) * zoom,
      (n.y - ot) * zoom,
      (n.width + ot * 2) * zoom,
      (n.height + ot * 2) * zoom,
      (rad + ot) * zoom
    );
    ctx.fill();

    // Main foreground shape
    ctx.fillStyle = hexToRgba(n.color, 255);
    drawShapePath(ctx, shapeType, n.x * zoom, n.y * zoom, n.width * zoom, n.height * zoom, rad * zoom);
    ctx.fill();

  } else {
    // Translucent or no outline
    if (alpha > 0 && n.color !== 'transparent') {
      ctx.fillStyle = hexToRgba(n.color, alpha);
      drawShapePath(ctx, shapeType, n.x * zoom, n.y * zoom, n.width * zoom, n.height * zoom, rad * zoom);
      ctx.fill();
    }

    if (hasOutline) {
      // Stroke perimeter around shape
      const outAlpha = n.outlineAlpha ?? 255;
      ctx.strokeStyle = hexToRgba(n.outlineColor || '#4f8ef7', outAlpha);
      ctx.lineWidth = Math.max(1, ot * zoom);
      if (n.outlineStyle === 'dashed') ctx.setLineDash([4 * zoom, 3 * zoom]);
      else if (n.outlineStyle === 'dotted') ctx.setLineDash([2 * zoom, 2 * zoom]);
      else ctx.setLineDash([]);

      drawShapePath(ctx, shapeType, n.x * zoom, n.y * zoom, n.width * zoom, n.height * zoom, rad * zoom);
      ctx.stroke();
    }
  }

  ctx.restore();
}

/* ── Node definitions ─────────────────────────────────────── */
const NodeDefs = {

  /* ── Shape / Background (Unified) ────────────────────────── */
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
      outline: false,
      outlineColor: '#4f8ef7',
      outlineAlpha: 255,
      outlineThickness: 2,
      outlineStyle: 'solid',
      doubleSided: false,
    }),
    render(ctx, n, zoom) {
      renderShape(ctx, n, zoom);
    },
    label: n => `${getShapeDef(n.shapeType || 'rect').name} ${Math.round(n.width)}×${Math.round(n.height)}`,
    icon: '⬚',
    props: ['shapeType', 'x', 'y', 'depth', 'width', 'height', 'rotation', '_colorAlpha', 'cornerRadius', 'outline', '_outlineColorAlpha', 'outlineThickness', 'outlineStyle', 'doubleSided'],
  },

  /* ── Background (Backward Compatibility Alias) ──────────── */
  background: {
    defaults: () => ({
      type: 'shape',
      shapeType: 'rect',
      id: uniqueId('bg'),
      x: 10, y: 10, depth: 0.001,
      width: 100, height: 60,
      color: '#1a2035', alpha: 210,
      cornerRadius: 6,
      outline: false,
      outlineColor: '#4f8ef7',
      outlineAlpha: 255,
      outlineThickness: 2,
      outlineStyle: 'solid',
      doubleSided: false,
    }),
    render(ctx, n, zoom) {
      renderShape(ctx, n, zoom);
    },
    label: n => `${getShapeDef(n.shapeType || 'rect').name} ${Math.round(n.width)}×${Math.round(n.height)}`,
    icon: '▭',
    props: ['shapeType', 'x', 'y', 'depth', 'width', 'height', '_colorAlpha', 'cornerRadius', 'outline', '_outlineColorAlpha', 'outlineThickness', 'outlineStyle', 'doubleSided'],
  },

  /* ── Text ────────────────────────────────────────────── */
  text: {
    defaults: () => ({
      type: 'text',
      id: uniqueId('txt'),
      text: 'Hello <white>World</white>',
      boxX: 10, boxY: 10, depth: 0.002,
      width: 120, height: 20,
      fontSize: 8.0, contentWidth: 114,
      alignment: 'CENTER',
      verticalAlignment: 'MIDDLE',
      leftOffset: 0, rightOffset: 0, verticalOffset: 0,
      shadow: true, seeThrough: false, doubleSided: false,
    }),
    render(ctx, n, zoom) {
      // Bounding-box guide
      ctx.save();
      ctx.strokeStyle = 'rgba(79,142,247,0.3)';
      ctx.lineWidth = 1;
      ctx.setLineDash([3, 3]);
      ctx.strokeRect(n.boxX * zoom, n.boxY * zoom, n.width * zoom, n.height * zoom);
      ctx.restore();

      // Text rendering with Minecraft font
      const raw = stripFormatting(n.text);
      const fsz = Math.max(7, n.fontSize * zoom * 0.9);
      ctx.save();
      ctx.font = `${fsz}px "Minecraft", "Minecraftia", "VT323", "JetBrains Mono", monospace`;
      ctx.textBaseline = 'middle';
      
      const cy = (n.boxY + n.height / 2) * zoom;
      let tx = (n.boxX + 3) * zoom;
      if (n.alignment === 'CENTER') {
        ctx.textAlign = 'center';
        tx = (n.boxX + n.width / 2) * zoom;
      } else if (n.alignment === 'RIGHT') {
        ctx.textAlign = 'right';
        tx = (n.boxX + n.width) * zoom - 3;
      } else {
        ctx.textAlign = 'left';
      }

      if (n.shadow) {
        // Minecraft drop shadow: offset by ~1px with dark shadow color
        const sOff = Math.max(1, Math.round(zoom * 0.8));
        ctx.fillStyle = '#3f3f3f';
        ctx.fillText(raw, tx + sOff, cy + sOff);
      }

      ctx.fillStyle = '#ffffff';
      ctx.fillText(raw, tx, cy);
      ctx.restore();
    },
    label: n => `"${stripFormatting(n.text).slice(0, 20) || '(empty)'}"`,
    icon: 'T',
    props: ['text', 'boxX', 'boxY', 'depth', 'width', 'height', 'fontSize', 'alignment', 'verticalAlignment', 'shadow', 'seeThrough', 'doubleSided'],
  },

  /* ── Item ────────────────────────────────────────────── */
  item: {
    defaults: () => ({
      type: 'item',
      id: uniqueId('item'),
      material: 'DIAMOND_SWORD',
      x: 20, y: 20, depth: 0.003,
      scale: 0.8,
      transform: 'FIXED',
      doubleSided: false,
    }),
    render(ctx, n, zoom) {
      const sz = 16 * n.scale * zoom;
      const px = n.x * zoom - sz / 2;
      const py = n.y * zoom - sz / 2;

      const img = getMcTexture('item', n.material);
      ctx.save();
      ctx.imageSmoothingEnabled = false; // pixel-art crispness
      if (img && img.complete && img.naturalWidth > 0) {
        ctx.drawImage(img, px, py, sz, sz);
      } else {
        drawPlaceholder(ctx, px, py, sz, sz, n.material);
      }
      ctx.restore();
    },
    label: n => `${n.material.toLowerCase().replace(/_/g, ' ')}`,
    icon: '⬡',
    props: ['material', 'x', 'y', 'depth', 'scale', 'transform', 'doubleSided'],
  },

  /* ── Block ───────────────────────────────────────────── */
  block: {
    defaults: () => ({
      type: 'block',
      id: uniqueId('blk'),
      material: 'STONE',
      x: 30, y: 30, depth: 0.004,
      width: 24, height: 24, thickness: 1.0,
      doubleSided: false,
    }),
    render(ctx, n, zoom) {
      const px = n.x * zoom, py = n.y * zoom;
      const w = n.width * zoom, h = n.height * zoom;

      const img = getMcTexture('block', n.material);
      ctx.save();
      ctx.imageSmoothingEnabled = false;
      if (img && img.complete && img.naturalWidth > 0) {
        ctx.drawImage(img, px, py, w, h);
      } else {
        drawPlaceholder(ctx, px, py, w, h, n.material);
      }
      ctx.restore();
    },
    label: n => `${n.material.toLowerCase().replace(/_/g, ' ')} block`,
    icon: '▪',
    props: ['material', 'x', 'y', 'depth', 'width', 'height', 'thickness', 'doubleSided'],
  },

  /* ── Line ────────────────────────────────────────────── */
  line: {
    defaults: () => ({
      type: 'line',
      id: uniqueId('line'),
      x1: 10, y1: 10, x2: 80, y2: 10,
      thickness: 2.0,
      color: '#4a90d9',
    }),
    render(ctx, n, zoom) {
      ctx.save();
      ctx.strokeStyle = n.color || '#4a90d9';
      ctx.lineWidth = Math.max(1, n.thickness * zoom * 0.4);
      ctx.lineCap = 'round';
      ctx.beginPath();
      ctx.moveTo(n.x1 * zoom, n.y1 * zoom);
      ctx.lineTo(n.x2 * zoom, n.y2 * zoom);
      ctx.stroke();
      ctx.restore();
    },
    label: n => `(${n.x1},${n.y1}) → (${n.x2},${n.y2})`,
    icon: '╱',
    // '_color' is a virtual prop that renders an RGB-only color picker
    props: ['x1', 'y1', 'x2', 'y2', 'thickness', '_color'],
  },
};
