/**
 * item-picker.js — Creative-mode-style item/block picker modal.
 *
 * Data source: PrismarineJS/minecraft-data (MIT) — items.json, blocks.json per version
 * Textures: InventivetalentDev/minecraft-assets (MIT)
 *
 * Usage:
 *   ItemPicker.open(currentNamespace, (namespace, itemMeta) => { ... });
 */

const ItemPicker = (() => {

  const DATA_PATHS_URL = 'https://raw.githubusercontent.com/PrismarineJS/minecraft-data/master/data/dataPaths.json';
  const DATA_BASE  = 'https://raw.githubusercontent.com/PrismarineJS/minecraft-data/master/data/pc';
  const TEX_BASE   = 'https://raw.githubusercontent.com/InventivetalentDev/minecraft-assets';

  const _itemCache  = {}; // version → merged item array
  const _texFailed  = new Set();
  let _versions     = null; // [{label, value}] loaded from dataPaths.json

  let _modal     = null;
  let _callback  = null;
  let _version   = null; // set to latest after versions load
  let _tab       = 'all';      // 'all' | 'items' | 'blocks'
  let _query     = '';
  let _selected  = '';
  let _allItems  = [];
  let _tooltip   = null;

  /* ── Version list (fetched once, cached) ─────────────── */

  async function getVersions() {
    if (_versions) return _versions;
    try {
      const data = await fetch(DATA_PATHS_URL).then(r => r.json());
      const pc = data?.pc ?? {};

      // Semver-aware sort: collect versions that have items data
      const list = Object.keys(pc)
        .filter(v => pc[v].items)
        .sort((a, b) => {
          const pa = a.split('.').map(Number);
          const pb = b.split('.').map(Number);
          for (let i = 0; i < 3; i++) {
            const diff = (pb[i] || 0) - (pa[i] || 0);
            if (diff !== 0) return diff;
          }
          return 0;
        });

      _versions = list.map((v, i) => ({
        value: v,
        label: i === 0 ? `${v} (Latest)` : v,
      }));
    } catch {
      // Fallback if GitHub is unreachable
      _versions = [
        { value: '1.21.5', label: '1.21.5 (Latest)' },
        { value: '1.21.4', label: '1.21.4' },
        { value: '1.21.1', label: '1.21.1' },
        { value: '1.20.4', label: '1.20.4' },
      ];
    }
    if (!_version) _version = _versions[0].value;
    return _versions;
  }

  /* ── Public ──────────────────────────────────────────── */

  function open(current, callback) {
    _callback = callback;
    _selected = current || '';
    _createModal();
    // Fetch versions first, then load items
    getVersions().then(versions => {
      if (!_modal) return; // closed before versions arrived
      _version = _version || versions[0].value;
      _populateVersionSelect(versions);
      _loadItems(_version);
    });
  }

  /* ── Modal scaffold ──────────────────────────────────── */

  function _createModal() {
    _destroyModal();

    const overlay = document.createElement('div');
    overlay.id = 'icp-overlay';
    overlay.innerHTML = `
      <div id="icp-modal" role="dialog" aria-label="Select item">
        <!-- Header -->
        <div id="icp-header">
          <div id="icp-title">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/></svg>
            Select Item
          </div>
          <div id="icp-search-wrap">
            <svg class="icp-search-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
            <input id="icp-search" type="text" placeholder="Search items…" autocomplete="off" spellcheck="false" />
            <button id="icp-search-clear" class="icp-search-clear">✕</button>
          </div>
          <div id="icp-tabs">
            <button class="icp-tab ${_tab==='all'?'active':''}"    data-tab="all">All</button>
            <button class="icp-tab ${_tab==='items'?'active':''}"  data-tab="items">Items</button>
            <button class="icp-tab ${_tab==='blocks'?'active':''}" data-tab="blocks">Blocks</button>
          </div>
          <select id="icp-version" disabled title="Loading versions…">
            <option>Loading…</option>
          </select>
          <button id="icp-close" title="Close">✕</button>
        </div>

        <!-- Body -->
        <div id="icp-body">
          <div id="icp-loading">
            <div class="icp-spinner"></div>
            <span>Loading item registry…</span>
          </div>
          <div id="icp-grid"></div>
        </div>

        <!-- Footer -->
        <div id="icp-footer">
          <div id="icp-selected-preview">
            <img id="icp-sel-img" src="" alt="" />
            <span id="icp-sel-name">None selected</span>
          </div>
          <span id="icp-count"></span>
        </div>
      </div>
    `;

    document.body.appendChild(overlay);
    _modal = overlay;

    // Tooltip element
    _tooltip = document.createElement('div');
    _tooltip.id = 'icp-tooltip';
    document.body.appendChild(_tooltip);

    // Events
    overlay.addEventListener('click', e => { if (e.target === overlay) _destroyModal(); });
    document.getElementById('icp-close').addEventListener('click', _destroyModal);
    document.getElementById('icp-search').addEventListener('input', e => {
      _query = e.target.value.toLowerCase().trim();
      document.getElementById('icp-search-clear').style.display = _query ? 'flex' : 'none';
      _renderGrid();
    });
    document.getElementById('icp-search-clear').addEventListener('click', () => {
      document.getElementById('icp-search').value = '';
      _query = '';
      document.getElementById('icp-search-clear').style.display = 'none';
      _renderGrid();
    });
    document.getElementById('icp-version').addEventListener('change', e => {
      _version = e.target.value;
      _loadItems(_version);
    });

    document.querySelectorAll('.icp-tab').forEach(btn => {
      btn.addEventListener('click', () => {
        document.querySelectorAll('.icp-tab').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        _tab = btn.dataset.tab;
        _renderGrid();
      });
    });

    // Keyboard: Escape to close
    const onKey = e => { if (e.key === 'Escape') { _destroyModal(); document.removeEventListener('keydown', onKey); } };
    document.addEventListener('keydown', onKey);

    // Focus search after animation
    setTimeout(() => document.getElementById('icp-search')?.focus(), 60);
    _updateSelectedPreview(_selected);
  }

  function _populateVersionSelect(versions) {
    const sel = document.getElementById('icp-version');
    if (!sel) return;
    sel.innerHTML = versions
      .map(v => `<option value="${v.value}" ${v.value === _version ? 'selected' : ''}>${v.label}</option>`)
      .join('');
    sel.disabled = false;
    sel.title = '';
  }

  /* ── Data loading ────────────────────────────────────── */

  async function _loadItems(version) {
    document.getElementById('icp-loading').style.display = 'flex';
    document.getElementById('icp-grid').style.display    = 'none';

    if (_itemCache[version]) {
      _allItems = _itemCache[version];
      _showGrid();
      return;
    }

    try {
      const [itemsData, blocksData] = await Promise.all([
        fetch(`${DATA_BASE}/${version}/items.json`).then(r => r.ok ? r.json() : []).catch(() => []),
        fetch(`${DATA_BASE}/${version}/blocks.json`).then(r => r.ok ? r.json() : []).catch(() => []),
      ]);

      const blockSet = new Set((blocksData || []).map(b => b.name));

      // Merge: items first, add any block-only entries
      const itemMap = new Map();
      (itemsData || []).forEach(item => {
        itemMap.set(item.name, {
          name: item.name,
          namespace: `minecraft:${item.name}`,
          displayName: item.displayName || _toDisplay(item.name),
          isBlock: blockSet.has(item.name),
          stackSize: item.stackSize,
        });
      });

      _itemCache[version] = [...itemMap.values()];
      _allItems = _itemCache[version];
      _showGrid();
    } catch {
      _allItems = [];
      _showGrid();
    }
  }

  function _showGrid() {
    document.getElementById('icp-loading').style.display = 'none';
    document.getElementById('icp-grid').style.display    = '';
    _renderGrid();
  }

  /* ── Grid rendering ──────────────────────────────────── */

  function _renderGrid() {
    const grid = document.getElementById('icp-grid');
    const countEl = document.getElementById('icp-count');
    if (!grid) return;

    const filtered = _allItems.filter(item => {
      if (_tab === 'items'  && item.isBlock)  return false;
      if (_tab === 'blocks' && !item.isBlock) return false;
      if (_query) return item.name.includes(_query) || item.displayName.toLowerCase().includes(_query) || item.namespace.includes(_query);
      return true;
    });

    countEl.textContent = `${filtered.length.toLocaleString()} items`;

    // Use document fragment for performance
    const frag = document.createDocumentFragment();
    filtered.forEach(item => {
      const cell = _createCell(item);
      frag.appendChild(cell);
    });

    grid.innerHTML = '';
    grid.appendChild(frag);
  }

  function _createCell(item) {
    const cell = document.createElement('div');
    cell.className = 'icp-cell' + (item.namespace === _selected ? ' selected' : '');
    cell.dataset.ns = item.namespace;

    const img = document.createElement('img');
    img.className = 'icp-tex';
    img.loading = 'lazy';
    img.decoding = 'async';
    img.draggable = false;
    _loadCellTexture(img, item);

    const label = document.createElement('div');
    label.className = 'icp-cell-label';
    label.textContent = item.displayName.length > 10 ? item.displayName.slice(0, 9) + '…' : item.displayName;

    cell.appendChild(img);
    cell.appendChild(label);

    // Click to select
    cell.addEventListener('click', () => {
      document.querySelectorAll('.icp-cell.selected').forEach(c => c.classList.remove('selected'));
      cell.classList.add('selected');
      _selected = item.namespace;
      _updateSelectedPreview(item.namespace);
      // Confirm selection on double-click or after brief delay on single click
      _callback?.(item.namespace, item);
      _destroyModal();
    });

    // Tooltip on hover
    cell.addEventListener('mouseenter', e => _showTooltip(e, item));
    cell.addEventListener('mousemove',  e => _moveTooltip(e));
    cell.addEventListener('mouseleave', _hideTooltip);

    return cell;
  }

  /* ── Pixel-art placeholder ──────────────────────────── */

  // Color palette keyed by first char of name for consistent per-item color
  const _PAL = ['#3a5080','#4a6030','#603030','#504080','#307060','#605030','#403060'];
  function _placeholder(name) {
    const cvs = document.createElement('canvas');
    cvs.width = cvs.height = 16;
    const c = cvs.getContext('2d');
    const col = _PAL[name.charCodeAt(0) % _PAL.length];
    const col2 = _PAL[(name.charCodeAt(0) + 3) % _PAL.length];
    c.fillStyle = col;  c.fillRect(0, 0, 16, 16);
    c.fillStyle = col2; c.fillRect(0, 0, 8, 8);
    c.fillStyle = col2; c.fillRect(8, 8, 8, 8);
    c.fillStyle = 'rgba(255,255,255,.12)';
    c.font = '7px monospace'; c.textAlign = 'center'; c.textBaseline = 'middle';
    c.fillText(name.slice(0, 1).toUpperCase(), 8, 9);
    return cvs.toDataURL();
  }

  function _loadCellTexture(img, item) {
    const name = item.name;
    img.alt = '';

    // Build ordered source list:
    // 1. InventivetalentDev item/ or block/ (primary)
    // 2. InventivetalentDev the other folder  (swap)
    // 3. Minecraft Wiki Invicon               (official renders, highest coverage)
    // 4. Placeholder canvas                   (never broken-image icon)
    const cdnTypes = item.isBlock ? ['block', 'item'] : ['item', 'block'];
    const sources = [
      ...cdnTypes.map(t => ({
        url: `${TEX_BASE}/${_version}/assets/minecraft/textures/${t}/${name}.png`,
        key: `${t}:${name}`,
      })),
      {
        url: `https://minecraft.wiki/images/Invicon_${encodeURIComponent(item.displayName.replace(/\s+/g, '_'))}.png`,
        key: `wiki:${name}`,
      },
    ];
    let attempt = 0;

    const tryNext = () => {
      if (attempt >= sources.length) {
        img.onerror = null;
        img.src = _placeholder(name);
        img.style.imageRendering = 'pixelated';
        img.style.opacity = '0.45';
        return;
      }
      const src = sources[attempt++];
      if (_texFailed.has(src.key)) { tryNext(); return; }
      img.onerror = () => { _texFailed.add(src.key); tryNext(); };
      img.src = src.url;
    };

    tryNext();
  }

  /* ── Selected preview ────────────────────────────────── */

  function _updateSelectedPreview(namespace) {
    const nameEl = document.getElementById('icp-sel-name');
    const imgEl  = document.getElementById('icp-sel-img');
    if (!nameEl || !imgEl) return;
    if (!namespace) { nameEl.textContent = 'None selected'; imgEl.src = ''; return; }
    nameEl.textContent = namespace;
    const name = namespace.replace('minecraft:', '');
    // Try item → block → wiki for the footer preview too
    const previewSrcs = [
      `${TEX_BASE}/${_version}/assets/minecraft/textures/item/${name}.png`,
      `${TEX_BASE}/${_version}/assets/minecraft/textures/block/${name}.png`,
      `https://minecraft.wiki/images/Invicon_${encodeURIComponent(name.split('_').map(w => w[0].toUpperCase() + w.slice(1)).join('_'))}.png`,
    ];
    let pi = 0;
    const tryPrev = () => {
      if (pi >= previewSrcs.length) { imgEl.style.opacity = '.2'; return; }
      imgEl.onerror = () => { pi++; tryPrev(); };
      imgEl.src = previewSrcs[pi++];
    };
    tryPrev();
  }

  /* ── Tooltip ─────────────────────────────────────────── */

  function _showTooltip(e, item) {
    if (!_tooltip) return;
    _tooltip.innerHTML = `<strong>${item.displayName}</strong><br/><span class="icp-tt-ns">${item.namespace}</span>${item.isBlock ? '<br/><span class="icp-tt-tag">Block</span>' : ''}`;
    _tooltip.style.display = 'block';
    _moveTooltip(e);
  }
  function _moveTooltip(e) {
    if (!_tooltip) return;
    const x = e.clientX + 14, y = e.clientY + 14;
    const tw = _tooltip.offsetWidth, th = _tooltip.offsetHeight;
    _tooltip.style.left = (x + tw > window.innerWidth  - 8 ? e.clientX - tw - 8 : x) + 'px';
    _tooltip.style.top  = (y + th > window.innerHeight - 8 ? e.clientY - th - 8 : y) + 'px';
  }
  function _hideTooltip() { if (_tooltip) _tooltip.style.display = 'none'; }

  /* ── Helpers ─────────────────────────────────────────── */

  function _destroyModal() {
    _modal?.remove();  _modal = null;
    _tooltip?.remove(); _tooltip = null;
  }

  function _toDisplay(name) {
    return name.split('_').map(w => w[0].toUpperCase() + w.slice(1)).join(' ');
  }

  return { open };
})();
