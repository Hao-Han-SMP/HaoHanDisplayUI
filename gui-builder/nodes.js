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

/* ── Node definitions ─────────────────────────────────────── */
const NodeDefs = {

  /* ── Background ──────────────────────────────────────── */
  background: {
    defaults: () => ({
      type: 'background',
      id: uniqueId('bg'),
      x: 10, y: 10, depth: 0.001,
      width: 100, height: 60,
      color: '#1a2035', alpha: 210,
      doubleSided: false,
    }),
    render(ctx, n, zoom) {
      ctx.fillStyle = hexToRgba(n.color, n.alpha ?? 210);
      ctx.fillRect(n.x * zoom, n.y * zoom, n.width * zoom, n.height * zoom);
    },
    label: n => `Background ${n.width}×${n.height}`,
    icon: '▭',
    // '_colorAlpha' is a virtual prop that renders a combined RGBA picker
    props: ['x', 'y', 'depth', 'width', 'height', '_colorAlpha', 'doubleSided'],
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

      // Text approximation
      const raw = stripFormatting(n.text);
      const fsz = Math.max(6, n.fontSize * zoom * 0.86);
      ctx.save();
      ctx.font = `${n.shadow ? '600' : '500'} ${fsz}px "Inter", sans-serif`;
      ctx.fillStyle = '#ffffff';
      ctx.textBaseline = 'middle';
      if (n.shadow) {
        ctx.shadowColor = 'rgba(0,0,0,.65)';
        ctx.shadowOffsetX = zoom * 0.12;
        ctx.shadowOffsetY = zoom * 0.12;
        ctx.shadowBlur = 1;
      }
      const cy = (n.boxY + n.height / 2) * zoom;
      if (n.alignment === 'CENTER') {
        ctx.textAlign = 'center';
        ctx.fillText(raw, (n.boxX + n.width / 2) * zoom, cy);
      } else if (n.alignment === 'RIGHT') {
        ctx.textAlign = 'right';
        ctx.fillText(raw, (n.boxX + n.width) * zoom - 3, cy);
      } else {
        ctx.textAlign = 'left';
        ctx.fillText(raw, n.boxX * zoom + 3, cy);
      }
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
