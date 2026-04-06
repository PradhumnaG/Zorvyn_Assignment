package com.finance.zorvyn.service.impl;

import com.finance.zorvyn.dto.request.UpdateUserRequest;
import com.finance.zorvyn.dto.response.UserResponse;
import com.finance.zorvyn.entity.Role;
import com.finance.zorvyn.entity.User;
import com.finance.zorvyn.exception.BadRequestException;
import com.finance.zorvyn.exception.ResourceNotFoundException;
import com.finance.zorvyn.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getAllUsers_ShouldReturnPagedUserResponses() {
        // Given
        User user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .role(Role.VIEWER)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        Page<User> userPage = new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1);
        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);

        // When
        Page<UserResponse> result = userService.getAllUsers(PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void getUserById_ShouldReturnUserResponse_WhenUserExists() {
        // Given
        User user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .role(Role.VIEWER)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When
        UserResponse result = userService.getUserById(1L);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void getUserById_ShouldThrowResourceNotFoundException_WhenUserDoesNotExist() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found with id: 1");
    }

    @Test
    void getCurrentUser_ShouldReturnUserResponse_WhenUserExists() {
        // Given
        User user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .role(Role.VIEWER)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        // When
        UserResponse result = userService.getCurrentUser("john@example.com");

        // Then
        assertThat(result.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void updateUser_ShouldReturnUpdatedUserResponse() {
        // Given
        User existingUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .role(Role.VIEWER)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        UpdateUserRequest request = UpdateUserRequest.builder()
                .name("Updated Name")
                .role(Role.EDITOR)
                .active(false)
                .build();

        User updatedUser = User.builder()
                .id(1L)
                .name("Updated Name")
                .email("john@example.com")
                .role(Role.EDITOR)
                .active(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // When
        UserResponse result = userService.updateUser(1L, request);

        // Then
        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getRole()).isEqualTo(Role.EDITOR);
        assertThat(result.getActive()).isFalse();
    }

    @Test
    void deactivateUser_ShouldReturnDeactivatedUserResponse() {
        // Given
        User user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .role(Role.VIEWER)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        UserResponse result = userService.deactivateUser(1L);

        // Then
        assertThat(result.getActive()).isFalse();
    }

    @Test
    void deactivateUser_ShouldThrowBadRequestException_WhenDeactivatingLastAdmin() {
        // Given
        User adminUser = User.builder()
                .id(1L)
                .name("Admin")
                .email("admin@example.com")
                .role(Role.ADMIN)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        Page<User> adminPage = new PageImpl<>(List.of(adminUser), PageRequest.of(0, 10), 1);
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(userRepository.findByActive(true, Pageable.unpaged())).thenReturn(adminPage);

        // When & Then
        assertThatThrownBy(() -> userService.deactivateUser(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Cannot deactivate the last active administrator. Promote another user to ADMIN first.");
    }

    @Test
    void activateUser_ShouldReturnActivatedUserResponse() {
        // Given
        User user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .role(Role.VIEWER)
                .active(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        UserResponse result = userService.activateUser(1L);

        // Then
        assertThat(result.getActive()).isTrue();
    }
}
