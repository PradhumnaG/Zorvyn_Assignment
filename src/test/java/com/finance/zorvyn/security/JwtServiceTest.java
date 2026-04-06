package com.finance.zorvyn.security;

import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setup() throws Exception {
        jwtService = new JwtService();
        // create a 64-byte key and set as base64
        var key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
        String base64 = io.jsonwebtoken.io.Encoders.BASE64.encode(key.getEncoded());

        Field secret = JwtService.class.getDeclaredField("jwtSecret");
        secret.setAccessible(true);
        secret.set(jwtService, base64);

        Field exp = JwtService.class.getDeclaredField("jwtExpiration");
        exp.setAccessible(true);
        exp.set(jwtService, 1_000_000L);
    }

    @Test
    void generate_and_validate_token() {
        UserDetails ud = User.withUsername("u@x.com").password("x").authorities(Collections.emptyList()).build();

        String token = jwtService.generateToken(ud);
        assertNotNull(token);

        assertEquals("u@x.com", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, ud));
    }
}

