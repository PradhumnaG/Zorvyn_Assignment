package com.finance.zorvyn.controller;

import com.finance.zorvyn.dto.request.RegisterRequest;
import com.finance.zorvyn.dto.request.LoginRequest;
import com.finance.zorvyn.dto.response.ApiResponse;
import com.finance.zorvyn.dto.response.AuthResponse;
import com.finance.zorvyn.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register and login endpoints — no token required")
public class AuthController {
    private final AuthService authService;


    @PostMapping("/register")
    @Operation(summary = "Register a new user",
            description = "Creates a new account. Role defaults to VIEWER if not specified.")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", response));
    }


    @PostMapping("/login")
    @Operation(summary = "Login with email and password",
            description = "Returns a JWT Bearer token. Use it in the Authorization header.")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }
}
