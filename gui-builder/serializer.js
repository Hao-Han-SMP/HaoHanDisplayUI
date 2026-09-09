/**
 * serializer.js — Export/import the canvas state to/from .hhdui.json
 */

const Serializer = {

  /** Convert editor node list + document meta → JSON string */
  toJson(state) {
    const doc = {
      version: '1.0',
      name: state.name || 'untitled',
      canvasWidth: state.canvasW,
      canvasHeight: state.canvasH,
      nodes: state.nodes.map(n => this._serializeNode(n)),
      buttons: state.nodes
        .filter(n => n._button)
        .map(n => this._serializeButton(n._button)),
    };
    return JSON.stringify(doc, null, 2);
  },

  _serializeNode(n) {
    // Deep-clone, remove editor-only fields
    const out = { ...n };
    delete out._button;
    delete out._selected;
    return out;
  },

  _serializeButton(btn) {
    return {
      id: btn.id,
      nodeId: btn.nodeId,
      description: btn.description || '',
      action: btn.action ? { ...btn.action } : { type: 'NONE', value: '' },
    };
  },

  /** Parse JSON → state (nodes + meta) */
  fromJson(jsonStr) {
    let doc;
    try { doc = JSON.parse(jsonStr); }
    catch { throw new Error('Invalid JSON file'); }

    if (!doc.nodes || !Array.isArray(doc.nodes)) throw new Error('Missing nodes array');

    // Re-attach button data to matching nodes
    const btnMap = {};
    if (Array.isArray(doc.buttons)) {
      doc.buttons.forEach(b => { btnMap[b.nodeId] = b; });
    }

    const nodes = doc.nodes.map(n => {
      const node = { ...n };
      if (btnMap[n.id]) node._button = { ...btnMap[n.id] };
      return node;
    });

    return {
      version: doc.version || '1.0',
      name: doc.name || 'untitled',
      canvasW: doc.canvasWidth || 256,
      canvasH: doc.canvasHeight || 192,
      nodes,
    };
  },
};
