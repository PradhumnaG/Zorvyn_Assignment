package com.finance.zorvyn.service.impl;

import com.finance.zorvyn.dto.request.UpdateUserRequest;
import com.finance.zorvyn.dto.response.UserResponse;
import com.finance.zorvyn.entity.Role;
import com.finance.zorvyn.entity.User;
import com.finance.zorvyn.exception.BadRequestException;
import com.finance.zorvyn.exception.ResourceNotFoundException;
import com.finance.zorvyn.repository.UserRepository;
import com.finance.zorvyn.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
//user management business logic management
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return toResponse(findUserOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getUsersByRole(Role role, Pageable pageable) {
        return userRepository.findByRole(role, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        // Load the currently authenticated user from DB by their email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return toResponse(user);
    }
//for PATCH
    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = findUserOrThrow(id);

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getActive() != null) {
            if (!request.getActive()) {
                validateNotLastAdmin(user);
            }
            user.setActive(request.getActive());
        }

        User updated = userRepository.save(user);
        log.info("User {} updated", id);
        return toResponse(updated);
    }

    @Override
    @Transactional
    public UserResponse deactivateUser(Long id) {
        User user = findUserOrThrow(id);

        validateNotLastAdmin(user);
        user.setActive(false);
        log.info("User {} deactivated", id);
        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse activateUser(Long id) {
        User user = findUserOrThrow(id);
        user.setActive(true);
        log.info("User {} activated", id);
        return toResponse(userRepository.save(user));
    }

//404 if not found
    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

//atleast one admin should be active
    private void validateNotLastAdmin(User user) {
        if (user.getRole() == Role.ADMIN && user.isActive()) {
            long activeAdminCount = userRepository.findByActive(true, Pageable.unpaged())
                    .stream()
                    .filter(u -> u.getRole() == Role.ADMIN)
                    .count();

            if (activeAdminCount <= 1) {
                throw new BadRequestException(
                        "Cannot deactivate the last active administrator. " +
                                "Promote another user to ADMIN first.");
            }
        }
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }


}
