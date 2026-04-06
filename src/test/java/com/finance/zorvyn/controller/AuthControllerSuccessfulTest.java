package com.finance.zorvyn.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.zorvyn.dto.request.LoginRequest;
import com.finance.zorvyn.dto.request.RegisterRequest;
import com.finance.zorvyn.dto.response.AuthResponse;
import com.finance.zorvyn.entity.Role;
import com.finance.zorvyn.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController - happy path tests")
public class AuthControllerSuccessfulTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private com.finance.zorvyn.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("POST /api/auth/register - success")
    void register_success() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setName("Test User");
        req.setEmail("test@finance.com");
        req.setPassword("Password@123");

        AuthResponse resp = AuthResponse.builder()
                .token("token-xyz")
                .tokentype("Bearer")
                .userid(1L)
                .name("Test User")
                .email("test@finance.com")
                .role(Role.VIEWER)
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/auth/register")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("token-xyz"));
    }

    @Test
    @DisplayName("POST /api/auth/login - success")
    void login_success() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@finance.com");
        req.setPassword("Password@123");

        AuthResponse resp = AuthResponse.builder()
                .token("token-abc")
                .tokentype("Bearer")
                .userid(1L)
                .name("Test User")
                .email("test@finance.com")
                .role(Role.VIEWER)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/auth/login")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("token-abc"));
    }
}

