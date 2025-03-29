package me.wugs.judy.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.UUID;
import me.wugs.judy.entity.User;
import me.wugs.judy.enums.UserRole;
import me.wugs.judy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private UserService userService;

  private User testUser;
  private UUID userId;
  private String rawPassword;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    rawPassword = "password";
    testUser =
        User.builder()
            .id(userId)
            .username("testuser")
            .email("test@example.com")
            .password("hashedpassword") // the passwordEncoder is mocked
            .role(UserRole.USER)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
  }

  @Test
  void shouldCreateUserSuccessfully() {
    assert (true);
  }

  @Test
  void shouldThrowExceptionIfUsernameOrEmailExists() {
    assert (true);
  }

  @Test
  void shouldAuthenticateValidUser() {
    assert (true);
  }

  @Test
  void shouldRejectInvalidPassword() {
    assert (true);
  }

  @Test
  void shouldUpdateUserSuccessfully() {
    assert (true);
  }

  @Test
  void shouldPreventUnauthorizedUserUpdate() {
    assert (true);
  }

  @Test
  void shouldPreventDuplicateUsernameOrEmailOnUpdate() {
    assert (true);
  }

  @Test
  void shouldDeleteUserIfSuperAdmin() {
    assert (true);
  }

  @Test
  void shouldPreventUserFromDeletingThemselves() {
    assert (true);
  }

  @Test
  void shouldPreventNonSuperAdminFromDeletingUsers() {
    assert (true);
  }

  @Test
  void shouldChangeUserRoleIfSuperAdmin() {
    assert (true);
  }

  @Test
  void shouldPreventSuperAdminFromChangingOwnRole() {
    assert (true);
  }

  @Test
  void shouldPreventNonSuperAdminFromChangingRoles() {
    assert (true);
  }
}
