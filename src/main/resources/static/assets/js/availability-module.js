import { apiClient } from "./api-client.js";
import { appState } from "./state.js";
import { showToast } from "./toast.js";
import { shortMoney, toDatetimeLocal, toIsoFromLocal, nowPlusDays } from "./time-utils.js";

function createCard(car, onSelect) {
  const card = document.createElement("article");
  card.className = "result-card";
  card.innerHTML = `
    <h4>${car.brand} ${car.model}</h4>
    <div>Targa: <strong>${car.licensePlate}</strong></div>
    <div>Categoria: ${car.category}</div>
    <div>Prezzo stimato: <strong>${shortMoney(car.estimatedTotalPrice)}</strong></div>
    <div>Fattore dinamico: ${car.dynamicFactor}x</div>
  `;

  const selectBtn = document.createElement("button");
  selectBtn.type = "button";
  selectBtn.className = "btn btn-secondary";
  selectBtn.textContent = `Seleziona auto #${car.carId}`;
  selectBtn.addEventListener("click", () => onSelect(car));
  card.appendChild(selectBtn);
  return card;
}

export function initAvailabilityModule({ onCarSelected }) {
  const form = document.getElementById("availabilityForm");
  const results = document.getElementById("availabilityResults");

  const startInput = form.querySelector("input[name='startTime']");
  const endInput = form.querySelector("input[name='endTime']");
  startInput.value = toDatetimeLocal(nowPlusDays(2));
  endInput.value = toDatetimeLocal(nowPlusDays(4));

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    if (!appState.token) {
      showToast("Effettua prima il login", "error");
      return;
    }

    const formData = new FormData(form);
    const query = {
      branchId: String(formData.get("branchId") || ""),
      category: String(formData.get("category") || ""),
      startTime: toIsoFromLocal(String(formData.get("startTime") || "")),
      endTime: toIsoFromLocal(String(formData.get("endTime") || "")),
    };

    appState.setAvailabilityQuery(query);
    results.innerHTML = "";

    try {
      const cars = await apiClient.searchAvailability(query);
      if (!cars.length) {
        results.innerHTML = "<p>Nessuna auto disponibile per i filtri selezionati.</p>";
        return;
      }

      cars.forEach((car) => {
        results.appendChild(createCard(car, onCarSelected));
      });
      showToast(`Trovate ${cars.length} auto`, "info");
    } catch (error) {
      showToast(error.message, "error");
    }
  });
}
