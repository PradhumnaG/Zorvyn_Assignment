package com.finance.zorvyn.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.zorvyn.dto.request.UpdateUserRequest;
import com.finance.zorvyn.dto.response.UserResponse;
import com.finance.zorvyn.entity.Role;
import com.finance.zorvyn.service.UserService;
import com.finance.zorvyn.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "john@example.com")
    void getCurrentUser_ShouldReturnUser_WhenAuthenticated() throws Exception {
        // Given
        UserResponse userResponse = UserResponse.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .role(Role.VIEWER)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.getCurrentUser("john@example.com")).thenReturn(userResponse);

        // When & Then
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("john@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_ShouldReturnPagedUsers_WhenAdmin() throws Exception {
        // Given
        UserResponse user = UserResponse.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .role(Role.VIEWER)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        Page<UserResponse> page = new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1);

        when(userService.getAllUsers(any(Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].email").value("john@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_ShouldReturnUser_WhenAdmin() throws Exception {
        // Given
        UserResponse userResponse = UserResponse.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .role(Role.VIEWER)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.getUserById(1L)).thenReturn(userResponse);

        // When & Then
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_ShouldReturnUpdatedUser_WhenAdmin() throws Exception {
        // Given
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Updated Name");
        request.setRole(Role.ANALYST);
        request.setActive(false);

        UserResponse updatedUser = UserResponse.builder()
                .id(1L)
                .name("Updated Name")
                .email("john@example.com")
                .role(Role.ANALYST)
                .active(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.updateUser(eq(1L), any(UpdateUserRequest.class))).thenReturn(updatedUser);

        // When & Then
        mockMvc.perform(patch("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Updated Name"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivateUser_ShouldReturnDeactivatedUser_WhenAdmin() throws Exception {
        // Given
        UserResponse deactivatedUser = UserResponse.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .role(Role.VIEWER)
                .active(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.deactivateUser(1L)).thenReturn(deactivatedUser);

        // When & Then
        mockMvc.perform(post("/api/users/1/deactivate")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.active").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activateUser_ShouldReturnActivatedUser_WhenAdmin() throws Exception {
        // Given
        UserResponse activatedUser = UserResponse.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .role(Role.VIEWER)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.activateUser(1L)).thenReturn(activatedUser);

        // When & Then
        mockMvc.perform(post("/api/users/1/activate")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUsersByRole_ShouldReturnPagedUsers_WhenAdmin() throws Exception {
        // Given
        UserResponse user = UserResponse.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .role(Role.VIEWER)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        Page<UserResponse> page = new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1);

        when(userService.getUsersByRole(eq(Role.VIEWER), any(Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/users/by-role")
                        .param("role", "VIEWER")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].role").value("VIEWER"));
    }
}
