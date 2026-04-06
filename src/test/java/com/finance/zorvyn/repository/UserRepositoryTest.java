package com.finance.zorvyn.repository;

import com.finance.zorvyn.entity.Role;
import com.finance.zorvyn.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void saveAndQueryByEmail_and_role_and_active() {
        User u = User.builder()
                .name("Alice")
                .email("alice@test.com")
                .password("x")
                .role(Role.ANALYST)
                .active(true)
                .build();

        userRepository.save(u);

        assertTrue(userRepository.existsByEmail("alice@test.com"));
        var byEmail = userRepository.findByEmail("alice@test.com");
        assertTrue(byEmail.isPresent());

        var page = userRepository.findByRole(Role.ANALYST, PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());

        var activePage = userRepository.findByActive(true, PageRequest.of(0, 10));
        assertEquals(1, activePage.getTotalElements());
    }
}

