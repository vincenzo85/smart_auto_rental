package com.smartautorental.platform.ui;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.smartautorental.platform.security.JwtAuthenticationFilter;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UiController.class)
@AutoConfigureMockMvc(addFilters = false)
class UiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void shouldRenderDashboardAtRoot() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Operational Console")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/assets/js/app.js")));
    }

    @Test
    void shouldRenderDashboardAtUiPath() throws Exception {
        mockMvc.perform(get("/ui"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Smart Auto Rental Platform")));
    }
}
