package com.finance.zorvyn.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ResourceNotFoundExceptionTest {

    @Test
    void message_and_getters() {
        ResourceNotFoundException ex = new ResourceNotFoundException("User", "id", 5);
        assertTrue(ex.getMessage().contains("User not found"));
        assertEquals("User", ex.getResourceName());
        assertEquals("id", ex.getFieldName());
        assertEquals(5, ex.getFieldValue());
    }
}

