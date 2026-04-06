package com.finance.zorvyn.config;

import com.finance.zorvyn.entity.Role;
import com.finance.zorvyn.entity.User;
import com.finance.zorvyn.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class DatalntializerTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private Datalntializer dataInitializer;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        dataInitializer = new Datalntializer(userRepository, passwordEncoder);
    }

    @Test
    void initData_should_seed_when_users_do_not_exist() throws Exception {
        // all emails reported as not existing
        when(userRepository.existsByEmail(any(String.class))).thenReturn(false);
        when(passwordEncoder.encode(any(String.class))).thenAnswer(inv -> "encoded-" + inv.getArgument(0));

        var runner = dataInitializer.initData();
        // execute the CommandLineRunner
        runner.run();

        // capture saved users
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, atLeastOnce()).save(captor.capture());

        var saved = captor.getAllValues();
        // Expect at least the three default users to be saved
        assertTrue(saved.size() >= 3, "expected at least 3 seeded users");

        // verify admin user present with encoded password and role
        boolean foundAdmin = saved.stream().anyMatch(u -> "admin@finance.com".equals(u.getEmail())
                && u.getPassword().startsWith("encoded-Admin@123")
                && u.getRole() == Role.ADMIN);
        assertTrue(foundAdmin, "admin user should be seeded with encoded password and ADMIN role");
    }

    @Test
    void initData_should_not_seed_when_users_exist() throws Exception {
        // all emails reported as existing
        when(userRepository.existsByEmail(any(String.class))).thenReturn(true);

        var runner = dataInitializer.initData();
        runner.run();

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any(String.class));
    }
}

