import { apiClient } from "./api-client.js";
import { appState } from "./state.js";
import { showToast } from "./toast.js";

export function initAuthModule({ onSessionReady }) {
  const form = document.getElementById("loginForm");
  const authBadge = document.getElementById("authBadge");
  const roleBadge = document.getElementById("roleBadge");
  const logoutBtn = document.getElementById("logoutBtn");

  function renderAuth(state) {
    const logged = Boolean(state.user && state.token);
    authBadge.textContent = logged ? `Autenticato: ${state.user.email}` : "Non autenticato";
    authBadge.className = logged ? "badge badge-online" : "badge badge-offline";
    roleBadge.textContent = `Ruolo: ${logged ? state.user.role : "-"}`;
    logoutBtn.disabled = !logged;
  }

  appState.subscribe((state) => {
    renderAuth(state);
    if (state.user && onSessionReady) {
      onSessionReady(state.user);
    }
  });

  renderAuth(appState);

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    const formData = new FormData(form);
    const email = String(formData.get("email") || "").trim();
    const password = String(formData.get("password") || "");

    try {
      const response = await apiClient.login(email, password);
      appState.setSession(response.token, response.user);
      showToast("Login eseguito", "info");
    } catch (error) {
      showToast(error.message, "error");
    }
  });

  logoutBtn.addEventListener("click", () => {
    appState.clearSession();
    showToast("Sessione chiusa", "info");
  });
}
