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

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    // ---------------------------------------------------------------
    // Read operations
    // ---------------------------------------------------------------

    @Override
    @Transactional(readOnly = true) // readOnly = true: hint to JPA to skip dirty-checking
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        // JpaRepository.findAll(Pageable) handles offset + limit SQL automatically
        return userRepository.findAll(pageable)
                .map(this::toResponse); // Convert each User entity to UserResponse DTO
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

    // ---------------------------------------------------------------
    // Write operations
    // ---------------------------------------------------------------

    /**
     * Partially updates a user — only applies non-null fields from the request.
     * This implements PATCH semantics: "change only what is provided".
     */
    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = findUserOrThrow(id);

        // Apply only the fields that were actually provided in the request
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getActive() != null) {
            // If deactivating, verify this isn't the last active admin
            if (!request.getActive()) {
                validateNotLastAdmin(user);
            }
            user.setActive(request.getActive());
        }

        // save() triggers @PreUpdate which sets updatedAt
        User updated = userRepository.save(user);
        log.info("User {} updated", id);
        return toResponse(updated);
    }

    @Override
    @Transactional
    public UserResponse deactivateUser(Long id) {
        User user = findUserOrThrow(id);
        // Guard: prevent locking out the entire system
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

    // ---------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------

    /** Loads a User by ID or throws 404 if not found */
    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    /**
     * Business rule: the system must always have at least one active ADMIN.
     * If this user is the last active admin, we block deactivation.
     */
    private void validateNotLastAdmin(User user) {
        if (user.getRole() == Role.ADMIN && user.isActive()) {
            // Count how many active admins exist (including this user)
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

    /**
     * Maps a User entity to a UserResponse DTO.
     * Excludes the password field for security.
     */
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
