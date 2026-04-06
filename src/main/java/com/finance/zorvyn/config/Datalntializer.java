package com.finance.zorvyn.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.finance.zorvyn.entity.Role;
import com.finance.zorvyn.entity.User;
import com.finance.zorvyn.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
//seeds the database with default data on application startup
@Configuration
@RequiredArgsConstructor
@Slf4j
public class Datalntializer {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData() {
        return args -> {

            if (!userRepository.existsByEmail("admin@finance.com")) {

                // Build the admin user with a BCrypt-hashed password
                User admin = User.builder()
                        .name("System Administrator")
                        .email("admin@finance.com")
                        // NEVER hardcode passwords in production — use env vars
                        // This is acceptable for a dev seed only
                        .password(passwordEncoder.encode("Admin@123"))
                        .role(Role.ADMIN)
                        .active(true)
                        .build();

                userRepository.save(admin);
                log.info("Default admin seeded: admin@finance.com / Admin@123");
            }


            if (!userRepository.existsByEmail("analyst@finance.com")) {
                User analyst = User.builder()
                        .name("Demo Analyst")
                        .email("analyst@finance.com")
                        .password(passwordEncoder.encode("Analyst@123"))
                        .role(Role.ANALYST)
                        .active(true)
                        .build();
                userRepository.save(analyst);
                log.info("Demo analyst seeded: analyst@finance.com / Analyst@123");
            }


            if (!userRepository.existsByEmail("viewer@finance.com")) {
                User viewer = User.builder()
                        .name("Demo Viewer")
                        .email("viewer@finance.com")
                        .password(passwordEncoder.encode("Viewer@123"))
                        .role(Role.VIEWER)
                        .active(true)
                        .build();
                userRepository.save(viewer);
                log.info("Demo viewer seeded: viewer@finance.com / Viewer@123");
            }

            log.info("Data initialization complete.");
        };
    }

}
