import { appState } from "./state.js";

const BASE = "";

async function request(path, options = {}) {
  const headers = {
    "Content-Type": "application/json",
    ...(options.headers || {}),
  };

  if (appState.token && !options.skipAuth) {
    headers.Authorization = `Bearer ${appState.token}`;
  }

  const response = await fetch(`${BASE}${path}`, {
    ...options,
    headers,
  });

  const contentType = response.headers.get("content-type") || "";
  const isJson = contentType.includes("application/json");
  const payload = isJson ? await response.json() : await response.text();

  if (!response.ok) {
    const message = isJson ? payload.message || "Request failed" : "Request failed";
    const details = isJson && payload.details ? payload.details.join(" | ") : "";
    throw new Error(details ? `${message}: ${details}` : message);
  }

  return payload;
}

export const apiClient = {
  login(email, password) {
    return request("/api/v1/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password }),
      skipAuth: true,
    });
  },

  searchAvailability({ branchId, startTime, endTime, category }) {
    const params = new URLSearchParams({ branchId, startTime, endTime });
    if (category) {
      params.set("category", category);
    }
    return request(`/api/v1/availability?${params.toString()}`, { method: "GET" });
  },

  createBooking(payload) {
    return request("/api/v1/bookings", {
      method: "POST",
      body: JSON.stringify(payload),
    });
  },

  myBookings() {
    return request("/api/v1/bookings/me", { method: "GET" });
  },

  topRented(limit = 5) {
    return request(`/api/v1/admin/reports/top-rented?limit=${limit}`, { method: "GET" });
  },

  utilization({ branchId, from, to }) {
    const params = new URLSearchParams({ branchId, from, to });
    return request(`/api/v1/admin/reports/utilization?${params.toString()}`, { method: "GET" });
  },
};
