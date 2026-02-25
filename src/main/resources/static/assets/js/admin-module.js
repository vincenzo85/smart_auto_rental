import { apiClient } from "./api-client.js";
import { appState } from "./state.js";
import { showToast } from "./toast.js";

export function initAdminModule() {
  const panel = document.getElementById("adminPanel");
  const loadBtn = document.getElementById("loadAdminReportsBtn");
  const topList = document.getElementById("topRentedList");
  const utilizationOutput = document.getElementById("utilizationOutput");

  function syncVisibility() {
    const isAdmin = appState.user?.role === "ADMIN";
    panel.classList.toggle("hidden", !isAdmin);
  }

  appState.subscribe(syncVisibility);
  syncVisibility();

  loadBtn.addEventListener("click", async () => {
    if (!appState.token) {
      showToast("Login richiesto", "error");
      return;
    }

    try {
      const [top, utilization] = await Promise.all([
        apiClient.topRented(5),
        apiClient.utilization({
          branchId: "1",
          from: "2026-03-01T00:00:00Z",
          to: "2026-03-31T00:00:00Z",
        }),
      ]);

      topList.innerHTML = "";
      top.forEach((item) => {
        const li = document.createElement("li");
        li.textContent = `${item.licensePlate} - ${item.brand} ${item.model} (${item.rentalCount})`;
        topList.appendChild(li);
      });

      utilizationOutput.textContent = JSON.stringify(utilization, null, 2);
      showToast("Report admin caricati", "info");
    } catch (error) {
      showToast(error.message, "error");
    }
  });
}
