/**
 * nodes.js — Node type definitions, default factories, and canvas renderers.
 * Each node type has:
 *   - defaults()      → returns a fresh node object with sensible defaults
 *   - render(ctx, node, zoom) → draws the node on the canvas 2D context
 *   - label(node)     → short display label for the layers panel
 */

const NodeDefs = {

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
      const col = hexToRgba(n.color, n.alpha ?? 210);
      ctx.fillStyle = col;
      ctx.fillRect(n.x * zoom, n.y * zoom, n.width * zoom, n.height * zoom);
    },
    label: n => `Background ${n.width}×${n.height}`,
    props: ['x','y','depth','width','height','color','alpha','doubleSided'],
  },

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
      // Draw bounding box (dashed)
      ctx.save();
      ctx.strokeStyle = 'rgba(79,142,247,0.5)';
      ctx.lineWidth = 1;
      ctx.setLineDash([3, 3]);
      ctx.strokeRect(n.boxX * zoom, n.boxY * zoom, n.width * zoom, n.height * zoom);
      ctx.restore();
      // Render text approximation
      const fontSize = Math.max(6, n.fontSize * zoom * 0.9);
      ctx.save();
      ctx.font = `${n.shadow ? '600 ' : ''}${fontSize}px "Segoe UI", sans-serif`;
      ctx.fillStyle = '#ffffff';
      ctx.textBaseline = 'middle';
      const raw = stripFormatting(n.text);
      const cx = n.boxX * zoom + (n.width * zoom) / 2;
      const cy = n.boxY * zoom + (n.height * zoom) / 2;
      if (n.alignment === 'CENTER') {
        ctx.textAlign = 'center';
        ctx.fillText(raw, cx, cy);
      } else if (n.alignment === 'RIGHT') {
        ctx.textAlign = 'right';
        ctx.fillText(raw, (n.boxX + n.width) * zoom - 2, cy);
      } else {
        ctx.textAlign = 'left';
        ctx.fillText(raw, n.boxX * zoom + 2, cy);
      }
      ctx.restore();
    },
    label: n => `Text "${stripFormatting(n.text).slice(0, 20)}"`,
    props: ['id','text','boxX','boxY','depth','width','height','fontSize','alignment','verticalAlignment','shadow','seeThrough','doubleSided'],
  },

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
      // Draw placeholder square with material name
      ctx.save();
      ctx.fillStyle = '#2a3a5c';
      ctx.strokeStyle = '#4f8ef7';
      ctx.lineWidth = 1.5;
      ctx.fillRect(px, py, sz, sz);
      ctx.strokeRect(px, py, sz, sz);
      // Label
      if (sz > 18) {
        ctx.fillStyle = '#8ab4f8';
        ctx.font = `${Math.max(7, sz * 0.25)}px monospace`;
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillText(n.material.slice(0,6), n.x * zoom, n.y * zoom);
      }
      ctx.restore();
    },
    label: n => `Item ${n.material}`,
    props: ['id','material','x','y','depth','scale','transform','doubleSided'],
  },

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
      const px = n.x * zoom;
      const py = n.y * zoom;
      const w = n.width * zoom;
      const h = n.height * zoom;
      ctx.save();
      ctx.fillStyle = '#3a4060';
      ctx.strokeStyle = '#7a8aaa';
      ctx.lineWidth = 1.5;
      ctx.fillRect(px, py, w, h);
      ctx.strokeRect(px, py, w, h);
      if (w > 16) {
        ctx.fillStyle = '#9ab0c8';
        ctx.font = `${Math.max(7, w * 0.22)}px monospace`;
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillText(n.material.slice(0,5), px + w / 2, py + h / 2);
      }
      ctx.restore();
    },
    label: n => `Block ${n.material}`,
    props: ['id','material','x','y','depth','width','height','thickness','doubleSided'],
  },

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
      ctx.strokeStyle = n.color;
      ctx.lineWidth = Math.max(1, n.thickness * zoom * 0.4);
      ctx.lineCap = 'round';
      ctx.beginPath();
      ctx.moveTo(n.x1 * zoom, n.y1 * zoom);
      ctx.lineTo(n.x2 * zoom, n.y2 * zoom);
      ctx.stroke();
      ctx.restore();
    },
    label: n => `Line (${n.x1},${n.y1})→(${n.x2},${n.y2})`,
    props: ['id','x1','y1','x2','y2','thickness','color'],
  },
};

/* ── Helpers ──────────────────────────────────────────── */

let _uid = 1;
function uniqueId(prefix) { return `${prefix}_${_uid++}`; }

function hexToRgba(hex, alpha) {
  const r = parseInt(hex.slice(1,3),16);
  const g = parseInt(hex.slice(3,5),16);
  const b = parseInt(hex.slice(5,7),16);
  return `rgba(${r},${g},${b},${(alpha ?? 255) / 255})`;
}

/** Strip MiniMessage tags and legacy & codes for canvas preview */
function stripFormatting(text) {
  return text
    .replace(/<[^>]+>/g, '')          // MiniMessage tags
    .replace(/&[0-9a-fklmnor]/gi, '') // legacy & codes
    .trim();
}
