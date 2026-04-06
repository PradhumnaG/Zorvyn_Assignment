package com.finance.zorvyn.service;

import com.finance.zorvyn.dto.request.LoginRequest;
import com.finance.zorvyn.dto.request.RegisterRequest;
import com.finance.zorvyn.dto.response.AuthResponse;
//for new user login or register
public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
