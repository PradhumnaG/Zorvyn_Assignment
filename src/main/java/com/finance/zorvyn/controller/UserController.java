package com.finance.zorvyn.controller;

import com.finance.zorvyn.dto.request.UpdateUserRequest;
import com.finance.zorvyn.dto.response.UserResponse;
import com.finance.zorvyn.entity.Role;
import com.finance.zorvyn.service.UserService;
import com.finance.zorvyn.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User CRUD and status management — mostly ADMIN only")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService userService;

    /**
     * GET /api/users/me
     * Returns the currently authenticated user's profile.
     * Available to any role — everyone can see their own data.
     *
     * Authentication is injected by Spring Security from the SecurityContext.
     * authentication.getName() returns the username (email) from the JWT subject.
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user's profile")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            Authentication authentication) { // Spring injects this from SecurityContext

        UserResponse user = userService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved", user));
    }

    /**
     * GET /api/users?page=0&size=10&sort=createdAt,desc
     * Lists all users with pagination. ADMIN only.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") // Rejects with 403 if caller is not ADMIN
    @Operation(summary = "List all users (paginated) — ADMIN only")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        // Build Pageable: combines page number, page size, and sort direction
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved", users));
    }

    /**
     * GET /api/users/{id}
     * Returns a specific user by ID. ADMIN only.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by ID — ADMIN only")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("User retrieved", userService.getUserById(id)));
    }

    /**
     * PATCH /api/users/{id}
     * Partially updates a user's name, role, or active status. ADMIN only.
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user name/role/status — ADMIN only")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success("User updated", userService.updateUser(id, request)));
    }

    /**
     * POST /api/users/{id}/deactivate
     * Disables the user account without deleting it. ADMIN only.
     * Uses POST (not DELETE) because deactivation is reversible.
     */
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate a user account — ADMIN only")
    public ResponseEntity<ApiResponse<UserResponse>> deactivateUser(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("User deactivated", userService.deactivateUser(id)));
    }

    /**
     * POST /api/users/{id}/activate
     * Re-enables a previously deactivated account. ADMIN only.
     */
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate a user account — ADMIN only")
    public ResponseEntity<ApiResponse<UserResponse>> activateUser(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("User activated", userService.activateUser(id)));
    }

    /**
     * GET /api/users/by-role?role=ANALYST&page=0&size=10
     * Lists users filtered by role with pagination. ADMIN only.
     */
    @GetMapping("/by-role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List users by role — ADMIN only")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getUsersByRole(
            @RequestParam Role role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return ResponseEntity.ok(
                ApiResponse.success("Users retrieved", userService.getUsersByRole(role, pageable)));
    }

}
