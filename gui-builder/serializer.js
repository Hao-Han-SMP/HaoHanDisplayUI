/**
 * serializer.js — Export/import the canvas state to/from .hhdui.json
 * Hardened with validation, normalization, duplicate ID prevention, and orphan button cleanup.
 */

const Serializer = {

  /** Convert editor node list + document meta → JSON string */
  toJson(state) {
    const nodeIds = new Set((state.nodes || []).map(n => n.id));

    // Serialize valid buttons, omitting orphans
    const buttons = (state.nodes || [])
      .filter(n => n._button && n._button.nodeId && nodeIds.has(n._button.nodeId))
      .map(n => this._serializeButton(n._button));

    const doc = {
      version: '1.0',
      name: this._sanitizeName(state.name || 'untitled'),
      canvasWidth: Math.max(16, Math.min(4096, Math.round(state.canvasW || 256))),
      canvasHeight: Math.max(16, Math.min(4096, Math.round(state.canvasH || 192))),
      nodes: (state.nodes || []).map(n => this._serializeNode(n)),
      buttons,
      scene: state.scene ? JSON.parse(JSON.stringify(state.scene)) : undefined,
    };

    return JSON.stringify(doc, null, 2);
  },

  _serializeNode(n) {
    // Deep-clone and remove editor-only runtime fields
    const out = JSON.parse(JSON.stringify(n));
    delete out._button;
    delete out._selected;
    return out;
  },

  _serializeButton(btn) {
    const validActions = ['NONE', 'OPEN_URL', 'PLAYER_COMMAND', 'CONSOLE_COMMAND', 'SUGGEST_COMMAND'];
    const actType = (btn.action?.type || 'NONE').toUpperCase();
    return {
      id: String(btn.id || uniqueId('btn')),
      nodeId: String(btn.nodeId || ''),
      description: String(btn.description || ''),
      action: {
        type: validActions.includes(actType) ? actType : 'NONE',
        value: String(btn.action?.value || ''),
      },
    };
  },

  _sanitizeName(name) {
    if (!name || typeof name !== 'string') return 'untitled';
    // Remove invalid filename characters and path traversal
    return name.replace(/[/\\?%*:|"<>]/g, '_').trim() || 'untitled';
  },

  /** Parse JSON string → validated, normalized state */
  fromJson(jsonStr) {
    if (!jsonStr || typeof jsonStr !== 'string') {
      throw new Error('Tập tin rỗng hoặc không hợp lệ');
    }

    let doc;
    try {
      doc = JSON.parse(jsonStr);
    } catch {
      throw new Error('Cú pháp JSON không hợp lệ. Vui lòng kiểm tra lại file.');
    }

    if (!doc || typeof doc !== 'object' || Array.isArray(doc)) {
      throw new Error('Dữ liệu JSON không đúng cấu trúc tài liệu HaoHan Display UI.');
    }

    if (!Array.isArray(doc.nodes)) {
      throw new Error('Tài liệu thiếu danh sách nodes ("nodes" array).');
    }

    // Build button lookup map
    const btnMap = {};
    if (Array.isArray(doc.buttons)) {
      doc.buttons.forEach(b => {
        if (b && b.nodeId && b.id) {
          btnMap[b.nodeId] = {
            id: String(b.id),
            nodeId: String(b.nodeId),
            description: String(b.description || ''),
            action: {
              type: String(b.action?.type || 'NONE').toUpperCase(),
              value: String(b.action?.value || ''),
            },
          };
        }
      });
    }

    // Normalize and validate nodes
    const existingIds = new Set();
    const normalizedNodes = [];

    doc.nodes.forEach(rawNode => {
      const node = normalizeNode(rawNode);
      if (!node) return;

      // Handle duplicate IDs
      if (existingIds.has(node.id)) {
        node.id = uniqueId(node.type || 'node');
      }
      existingIds.add(node.id);

      // Re-attach button if defined
      if (btnMap[node.id]) {
        node._button = { ...btnMap[node.id] };
      }

      normalizedNodes.push(node);
    });

    // Advance uniqueId counter past all imported IDs
    syncUid(normalizedNodes);

    const canvasW = Math.max(16, Math.min(4096, parseInt(doc.canvasWidth) || 256));
    const canvasH = Math.max(16, Math.min(4096, parseInt(doc.canvasHeight) || 192));
    const name = this._sanitizeName(doc.name || 'untitled');

    return {
      version: doc.version || '1.0',
      name,
      canvasW,
      canvasH,
      nodes: normalizedNodes,
      scene: doc.scene || null,
    };
  },
};
