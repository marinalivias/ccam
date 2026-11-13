(() => {
  "use strict";

  // Configuracao
  const AUTH_KEY = "ccam_auth";
  const FLASH_KEY = "ccam_flash";
  const PUBLIC_PAGES = ["login.html", "esqueci-senha.html", "redefinir-senha.html"];

  const PAGE_PERMS = {
    "index.html": null,
    "conta.html": null,
    "cadastro.html": ["ALUNOS_VER"],
    "treinos.html": ["TREINOS_VER"],
    "financeiro.html": ["FINANCEIRO_VER"],
    "empresa.html": ["EMPRESA_VER"],
    "mural.html": ["MURAL_VER"],
    "crm.html": ["CRM_VER"],
    "usuarios.html": ["USUARIOS_GERENCIAR"],
    "empresas.html": ["EMPRESAS_GERENCIAR"]
  };

  const getPath = () => (location.pathname || "").split("/").pop() || "index.html";

  const API_URL = (() => {
    if (location.port === "8081") return "/api";
    if (["localhost", "127.0.0.1", ""].includes(location.hostname)) return "http://localhost:8081/api";
    if (location.hostname.endsWith("onrender.com")) return "/api";
    return "https://ccam-2-0.onrender.com/api";
  })();

  // Sessao
  const safeJsonParse = (raw) => { try { return JSON.parse(raw); } catch { return null; } };
  const getWindowNameSession = () => {
    const prefix = `${AUTH_KEY}=`;
    if (!window.name || !window.name.startsWith(prefix)) return null;
    return safeJsonParse(window.name.slice(prefix.length));
  };
  const setWindowNameSession = (session) => { try { window.name = `${AUTH_KEY}=${JSON.stringify(session)}`; } catch {} };
  const clearWindowNameSession = () => {
    const prefix = `${AUTH_KEY}=`;
    if (window.name && window.name.startsWith(prefix)) window.name = "";
  };
  const loadSession = () => {
    const fromSession = safeJsonParse(sessionStorage.getItem(AUTH_KEY) || "");
    if (fromSession?.token && fromSession?.usuario) return fromSession;
    const fromName = getWindowNameSession();
    if (fromName?.token && fromName?.usuario) return fromName;
    return null;
  };
  const saveSession = (session) => {
    try { sessionStorage.setItem(AUTH_KEY, JSON.stringify(session)); } catch {}
    setWindowNameSession(session);
  };
  const clearSession = () => {
    try { sessionStorage.removeItem(AUTH_KEY); } catch {}
    clearWindowNameSession();
  };

  const currentUser = () => loadSession()?.usuario || null;
  const hasPerm = (perm) => !!currentUser()?.permissoes?.includes(perm);
  const hasAnyPerm = (list) => !list || list.some(hasPerm);

  const setFlash = (flash) => { try { sessionStorage.setItem(FLASH_KEY, JSON.stringify(flash)); } catch {} };
  const takeFlash = () => {
    const flash = safeJsonParse(sessionStorage.getItem(FLASH_KEY) || "");
    try { sessionStorage.removeItem(FLASH_KEY); } catch {}
    return flash;
  };

  // Icones
  const ICONS = {
    "home": '<path d="M3 10.5 12 3l9 7.5"/><path d="M5 9.5V21h14V9.5"/><path d="M9.5 21v-6h5v6"/>',
    "user-plus": '<path d="M15 20v-1.5a4.5 4.5 0 0 0-4.5-4.5h-3A4.5 4.5 0 0 0 3 18.5V20"/><circle cx="9" cy="7.5" r="3.5"/><path d="M19 8v6M16 11h6"/>',
    "user": '<circle cx="12" cy="8" r="3.8"/><path d="M4.5 20.5v-1a6 6 0 0 1 6-6h3a6 6 0 0 1 6 6v1"/>',
    "users": '<circle cx="9" cy="8" r="3.2"/><path d="M3 20v-1a5 5 0 0 1 5-5h2a5 5 0 0 1 5 5v1"/><path d="M16 4.8a3.2 3.2 0 0 1 0 6.4"/><path d="M18.5 14.3A5 5 0 0 1 21.5 19v1"/>',
    "dumbbell": '<path d="M6.5 6.5v11M17.5 6.5v11M3.5 9v6M20.5 9v6M6.5 12h11"/>',
    "wallet": '<rect x="2.5" y="5" width="19" height="14" rx="2.5"/><path d="M2.5 10h19"/><path d="M6.5 15h3"/>',
    "building": '<path d="M4 21V5a1.5 1.5 0 0 1 1.5-1.5h8A1.5 1.5 0 0 1 15 5v16"/><path d="M15 9h3.5A1.5 1.5 0 0 1 20 10.5V21"/><path d="M2.5 21h19"/><path d="M8 8h3M8 12h3M8 16h3"/>',
    "megaphone": '<path d="M4 10v4h3l5 4V6L7 10z"/><path d="M16 9a4 4 0 0 1 0 6"/><path d="M18.5 6.5a8 8 0 0 1 0 11"/>',
    "chart": '<path d="M4 20V4"/><path d="M4 20h16"/><path d="M8.5 16v-4M12.5 16V8M16.5 16v-6"/>',
    "logout": '<path d="M9 21H5.5A1.5 1.5 0 0 1 4 19.5v-15A1.5 1.5 0 0 1 5.5 3H9"/><path d="M16 17l5-5-5-5"/><path d="M21 12H9"/>',
    "menu": '<path d="M4 7h16M4 12h16M4 17h16"/>',
    "x": '<path d="M6 6l12 12M18 6 6 18"/>',
    "check": '<path d="m5 12.5 4.5 4.5L19 7.5"/>',
    "check-circle": '<circle cx="12" cy="12" r="9"/><path d="m8.5 12.5 2.5 2.5 4.5-5"/>',
    "x-circle": '<circle cx="12" cy="12" r="9"/><path d="m9 9 6 6M15 9l-6 6"/>',
    "alert-triangle": '<path d="M12 4 2.8 19.5a1 1 0 0 0 .9 1.5h16.6a1 1 0 0 0 .9-1.5z"/><path d="M12 10v4.5"/><path d="M12 17.5h.01"/>',
    "info": '<circle cx="12" cy="12" r="9"/><path d="M12 11v5"/><path d="M12 7.5h.01"/>',
    "calendar": '<rect x="3.5" y="5" width="17" height="15.5" rx="2.5"/><path d="M3.5 10h17M8 3v4M16 3v4"/>',
    "repeat": '<path d="M17 3l3 3-3 3"/><path d="M4 11V9.5A3.5 3.5 0 0 1 7.5 6H20"/><path d="M7 21l-3-3 3-3"/><path d="M20 13v1.5a3.5 3.5 0 0 1-3.5 3.5H4"/>',
    "weight": '<circle cx="12" cy="14" r="6.5"/><path d="M9 7.5a3 3 0 0 1 6 0"/>',
    "chevron-down": '<path d="m6 9 6 6 6-6"/>',
    "eye": '<path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12z"/><circle cx="12" cy="12" r="3"/>',
    "eye-off": '<path d="M3 3l18 18"/><path d="M10.6 6.1A9.8 9.8 0 0 1 12 6c6.5 0 10 6 10 6a17 17 0 0 1-3.1 3.9"/><path d="M6.6 7.6C3.6 9.4 2 12 2 12s3.5 6 10 6c1.5 0 2.8-.3 4-.8"/><path d="M9.9 9.9a3 3 0 0 0 4.2 4.2"/>',
    "trending-up": '<path d="M3 17l6-6 4 4 8-8"/><path d="M15 7h6v6"/>',
    "cake": '<path d="M4 20h16v-6a2 2 0 0 0-2-2H6a2 2 0 0 0-2 2z"/><path d="M4 16c2 1.5 4 1.5 6 0s4-1.5 6 0 3 1 4 0"/><path d="M12 12V8"/><path d="M12 4.5c.8.9.8 1.8 0 2.5-.8-.7-.8-1.6 0-2.5z"/>',
    "clock": '<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/>',
    "arrow-right": '<path d="M5 12h14M13 6l6 6-6 6"/>',
    "arrow-left": '<path d="M19 12H5M11 6l-6 6 6 6"/>',
    "mail": '<rect x="3" y="5" width="18" height="14" rx="2.5"/><path d="m3.5 7 8.5 6 8.5-6"/>',
    "lock": '<rect x="4.5" y="10.5" width="15" height="10" rx="2.5"/><path d="M8 10.5V8a4 4 0 0 1 8 0v2.5"/>',
    "key": '<circle cx="8" cy="15" r="4"/><path d="m11 12 9-9"/><path d="m16 7 3 3"/>',
    "shield": '<path d="M12 3 4.5 6v5.5c0 4.4 3 7.9 7.5 9.5 4.5-1.6 7.5-5.1 7.5-9.5V6z"/><path d="m9 12 2.2 2.2L15.5 10"/>',
    "edit": '<path d="M4 20h4L19 9a2.1 2.1 0 0 0-3-3L5 17z"/><path d="m14 7 3 3"/>',
    "power": '<path d="M12 3v8"/><path d="M6.4 6.6a8 8 0 1 0 11.2 0"/>',
    "map-pin": '<path d="M12 21s7-6.2 7-11.5A7 7 0 0 0 5 9.5C5 14.8 12 21 12 21z"/><circle cx="12" cy="9.5" r="2.5"/>',
    "phone": '<path d="M5 4h3.2l1.6 4-2 1.3a11 11 0 0 0 5 5l1.3-2 4 1.6V17a2 2 0 0 1-2 2A14 14 0 0 1 3 6a2 2 0 0 1 2-2z"/>',
    "activity": '<path d="M3 12h4l3-8 4 16 3-8h4"/>',
    "list": '<path d="M8 6h13M8 12h13M8 18h13"/><circle cx="3.8" cy="6" r="1"/><circle cx="3.8" cy="12" r="1"/><circle cx="3.8" cy="18" r="1"/>',
    "clipboard": '<rect x="5" y="4" width="14" height="17" rx="2.5"/><path d="M9 4.5h6v2.5H9z"/><path d="M9 12h6M9 16h4"/>',
    "sparkles": '<path d="M12 3l1.8 4.7L18.5 9.5l-4.7 1.8L12 16l-1.8-4.7L5.5 9.5l4.7-1.8z"/><path d="M19 15l.8 2.2L22 18l-2.2.8L19 21l-.8-2.2L16 18l2.2-.8z"/>',
    "plus": '<path d="M12 5v14M5 12h14"/>',
    "minus": '<path d="M6 12h12"/>',
    "trash": '<path d="M4 7h16"/><path d="M9.5 7V4.5h5V7"/><path d="M6.5 7l1 13h9l1-13"/><path d="M10 11v5.5M14 11v5.5"/>',
    "search": '<circle cx="11" cy="11" r="6.5"/><path d="m20 20-4.2-4.2"/>',
    "history": '<path d="M3.5 12a8.5 8.5 0 1 0 2.5-6"/><path d="M3.5 4v4h4"/><path d="M12 8v4.5l3 2"/>',
    "chevron-left": '<path d="m15 6-6 6 6 6"/>',
    "chevron-right": '<path d="m9 6 6 6-6 6"/>',
    "send": '<path d="M21 3 10 14"/><path d="M21 3 14.5 21l-4.5-7-7-4.5z"/>',
    "message": '<path d="M4 5.5h16v11H9l-5 4z"/>',
    "instagram": '<rect x="3.5" y="3.5" width="17" height="17" rx="5"/><circle cx="12" cy="12" r="4"/><path d="M17.2 6.8h.01"/>',
    "facebook": '<path d="M14.5 21v-7.5h2.8l.4-3.2h-3.2V8.4c0-.9.3-1.6 1.6-1.6h1.7V4a20 20 0 0 0-2.5-.1c-2.5 0-4.1 1.5-4.1 4.2v2.2H8.4v3.2h2.8V21"/>',
    "linkedin": '<rect x="3.5" y="3.5" width="17" height="17" rx="3"/><path d="M8 10.5V16.5M8 7.5h.01"/><path d="M11.5 16.5v-6M11.5 13a2.5 2.5 0 0 1 5 0v3.5"/>',
    "whatsapp": '<path d="M4 20l1.2-4A8.5 8.5 0 1 1 8.4 19z"/><path d="M9 8.5c0 3.5 3 6.5 6.5 6.5l1-1.6-2.2-1-1 .9a5 5 0 0 1-2.6-2.6l.9-1-1-2.2z"/>'
  };

  const icon = (name, cls = "") =>
    `<svg class="icon${cls ? " " + cls : ""}" aria-hidden="true" focusable="false"><use href="#i-${name}"></use></svg>`;

  const mountSprite = () => {
    if (document.getElementById("ccam-sprite")) return;
    const symbols = Object.entries(ICONS)
      .map(([name, body]) => `<symbol id="i-${name}" viewBox="0 0 24 24">${body}</symbol>`)
      .join("");
    const wrap = document.createElement("div");
    wrap.id = "ccam-sprite";
    wrap.setAttribute("aria-hidden", "true");
    wrap.style.cssText = "position:absolute;width:0;height:0;overflow:hidden";
    wrap.innerHTML = `<svg xmlns="http://www.w3.org/2000/svg" width="0" height="0" focusable="false">${symbols}</svg>`;
    document.body.prepend(wrap);
  };

  const esc = (value) =>
    String(value ?? "").replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));

  const onlyDigits = (v) => String(v ?? "").replace(/\D/g, "");

  const friendlyError = (err) => {
    const msg = String(err?.message || err || "");
    if (err instanceof TypeError || /failed to fetch|networkerror|load failed/i.test(msg)) {
      return "Não foi possível conectar ao servidor. Verifique se o sistema está em execução e tente novamente.";
    }
    return msg || "Ocorreu um erro inesperado. Tente novamente.";
  };

  const setBusy = (btn, busy, busyLabel) => {
    if (!btn) return;
    if (busy) {
      btn.dataset.html = btn.innerHTML;
      btn.classList.add("is-loading");
      btn.disabled = true;
      if (busyLabel) btn.textContent = busyLabel;
    } else {
      if (btn.dataset.html !== undefined) btn.innerHTML = btn.dataset.html;
      btn.classList.remove("is-loading");
      btn.disabled = false;
    }
  };

  const stateBlock = ({ icon: name = "info", title, text = "", error = false }) => `
    <div class="empty${error ? " empty--error" : ""}">
      <span class="chip-icon">${icon(name)}</span>
      <strong>${esc(title)}</strong>
      ${text ? `<span>${esc(text)}</span>` : ""}
    </div>`;

  const formatCnpj = (v) => {
    const d = onlyDigits(v).slice(0, 14);
    let o = d.slice(0, 2);
    if (d.length > 2) o += "." + d.slice(2, 5);
    if (d.length > 5) o += "." + d.slice(5, 8);
    if (d.length > 8) o += "/" + d.slice(8, 12);
    if (d.length > 12) o += "-" + d.slice(12, 14);
    return o;
  };
  const formatCpf = (v) => {
    const d = onlyDigits(v).slice(0, 11);
    let o = d.slice(0, 3);
    if (d.length > 3) o += "." + d.slice(3, 6);
    if (d.length > 6) o += "." + d.slice(6, 9);
    if (d.length > 9) o += "-" + d.slice(9, 11);
    return o;
  };
  const formatCep = (v) => {
    const d = onlyDigits(v).slice(0, 8);
    return d.length > 5 ? `${d.slice(0, 5)}-${d.slice(5)}` : d;
  };
  const formatPhone = (v) => {
    const d = onlyDigits(v).slice(0, 11);
    if (d.length === 0) return "";
    if (d.length <= 2) return `(${d}`;
    if (d.length <= 6) return `(${d.slice(0, 2)}) ${d.slice(2)}`;
    if (d.length <= 10) return `(${d.slice(0, 2)}) ${d.slice(2, 6)}-${d.slice(6)}`;
    return `(${d.slice(0, 2)}) ${d.slice(2, 7)}-${d.slice(7)}`;
  };
  const MASKS = { cnpj: formatCnpj, cpf: formatCpf, cep: formatCep, phone: formatPhone };

  const wireMasks = () => {
    document.addEventListener("input", (e) => {
      const el = e.target;
      if (!(el instanceof HTMLInputElement)) return;
      const fn = MASKS[el.dataset.mask || ""];
      if (fn) el.value = fn(el.value);
    });
  };

  const initials = (name) => {
    const parts = String(name || "?").trim().split(/\s+/).filter(Boolean);
    const a = parts[0]?.[0] || "?";
    const b = parts.length > 1 ? parts[parts.length - 1][0] : "";
    return (a + b).toUpperCase();
  };

  const profileBadge = (perfil, rotulo) => {
    const cls = { ADMIN: "badge--danger", GESTOR: "badge--brand", PROFESSOR: "badge--ok", ATENDENTE: "badge--sky" }[perfil] || "";
    return `<span class="badge ${cls}">${esc(rotulo || perfil)}</span>`;
  };

  const formatDateTime = (iso) => {
    if (!iso) return "Nunca";
    const d = new Date(iso);
    return isNaN(d) ? "" : d.toLocaleString("pt-BR", { day: "2-digit", month: "2-digit", year: "numeric", hour: "2-digit", minute: "2-digit" });
  };

  const formatDate = (iso) => {
    if (!iso) return "--";
    const d = new Date(String(iso).length === 10 ? `${iso}T00:00:00` : iso);
    return isNaN(d) ? "" : d.toLocaleDateString("pt-BR");
  };

  const formatMoney = (v) => Number(v || 0).toLocaleString("pt-BR", { style: "currency", currency: "BRL" });

  const todayIso = () => {
    const d = new Date();
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
  };

  const debounce = (fn, ms = 350) => {
    let timer = 0;
    return (...args) => { clearTimeout(timer); timer = setTimeout(() => fn(...args), ms); };
  };

  const queryString = (params) => {
    const q = new URLSearchParams();
    Object.entries(params || {}).forEach(([k, v]) => {
      if (v !== undefined && v !== null && String(v) !== "") q.set(k, v);
    });
    const out = q.toString();
    return out ? `?${out}` : "";
  };

  const multiline = (text) => esc(text).replace(/\n/g, "<br>");

  const DIAS_SEMANA = ["Segunda-feira", "Terça-feira", "Quarta-feira", "Quinta-feira", "Sexta-feira", "Sábado", "Domingo", "Rotativo (Ficha)"];

  // Chamadas a API
  class ApiError extends Error {
    constructor(message, status) {
      super(message);
      this.status = status;
    }
  }

  const api = async (path, { method = "GET", body, auth = true } = {}) => {
    const headers = {};
    if (body !== undefined) headers["Content-Type"] = "application/json";
    const session = auth ? loadSession() : null;
    if (session?.token) headers["Authorization"] = `Bearer ${session.token}`;

    const res = await fetch(`${API_URL}${path}`, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined
    });

    const text = await res.text();
    let data = null;
    try { data = text ? JSON.parse(text) : null; } catch { data = null; }

    if (res.status === 401 && auth) {
      clearSession();
      setFlash({ type: "warning", title: "Sessão expirada", message: "Por segurança, faça login novamente." });
      location.replace("login.html");
      throw new ApiError("Sessão expirada.", 401);
    }
    if (!res.ok) {
      throw new ApiError(data?.erro || `O servidor respondeu com erro (código ${res.status}).`, res.status);
    }
    return data;
  };

  const fail = (title, err) => {
    if (err?.status === 401) return;
    ui.alert({ type: "error", title, message: friendlyError(err) });
  };

  // Modal de avisos e notificacoes
  const ui = (() => {
    const MODAL_ICONS = { success: "check-circle", error: "x-circle", warning: "alert-triangle", info: "info" };
    let current = null;

    const modal = ({ type = "info", title = "", message = "", confirmText = "Entendi", cancelText = "", danger = false }) =>
      new Promise((resolve) => {
        if (current) current.finish(false);

        const lastFocus = document.activeElement;
        const uid = `m${Date.now()}`;
        const okClass = danger ? "btn--danger" : type === "success" ? "btn--success" : "btn--brand";

        const backdrop = document.createElement("div");
        backdrop.className = "modal-backdrop";
        backdrop.innerHTML = `
          <div class="modal modal--${esc(type)}" role="alertdialog" aria-modal="true" aria-labelledby="${uid}-t" aria-describedby="${uid}-m">
            <div class="modal__icon">${icon(MODAL_ICONS[type] || "info")}</div>
            <h2 class="modal__title" id="${uid}-t">${esc(title)}</h2>
            <p class="modal__message" id="${uid}-m">${esc(message)}</p>
            <div class="modal__actions">
              ${cancelText ? `<button type="button" class="btn btn--ghost" data-act="cancel">${esc(cancelText)}</button>` : ""}
              <button type="button" class="btn ${okClass}" data-act="ok">${esc(confirmText)}</button>
            </div>
          </div>`;
        document.body.appendChild(backdrop);
        document.body.classList.add("modal-open");

        let done = false;
        const finish = (result) => {
          if (done) return;
          done = true;
          document.removeEventListener("keydown", onKey, true);
          if (current && current.backdrop === backdrop) current = null;
          backdrop.classList.add("is-closing");
          setTimeout(() => {
            backdrop.remove();
            if (!document.querySelector(".modal-backdrop:not(.is-closing)")) document.body.classList.remove("modal-open");
            if (lastFocus && typeof lastFocus.focus === "function") lastFocus.focus();
          }, 160);
          resolve(result);
        };

        const buttons = () => Array.from(backdrop.querySelectorAll("button"));
        const onKey = (e) => {
          if (e.key === "Escape") { e.preventDefault(); finish(false); return; }
          if (e.key !== "Tab") return;
          const list = buttons();
          const first = list[0];
          const last = list[list.length - 1];
          if (e.shiftKey && document.activeElement === first) { e.preventDefault(); last.focus(); }
          else if (!e.shiftKey && document.activeElement === last) { e.preventDefault(); first.focus(); }
        };

        backdrop.addEventListener("click", (e) => {
          if (e.target === backdrop) { finish(false); return; }
          const act = e.target instanceof Element ? e.target.closest("[data-act]") : null;
          if (act) finish(act.getAttribute("data-act") === "ok");
        });
        document.addEventListener("keydown", onKey, true);

        current = { backdrop, finish };
        backdrop.querySelector('[data-act="ok"]').focus();
      });

    const alert = (opts) => modal(opts).then(() => undefined);
    const confirm = (opts) => modal({ type: "warning", confirmText: "Confirmar", cancelText: "Cancelar", ...opts });

    const toast = ({ type = "success", title = "", message = "", duration = 4500 }) => {
      let host = document.querySelector(".toasts");
      if (!host) {
        host = document.createElement("div");
        host.className = "toasts";
        host.setAttribute("role", "region");
        host.setAttribute("aria-label", "Notificações");
        document.body.appendChild(host);
      }
      while (host.children.length >= 4) host.firstElementChild.remove();

      const el = document.createElement("div");
      el.className = `toast toast--${type}`;
      el.setAttribute("role", type === "error" ? "alert" : "status");
      el.style.setProperty("--dur", `${duration}ms`);
      el.innerHTML = `
        <div class="toast__icon">${icon(MODAL_ICONS[type] || "info")}</div>
        <div class="toast__body">
          ${title ? `<span class="toast__title">${esc(title)}</span>` : ""}
          ${message ? `<span class="toast__msg">${esc(message)}</span>` : ""}
        </div>
        <button type="button" class="toast__close" aria-label="Fechar notificação">${icon("x")}</button>
        <span class="toast__bar"></span>`;
      host.appendChild(el);

      let timer = 0;
      const dismiss = () => {
        clearTimeout(timer);
        el.classList.add("is-closing");
        setTimeout(() => el.remove(), 200);
      };
      el.querySelector(".toast__close").addEventListener("click", dismiss);
      timer = setTimeout(dismiss, duration);
    };

    const dialog = ({ title = "", html = "", confirmText = "Salvar", cancelText = "Cancelar", danger = false, wide = false, onConfirm, onOpen }) =>
      new Promise((resolve) => {
        const lastFocus = document.activeElement;
        const uid = `d${Date.now()}`;
        const backdrop = document.createElement("div");
        backdrop.className = "modal-backdrop";
        backdrop.innerHTML = `
          <div class="modal modal--form${wide ? " modal--wide" : ""}" role="dialog" aria-modal="true" aria-labelledby="${uid}-t">
            <div class="modal__head">
              <h2 class="modal__title" id="${uid}-t">${esc(title)}</h2>
              <button type="button" class="btn btn--ghost btn--icon" data-act="close" aria-label="Fechar">${icon("x")}</button>
            </div>
            <form class="modal__body" novalidate>
              ${html}
              <p class="form-alert" data-dialog-error role="alert" hidden></p>
              <div class="modal__actions">
                ${cancelText ? `<button type="button" class="btn btn--ghost" data-act="close">${esc(onConfirm ? cancelText : "Fechar")}</button>` : ""}
                ${onConfirm ? `<button type="submit" class="btn ${danger ? "btn--danger" : "btn--brand"}">${esc(confirmText)}</button>` : ""}
              </div>
            </form>
          </div>`;
        document.body.appendChild(backdrop);
        document.body.classList.add("modal-open");

        const form = backdrop.querySelector("form");
        const errorEl = backdrop.querySelector("[data-dialog-error]");
        let done = false;

        const finish = (result) => {
          if (done) return;
          done = true;
          document.removeEventListener("keydown", onKey, true);
          backdrop.classList.add("is-closing");
          setTimeout(() => {
            backdrop.remove();
            if (!document.querySelector(".modal-backdrop:not(.is-closing)")) document.body.classList.remove("modal-open");
            if (lastFocus && typeof lastFocus.focus === "function") lastFocus.focus();
          }, 160);
          resolve(result);
        };

        const onKey = (e) => {
          if (e.key === "Escape" && !document.querySelector(".modal--warning, .modal--error, .modal--success, .modal--info")) {
            e.preventDefault();
            finish(false);
          }
        };
        document.addEventListener("keydown", onKey, true);

        backdrop.addEventListener("mousedown", (e) => { if (e.target === backdrop) finish(false); });
        backdrop.addEventListener("click", (e) => {
          const act = e.target instanceof Element ? e.target.closest('[data-act="close"]') : null;
          if (act) finish(false);
        });

        form.addEventListener("submit", async (e) => {
          e.preventDefault();
          if (!onConfirm) return;
          errorEl.hidden = true;
          for (const field of form.querySelectorAll("[required]")) {
            if (String(field.value).trim() === "") {
              field.focus();
              errorEl.innerHTML = `${icon("alert-triangle")}<span>Preencha todos os campos obrigatórios.</span>`;
              errorEl.hidden = false;
              return;
            }
          }
          const btn = form.querySelector('button[type="submit"]');
          setBusy(btn, true, "Salvando...");
          try {
            const result = await onConfirm(form);
            finish(result === undefined ? true : result);
          } catch (err) {
            setBusy(btn, false);
            if (err?.status === 401) { finish(false); return; }
            errorEl.innerHTML = `${icon("alert-triangle")}<span>${esc(friendlyError(err))}</span>`;
            errorEl.hidden = false;
          }
        });

        if (onOpen) onOpen(form);
        const first = form.querySelector("input:not([type=hidden]):not([tabindex='-1']), select, textarea");
        (first || backdrop.querySelector('[data-act="close"]')).focus();
      });

    return { alert, confirm, toast, dialog };
  })();

  // Listagens paginadas com filtros
  const PAGE_SIZES = [5, 10, 20, 50, 100];

  const pagerMarkup = (pg) => {
    if (!pg || !pg.totalItens) return "";
    const atual = pg.pagina;
    const total = Math.max(1, pg.totalPaginas);
    const ini = atual * pg.tamanho + 1;
    const fim = Math.min(pg.totalItens, ini + pg.itens.length - 1);

    const nums = new Set([0, total - 1]);
    for (let i = atual - 1; i <= atual + 1; i++) if (i >= 0 && i < total) nums.add(i);
    let botoes = "";
    let anterior = -1;
    [...nums].sort((a, b) => a - b).forEach((n) => {
      if (n - anterior > 1) botoes += '<span class="pager__gap" aria-hidden="true">...</span>';
      botoes += `<button type="button" class="pager__num${n === atual ? " is-current" : ""}" data-page="${n}" ${n === atual ? 'aria-current="page"' : ""} aria-label="Página ${n + 1}">${n + 1}</button>`;
      anterior = n;
    });

    return `
      <div class="pager">
        <span class="pager__info">Mostrando ${ini} a ${fim} de ${pg.totalItens}</span>
        <nav class="pager__nav" aria-label="Paginação">
          <button type="button" class="pager__num" data-page="${atual - 1}" ${atual <= 0 ? "disabled" : ""} aria-label="Página anterior">${icon("chevron-left")}</button>
          ${botoes}
          <button type="button" class="pager__num" data-page="${atual + 1}" ${atual >= total - 1 ? "disabled" : ""} aria-label="Próxima página">${icon("chevron-right")}</button>
        </nav>
        <label class="pager__size">Por página
          <select data-page-size>${PAGE_SIZES.map((t) => `<option value="${t}" ${t === pg.tamanho ? "selected" : ""}>${t}</option>`).join("")}</select>
        </label>
      </div>`;
  };

  const pagedList = ({ pagerHost, scrollTarget, fetchPage, render, renderError, size = 10, filters = {} }) => {
    const iniciais = { ...filters };
    const state = { pagina: 0, tamanho: size, filtros: { ...filters }, seq: 0 };

    const load = async () => {
      const seq = ++state.seq;
      try {
        const pg = await fetchPage({ ...state.filtros, pagina: state.pagina, tamanho: state.tamanho });
        if (seq !== state.seq) return;
        if (pg.itens.length === 0 && pg.pagina > 0 && pg.totalItens > 0) {
          state.pagina = Math.max(0, pg.totalPaginas - 1);
          load();
          return;
        }
        render(pg, state.filtros);
        if (pagerHost) pagerHost.innerHTML = pagerMarkup(pg);
      } catch (err) {
        if (seq !== state.seq || err?.status === 401) return;
        renderError(err);
        if (pagerHost) pagerHost.innerHTML = "";
      }
    };

    if (pagerHost) {
      pagerHost.addEventListener("click", (e) => {
        const b = e.target instanceof Element ? e.target.closest("[data-page]") : null;
        if (!b || b.disabled) return;
        state.pagina = Number(b.dataset.page);
        load();
        scrollTarget?.scrollIntoView({ behavior: "smooth", block: "start" });
      });
      pagerHost.addEventListener("change", (e) => {
        if (!(e.target instanceof Element) || !e.target.matches("[data-page-size]")) return;
        state.tamanho = Number(e.target.value);
        state.pagina = 0;
        load();
      });
    }

    const setFilter = (name, value) => {
      state.filtros[name] = value;
      state.pagina = 0;
      load();
    };
    const reset = () => {
      state.filtros = { ...iniciais };
      state.pagina = 0;
      load();
    };
    return { load, setFilter, reset, state };
  };

  const wireFilters = (container, list) => {
    if (!container) return;
    const campos = Array.from(container.querySelectorAll("[data-filter]"));
    const aplicar = (el) => list.setFilter(el.dataset.filter, el.value.trim());
    const atrasado = debounce(aplicar, 350);
    campos.forEach((el) => {
      const texto = el.tagName === "INPUT" && ["search", "text"].includes(el.type);
      el.addEventListener(texto ? "input" : "change", () => (texto ? atrasado(el) : aplicar(el)));
    });
    container.querySelector("[data-filter-clear]")?.addEventListener("click", () => {
      campos.forEach((el) => {
        el.value = el.tagName === "SELECT" ? (el.querySelector("option[data-default]")?.value ?? el.options[0]?.value ?? "") : "";
      });
      list.reset();
    });
  };

  const linhaTabela = (cols, html) => `<tr><td colspan="${cols}" class="empty-cell">${html}</td></tr>`;

  // Estrutura comum
  const NAV_ITEMS = [
    { href: "index.html", label: "Início", icon: "home" },
    { href: "cadastro.html", label: "Alunos", icon: "user-plus" },
    { href: "treinos.html", label: "Treinos", icon: "dumbbell" },
    { href: "financeiro.html", label: "Financeiro", icon: "wallet" },
    { href: "empresa.html", label: "Empresa", icon: "building" },
    { href: "mural.html", label: "Mural", icon: "megaphone" },
    { href: "crm.html", label: "CRM", icon: "chart" },
    { href: "usuarios.html", label: "Usuários", icon: "users" },
    { href: "empresas.html", label: "Empresas", icon: "building" }
  ];

  const brandMarkup = () => `
    <span class="logo" aria-hidden="true"></span>
    <span class="brand__text">
      <span class="brand__name">CCAM</span>
      <span class="brand__tag">Controle &amp; acompanhamento</span>
    </span>`;

  const userMenuMarkup = (user) => `
    <div class="usermenu" data-usermenu>
      <button class="usermenu__btn" type="button" aria-haspopup="true" aria-expanded="false" aria-label="Menu do usuário">
        <span class="avatar" aria-hidden="true">${esc(initials(user.nome))}</span>
        ${icon("chevron-down")}
      </button>
      <div class="usermenu__panel" role="menu" hidden>
        <div class="usermenu__head">
          <strong>${esc(user.nome)}</strong>
          <span>${esc(user.email)}</span>
          ${user.empresaNome ? `<span>${esc(user.empresaNome)}</span>` : ""}
          ${profileBadge(user.perfil, user.perfilRotulo)}
        </div>
        <a href="conta.html" role="menuitem">${icon("user")}Minha conta</a>
        <button type="button" role="menuitem" data-logout>${icon("logout")}Sair</button>
      </div>
    </div>`;

  const mountChrome = () => {
    mountSprite();

    const isPublic = PUBLIC_PAGES.includes(getPath());
    const user = currentUser();

    const topbar = document.querySelector("[data-topbar]");
    if (topbar) {
      if (isPublic || !user) {
        topbar.innerHTML = `
          <div class="container topbar__inner">
            <div class="brand">${brandMarkup()}</div>
          </div>`;
      } else {
        const links = NAV_ITEMS
          .filter((item) => hasAnyPerm(PAGE_PERMS[item.href]))
          .map((item) => `<a href="${item.href}">${icon(item.icon)}<span>${item.label}</span></a>`)
          .join("");
        topbar.innerHTML = `
          <div class="container topbar__inner">
            <a class="brand" href="index.html" aria-label="CCAM, ir para o início">${brandMarkup()}</a>
            <nav class="nav" id="menu-principal" data-nav aria-label="Navegação principal">${links}</nav>
            <div class="topbar__actions">
              ${userMenuMarkup(user)}
              <button class="btn btn--ghost btn--icon menu-toggle" type="button" aria-label="Abrir menu" aria-expanded="false" aria-controls="menu-principal">${icon("menu")}</button>
            </div>
          </div>`;
      }
    }

    const footer = document.querySelector("[data-footer]");
    if (footer) {
      footer.innerHTML = `
        <div class="container footer__inner">
          <div class="brand"><span class="logo" aria-hidden="true"></span><span class="brand__name">CCAM</span></div>
          <div>Controle &amp; acompanhamento &middot; Gestão de academia</div>
        </div>`;
    }

    const page = document.querySelector("main .page");
    if (page && user && !isPublic && getPath() !== "index.html") {
      page.insertAdjacentHTML("afterbegin",
        `<a class="back-link" href="index.html">${icon("arrow-left")}Voltar ao menu inicial</a>`);
    }
  };

  const wireMenuToggle = () => {
    const toggle = document.querySelector(".menu-toggle");
    const nav = document.querySelector("[data-nav]");
    if (!toggle || !nav) return;
    const topbarEl = document.querySelector(".topbar");

    const setOpen = (open) => {
      nav.classList.toggle("is-open", open);
      toggle.setAttribute("aria-expanded", String(open));
      toggle.setAttribute("aria-label", open ? "Fechar menu" : "Abrir menu");
      toggle.innerHTML = icon(open ? "x" : "menu");
    };
    toggle.addEventListener("click", () => setOpen(!nav.classList.contains("is-open")));
    nav.addEventListener("click", (e) => { if (e.target instanceof Element && e.target.closest("a")) setOpen(false); });
    document.addEventListener("keydown", (e) => { if (e.key === "Escape") setOpen(false); });
    document.addEventListener("click", (e) => {
      if (!nav.classList.contains("is-open")) return;
      if (!e.composedPath().includes(topbarEl)) setOpen(false);
    });
  };

  const wireUserMenu = () => {
    const wrap = document.querySelector("[data-usermenu]");
    if (!wrap) return;
    const btn = wrap.querySelector(".usermenu__btn");
    const panel = wrap.querySelector(".usermenu__panel");

    const setOpen = (open) => {
      panel.hidden = !open;
      btn.setAttribute("aria-expanded", String(open));
    };
    btn.addEventListener("click", () => setOpen(panel.hidden));
    document.addEventListener("keydown", (e) => { if (e.key === "Escape") setOpen(false); });
    document.addEventListener("click", (e) => {
      if (panel.hidden) return;
      if (!e.composedPath().includes(wrap)) setOpen(false);
    });
  };

  const setActiveNav = () => {
    const path = getPath();
    document.querySelectorAll("[data-nav] a[href]").forEach((a) => {
      const href = a.getAttribute("href") || "";
      if (href === path) a.setAttribute("aria-current", "page");
      else a.removeAttribute("aria-current");
    });
  };

  const applyPermissions = () => {
    document.querySelectorAll("[data-perm]").forEach((el) => {
      const need = (el.getAttribute("data-perm") || "").split(",").map((s) => s.trim()).filter(Boolean);
      if (need.length && !need.some(hasPerm)) el.hidden = true;
    });
    document.querySelectorAll(".grid").forEach((grid) => {
      const visible = Array.from(grid.children).filter((c) => !c.hidden);
      if (grid.children.length > 1 && visible.length === 1) grid.classList.add("grid--single");
    });
  };

  const showFlash = () => {
    const flash = takeFlash();
    if (flash) ui.toast({ type: flash.type || "info", title: flash.title || "", message: flash.message || "", duration: 6000 });
  };

  const enforceAuth = () => {
    const path = getPath();
    const session = loadSession();
    const isPublic = PUBLIC_PAGES.includes(path);

    if (!session) {
      if (!isPublic) { location.replace("login.html"); return false; }
      return true;
    }
    if (path === "login.html") { location.replace("index.html"); return false; }

    const need = PAGE_PERMS[path];
    if (need && !hasAnyPerm(need)) {
      setFlash({ type: "warning", title: "Acesso restrito", message: "Seu perfil não tem permissão para acessar esta área." });
      location.replace("index.html");
      return false;
    }
    return true;
  };

  const wireLogout = () => {
    document.addEventListener("click", async (e) => {
      if (!(e.target instanceof Element) || !e.target.closest("[data-logout]")) return;
      e.preventDefault();
      try { await api("/auth/logout", { method: "POST" }); } catch {}
      clearSession();
      location.replace("login.html");
    });
  };

  const wireConfirmables = () => {
    document.addEventListener("click", async (e) => {
      const el = e.target instanceof Element ? e.target.closest("[data-confirm]") : null;
      if (!el || el.dataset.confirmed === "1") return;
      e.preventDefault();
      const ok = await ui.confirm({
        title: "Confirmar ação",
        message: el.getAttribute("data-confirm") || "Deseja continuar com esta ação?"
      });
      if (!ok) return;
      el.dataset.confirmed = "1";
      el.click();
      delete el.dataset.confirmed;
    });
  };

  const wireFormHints = () => {
    document.querySelectorAll("form[data-enhance]").forEach((form) => {
      form.addEventListener("submit", (e) => {
        for (const field of form.querySelectorAll("[required]")) {
          if (String(field.value).trim() === "") {
            e.preventDefault();
            field.focus();
            ui.alert({
              type: "warning",
              title: "Campos obrigatórios",
              message: "Preencha todos os campos obrigatórios antes de continuar."
            });
            return;
          }
        }
      });
    });
  };

  // Login, recuperacao e troca de senha
  const wirePasswordToggles = () => {
    document.querySelectorAll("[data-toggle-password]").forEach((toggle) => {
      toggle.addEventListener("click", () => {
        const input = toggle.closest(".input-wrap")?.querySelector("input");
        if (!input) return;
        const show = input.type === "password";
        input.type = show ? "text" : "password";
        toggle.setAttribute("aria-label", show ? "Ocultar senha" : "Mostrar senha");
        toggle.innerHTML = icon(show ? "eye-off" : "eye");
      });
    });
  };

  const showInlineError = (el, text) => {
    if (!el) return;
    el.innerHTML = `${icon("alert-triangle")}<span>${esc(text)}</span>`;
    el.hidden = false;
  };

  const wireLoginForm = () => {
    const form = document.getElementById("loginForm");
    if (!form) return;

    const emailEl = document.getElementById("email");
    const passEl = document.getElementById("password");
    const errorEl = document.getElementById("loginError");
    const clearError = () => { if (errorEl) errorEl.hidden = true; };
    emailEl?.addEventListener("input", clearError);
    passEl?.addEventListener("input", clearError);

    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      const btn = form.querySelector('button[type="submit"]');
      setBusy(btn, true, "Entrando...");
      try {
        const data = await api("/auth/login", {
          method: "POST",
          auth: false,
          body: { email: emailEl.value.trim(), senha: passEl.value }
        });
        saveSession({ token: data.token, expiraEm: data.expiraEm, usuario: data.usuario });
        location.replace("index.html");
      } catch (err) {
        setBusy(btn, false);
        showInlineError(errorEl, friendlyError(err));
      }
    });
  };

  const CONTATO_FORM = `
    <p class="help">Conte um pouco sobre a sua academia. Nossa equipe entra em contato para apresentar o CCAM e fazer o cadastro da empresa.</p>
    <div class="fgrid">
      <div class="field col-3"><label for="ct_nome">Seu nome <span class="req" aria-hidden="true">*</span></label>
        <input id="ct_nome" name="nome" type="text" required maxlength="120" autocomplete="name" placeholder="Nome e sobrenome" /></div>
      <div class="field col-3"><label for="ct_email">E-mail <span class="req" aria-hidden="true">*</span></label>
        <input id="ct_email" name="email" type="email" required maxlength="160" autocomplete="email" placeholder="voce@academia.com" /></div>
      <div class="field col-3"><label for="ct_tel">Telefone / WhatsApp</label>
        <input id="ct_tel" name="telefone" type="tel" data-mask="phone" autocomplete="tel" placeholder="(00) 00000-0000" /></div>
      <div class="field col-3"><label for="ct_academia">Nome da academia</label>
        <input id="ct_academia" name="academia" type="text" maxlength="160" autocomplete="organization" placeholder="Ex: Academia Movimento" /></div>
      <div class="field col-6"><label for="ct_cidade">Cidade / UF</label>
        <input id="ct_cidade" name="cidade" type="text" maxlength="120" placeholder="Ex: Campinas / SP" /></div>
      <div class="field col-6"><label for="ct_msg">Mensagem</label>
        <textarea id="ct_msg" name="mensagem" rows="4" maxlength="2000" placeholder="Quantos alunos, o que você procura, melhor horário para contato..."></textarea></div>
      <div class="hp" aria-hidden="true"><label for="ct_site">Site</label><input id="ct_site" name="site" type="text" tabindex="-1" autocomplete="off" /></div>
    </div>`;

  const wireContato = () => {
    const botoes = document.querySelectorAll("[data-contato]");
    if (!botoes.length) return;

    const abrir = async () => {
      const resposta = await ui.dialog({
        title: "Fale conosco",
        html: CONTATO_FORM,
        confirmText: "Enviar mensagem",
        wide: true,
        onConfirm: (form) => {
          const v = (n) => form.elements[n]?.value.trim() || null;
          return api("/contato", {
            method: "POST",
            auth: false,
            body: {
              nome: v("nome"), email: v("email"), telefone: v("telefone"), academia: v("academia"),
              cidade: v("cidade"), mensagem: v("mensagem"), site: v("site")
            }
          });
        }
      });
      if (resposta) {
        ui.alert({
          type: "success",
          title: "Mensagem enviada",
          message: resposta.mensagem || "Recebemos o seu contato. Em breve nossa equipe vai falar com você."
        });
      }
    };

    botoes.forEach((b) => b.addEventListener("click", (e) => { e.preventDefault(); abrir(); }));
    if (location.hash === "#contato") abrir();
  };

  const wireEsqueciSenha = () => {
    const form = document.getElementById("formEsqueci");
    if (!form) return;
    const result = document.getElementById("esqueci-resultado");
    const errorEl = document.getElementById("esqueciError");

    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      if (errorEl) errorEl.hidden = true;
      const btn = form.querySelector('button[type="submit"]');
      setBusy(btn, true, "Enviando...");
      try {
        const data = await api("/auth/esqueci-senha", {
          method: "POST",
          auth: false,
          body: { email: document.getElementById("email").value.trim() }
        });
        form.hidden = true;
        const dev = data?.tokenDesenvolvimento
          ? `<div class="callout">
               <strong>Modo desenvolvimento</strong>
               <span>O envio de e-mail ainda não está configurado neste ambiente. Use o botão abaixo para continuar.</span>
               <a class="btn btn--sm" href="redefinir-senha.html?token=${encodeURIComponent(data.tokenDesenvolvimento)}">${icon("key")}Abrir link de redefinição</a>
             </div>`
          : "";
        result.innerHTML = `
          <div class="callout callout--ok">
            <strong>Solicitação recebida</strong>
            <span>${esc(data?.mensagem || "Se o e-mail estiver cadastrado, enviaremos as instruções.")}</span>
          </div>${dev}
          <p class="help" style="margin-top:12px">Não chegou? Confira o spam ou, se for um usuário da academia, peça ao gestor para redefinir a sua senha.</p>`;
        result.hidden = false;
      } catch (err) {
        setBusy(btn, false);
        showInlineError(errorEl, friendlyError(err));
      }
    });
  };

  const wireRedefinirSenha = () => {
    const form = document.getElementById("formRedefinir");
    if (!form) return;
    const token = new URLSearchParams(location.search).get("token");
    const errorEl = document.getElementById("redefinirError");

    if (!token) {
      form.hidden = true;
      document.getElementById("redefinir-invalido").hidden = false;
      return;
    }

    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      if (errorEl) errorEl.hidden = true;
      const nova = document.getElementById("novaSenha").value;
      const conf = document.getElementById("confirmaSenha").value;
      if (nova !== conf) { showInlineError(errorEl, "As senhas digitadas não são iguais."); return; }

      const btn = form.querySelector('button[type="submit"]');
      setBusy(btn, true, "Salvando...");
      try {
        await api("/auth/redefinir-senha", { method: "POST", auth: false, body: { token, novaSenha: nova } });
        await ui.alert({
          type: "success",
          title: "Senha redefinida",
          message: "Sua senha foi alterada. Entre no sistema com a nova senha.",
          confirmText: "Ir para o login"
        });
        location.replace("login.html");
      } catch (err) {
        setBusy(btn, false);
        showInlineError(errorEl, friendlyError(err));
      }
    });
  };

  const wireConta = () => {
    const box = document.getElementById("conta-dados");
    const form = document.getElementById("formSenha");
    if (!box || !form) return;
    const user = currentUser();

    box.innerHTML = `
      <div class="account">
        <span class="avatar avatar--lg" aria-hidden="true">${esc(initials(user.nome))}</span>
        <div>
          <h2>${esc(user.nome)}</h2>
          <p class="muted">${esc(user.email)}</p>
        </div>
      </div>
      <dl class="dl">
        <div><dt>Categoria</dt><dd>${profileBadge(user.perfil, user.perfilRotulo)}${user.titular ? ' <span class="you-tag">Titular</span>' : ""}</dd></div>
        <div><dt>Academia</dt><dd>${esc(user.empresaNome || "Administração do sistema")}</dd></div>
      </dl>`;

    const errorEl = document.getElementById("senhaError");
    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      if (errorEl) errorEl.hidden = true;
      const atual = document.getElementById("senhaAtual").value;
      const nova = document.getElementById("novaSenha").value;
      const conf = document.getElementById("confirmaSenha").value;
      if (nova !== conf) { showInlineError(errorEl, "A confirmação não é igual à nova senha."); return; }

      const btn = form.querySelector('button[type="submit"]');
      setBusy(btn, true, "Salvando...");
      try {
        await api("/auth/alterar-senha", { method: "POST", body: { senhaAtual: atual, novaSenha: nova } });
        clearSession();
        setFlash({ type: "success", title: "Senha alterada", message: "Entre novamente com a nova senha." });
        location.replace("login.html");
      } catch (err) {
        setBusy(btn, false);
        if (err?.status !== 401) showInlineError(errorEl, friendlyError(err));
      }
    });
  };

  // Historico de alteracoes
  const ACAO_LOG = {
    CRIACAO: ["Cadastro", "badge--ok"],
    EDICAO: ["Alteração", "badge--sky"],
    INATIVACAO: ["Inativação", "badge--danger"],
    REATIVACAO: ["Reativação", "badge--ok"],
    EXCLUSAO: ["Exclusão", "badge--danger"]
  };
  const ENTIDADE_LOG = { ALUNO: "Cadastro", TREINO: "Treino", FINANCEIRO: "Financeiro", AVISO: "Aviso" };

  const abrirHistorico = (alunoId, nome) => ui.dialog({
    title: `Histórico: ${nome}`,
    wide: true,
    html: `
      <p class="help">Todas as alterações no cadastro, na ficha de treino e no financeiro deste aluno, com data e responsável.</p>
      <ol class="timeline" data-hist-lista><li class="msg-info">Carregando histórico...</li></ol>
      <div data-hist-pager></div>`,
    onOpen: (form) => {
      const listaEl = form.querySelector("[data-hist-lista]");
      pagedList({
        pagerHost: form.querySelector("[data-hist-pager]"),
        size: 10,
        fetchPage: (p) => api(`/clientes/${encodeURIComponent(alunoId)}/historico${queryString(p)}`),
        render: (pg) => {
          if (pg.itens.length === 0) {
            listaEl.innerHTML = `<li>${stateBlock({ icon: "history", title: "Sem registros", text: "Nenhuma alteração registrada para este aluno." })}</li>`;
            return;
          }
          listaEl.innerHTML = pg.itens.map((l) => {
            const [rotulo, cls] = ACAO_LOG[l.acao] || [l.acao, ""];
            return `
              <li class="timeline__item">
                <div class="timeline__head">
                  <span class="badge ${cls}">${esc(rotulo)}</span>
                  <span class="tag">${esc(ENTIDADE_LOG[l.entidade] || l.entidade)}</span>
                  <time datetime="${esc(l.dataHora)}">${esc(formatDateTime(l.dataHora))}</time>
                </div>
                <p class="timeline__desc">${multiline(l.descricao)}</p>
                <span class="timeline__who">Por ${esc(l.usuarioNome || "sistema")}</span>
              </li>`;
          }).join("");
        },
        renderError: (err) => {
          listaEl.innerHTML = `<li>${stateBlock({ icon: "x-circle", error: true, title: "Não foi possível carregar o histórico", text: friendlyError(err) })}</li>`;
        }
      }).load();
    }
  });

  // Alunos
  const wireAlunos = () => {
    const form = document.getElementById("formCadastro");
    const tbody = document.getElementById("alunos-lista");
    if (!form && !tbody) return;

    const podeEditar = hasPerm("ALUNOS_EDITAR");
    let editandoId = null;
    let lista = null;
    let itens = new Map();

    const title = document.getElementById("aluno-form-title");
    const subtitle = document.getElementById("aluno-form-sub");
    const btnSalvar = document.getElementById("btn-salvar-aluno");
    const btnCancelar = document.getElementById("btn-cancelar-aluno");
    const campo = (id) => document.getElementById(id);

    const modoCadastro = () => {
      if (!form) return;
      editandoId = null;
      form.reset();
      title.textContent = "Novo aluno";
      subtitle.innerHTML = 'Campos marcados com <span class="req" aria-hidden="true">*</span> são obrigatórios.';
      btnSalvar.innerHTML = `${icon("check")}Salvar cadastro`;
      btnCancelar.hidden = true;
      form.closest(".card")?.classList.remove("card--editing");
    };

    const modoEdicao = (a) => {
      editandoId = a.id;
      form.reset();
      campo("nome").value = a.nome || "";
      campo("cpf").value = formatCpf(a.cpf);
      campo("data_nascimento").value = a.dataNascimento || "";
      campo("sexo").value = a.sexo || "M";
      campo("email").value = a.email || "";
      campo("celular").value = formatPhone(a.telefone);
      campo("telefone_fixo").value = formatPhone(a.telefoneFixo);
      campo("peso").value = a.peso ?? "";
      campo("altura").value = a.altura ?? "";
      campo("condicao_saude").value = a.condicaoSaude || "";
      title.textContent = `Editando: ${a.nome}`;
      subtitle.textContent = `Cadastrado em ${formatDate(a.dataCadastro)}${a.dataAtualizacao ? `, última alteração em ${formatDateTime(a.dataAtualizacao)}` : ""}. As mudanças ficam registradas no histórico.`;
      btnSalvar.innerHTML = `${icon("check")}Salvar alterações`;
      btnCancelar.hidden = false;
      form.closest(".card")?.classList.add("card--editing");
      form.closest(".card")?.scrollIntoView({ behavior: "smooth", block: "start" });
      campo("nome").focus({ preventScroll: true });
    };

    if (form && podeEditar) {
      btnCancelar.addEventListener("click", modoCadastro);

      form.addEventListener("submit", async (e) => {
        e.preventDefault();
        setBusy(btnSalvar, true, "Salvando...");

        const valor = (id) => campo(id)?.value.trim() || null;
        const numero = (id) => { const n = parseFloat(campo(id)?.value); return isNaN(n) ? null : n; };

        const dadosCliente = {
          nome: valor("nome"),
          email: valor("email"),
          cpf: valor("cpf"),
          telefone: valor("celular"),
          telefoneFixo: valor("telefone_fixo"),
          dataNascimento: valor("data_nascimento"),
          peso: numero("peso"),
          altura: numero("altura"),
          sexo: valor("sexo"),
          condicaoSaude: valor("condicao_saude")
        };

        try {
          if (editandoId) {
            await api(`/clientes/${encodeURIComponent(editandoId)}`, { method: "PUT", body: dadosCliente });
            ui.toast({ type: "success", title: "Dados atualizados", message: `O cadastro de ${dadosCliente.nome} foi alterado.` });
          } else {
            await api("/clientes", { method: "POST", body: dadosCliente });
            ui.toast({ type: "success", title: "Cadastro concluído", message: `${dadosCliente.nome} foi cadastrado(a) com sucesso.` });
          }
          setBusy(btnSalvar, false);
          modoCadastro();
          lista?.load();
        } catch (err) {
          setBusy(btnSalvar, false);
          fail(editandoId ? "Não foi possível salvar as alterações" : "Não foi possível cadastrar", err);
        }
      });
      modoCadastro();
    }

    if (!tbody) return;
    const COLS = 6;

    const linha = (a) => `
      <tr class="${a.ativo ? "" : "is-inactive"}">
        <td class="col-name"><strong>${esc(a.nome)}</strong><span class="cell-sub">${esc(a.email)}</span></td>
        <td>${esc(formatCpf(a.cpf) || "--")}</td>
        <td>${esc(formatPhone(a.telefone) || "--")}</td>
        <td>${esc(formatDate(a.dataCadastro))}${a.dataAtualizacao ? `<span class="cell-sub">Alterado em ${esc(formatDate(a.dataAtualizacao))}</span>` : ""}</td>
        <td><span class="badge ${a.ativo ? "badge--ok" : "badge--danger"}">${a.ativo ? "Ativo" : "Inativo"}</span></td>
        <td class="num"><div class="row-actions">
          ${podeEditar ? `<button type="button" class="btn btn--sm" data-acao="editar" data-id="${esc(a.id)}">${icon("edit")}Editar</button>` : ""}
          <button type="button" class="btn btn--sm btn--ghost" data-acao="historico" data-id="${esc(a.id)}">${icon("history")}Histórico</button>
          ${podeEditar ? `<button type="button" class="btn btn--sm ${a.ativo ? "btn--danger" : "btn--success"}" data-acao="ativo" data-id="${esc(a.id)}">${icon("power")}${a.ativo ? "Inativar" : "Reativar"}</button>` : ""}
        </div></td>
      </tr>`;

    lista = pagedList({
      pagerHost: document.getElementById("alunos-paginacao"),
      scrollTarget: document.getElementById("alunos-card"),
      filters: { busca: "", status: "ativos" },
      fetchPage: (p) => api(`/clientes/busca${queryString(p)}`),
      render: (pg, f) => {
        itens = new Map(pg.itens.map((a) => [a.id, a]));
        if (pg.itens.length === 0) {
          const titulo = f.busca ? "Nenhum aluno encontrado"
            : f.status === "inativos" ? "Nenhum aluno inativo" : "Nenhum aluno cadastrado";
          const texto = f.busca ? "Confira o termo digitado ou mude o filtro de situação."
            : f.status === "inativos" ? "Os alunos inativados aparecem aqui, com todo o histórico." : "Use o formulário acima para cadastrar o primeiro aluno.";
          tbody.innerHTML = linhaTabela(COLS, stateBlock({ icon: "users", title: titulo, text: texto }));
          return;
        }
        tbody.innerHTML = pg.itens.map(linha).join("");
      },
      renderError: (err) => {
        tbody.innerHTML = linhaTabela(COLS, stateBlock({ icon: "x-circle", error: true, title: "Não foi possível carregar os alunos", text: friendlyError(err) }));
      }
    });
    wireFilters(document.getElementById("alunos-filtros"), lista);

    tbody.addEventListener("click", async (e) => {
      const alvo = e.target instanceof Element ? e.target.closest("[data-acao]") : null;
      if (!alvo) return;
      const id = alvo.dataset.id;
      const aluno = itens.get(id);
      const acao = alvo.dataset.acao;

      if (acao === "historico") {
        abrirHistorico(id, aluno?.nome || "aluno");
        return;
      }
      if (acao === "editar") {
        try {
          modoEdicao(await api(`/clientes/${encodeURIComponent(id)}`));
        } catch (err) {
          fail("Não foi possível abrir o cadastro", err);
        }
        return;
      }
      if (acao === "ativo" && aluno) {
        const inativar = aluno.ativo;
        const ok = await ui.confirm({
          title: inativar ? "Inativar aluno?" : "Reativar aluno?",
          message: inativar
            ? `${aluno.nome} sai da lista de alunos atuais e não consegue mais entrar no aplicativo. Nada é apagado: o cadastro e o histórico continuam guardados.`
            : `${aluno.nome} volta para a lista de alunos atuais.`,
          confirmText: inativar ? "Inativar" : "Reativar",
          danger: inativar
        });
        if (!ok) return;
        try {
          await api(`/clientes/${encodeURIComponent(id)}/ativo`, { method: "PATCH", body: { ativo: !inativar } });
          ui.toast({ type: "success", title: inativar ? "Aluno inativado" : "Aluno reativado", message: aluno.nome });
          if (editandoId === id) modoCadastro();
          lista.load();
        } catch (err) {
          fail("Não foi possível alterar a situação do aluno", err);
        }
      }
    });

    lista.load();
  };

  // Treinos
  const wireTreinoForm = async () => {
    const selectAluno = document.getElementById("select-aluno");
    const formTreino = document.getElementById("formTreino");
    const catalogoContainer = document.getElementById("catalogo-container");
    const exercicioDisplay = document.getElementById("exercicio-display");
    if (!selectAluno || !formTreino || !catalogoContainer || !hasPerm("TREINOS_EDITAR")) return;

    let catalogoCompleto = [];
    let idExercicioSelecionado = null;

    try {
      const alunos = await api("/clientes");
      selectAluno.innerHTML = alunos.length
        ? '<option value="" disabled selected>Selecione um aluno...</option>'
        : '<option value="" disabled selected>Nenhum aluno cadastrado</option>';
      alunos.forEach((a) => { selectAluno.innerHTML += `<option value="${esc(a.id)}">${esc(a.nome)}</option>`; });
    } catch (err) {
      console.error("Erro alunos:", err);
      selectAluno.innerHTML = '<option value="" disabled selected>Erro ao carregar alunos</option>';
    }

    try {
      const catalogo = await api("/catalogo");
      catalogoCompleto = catalogo;

      if (!catalogo || catalogo.length === 0) {
        catalogoContainer.innerHTML = stateBlock({
          icon: "list",
          title: "Catálogo vazio",
          text: "Nenhum exercício cadastrado. Verifique o arquivo data.sql no backend."
        });
      } else {
        const grupos = {};
        catalogo.forEach((ex) => {
          const g = ex.grupoMuscular || "Geral";
          (grupos[g] = grupos[g] || []).push(ex);
        });

        catalogoContainer.innerHTML = `<div class="acc-group">${Object.entries(grupos).map(([grupo, exercicios]) => `
          <details class="acc">
            <summary class="acc__sum">
              <span class="acc__title">${icon("dumbbell")}${esc(grupo.toUpperCase())}</span>
              <span class="acc__count">${exercicios.length}</span>
              ${icon("chevron-down", "acc__chev")}
            </summary>
            <ul class="ex-list">
              ${exercicios.map((ex) => `<li><button type="button" class="exercicio-item" data-id="${esc(ex.id)}">${esc(ex.nome)}</button></li>`).join("")}
            </ul>
          </details>`).join("")}</div>`;

        catalogoContainer.querySelectorAll(".exercicio-item").forEach((item) => {
          item.addEventListener("click", () => {
            catalogoContainer.querySelectorAll(".exercicio-item").forEach((el) => el.classList.remove("selecionado"));
            item.classList.add("selecionado");
            idExercicioSelecionado = item.getAttribute("data-id");
            const exData = catalogoCompleto.find((x) => String(x.id) === String(idExercicioSelecionado));
            if (exercicioDisplay && exData) {
              exercicioDisplay.classList.add("is-filled");
              exercicioDisplay.innerHTML = `<span class="tag">${esc(exData.grupoMuscular)}</span><strong>${esc(exData.nome)}</strong>`;
            }
          });
        });
      }
    } catch (err) {
      console.error("Erro catalogo:", err);
      catalogoContainer.innerHTML = stateBlock({
        icon: "x-circle",
        error: true,
        title: "Não foi possível carregar o catálogo",
        text: "Verifique se o sistema está em execução e atualize a página."
      });
    }

    selectAluno.addEventListener("change", () => {
      document.dispatchEvent(new CustomEvent("ccam:aluno-escolhido", { detail: { alunoId: selectAluno.value } }));
    });

    formTreino.addEventListener("submit", async (e) => {
      e.preventDefault();
      if (!idExercicioSelecionado) {
        ui.alert({
          type: "warning",
          title: "Selecione um exercício",
          message: "Escolha um exercício no catálogo antes de adicionar à ficha."
        });
        return;
      }

      const btn = formTreino.querySelector('button[type="submit"]');
      setBusy(btn, true, "Salvando...");

      try {
        const dadosCat = catalogoCompleto.find((ex) => String(ex.id) === String(idExercicioSelecionado));
        const prescricao = {
          clienteId: selectAluno.value,
          grupoMuscular: dadosCat.grupoMuscular,
          nomeExercicio: dadosCat.nome,
          imagemUrl: dadosCat.imagemUrl,
          series: document.getElementById("series").value,
          repeticoes: document.getElementById("repeticoes").value,
          carga: document.getElementById("carga").value,
          diaSemana: document.getElementById("dia_semana")?.value || "Geral"
        };

        await api("/exercicios", { method: "POST", body: prescricao });
        const alunoId = selectAluno.value;
        formTreino.reset();
        selectAluno.value = alunoId;
        document.dispatchEvent(new CustomEvent("ccam:ficha-alterada", { detail: { alunoId } }));
        ui.toast({ type: "success", title: "Exercício adicionado", message: `${dadosCat.nome} foi incluído na ficha do aluno.` });
      } catch (err) {
        fail("Não foi possível salvar", err);
      } finally {
        setBusy(btn, false);
      }
    });
  };

  // Treinos
  const exercicioFormHtml = (ex) => `
    <p class="help"><strong>${esc(ex.nomeExercicio)}</strong>${ex.grupoMuscular ? ` (${esc(ex.grupoMuscular)})` : ""}</p>
    <div class="fgrid">
      <div class="field col-3"><label for="ed_dia">Dia da semana <span class="req" aria-hidden="true">*</span></label>
        <select id="ed_dia" name="diaSemana" required>${DIAS_SEMANA.map((d) => `<option value="${esc(d)}" ${d === ex.diaSemana ? "selected" : ""}>${esc(d)}</option>`).join("")}${DIAS_SEMANA.includes(ex.diaSemana) || !ex.diaSemana ? "" : `<option value="${esc(ex.diaSemana)}" selected>${esc(ex.diaSemana)}</option>`}</select></div>
      <div class="field col-3"><label for="ed_series">Séries <span class="req" aria-hidden="true">*</span></label>
        <input id="ed_series" name="series" type="number" min="1" required value="${esc(ex.series)}" /></div>
      <div class="field col-3"><label for="ed_rep">Repetições <span class="req" aria-hidden="true">*</span></label>
        <input id="ed_rep" name="repeticoes" type="text" required value="${esc(ex.repeticoes)}" /></div>
      <div class="field col-3"><label for="ed_carga">Carga</label>
        <input id="ed_carga" name="carga" type="text" value="${esc(ex.carga)}" placeholder="Ex: 20kg" /></div>
    </div>`;

  const wireVisualizarTreinos = async () => {
    const selectView = document.getElementById("select-aluno-view");
    const displayDiv = document.getElementById("treino-aluno-display");
    if (!selectView || !displayDiv) return;

    const podeEditar = hasPerm("TREINOS_EDITAR");
    let treinosAtuais = [];

    try {
      const alunos = await api("/clientes");
      if (alunos && alunos.length > 0) {
        selectView.innerHTML = '<option value="" disabled selected>Escolha um aluno para ver a ficha...</option>';
        alunos.forEach((a) => { selectView.innerHTML += `<option value="${esc(a.id)}">${esc(a.nome)}</option>`; });
      } else {
        selectView.innerHTML = '<option value="" disabled selected>Nenhum aluno cadastrado no sistema</option>';
      }
    } catch (err) {
      console.error("Erro alunos visualizacao:", err);
      selectView.innerHTML = '<option value="" disabled selected>Erro ao carregar alunos</option>';
    }

    const carregarFicha = async (alunoId) => {
      if (!alunoId) return;
      displayDiv.innerHTML = '<p class="msg-info">Buscando a ficha do aluno...</p>';

      try {
        const treinos = await api(`/exercicios/aluno/${encodeURIComponent(alunoId)}`);
        treinosAtuais = treinos || [];

        if (treinosAtuais.length === 0) {
          displayDiv.innerHTML = stateBlock({
            icon: "clipboard",
            title: "Ficha vazia",
            text: "Este aluno ainda não possui exercícios na ficha."
          });
          return;
        }

        const ordem = (d) => { const i = DIAS_SEMANA.indexOf(d); return i < 0 ? 99 : i; };
        const grupos = {};
        [...treinosAtuais]
          .sort((a, b) => ordem(a.diaSemana) - ordem(b.diaSemana) || String(a.grupoMuscular).localeCompare(String(b.grupoMuscular)))
          .forEach((ex) => {
            const g = ex.diaSemana || "Geral";
            (grupos[g] = grupos[g] || []).push(ex);
          });

        const blocos = Object.entries(grupos).map(([dia, exercicios]) => `
          <div class="ficha-grupo">
            <h4 class="ficha-grupo__title">${esc(dia.toUpperCase())}<span class="count">${exercicios.length}</span></h4>
            <div class="grid grid--3">
              ${exercicios.map((ex) => `
                <div class="ficha-card" data-id="${esc(ex.id)}">
                  <span class="ficha-card-nome">${esc(ex.nomeExercicio)}</span>
                  <div class="ficha-card-detalhes">
                    <div class="meta">${icon("dumbbell")}<span>${esc(ex.grupoMuscular || "Geral")}</span></div>
                    <div class="meta">${icon("repeat")}<span>${esc(ex.series)} séries de ${esc(ex.repeticoes)}</span></div>
                    <div class="meta">${icon("weight")}<span>Carga: ${esc(ex.carga || "Livre")}</span></div>
                  </div>
                  ${podeEditar ? `
                  <div class="ficha-card__actions">
                    <button type="button" class="btn btn--sm" data-treino-editar="${esc(ex.id)}">${icon("edit")}Editar</button>
                    <button type="button" class="btn btn--sm btn--danger" data-treino-remover="${esc(ex.id)}">${icon("trash")}Remover</button>
                  </div>` : ""}
                </div>`).join("")}
            </div>
          </div>`).join("");

        displayDiv.innerHTML = `
          <div class="ficha-head">
            <h3 class="ficha-titulo">Ficha do aluno</h3>
            <span class="pill">${treinosAtuais.length} ${treinosAtuais.length === 1 ? "exercício" : "exercícios"}</span>
          </div>${blocos}`;
      } catch (err) {
        if (err?.status === 401) return;
        displayDiv.innerHTML = stateBlock({ icon: "x-circle", error: true, title: "Erro ao carregar a ficha", text: friendlyError(err) });
      }
    };

    selectView.addEventListener("change", (e) => carregarFicha(e.target.value));

    document.addEventListener("ccam:aluno-escolhido", (e) => {
      const id = e.detail?.alunoId;
      if (!id || selectView.value === id) return;
      selectView.value = id;
      carregarFicha(id);
    });
    document.addEventListener("ccam:ficha-alterada", (e) => {
      if (e.detail?.alunoId && e.detail.alunoId === selectView.value) carregarFicha(selectView.value);
    });

    if (!podeEditar) return;
    displayDiv.addEventListener("click", async (e) => {
      const alvo = e.target instanceof Element ? e.target.closest("[data-treino-editar],[data-treino-remover]") : null;
      if (!alvo) return;
      const id = alvo.dataset.treinoEditar || alvo.dataset.treinoRemover;
      const ex = treinosAtuais.find((t) => String(t.id) === String(id));
      if (!ex) return;

      if (alvo.hasAttribute("data-treino-editar")) {
        const salvo = await ui.dialog({
          title: "Editar exercício",
          html: exercicioFormHtml(ex),
          confirmText: "Salvar alterações",
          onConfirm: (form) => api(`/exercicios/${encodeURIComponent(id)}`, {
            method: "PUT",
            body: {
              diaSemana: form.elements.diaSemana.value,
              series: form.elements.series.value.trim(),
              repeticoes: form.elements.repeticoes.value.trim(),
              carga: form.elements.carga.value.trim()
            }
          })
        });
        if (salvo) {
          ui.toast({ type: "success", title: "Exercício atualizado", message: ex.nomeExercicio });
          carregarFicha(selectView.value);
        }
        return;
      }

      const ok = await ui.confirm({
        title: "Remover exercício?",
        message: `${ex.nomeExercicio} (${ex.diaSemana}) sai da ficha do aluno. A remoção fica registrada no histórico.`,
        confirmText: "Remover",
        danger: true
      });
      if (!ok) return;
      try {
        await api(`/exercicios/${encodeURIComponent(id)}`, { method: "DELETE" });
        ui.toast({ type: "success", title: "Exercício removido", message: ex.nomeExercicio });
        carregarFicha(selectView.value);
      } catch (err) {
        fail("Não foi possível remover", err);
      }
    });
  };

  // Mural de avisos
  const avisoFormHtml = (a = {}) => `
    <div class="field"><label for="ed_titulo">Título do aviso <span class="req" aria-hidden="true">*</span></label>
      <input id="ed_titulo" name="titulo" type="text" maxlength="255" required value="${esc(a.titulo)}" /></div>
    <div class="field"><label for="ed_mensagem">Mensagem <span class="req" aria-hidden="true">*</span></label>
      <textarea id="ed_mensagem" name="mensagem" rows="6" required>${esc(a.mensagem)}</textarea></div>`;

  const wireMural = async () => {
    const formMural = document.getElementById("formMural");
    const listaAvisos = document.getElementById("lista-avisos");
    if (!formMural || !listaAvisos) return;

    const podePublicar = hasPerm("MURAL_PUBLICAR");
    let itens = new Map();
    const bloco = (html) => `<div class="card span-2">${html}</div>`;

    const avisoHtml = (aviso) => {
      const data = new Date(aviso.dataPublicacao);
      const dataFormatada = isNaN(data) ? "" : data.toLocaleDateString("pt-BR", { day: "2-digit", month: "long", year: "numeric" });
      const extras = [
        aviso.autorNome ? `por ${esc(aviso.autorNome)}` : "",
        aviso.dataAtualizacao ? `editado em ${esc(formatDateTime(aviso.dataAtualizacao))}` : ""
      ].filter(Boolean).join(" · ");
      return `
        <article class="notice">
          <div class="notice__head">
            <span class="chip-icon chip-icon--sky">${icon("megaphone")}</span>
            <h3>${esc(aviso.titulo)}</h3>
          </div>
          <p class="notice__msg">${esc(aviso.mensagem)}</p>
          <div class="notice__foot">
            ${icon("calendar")}<span>Publicado em ${esc(dataFormatada)}${extras ? ` ${extras}` : ""}</span>
            ${podePublicar ? `
            <div class="notice__actions">
              <button type="button" class="btn btn--sm" data-aviso-editar="${esc(aviso.id)}">${icon("edit")}Editar</button>
              <button type="button" class="btn btn--sm btn--danger" data-aviso-apagar="${esc(aviso.id)}">${icon("trash")}Apagar</button>
            </div>` : ""}
          </div>
        </article>`;
    };

    const lista = pagedList({
      pagerHost: document.getElementById("avisos-paginacao"),
      scrollTarget: document.getElementById("avisos-titulo"),
      size: 10,
      filters: { busca: "", mes: "" },
      fetchPage: (p) => api(`/mural${queryString(p)}`),
      render: (pg, f) => {
        itens = new Map(pg.itens.map((a) => [a.id, a]));
        if (pg.itens.length === 0) {
          const filtrando = f.busca || f.mes;
          listaAvisos.innerHTML = bloco(stateBlock({
            icon: "megaphone",
            title: filtrando ? "Nenhum aviso encontrado" : "Nenhum aviso publicado",
            text: filtrando ? "Tente outro termo ou outro mês." : "Os comunicados publicados aparecem aqui."
          }));
          return;
        }
        listaAvisos.innerHTML = pg.itens.map(avisoHtml).join("");
      },
      renderError: (err) => {
        console.error("Erro ao carregar mural:", err);
        listaAvisos.innerHTML = bloco(stateBlock({
          icon: "x-circle",
          error: true,
          title: "Não foi possível carregar o mural",
          text: friendlyError(err)
        }));
      }
    });
    wireFilters(document.getElementById("avisos-filtros"), lista);

    if (podePublicar) {
      formMural.addEventListener("submit", async (e) => {
        e.preventDefault();
        const titulo = document.getElementById("aviso_titulo").value;
        const mensagem = document.getElementById("aviso_mensagem").value;
        const btn = formMural.querySelector('button[type="submit"]');
        setBusy(btn, true, "Publicando...");

        try {
          await api("/mural", { method: "POST", body: { titulo, mensagem } });
          formMural.reset();
          ui.toast({ type: "success", title: "Aviso publicado", message: "O comunicado já está disponível no mural." });
          lista.load();
        } catch (err) {
          fail("Não foi possível publicar", err);
        } finally {
          setBusy(btn, false);
        }
      });

      listaAvisos.addEventListener("click", async (e) => {
        const alvo = e.target instanceof Element ? e.target.closest("[data-aviso-editar],[data-aviso-apagar]") : null;
        if (!alvo) return;
        const id = alvo.dataset.avisoEditar || alvo.dataset.avisoApagar;
        const aviso = itens.get(id);
        if (!aviso) return;

        if (alvo.hasAttribute("data-aviso-editar")) {
          const salvo = await ui.dialog({
            title: "Editar aviso",
            html: avisoFormHtml(aviso),
            confirmText: "Salvar alterações",
            onConfirm: (form) => api(`/mural/${encodeURIComponent(id)}`, {
              method: "PUT",
              body: { titulo: form.elements.titulo.value.trim(), mensagem: form.elements.mensagem.value }
            })
          });
          if (salvo) {
            ui.toast({ type: "success", title: "Aviso atualizado", message: salvo.titulo || aviso.titulo });
            lista.load();
          }
          return;
        }

        const ok = await ui.confirm({
          title: "Apagar aviso?",
          message: `"${aviso.titulo}" sai do mural. Esta ação não pode ser desfeita.`,
          confirmText: "Apagar",
          danger: true
        });
        if (!ok) return;
        try {
          await api(`/mural/${encodeURIComponent(id)}`, { method: "DELETE" });
          ui.toast({ type: "success", title: "Aviso apagado", message: aviso.titulo });
          lista.load();
        } catch (err) {
          fail("Não foi possível apagar o aviso", err);
        }
      });
    }

    lista.load();
  };

  // CRM
  const loadCRMData = async () => {
    const kpiTotal = document.getElementById("kpi-total-alunos");
    if (!kpiTotal) return;
    try {
      const clientes = await api("/clientes");
      if (clientes) kpiTotal.textContent = clientes.length;
    } catch (err) {
      console.error("Erro CRM:", err);
      kpiTotal.textContent = "--";
    }
  };

  // Financeiro
  const financeiroFormHtml = (l) => `
    <p class="help">Aluno: <strong>${esc(l.alunoNome)}</strong>. A alteração fica registrada no histórico do aluno.</p>
    <div class="fgrid">
      <div class="field col-2"><label for="ed_valor">Valor (R$) <span class="req" aria-hidden="true">*</span></label>
        <input id="ed_valor" name="valor" type="number" step="0.01" min="0.01" inputmode="decimal" required value="${esc(l.valor)}" /></div>
      <div class="field col-2"><label for="ed_venc">Vencimento <span class="req" aria-hidden="true">*</span></label>
        <input id="ed_venc" name="vencimento" type="date" required value="${esc(l.vencimento)}" /></div>
      <div class="field col-2"><label for="ed_status">Status <span class="req" aria-hidden="true">*</span></label>
        <select id="ed_status" name="status" required>
          <option value="Pendente" ${l.status === "Pendente" ? "selected" : ""}>Pendente</option>
          <option value="Pago" ${l.status === "Pago" ? "selected" : ""}>Pago</option>
        </select></div>
    </div>`;

  const wireFinanceiro = async () => {
    const form = document.getElementById("formFinanceiro");
    const tabela = document.getElementById("tabela-financeiro");
    const selectAluno = document.getElementById("aluno_select");
    if (!form || !tabela || !selectAluno) return;

    const podeLancar = hasPerm("FINANCEIRO_LANCAR");
    const podeEditar = hasPerm("FINANCEIRO_EDITAR");
    const COLS = 5;
    let itens = new Map();

    const carregarAlunos = async () => {
      if (!podeLancar) return;
      try {
        const alunos = await api("/clientes");
        const selecionado = selectAluno.value;
        selectAluno.innerHTML = alunos.length
          ? '<option value="" disabled selected>Selecione um aluno...</option>'
          : '<option value="" disabled selected>Nenhum aluno ativo cadastrado</option>';
        alunos.forEach((a) => { selectAluno.innerHTML += `<option value="${esc(a.id)}">${esc(a.nome)}</option>`; });
        if (selecionado) selectAluno.value = selecionado;
      } catch (err) {
        if (err?.status !== 401) selectAluno.innerHTML = '<option value="" disabled selected>Erro ao carregar alunos</option>';
      }
    };

    const linha = (l) => {
      const pago = l.status === "Pago";
      const vencido = !pago && l.vencimento < todayIso();
      const badge = `<span class="badge ${pago ? "badge--ok" : vencido ? "badge--danger" : "badge--warn"}">${esc(l.status)}</span>`;
      const acoes = [
        !pago && podeLancar ? `<button type="button" class="btn btn--sm btn--success" data-fin="pagar" data-id="${esc(l.id)}" title="Marcar como pago">${icon("check")}Pago</button>` : "",
        podeEditar ? `<button type="button" class="btn btn--sm btn--icon-sm" data-fin="editar" data-id="${esc(l.id)}" title="Editar lançamento" aria-label="Editar lançamento">${icon("edit")}</button>` : "",
        podeEditar ? `<button type="button" class="btn btn--sm btn--icon-sm btn--danger" data-fin="excluir" data-id="${esc(l.id)}" title="Excluir lançamento" aria-label="Excluir lançamento">${icon("trash")}</button>` : ""
      ].join("");
      return `<tr>
        <td class="col-name"><strong>${esc(l.alunoNome)}</strong>${l.alunoAtivo ? "" : '<span class="cell-sub">Aluno inativo</span>'}</td>
        <td class="num">${esc(formatMoney(l.valor))}</td>
        <td>${esc(formatDate(l.vencimento))}${vencido ? '<span class="cell-sub">Vencida</span>' : ""}</td>
        <td>${badge}</td>
        <td class="num"><div class="row-actions">${acoes || '<span class="muted">--</span>'}</div></td>
      </tr>`;
    };

    const lista = pagedList({
      pagerHost: document.getElementById("financeiro-paginacao"),
      scrollTarget: document.getElementById("financeiro-card"),
      filters: { busca: "", status: "", mes: "" },
      fetchPage: (p) => api(`/financeiro${queryString(p)}`),
      render: (pg, f) => {
        itens = new Map(pg.itens.map((l) => [l.id, l]));
        if (pg.itens.length === 0) {
          const filtrando = f.busca || f.status || f.mes;
          tabela.innerHTML = linhaTabela(COLS, stateBlock({
            icon: "wallet",
            title: filtrando ? "Nenhum lançamento encontrado" : "Nenhum lançamento ainda",
            text: filtrando ? "Mude os filtros para ver outros lançamentos." : "As mensalidades geradas aparecem aqui."
          }));
          return;
        }
        tabela.innerHTML = pg.itens.map(linha).join("");
      },
      renderError: (err) => {
        console.error("Erro financeiro:", err);
        tabela.innerHTML = linhaTabela(COLS, stateBlock({
          icon: "x-circle",
          error: true,
          title: "Não foi possível carregar os lançamentos",
          text: friendlyError(err)
        }));
      }
    });
    wireFilters(document.getElementById("financeiro-filtros"), lista);

    if (podeLancar) {
      form.addEventListener("submit", async (e) => {
        e.preventDefault();
        const btn = form.querySelector('button[type="submit"]');
        setBusy(btn, true, "Gerando...");
        try {
          await api("/financeiro", {
            method: "POST",
            body: {
              clienteId: selectAluno.value,
              valor: parseFloat(document.getElementById("fin_valor").value),
              vencimento: document.getElementById("fin_vencimento").value,
              status: "Pendente"
            }
          });
          form.reset();
          ui.toast({ type: "success", title: "Cobrança gerada", message: "A mensalidade foi lançada com status pendente." });
          lista.load();
        } catch (err) {
          fail("Não foi possível gerar a cobrança", err);
        } finally {
          setBusy(btn, false);
        }
      });
    }

    tabela.addEventListener("click", async (e) => {
      const alvo = e.target instanceof Element ? e.target.closest("[data-fin]") : null;
      if (!alvo) return;
      const l = itens.get(alvo.dataset.id);
      if (!l) return;
      const id = encodeURIComponent(l.id);

      try {
        if (alvo.dataset.fin === "pagar") {
          await api(`/financeiro/${id}/pagar`, { method: "PATCH" });
          ui.toast({ type: "success", title: "Pagamento registrado", message: `${l.alunoNome}: ${formatMoney(l.valor)}` });
          lista.load();
        } else if (alvo.dataset.fin === "editar") {
          const salvo = await ui.dialog({
            title: "Editar lançamento",
            html: financeiroFormHtml(l),
            confirmText: "Salvar alterações",
            onConfirm: (f) => api(`/financeiro/${id}`, {
              method: "PUT",
              body: { valor: parseFloat(f.elements.valor.value), vencimento: f.elements.vencimento.value, status: f.elements.status.value }
            })
          });
          if (salvo) {
            ui.toast({ type: "success", title: "Lançamento atualizado", message: l.alunoNome });
            lista.load();
          }
        } else if (alvo.dataset.fin === "excluir") {
          const ok = await ui.confirm({
            title: "Excluir lançamento?",
            message: `${l.alunoNome}: ${formatMoney(l.valor)} com vencimento em ${formatDate(l.vencimento)}. A exclusão fica registrada no histórico do aluno.`,
            confirmText: "Excluir",
            danger: true
          });
          if (!ok) return;
          await api(`/financeiro/${id}`, { method: "DELETE" });
          ui.toast({ type: "success", title: "Lançamento excluído", message: l.alunoNome });
          lista.load();
        }
      } catch (err) {
        fail("Não foi possível atualizar o lançamento", err);
      }
    });

    carregarAlunos();
    lista.load();
  };

  // Empresa
  const UFS = ["AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO"];

  const EMPRESA_SECOES = [
    {
      titulo: "Dados da empresa", icon: "building", campos: [
        { n: "razaoSocial", label: "Razão social", req: true, span: 3, ph: "Nome empresarial" },
        { n: "nomeFantasia", label: "Nome fantasia", span: 3, ph: "Como a academia é conhecida" },
        { n: "cnpj", label: "CNPJ", req: true, span: 2, ph: "00.000.000/0000-00", mask: "cnpj", inputmode: "numeric" },
        { n: "inscricaoEstadual", label: "Inscrição estadual", span: 2, ph: "Opcional" },
        { n: "dataFundacao", label: "Data de fundação", span: 2, type: "date" },
        { n: "proprietario", label: "Proprietário / responsável", span: 3, ph: "Nome completo" },
        { n: "areasAtuacao", label: "Áreas de atuação", span: 3, ph: "Ex: Musculação, Ergometria, Funcional" }
      ]
    },
    {
      titulo: "Contato", icon: "phone", campos: [
        { n: "email", label: "E-mail", span: 3, type: "email", ph: "contato@academia.com" },
        { n: "telefone", label: "Telefone", span: 3, type: "tel", mask: "phone", ph: "(00) 0000-0000" },
        { n: "whatsapp", label: "WhatsApp", span: 2, type: "tel", mask: "phone", ph: "(00) 00000-0000" },
        { n: "site", label: "Site", span: 2, type: "url", ph: "https://" },
        { n: "instagram", label: "Instagram", span: 2, ph: "@perfil" }
      ]
    },
    {
      titulo: "Endereço", icon: "map-pin", campos: [
        { n: "cep", label: "CEP", span: 2, ph: "00000-000", mask: "cep", inputmode: "numeric" },
        { n: "logradouro", label: "Logradouro", span: 4, ph: "Rua, avenida..." },
        { n: "numero", label: "Número", span: 2, ph: "Ex: 120" },
        { n: "complemento", label: "Complemento", span: 4, ph: "Sala, bloco..." },
        { n: "bairro", label: "Bairro", span: 2 },
        { n: "cidade", label: "Cidade", span: 3 },
        { n: "uf", label: "UF", span: 1, select: true }
      ]
    },
    {
      titulo: "Funcionamento", icon: "clock", campos: [
        { n: "horarioFuncionamento", label: "Horário de funcionamento", span: 4, ph: "Ex: Seg a Sex, 6h às 22h. Sáb, 8h às 14h" },
        { n: "capacidadeAlunos", label: "Capacidade de alunos", span: 2, type: "number", min: 0, ph: "Ex: 300" },
        { n: "observacoes", label: "Observações", span: 6, textarea: true, ph: "Informações adicionais sobre a academia" }
      ]
    }
  ];

  const empresaFieldHtml = (prefix, f) => {
    const id = `${prefix}${f.n}`;
    const label = `<label for="${id}">${f.label}${f.req ? ' <span class="req" aria-hidden="true">*</span>' : ""}</label>`;
    let control;
    if (f.select) {
      control = `<select id="${id}"><option value="">--</option>${UFS.map((u) => `<option value="${u}">${u}</option>`).join("")}</select>`;
    } else if (f.textarea) {
      control = `<textarea id="${id}" rows="3" placeholder="${esc(f.ph || "")}"></textarea>`;
    } else {
      control = `<input id="${id}" type="${f.type || "text"}" ${f.min !== undefined ? `min="${f.min}"` : ""} ${f.inputmode ? `inputmode="${f.inputmode}"` : ""} ${f.mask ? `data-mask="${f.mask}"` : ""} ${f.req ? "required" : ""} placeholder="${esc(f.ph || "")}" />`;
    }
    return `<div class="field col-${f.span}">${label}${control}</div>`;
  };

  const empresaFormMarkup = (prefix) => EMPRESA_SECOES.map((s) => `
    <div class="form-section">
      <h3 class="form-section__title">${icon(s.icon)}${s.titulo}</h3>
      <div class="fgrid">${s.campos.map((f) => empresaFieldHtml(prefix, f)).join("")}</div>
    </div>`).join("");

  const empresaFieldNames = EMPRESA_SECOES.flatMap((s) => s.campos.map((c) => c.n));

  const fillEmpresaForm = (prefix, empresa) => {
    empresaFieldNames.forEach((n) => {
      const el = document.getElementById(`${prefix}${n}`);
      if (!el) return;
      let v = empresa?.[n];
      if (v === null || v === undefined) v = "";
      if (n === "cnpj") v = formatCnpj(v);
      if (n === "cep") v = formatCep(v);
      if (n === "telefone" || n === "whatsapp") v = formatPhone(v);
      el.value = v;
    });
  };

  const readEmpresaForm = (prefix) => {
    const out = {};
    empresaFieldNames.forEach((n) => {
      const el = document.getElementById(`${prefix}${n}`);
      let v = el ? String(el.value).trim() : "";
      if (n === "cnpj" || n === "cep") v = onlyDigits(v);
      if (n === "capacidadeAlunos") out[n] = v === "" ? null : Number(v);
      else out[n] = v === "" ? null : v;
    });
    return out;
  };

  const wireMinhaEmpresa = async () => {
    const host = document.getElementById("empresa-form-host");
    const header = document.getElementById("empresa-header");
    if (!host || document.getElementById("formEmpresa")) return;
    if (getPath() !== "empresa.html") return;

    const podeEditar = hasPerm("EMPRESA_EDITAR");
    host.innerHTML = `
      <form id="formMinhaEmpresa">
        ${empresaFormMarkup("emp_")}
        ${podeEditar ? `<div class="form-actions"><button class="btn btn--brand" type="submit">${icon("check")}Salvar alterações</button></div>` : ""}
      </form>`;
    const form = document.getElementById("formMinhaEmpresa");

    const pintarCabecalho = (e) => {
      if (!header) return;
      header.innerHTML = `
        <span class="logo" aria-hidden="true"></span>
        <div>
          <h2>${esc(e.nomeFantasia || e.razaoSocial)}</h2>
          <p class="help">CNPJ ${esc(formatCnpj(e.cnpj))}</p>
        </div>
        <span class="badge ${e.ativo ? "badge--ok" : "badge--danger"}" style="margin-left:auto">${e.ativo ? "Ativa" : "Inativa"}</span>`;
    };

    try {
      const empresa = await api("/empresa/minha");
      fillEmpresaForm("emp_", empresa);
      pintarCabecalho(empresa);
    } catch (err) {
      fail("Não foi possível carregar os dados da empresa", err);
    }

    document.getElementById("emp_cnpj").disabled = true;
    if (!podeEditar) {
      form.querySelectorAll("input, select, textarea").forEach((el) => { el.disabled = true; });
      return;
    }

    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      const btn = form.querySelector('button[type="submit"]');
      setBusy(btn, true, "Salvando...");
      try {
        const salvo = await api("/empresa/minha", { method: "PUT", body: readEmpresaForm("emp_") });
        fillEmpresaForm("emp_", salvo);
        pintarCabecalho(salvo);
        ui.toast({ type: "success", title: "Dados atualizados", message: "As informações da empresa foram salvas." });
      } catch (err) {
        fail("Não foi possível salvar", err);
      } finally {
        setBusy(btn, false);
      }
    });
  };

  const wireEmpresasAdmin = async () => {
    const tbody = document.getElementById("empresas-lista");
    const host = document.getElementById("empresa-form-host");
    const title = document.getElementById("empresa-form-title");
    if (!tbody || !host || getPath() !== "empresas.html") return;

    let itens = [];
    let editandoId = null;

    host.innerHTML = `
      <form id="formEmpresa">
        ${empresaFormMarkup("emp_")}
        <div class="form-section">
          <h3 class="form-section__title">${icon("key")}Acesso do titular</h3>
          <p class="help" id="titular-ajuda">O titular é a conta principal da empresa. Ele poderá criar os demais usuários (gestor, professor e atendente).</p>
          <div class="fgrid">
            <div class="field col-2"><label for="tit_nome">Nome do titular <span class="req" aria-hidden="true">*</span></label><input id="tit_nome" type="text" autocomplete="off" placeholder="Nome completo" /></div>
            <div class="field col-2"><label for="tit_email">E-mail de acesso <span class="req" aria-hidden="true">*</span></label><input id="tit_email" type="email" autocomplete="off" placeholder="titular@academia.com" /></div>
            <div class="field col-2"><label for="tit_senha" id="tit_senha_label">Senha provisória <span class="req" aria-hidden="true">*</span></label><input id="tit_senha" type="text" autocomplete="new-password" placeholder="Mínimo de 6 caracteres" /></div>
          </div>
        </div>
        <div class="form-actions">
          <button class="btn btn--brand" type="submit" id="btn-salvar-empresa">${icon("check")}Cadastrar empresa</button>
          <button class="btn btn--ghost" type="button" id="btn-cancelar-empresa" hidden>Cancelar edição</button>
        </div>
      </form>`;

    const form = document.getElementById("formEmpresa");
    const btnCancelar = document.getElementById("btn-cancelar-empresa");
    const btnSalvar = document.getElementById("btn-salvar-empresa");

    const modoCadastro = () => {
      editandoId = null;
      form.reset();
      document.getElementById("emp_cnpj").disabled = false;
      ["tit_nome", "tit_email"].forEach((id) => { const el = document.getElementById(id); el.disabled = false; el.required = true; });
      document.getElementById("tit_senha").required = true;
      document.getElementById("tit_senha_label").innerHTML = 'Senha provisória <span class="req" aria-hidden="true">*</span>';
      document.getElementById("titular-ajuda").textContent = "O titular é a conta principal da empresa. Ele poderá criar os demais usuários (gestor, professor e atendente).";
      title.textContent = "Cadastrar nova empresa";
      btnSalvar.innerHTML = `${icon("check")}Cadastrar empresa`;
      btnCancelar.hidden = true;
    };

    const modoEdicao = (item) => {
      editandoId = item.empresa.id;
      form.reset();
      fillEmpresaForm("emp_", item.empresa);
      document.getElementById("tit_nome").value = item.titularNome || "";
      document.getElementById("tit_email").value = item.titularEmail || "";
      ["tit_nome", "tit_email"].forEach((id) => { const el = document.getElementById(id); el.disabled = true; el.required = false; });
      const senha = document.getElementById("tit_senha");
      senha.required = false;
      senha.value = "";
      document.getElementById("tit_senha_label").textContent = "Nova senha do titular (opcional)";
      document.getElementById("titular-ajuda").textContent = "Preencha a senha apenas se precisar redefinir o acesso do titular (por exemplo, se ele esqueceu a senha).";
      title.textContent = `Editar: ${item.empresa.nomeFantasia || item.empresa.razaoSocial}`;
      btnSalvar.innerHTML = `${icon("check")}Salvar alterações`;
      btnCancelar.hidden = false;
      form.scrollIntoView({ behavior: "smooth", block: "start" });
    };

    const pintar = () => {
      if (itens.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="empty-cell">${stateBlock({ icon: "building", title: "Nenhuma empresa cadastrada", text: "Use o formulário abaixo para cadastrar a primeira academia." })}</td></tr>`;
        return;
      }
      tbody.innerHTML = itens.map((it, i) => {
        const e = it.empresa;
        const local = [e.cidade, e.uf].filter(Boolean).join(" / ");
        return `<tr>
          <td class="col-name"><strong>${esc(e.nomeFantasia || e.razaoSocial)}</strong>${e.nomeFantasia ? `<span class="cell-sub">${esc(e.razaoSocial)}</span>` : ""}</td>
          <td>${esc(formatCnpj(e.cnpj))}</td>
          <td>${esc(local || "--")}</td>
          <td>${esc(it.titularNome || "--")}${it.titularEmail ? `<span class="cell-sub">${esc(it.titularEmail)}</span>` : ""}</td>
          <td><span class="badge ${e.ativo ? "badge--ok" : "badge--danger"}">${e.ativo ? "Ativa" : "Inativa"}</span></td>
          <td class="num"><div class="row-actions">
            <button type="button" class="btn btn--sm" data-editar="${i}">${icon("edit")}Editar</button>
            <button type="button" class="btn btn--sm ${e.ativo ? "btn--danger" : "btn--success"}" data-alternar="${i}">${icon("power")}${e.ativo ? "Desativar" : "Ativar"}</button>
          </div></td>
        </tr>`;
      }).join("");
    };

    const carregar = async () => {
      try {
        itens = await api("/empresas");
        pintar();
      } catch (err) {
        if (err?.status === 401) return;
        tbody.innerHTML = `<tr><td colspan="6" class="empty-cell">${stateBlock({ icon: "x-circle", error: true, title: "Não foi possível carregar as empresas", text: friendlyError(err) })}</td></tr>`;
      }
    };

    tbody.addEventListener("click", async (ev) => {
      const alvo = ev.target instanceof Element ? ev.target.closest("[data-editar],[data-alternar]") : null;
      if (!alvo) return;
      if (alvo.hasAttribute("data-editar")) {
        modoEdicao(itens[Number(alvo.getAttribute("data-editar"))]);
        return;
      }
      const item = itens[Number(alvo.getAttribute("data-alternar"))];
      const desativar = item.empresa.ativo;
      const nome = item.empresa.nomeFantasia || item.empresa.razaoSocial;
      const ok = await ui.confirm({
        title: desativar ? "Desativar empresa?" : "Ativar empresa?",
        message: desativar
          ? `${nome} e todos os usuários dela perderão o acesso ao sistema imediatamente. Os dados são mantidos.`
          : `${nome} voltará a ter acesso ao sistema.`,
        confirmText: desativar ? "Desativar" : "Ativar",
        danger: desativar
      });
      if (!ok) return;
      try {
        await api(`/empresas/${encodeURIComponent(item.empresa.id)}/ativo`, { method: "PATCH", body: { ativo: !desativar } });
        ui.toast({ type: "success", title: desativar ? "Empresa desativada" : "Empresa ativada", message: nome });
        await carregar();
      } catch (err) {
        fail("Não foi possível alterar a empresa", err);
      }
    });

    btnCancelar.addEventListener("click", modoCadastro);

    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      setBusy(btnSalvar, true, "Salvando...");
      try {
        const dados = readEmpresaForm("emp_");
        if (editandoId) {
          await api(`/empresas/${encodeURIComponent(editandoId)}`, { method: "PUT", body: dados });
          const novaSenha = document.getElementById("tit_senha").value;
          if (novaSenha.trim() !== "") {
            await api(`/empresas/${encodeURIComponent(editandoId)}/redefinir-senha-titular`, { method: "POST", body: { senha: novaSenha } });
          }
          ui.toast({ type: "success", title: "Empresa atualizada", message: "As alterações foram salvas." });
        } else {
          await api("/empresas", {
            method: "POST",
            body: {
              empresa: dados,
              titular: {
                nome: document.getElementById("tit_nome").value.trim(),
                email: document.getElementById("tit_email").value.trim(),
                senha: document.getElementById("tit_senha").value
              }
            }
          });
          ui.toast({ type: "success", title: "Empresa cadastrada", message: "O titular já pode entrar no sistema com o e-mail e a senha informados." });
        }
        modoCadastro();
        await carregar();
      } catch (err) {
        fail("Não foi possível salvar a empresa", err);
      } finally {
        setBusy(btnSalvar, false);
      }
    });

    modoCadastro();
    carregar();
  };

  const wireContatosAdmin = () => {
    const tbody = document.getElementById("contatos-lista");
    if (!tbody || !hasPerm("EMPRESAS_GERENCIAR")) return;
    const COLS = 5;
    let itens = new Map();

    const lista = pagedList({
      pagerHost: document.getElementById("contatos-paginacao"),
      scrollTarget: document.getElementById("contatos-card"),
      filters: { busca: "", status: "pendentes" },
      fetchPage: (p) => api(`/contato${queryString(p)}`),
      render: (pg, f) => {
        itens = new Map(pg.itens.map((c) => [c.id, c]));
        if (pg.itens.length === 0) {
          tbody.innerHTML = linhaTabela(COLS, stateBlock({
            icon: "message",
            title: f.busca ? "Nenhum contato encontrado" : f.status === "pendentes" ? "Nenhum contato pendente" : "Nenhum contato",
            text: "As mensagens enviadas pelo botão Fale conosco da tela de login aparecem aqui."
          }));
          return;
        }
        tbody.innerHTML = pg.itens.map((c) => `
          <tr class="${c.atendido ? "is-inactive" : ""}">
            <td class="col-name"><strong>${esc(c.nome)}</strong>
              <span class="cell-sub"><a class="link" href="mailto:${esc(c.email)}">${esc(c.email)}</a></span>
              ${c.telefone ? `<span class="cell-sub">${esc(formatPhone(c.telefone))}</span>` : ""}</td>
            <td>${esc(c.academia || "--")}${c.cidade ? `<span class="cell-sub">${esc(c.cidade)}</span>` : ""}</td>
            <td><div class="cell-msg">${multiline(c.mensagem || "--")}</div></td>
            <td>${esc(formatDateTime(c.dataEnvio))}</td>
            <td class="num"><div class="row-actions">
              <button type="button" class="btn btn--sm ${c.atendido ? "btn--ghost" : "btn--success"}" data-contato-id="${esc(c.id)}">${icon(c.atendido ? "repeat" : "check")}${c.atendido ? "Reabrir" : "Marcar atendido"}</button>
            </div></td>
          </tr>`).join("");
      },
      renderError: (err) => {
        tbody.innerHTML = linhaTabela(COLS, stateBlock({ icon: "x-circle", error: true, title: "Não foi possível carregar os contatos", text: friendlyError(err) }));
      }
    });
    wireFilters(document.getElementById("contatos-filtros"), lista);

    tbody.addEventListener("click", async (e) => {
      const alvo = e.target instanceof Element ? e.target.closest("[data-contato-id]") : null;
      if (!alvo) return;
      const c = itens.get(alvo.dataset.contatoId);
      if (!c) return;
      try {
        await api(`/contato/${encodeURIComponent(c.id)}/atendido`, { method: "PATCH", body: { ativo: !c.atendido } });
        ui.toast({ type: "success", title: c.atendido ? "Contato reaberto" : "Contato atendido", message: c.nome });
        lista.load();
      } catch (err) {
        fail("Não foi possível atualizar o contato", err);
      }
    });

    lista.load();
  };

  // Usuarios da empresa
  const PERM_LABELS = [
    ["ALUNOS_VER", "Consultar alunos"],
    ["ALUNOS_EDITAR", "Cadastrar, editar e inativar alunos"],
    ["TREINOS_VER", "Consultar treinos"],
    ["TREINOS_EDITAR", "Montar e editar fichas de treino"],
    ["FINANCEIRO_VER", "Consultar financeiro"],
    ["FINANCEIRO_LANCAR", "Lançar e dar baixa em mensalidades"],
    ["FINANCEIRO_EDITAR", "Alterar ou excluir lançamentos"],
    ["MURAL_VER", "Ver o mural"],
    ["MURAL_PUBLICAR", "Publicar, editar e apagar avisos"],
    ["CRM_VER", "Consultar o CRM"],
    ["EMPRESA_VER", "Ver dados da empresa"],
    ["EMPRESA_EDITAR", "Editar dados da empresa"],
    ["USUARIOS_GERENCIAR", "Gerenciar usuários"]
  ];

  const wireUsuarios = async () => {
    const tbody = document.getElementById("usuarios-lista");
    const form = document.getElementById("formUsuario");
    const matrixHost = document.getElementById("matriz-permissoes");
    if (!tbody || !form) return;

    const me = currentUser();
    let usuarios = [];
    let editandoId = null;

    const title = document.getElementById("usuario-form-title");
    const btnSalvar = document.getElementById("btn-salvar-usuario");
    const btnCancelar = document.getElementById("btn-cancelar-usuario");
    const campoNome = document.getElementById("usr_nome");
    const campoEmail = document.getElementById("usr_email");
    const campoPerfil = document.getElementById("usr_perfil");
    const campoSenha = document.getElementById("usr_senha");
    const campoAtivo = document.getElementById("usr_ativo");
    const linhaAtivo = document.getElementById("usr_ativo_row");
    const labelSenha = document.getElementById("usr_senha_label");

    const modoCriar = () => {
      editandoId = null;
      form.reset();
      campoEmail.disabled = false;
      campoPerfil.disabled = false;
      campoSenha.required = true;
      labelSenha.innerHTML = 'Senha provisória <span class="req" aria-hidden="true">*</span>';
      linhaAtivo.hidden = true;
      title.textContent = "Novo usuário";
      btnSalvar.innerHTML = `${icon("check")}Cadastrar usuário`;
      btnCancelar.hidden = true;
    };

    const modoEditar = (u) => {
      editandoId = u.id;
      form.reset();
      campoNome.value = u.nome;
      campoEmail.value = u.email;
      campoEmail.disabled = true;
      campoPerfil.value = u.perfil;
      const travado = u.titular || u.id === me.id;
      campoPerfil.disabled = travado;
      campoSenha.required = false;
      labelSenha.textContent = "Nova senha (opcional)";
      linhaAtivo.hidden = travado;
      campoAtivo.checked = u.ativo;
      title.textContent = `Editar: ${u.nome}`;
      btnSalvar.innerHTML = `${icon("check")}Salvar alterações`;
      btnCancelar.hidden = false;
      form.scrollIntoView({ behavior: "smooth", block: "center" });
    };

    const pintar = () => {
      if (usuarios.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" class="empty-cell">${stateBlock({ icon: "users", title: "Nenhum usuário", text: "Cadastre o primeiro usuário ao lado." })}</td></tr>`;
        return;
      }
      tbody.innerHTML = usuarios.map((u, i) => `<tr>
        <td class="col-name"><strong>${esc(u.nome)}</strong>${u.id === me.id ? '<span class="you-tag">Você</span>' : ""}${u.titular ? '<span class="you-tag">Titular</span>' : ""}<span class="cell-sub">${esc(u.email)}</span></td>
        <td>${profileBadge(u.perfil, u.perfilRotulo)}</td>
        <td><span class="badge ${u.ativo ? "badge--ok" : "badge--danger"}">${u.ativo ? "Ativo" : "Desativado"}</span></td>
        <td>${esc(formatDateTime(u.ultimoAcesso))}</td>
        <td class="num"><div class="row-actions"><button type="button" class="btn btn--sm" data-editar="${i}">${icon("edit")}Editar</button></div></td>
      </tr>`).join("");
    };

    const carregar = async () => {
      try {
        usuarios = await api("/usuarios");
        pintar();
      } catch (err) {
        if (err?.status === 401) return;
        tbody.innerHTML = `<tr><td colspan="5" class="empty-cell">${stateBlock({ icon: "x-circle", error: true, title: "Não foi possível carregar os usuários", text: friendlyError(err) })}</td></tr>`;
      }
    };

    const carregarMatriz = async () => {
      if (!matrixHost) return;
      try {
        const perfis = await api("/usuarios/perfis");
        matrixHost.innerHTML = `
          <table class="matrix" aria-label="Permissões por categoria">
            <thead><tr><th>Ação</th>${perfis.map((p) => `<th>${esc(p.rotulo)}</th>`).join("")}</tr></thead>
            <tbody>${PERM_LABELS.map(([perm, label]) => `<tr><td>${esc(label)}</td>${perfis.map((p) =>
              p.permissoes.includes(perm)
                ? `<td class="yes" aria-label="Permitido">${icon("check")}</td>`
                : `<td class="no" aria-label="Não permitido">${icon("minus")}</td>`).join("")}</tr>`).join("")}</tbody>
          </table>`;
      } catch (err) {
        if (err?.status === 401) return;
        matrixHost.innerHTML = stateBlock({ icon: "x-circle", error: true, title: "Não foi possível carregar a tabela", text: friendlyError(err) });
      }
    };

    tbody.addEventListener("click", (e) => {
      const alvo = e.target instanceof Element ? e.target.closest("[data-editar]") : null;
      if (alvo) modoEditar(usuarios[Number(alvo.getAttribute("data-editar"))]);
    });
    btnCancelar.addEventListener("click", modoCriar);

    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      setBusy(btnSalvar, true, "Salvando...");
      try {
        if (editandoId) {
          const alvo = usuarios.find((u) => u.id === editandoId);
          const corpo = { nome: campoNome.value.trim(), senha: campoSenha.value || null };
          if (!campoPerfil.disabled) corpo.perfil = campoPerfil.value;
          if (!linhaAtivo.hidden) corpo.ativo = campoAtivo.checked;
          await api(`/usuarios/${encodeURIComponent(editandoId)}`, { method: "PUT", body: corpo });
          ui.toast({ type: "success", title: "Usuário atualizado", message: alvo ? alvo.nome : "" });
        } else {
          await api("/usuarios", {
            method: "POST",
            body: {
              nome: campoNome.value.trim(),
              email: campoEmail.value.trim(),
              perfil: campoPerfil.value,
              senha: campoSenha.value
            }
          });
          ui.toast({ type: "success", title: "Usuário cadastrado", message: "Ele já pode entrar com o e-mail e a senha informados." });
        }
        modoCriar();
        await carregar();
      } catch (err) {
        fail("Não foi possível salvar o usuário", err);
      } finally {
        setBusy(btnSalvar, false);
      }
    });

    modoCriar();
    carregar();
    carregarMatriz();
  };

  // Inicializacao
  document.addEventListener("DOMContentLoaded", () => {
    if (!enforceAuth()) return;
    mountChrome();
    applyPermissions();
    wireMenuToggle();
    wireUserMenu();
    setActiveNav();
    wireMasks();
    wirePasswordToggles();
    wireConfirmables();
    wireFormHints();
    wireLogout();
    showFlash();

    wireLoginForm();
    wireContato();
    wireEsqueciSenha();
    wireRedefinirSenha();
    wireConta();

    wireAlunos();
    wireMural();
    wireFinanceiro();
    loadCRMData();
    wireTreinoForm();
    wireVisualizarTreinos();
    wireMinhaEmpresa();
    wireEmpresasAdmin();
    wireContatosAdmin();
    wireUsuarios();
  });
})();
