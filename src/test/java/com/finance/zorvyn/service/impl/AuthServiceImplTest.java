package com.finance.zorvyn.service.impl;

import com.finance.zorvyn.entity.Role;
import com.finance.zorvyn.entity.User;
import com.finance.zorvyn.exception.DuplicateResourceException;
import com.finance.zorvyn.repository.UserRepository;
import com.finance.zorvyn.security.JwtService;
import com.finance.zorvyn.dto.request.LoginRequest;
import com.finance.zorvyn.dto.request.RegisterRequest;
import com.finance.zorvyn.dto.response.AuthResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_ShouldReturnAuthResponse_WhenValidRequest() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setName("John Doe");
        request.setEmail("john@example.com");
        request.setPassword("password123");
        request.setRole(Role.VIEWER);

        User savedUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .password("encoded-password")
                .role(Role.VIEWER)
                .active(true)
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(savedUser)).thenReturn("jwt-token");

        // When
        AuthResponse response = authService.register(request);

        // Then
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getTokentype()).isEqualTo("Bearer");
        assertThat(response.getUserid()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("John Doe");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getRole()).isEqualTo(Role.VIEWER);

        verify(userRepository).existsByEmail(request.getEmail());
        verify(passwordEncoder).encode(request.getPassword());
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(savedUser);
    }

    @Test
    void register_ShouldThrowDuplicateResourceException_WhenEmailExists() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("An account with email 'existing@example.com' already exists");

        verify(userRepository).existsByEmail(request.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_ShouldReturnAuthResponse_WhenValidCredentials() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("password123");

        User user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .role(Role.VIEWER)
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        // When
        AuthResponse response = authService.login(request);

        // Then
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getTokentype()).isEqualTo("Bearer");
        assertThat(response.getUserid()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("John Doe");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getRole()).isEqualTo(Role.VIEWER);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail(request.getEmail());
        verify(jwtService).generateToken(user);
    }
}
