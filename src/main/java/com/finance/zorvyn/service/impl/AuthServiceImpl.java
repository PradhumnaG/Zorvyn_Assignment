package com.finance.zorvyn.service.impl;

import com.finance.zorvyn.entity.Role;
import com.finance.zorvyn.entity.User;
import com.finance.zorvyn.exception.DuplicateResourceException;
import com.finance.zorvyn.repository.UserRepository;
import com.finance.zorvyn.security.JwtService;
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
//register and login
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;   // BCryptPasswordEncoder from SecurityConfig
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager; // Injected from SecurityConfig

 //new user
    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {


        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "An account with email '" + request.getEmail() + "' already exists");
        }


        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : Role.VIEWER)
                .active(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("New user registered: {} with role {}", savedUser.getEmail(), savedUser.getRole());

        String token = jwtService.generateToken(savedUser);

        return buildAuthResponse(token, savedUser);
    }

   //login
    @Override
    public AuthResponse login(LoginRequest request) {


        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),    // principal
                        request.getPassword()  // credentials (plaintext for comparison)
                )
        );


        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(); // Can't happen here — authentication just succeeded

        log.info("User logged in: {}", user.getEmail());


        String token = jwtService.generateToken(user);

        return buildAuthResponse(token, user);
    }

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
