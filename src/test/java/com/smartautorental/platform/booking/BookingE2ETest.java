package com.smartautorental.platform.booking;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartautorental.platform.config.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class BookingE2ETest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCompleteCustomerBookingFlow() throws Exception {
        String loginPayload = """
                {
                  "email": "customer@smartauto.local",
                  "password": "Customer123!"
                }
                """;

        String loginJson = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode loginNode = objectMapper.readTree(loginJson);
        String token = loginNode.get("token").asText();

        String availabilityJson = mockMvc.perform(get("/api/v1/availability")
                        .param("branchId", "1")
                        .param("startTime", "2026-12-01T10:00:00Z")
                        .param("endTime", "2026-12-03T10:00:00Z")
                        .param("category", "ECONOMY")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode availabilityNode = objectMapper.readTree(availabilityJson);
        long carId = availabilityNode.get(0).get("carId").asLong();

        String bookingPayload = """
                {
                  "carId": %d,
                  "startTime": "2026-12-01T10:00:00Z",
                  "endTime": "2026-12-03T10:00:00Z",
                  "insuranceSelected": true,
                  "couponCode": "WELCOME10",
                  "payAtDesk": false,
                  "allowWaitlist": false
                }
                """.formatted(carId);

        mockMvc.perform(post("/api/v1/bookings")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.paymentStatus").value("SUCCESS"));
    }
}
