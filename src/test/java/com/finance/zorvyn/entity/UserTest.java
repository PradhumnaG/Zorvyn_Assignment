package com.finance.zorvyn.entity;

import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @Test
    void userDetails_contract() {
        User u = User.builder()
                .id(10L)
                .name("Bob")
                .email("bob@example.com")
                .password("pw")
                .role(Role.ADMIN)
                .active(true)
                .build();

        assertEquals("bob@example.com", u.getUsername());
        Collection<?> authorities = u.getAuthorities();
        assertNotNull(authorities);
        assertTrue(u.isAccountNonExpired());
        assertTrue(u.isCredentialsNonExpired());
        assertTrue(u.isEnabled());
        assertTrue(u.isAccountNonLocked());
    }
}

