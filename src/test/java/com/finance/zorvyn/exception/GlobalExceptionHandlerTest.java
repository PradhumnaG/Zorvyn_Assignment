package com.finance.zorvyn.exception;

import com.finance.zorvyn.dto.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.*;

public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFound_returnsNotFound() {
        ResponseEntity<ApiResponse<Void>> resp = handler.handleNotFound(new ResourceNotFoundException("R","f",1));
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertFalse(resp.getBody().isSuccess());
    }

    @Test
    void handleDuplicate_returnsConflict() {
        ResponseEntity<ApiResponse<Void>> resp = handler.handleDuplicate(new DuplicateResourceException("dup"));
        assertEquals(HttpStatus.CONFLICT, resp.getStatusCode());
    }

    @Test
    void handleBadCredentials_and_disabled_and_accessDenied() {
        ResponseEntity<ApiResponse<Void>> a = handler.handleBadCredentials(new BadCredentialsException("x"));
        assertEquals(HttpStatus.UNAUTHORIZED, a.getStatusCode());

        ResponseEntity<ApiResponse<Void>> b = handler.handleDisabled(new DisabledException("d"));
        assertEquals(HttpStatus.UNAUTHORIZED, b.getStatusCode());

        ResponseEntity<ApiResponse<Void>> c = handler.handleAccessDenied(new AccessDeniedException("no"));
        assertEquals(HttpStatus.FORBIDDEN, c.getStatusCode());
    }

    @Test
    void handleGeneral_returnsServerError() {
        ResponseEntity<ApiResponse<Void>> r = handler.handleGeneral(new RuntimeException("boom"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, r.getStatusCode());
    }
}

