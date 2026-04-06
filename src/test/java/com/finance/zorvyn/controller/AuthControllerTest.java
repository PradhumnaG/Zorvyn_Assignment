package com.finance.zorvyn.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.zorvyn.dto.request.LoginRequest;
import com.finance.zorvyn.dto.request.RegisterRequest;
import com.finance.zorvyn.dto.response.ApiResponse;
import com.finance.zorvyn.dto.response.AuthResponse;
import com.finance.zorvyn.entity.Role;
import com.finance.zorvyn.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_ShouldReturnCreated_WhenValidRequest() throws Exception {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .name("John Doe")
                .email("john@example.com")
                .password("password123")
                .role(Role.VIEWER)
                .build();

        AuthResponse response = AuthResponse.builder()
                .token("jwt-token")
                .tokentype("Bearer")
                .userid(1L)
                .name("John Doe")
                .email("john@example.com")
                .role(Role.VIEWER)
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Registration successful"))
                .andExpect(jsonPath("$.data.token").value("jwt-token"));
    }

    @Test
    void login_ShouldReturnOk_WhenValidRequest() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email("john@example.com")
                .password("password123")
                .build();

        AuthResponse response = AuthResponse.builder()
                .token("jwt-token")
                .tokentype("Bearer")
                .userid(1L)
                .name("John Doe")
                .email("john@example.com")
                .role(Role.VIEWER)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.token").value("jwt-token"));
    }
}
