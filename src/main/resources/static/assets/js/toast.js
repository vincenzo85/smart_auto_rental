const toastEl = document.getElementById("toast");
let hideTimer;

export function showToast(message, type = "info") {
  if (!toastEl) {
    return;
  }
  clearTimeout(hideTimer);
  toastEl.textContent = message;
  toastEl.className = `toast show ${type === "error" ? "error" : ""}`.trim();

  hideTimer = window.setTimeout(() => {
    toastEl.className = "toast";
  }, 2600);
}
