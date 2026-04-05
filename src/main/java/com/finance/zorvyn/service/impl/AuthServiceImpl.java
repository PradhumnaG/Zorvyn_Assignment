package com.finance.zorvyn.service.impl;

import com.finance.zorvyn.entity.Role;
import com.finance.zorvyn.entity.User;
import com.finance.zorvyn.exception.DuplicateResourceException;
import com.finance.zorvyn.repository.UserRepository;
import com.finance.zorvyn.security.JwtService;
import com.finance.zorvyn.dto.request.LoginRequest;
import com.finance.zorvyn.dto.request.LoginRequest;
import com.finance.zorvyn.dto.request.RegisterRequest;
import com.finance.zorvyn.dto.response.AuthResponse;
import com.finance.zorvyn.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;   // BCryptPasswordEncoder from SecurityConfig
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager; // Injected from SecurityConfig

    /**
     * Registers a new user:
     *   1. Check for duplicate email
     *   2. Hash the password
     *   3. Persist the user
     *   4. Generate and return a JWT
     */
    @Override
    @Transactional // Wraps the entire method in a DB transaction
    public AuthResponse register(RegisterRequest request) {

        // Guard: reject if email is already registered
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "An account with email '" + request.getEmail() + "' already exists");
        }

        // Build the User entity — default role is VIEWER if not specified
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                // Hash the plaintext password with BCrypt before storing
                .password(passwordEncoder.encode(request.getPassword()))
                // Use provided role or default to VIEWER (least privilege principle)
                .role(request.getRole() != null ? request.getRole() : Role.VIEWER)
                .active(true)
                .build();

        // Persist to DB — @PrePersist sets createdAt/updatedAt
        User savedUser = userRepository.save(user);
        log.info("New user registered: {} with role {}", savedUser.getEmail(), savedUser.getRole());

        // Generate JWT for the new user so they're immediately logged in
        String token = jwtService.generateToken(savedUser);

        return buildAuthResponse(token, savedUser);
    }

    /**
     * Authenticates a user with email + password:
     *   1. Delegate to Spring Security's AuthenticationManager
     *   2. AuthenticationManager calls UserDetailsService + BCrypt comparison
     *   3. If successful, load the user and generate JWT
     *
     * Spring Security handles the error cases:
     *   - Wrong password → BadCredentialsException (→ 401)
     *   - Disabled account → DisabledException (→ 401)
     *   - User not found → UsernameNotFoundException (→ 401)
     */
    @Override
    public AuthResponse login(LoginRequest request) {

        // This call validates credentials — throws on failure
        // UsernamePasswordAuthenticationToken carries the raw credentials
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),    // principal
                        request.getPassword()  // credentials (plaintext for comparison)
                )
        );

        // Authentication succeeded — load the full User entity
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(); // Can't happen here — authentication just succeeded

        log.info("User logged in: {}", user.getEmail());

        // Generate a fresh JWT
        String token = jwtService.generateToken(user);

        return buildAuthResponse(token, user);
    }

    /** Builds the AuthResponse DTO from a token and user entity */
    private AuthResponse buildAuthResponse(String token, User user) {
        return AuthResponse.builder()
                .token(token)
                .tokentype("Bearer")
                .userid(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
