package com.fintech.wallet.service;

import com.fintech.wallet.model.entity.User;
import com.fintech.wallet.model.enums.UserRole;
import com.fintech.wallet.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("encodedPassword")
                .email("test@example.com")
                .role(UserRole.ROLE_USER)
                .build();
    }

    @Test
    @DisplayName("loadUserByUsername - user found")
    void loadUserByUsername_UserFound_ReturnsUserDetails() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        UserDetails userDetails = userService.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_USER")));
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    @DisplayName("loadUserByUsername - user not found throws UsernameNotFoundException")
    void loadUserByUsername_UserNotFound_ThrowsException() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("unknown"));
        verify(userRepository, times(1)).findByUsername("unknown");
    }

    @Test
    @DisplayName("createUser - success")
    void createUser_Success_ReturnsSavedUser() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return user;
        });

        User created = userService.createUser("newuser", "password", "new@example.com", "ROLE_USER");

        assertNotNull(created);
        assertEquals("newuser", created.getUsername());
        assertEquals("encodedPassword", created.getPassword());
        assertEquals("new@example.com", created.getEmail());
        assertEquals(UserRole.ROLE_USER, created.getRole());
        verify(userRepository, times(1)).existsByUsername("newuser");
        verify(userRepository, times(1)).existsByEmail("new@example.com");
        verify(passwordEncoder, times(1)).encode("password");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("createUser - username already exists throws IllegalArgumentException")
    void createUser_UsernameExists_ThrowsIllegalArgumentException() {
        when(userRepository.existsByUsername("existing")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.createUser("existing", "password", "email@example.com", "ROLE_USER"));

        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository, times(1)).existsByUsername("existing");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("createUser - email already exists throws IllegalArgumentException")
    void createUser_EmailExists_ThrowsIllegalArgumentException() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.createUser("newuser", "password", "existing@example.com", "ROLE_USER"));

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository, times(1)).existsByUsername("newuser");
        verify(userRepository, times(1)).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("createUser - null email should not check email uniqueness")
    void createUser_NullEmail_SkipsEmailCheck() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = userService.createUser("newuser", "password", null, "ROLE_ADMIN");

        assertNotNull(created);
        assertNull(created.getEmail());
        assertEquals(UserRole.ROLE_ADMIN, created.getRole());
        verify(userRepository, times(1)).existsByUsername("newuser");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("getUserByUsername - user found")
    void getUserByUsername_UserFound_ReturnsUser() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        User found = userService.getUserByUsername("testuser");

        assertNotNull(found);
        assertEquals(testUser, found);
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    @DisplayName("getUserByUsername - user not found throws UsernameNotFoundException")
    void getUserByUsername_UserNotFound_ThrowsException() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.getUserByUsername("unknown"));
        verify(userRepository, times(1)).findByUsername("unknown");
    }
}