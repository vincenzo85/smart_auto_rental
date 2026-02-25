const KEY = "smart_auto_ui_session";

const state = {
  token: null,
  user: null,
  selectedCarId: null,
  lastAvailabilityQuery: null,
};

const listeners = [];

function notify() {
  listeners.forEach((listener) => listener({ ...state }));
}

function persist() {
  const payload = {
    token: state.token,
    user: state.user,
  };
  localStorage.setItem(KEY, JSON.stringify(payload));
}

function load() {
  const raw = localStorage.getItem(KEY);
  if (!raw) {
    return;
  }
  try {
    const parsed = JSON.parse(raw);
    state.token = parsed.token || null;
    state.user = parsed.user || null;
  } catch {
    localStorage.removeItem(KEY);
  }
}

function clearSession() {
  state.token = null;
  state.user = null;
  persist();
  notify();
}

function setSession(token, user) {
  state.token = token;
  state.user = user;
  persist();
  notify();
}

function setSelectedCarId(carId) {
  state.selectedCarId = carId;
  notify();
}

function setAvailabilityQuery(query) {
  state.lastAvailabilityQuery = query;
}

function subscribe(listener) {
  listeners.push(listener);
}

load();

export const appState = {
  get token() {
    return state.token;
  },
  get user() {
    return state.user;
  },
  get selectedCarId() {
    return state.selectedCarId;
  },
  get lastAvailabilityQuery() {
    return state.lastAvailabilityQuery;
  },
  setSession,
  clearSession,
  setSelectedCarId,
  setAvailabilityQuery,
  subscribe,
};
