package com.finance.zorvyn.service;

import com.finance.zorvyn.dto.request.UpdateUserRequest;
import com.finance.zorvyn.dto.response.UserResponse;
import com.finance.zorvyn.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    Page<UserResponse> getAllUsers(Pageable pageable);


    UserResponse getUserById(Long id);


    UserResponse updateUser(Long id, UpdateUserRequest request);


    UserResponse deactivateUser(Long id);


    UserResponse activateUser(Long id);


    Page<UserResponse> getUsersByRole(Role role, Pageable pageable);


    UserResponse getCurrentUser(String email);
}
