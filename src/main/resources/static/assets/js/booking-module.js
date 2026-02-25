import { apiClient } from "./api-client.js";
import { appState } from "./state.js";
import { showToast } from "./toast.js";
import { shortMoney, toIsoFromLocal } from "./time-utils.js";

function fillTable(tbody, bookings) {
  tbody.innerHTML = "";
  bookings.forEach((booking) => {
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td>${booking.id ?? "-"}</td>
      <td>${booking.code ?? "-"}</td>
      <td>${booking.licensePlate ?? "-"}</td>
      <td>${booking.status}</td>
      <td>${booking.paymentStatus}</td>
      <td>${shortMoney(booking.price?.total)}</td>
    `;
    tbody.appendChild(tr);
  });
}

export function initBookingModule() {
  const form = document.getElementById("bookingForm");
  const output = document.getElementById("bookingOutput");
  const tableBody = document.getElementById("bookingsTableBody");
  const refreshBtn = document.getElementById("refreshBookingsBtn");

  function syncSelectedCar() {
    const carInput = form.querySelector("input[name='carId']");
    if (appState.selectedCarId) {
      carInput.value = appState.selectedCarId;
    }
  }

  appState.subscribe(syncSelectedCar);
  syncSelectedCar();

  async function refreshBookings() {
    if (!appState.token) {
      tableBody.innerHTML = "";
      return;
    }

    try {
      const bookings = await apiClient.myBookings();
      fillTable(tableBody, bookings);
    } catch (error) {
      showToast(error.message, "error");
    }
  }

  refreshBtn.addEventListener("click", refreshBookings);

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    if (!appState.token) {
      showToast("Effettua prima il login", "error");
      return;
    }

    const formData = new FormData(form);
    const payload = {
      carId: Number(formData.get("carId")),
      startTime: toIsoFromLocal(String(formData.get("startTime") || "")),
      endTime: toIsoFromLocal(String(formData.get("endTime") || "")),
      insuranceSelected: formData.get("insuranceSelected") === "on",
      couponCode: String(formData.get("couponCode") || "").trim() || null,
      payAtDesk: formData.get("payAtDesk") === "on",
      allowWaitlist: formData.get("allowWaitlist") === "on",
    };

    try {
      const booking = await apiClient.createBooking(payload);
      output.textContent = JSON.stringify(booking, null, 2);
      showToast(`Prenotazione ${booking.status}`, "info");
      await refreshBookings();
    } catch (error) {
      showToast(error.message, "error");
      output.textContent = error.message;
    }
  });

  return { refreshBookings };
}
