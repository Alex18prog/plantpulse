package com.plantpulse.exception;

import com.plantpulse.config.SecurityConfig;
import com.plantpulse.controller.MachineController;
import com.plantpulse.repository.MachineRepository;
import com.plantpulse.repository.TelemetryReadingRepository;
import com.plantpulse.security.JwtService;
import com.plantpulse.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MachineController.class)
@Import(SecurityConfig.class)
@WithMockUser
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MachineRepository machineRepository;

    @MockBean
    private TelemetryReadingRepository telemetryReadingRepository;

    // Required by the auto-registered JwtAuthenticationFilter bean, unused by these tests (@WithMockUser bypasses it).
    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void resourceNotFoundException_isMappedTo404WithErrorBody() throws Exception {
        when(machineRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/machines/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Machine not found: 999"))
                .andExpect(jsonPath("$.path").value("/api/machines/999"));
    }

    @Test
    void invalidRequestBody_isMappedTo400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/machines")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.type").exists())
                .andExpect(jsonPath("$.fieldErrors.status").exists());
    }
}
