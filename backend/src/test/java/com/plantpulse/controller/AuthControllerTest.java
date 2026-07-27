package com.plantpulse.controller;

import com.plantpulse.config.SecurityConfig;
import com.plantpulse.domain.User;
import com.plantpulse.domain.enums.Role;
import com.plantpulse.security.AppUserPrincipal;
import com.plantpulse.security.JwtService;
import com.plantpulse.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void login_validCredentials_returnsToken() throws Exception {
        User admin = User.builder().id(1L).email("admin@plantpulse.dev").passwordHash("hash").role(Role.ADMIN).build();
        Authentication authentication = new UsernamePasswordAuthenticationToken(new AppUserPrincipal(admin), null);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtService.generateToken(admin)).thenReturn("fake-jwt-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"admin@plantpulse.dev\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                .andExpect(jsonPath("$.email").value("admin@plantpulse.dev"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void login_badCredentials_returns401() throws Exception {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad creds"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"admin@plantpulse.dev\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void login_blankFields_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }
}
