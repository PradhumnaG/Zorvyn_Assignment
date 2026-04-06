package com.finance.zorvyn.repository;

import com.finance.zorvyn.entity.Role;
import com.finance.zorvyn.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
//user table

@Repository
public interface UserRepository  extends JpaRepository<User, Long> {
//find by email
    Optional<User> findByEmail(String email);


    boolean existsByEmail(String email);

//find by role
    Page<User> findByRole(Role role, Pageable pageable);


    Page<User> findByActive(boolean active, Pageable pageable);
}
