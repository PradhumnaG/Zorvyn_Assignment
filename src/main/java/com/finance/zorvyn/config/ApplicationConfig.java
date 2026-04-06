package com.finance.zorvyn.config;

import com.finance.zorvyn.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
//provieds the bean that is shares accross the application
//seperated from spring security to avoid circular dependency
@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {
    private final UserRepository userRepository;

//uses lambda as it has single abstract method
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() ->

                        new UsernameNotFoundException("User not found with email: " + username)
                );
    }
}
