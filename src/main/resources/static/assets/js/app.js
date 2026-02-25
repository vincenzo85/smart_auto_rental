import { appState } from "./state.js";
import { initAuthModule } from "./auth-module.js";
import { initAvailabilityModule } from "./availability-module.js";
import { initBookingModule } from "./booking-module.js";
import { initAdminModule } from "./admin-module.js";
import { showToast } from "./toast.js";

function wireCarSelection() {
  return {
    onCarSelected(car) {
      appState.setSelectedCarId(car.carId);

      const bookingForm = document.getElementById("bookingForm");
      const startInput = bookingForm.querySelector("input[name='startTime']");
      const endInput = bookingForm.querySelector("input[name='endTime']");

      if (appState.lastAvailabilityQuery) {
        const query = appState.lastAvailabilityQuery;
        startInput.value = query.startTime.slice(0, 16);
        endInput.value = query.endTime.slice(0, 16);
      }

      showToast(`Auto #${car.carId} selezionata`, "info");
    },
  };
}

function initApp() {
  const bookingModule = initBookingModule();
  initAdminModule();

  initAuthModule({
    onSessionReady: () => {
      bookingModule.refreshBookings();
    },
  });

  initAvailabilityModule(wireCarSelection());

  if (appState.user && appState.token) {
    bookingModule.refreshBookings();
  }
}

initApp();
