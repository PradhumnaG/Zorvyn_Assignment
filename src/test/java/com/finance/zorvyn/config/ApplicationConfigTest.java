package com.finance.zorvyn.config;

import com.finance.zorvyn.entity.Role;
import com.finance.zorvyn.entity.User;
import com.finance.zorvyn.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ApplicationConfigTest {

    private UserRepository userRepository;
    private ApplicationConfig applicationConfig;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        applicationConfig = new ApplicationConfig(userRepository);
    }

    @Test
    void userDetailsService_should_return_userdetails_when_found() {
        User user = User.builder()
                .id(5L)
                .name("Test")
                .email("test@x.com")
                .password("secret")
                .role(Role.VIEWER)
                .build();

        when(userRepository.findByEmail("test@x.com")).thenReturn(Optional.of(user));

        var uds = applicationConfig.userDetailsService();
        UserDetails ud = uds.loadUserByUsername("test@x.com");

        assertNotNull(ud);
        assertEquals("test@x.com", ud.getUsername());
        assertTrue(ud.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("VIEWER")));
    }

    @Test
    void userDetailsService_should_throw_when_not_found() {
        when(userRepository.findByEmail("missing@x.com")).thenReturn(Optional.empty());

        var uds = applicationConfig.userDetailsService();
        assertThrows(UsernameNotFoundException.class, () -> uds.loadUserByUsername("missing@x.com"));
    }
}

