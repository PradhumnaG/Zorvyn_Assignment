package com.finance.zorvyn.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.zorvyn.dto.response.AuthResponse;
import com.finance.zorvyn.entity.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuthResponseTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void authResponse_serializesAndDeserializes() throws Exception {
        AuthResponse resp = AuthResponse.builder()
                .token("t")
                .tokentype("Bearer")
                .userid(5L)
                .email("u@x.com")
                .name("User")
                .role(Role.VIEWER)
                .build();

        String json = mapper.writeValueAsString(resp);
        assertTrue(json.contains("token"));

        AuthResponse read = mapper.readValue(json, AuthResponse.class);
        assertEquals(resp.getEmail(), read.getEmail());
        assertEquals(resp.getUserid(), read.getUserid());
        assertEquals(resp.getRole(), read.getRole());
    }
}

