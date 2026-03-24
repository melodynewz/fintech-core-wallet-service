package com.fintech.wallet.repository;

import com.fintech.wallet.model.entity.User;
import com.fintech.wallet.model.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("findByUsername - returns user when exists")
    void findByUsername_ExistingUser_ReturnsUser() {
        // Given
        User user = User.builder()
                .username("johndoe")
                .password("hashed")
                .email("john@example.com")
                .role(UserRole.ROLE_USER)
                .build();
        userRepository.save(user);

        // When
        Optional<User> found = userRepository.findByUsername("johndoe");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("johndoe");
        assertThat(found.get().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    @DisplayName("findByUsername - returns empty when not exists")
    void findByUsername_NonExistingUser_ReturnsEmpty() {
        Optional<User> found = userRepository.findByUsername("nonexistent");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("existsByUsername - returns true when username exists")
    void existsByUsername_ExistingUsername_ReturnsTrue() {
        User user = User.builder()
                .username("alice")
                .password("hash")
                .role(UserRole.ROLE_USER)
                .build();
        userRepository.save(user);

        boolean exists = userRepository.existsByUsername("alice");
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByUsername - returns false when username does not exist")
    void existsByUsername_NonExistingUsername_ReturnsFalse() {
        boolean exists = userRepository.existsByUsername("unknown");
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("existsByEmail - returns true when email exists")
    void existsByEmail_ExistingEmail_ReturnsTrue() {
        User user = User.builder()
                .username("bob")
                .password("hash")
                .email("bob@example.com")
                .role(UserRole.ROLE_USER)
                .build();
        userRepository.save(user);

        boolean exists = userRepository.existsByEmail("bob@example.com");
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByEmail - returns false when email does not exist")
    void existsByEmail_NonExistingEmail_ReturnsFalse() {
        boolean exists = userRepository.existsByEmail("unknown@example.com");
        assertThat(exists).isFalse();
    }
}