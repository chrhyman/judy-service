package me.wugs.judy.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import me.wugs.judy.dto.UserDto;
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
    when(userRepository.existsByUsername(testUser.getUsername())).thenReturn(false);
    when(userRepository.existsByEmail(testUser.getEmail())).thenReturn(false);
    when(passwordEncoder.encode(rawPassword)).thenReturn(testUser.getPassword());
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    UserDto result =
        userService.createUser(testUser.getUsername(), testUser.getEmail(), rawPassword);

    assertThat(result.username()).isEqualTo(testUser.getUsername());
    assertThat(result.email()).isEqualTo(testUser.getEmail());
    verify(userRepository).save(any(User.class));
  }

  @Test
  void shouldThrowExceptionIfUsernameOrEmailExists() {
    when(userRepository.existsByUsername(testUser.getUsername())).thenReturn(true);

    assertThatThrownBy(
            () -> userService.createUser(testUser.getUsername(), testUser.getEmail(), rawPassword))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Username or email already exists");

    when(userRepository.existsByUsername(testUser.getUsername())).thenReturn(false);
    when(userRepository.existsByEmail(testUser.getEmail())).thenReturn(true);

    assertThatThrownBy(
            () -> userService.createUser(testUser.getUsername(), testUser.getEmail(), rawPassword))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Username or email already exists");
  }

  @Test
  void shouldAuthenticateValidUser() {
    when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches(rawPassword, testUser.getPassword())).thenReturn(true);

    UUID authenticatedUserId = userService.authenticatePassword(testUser.getEmail(), rawPassword);

    assertThat(authenticatedUserId).isEqualTo(userId);
  }

  @Test
  void shouldRejectInvalidPassword() {
    when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches("wrongpassword", testUser.getPassword())).thenReturn(false);

    assertThatThrownBy(() -> userService.authenticatePassword(testUser.getEmail(), "wrongpassword"))
        .isInstanceOf(SecurityException.class)
        .hasMessageContaining("Invalid credentials");
  }

  @Test
  void shouldUpdateUserSuccessfully() {
    UUID requesterId = userId; // User updating themselves
    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
    User updatedUser = testUser;
    updatedUser.setUsername("newUsername");
    updatedUser.setEmail("new@example.com");
    when(userRepository.save(any(User.class))).thenReturn(updatedUser);

    Optional<UserDto> result =
        userService.updateUser(
            userId, "newUsername", "new@example.com", rawPassword, requesterId, false);

    assertThat(result).isPresent();
    assertThat(result.get().username()).isEqualTo("newUsername");
    assertThat(result.get().email()).isEqualTo("new@example.com");
  }

  @Test
  void shouldPreventUnauthorizedUserUpdate() {
    UUID anotherUserId = UUID.randomUUID();
    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

    assertThatThrownBy(
            () ->
                userService.updateUser(
                    userId, "newUsername", "new@example.com", rawPassword, anotherUserId, false))
        .isInstanceOf(SecurityException.class)
        .hasMessageContaining("You are not authorized to update this user");
  }

  @Test
  void shouldPreventDuplicateUsernameOrEmailOnUpdate() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(userRepository.existsByUsername("newUsername")).thenReturn(true);

    assertThatThrownBy(
            () ->
                userService.updateUser(
                    userId, "newUsername", "new@example.com", rawPassword, userId, false))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Username already taken");
  }

  @Test
  void shouldDeleteUserIfSuperAdmin() {
    UUID targetUserId = UUID.randomUUID();
    UUID superAdminId = UUID.randomUUID();

    userService.deleteUser(targetUserId, superAdminId, true);

    verify(userRepository).deleteById(targetUserId);
  }

  @Test
  void shouldPreventUserFromDeletingThemselves() {
    UUID superAdminId = UUID.randomUUID();

    assertThatThrownBy(() -> userService.deleteUser(superAdminId, superAdminId, true))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("SuperAdmins cannot delete themselves");
  }

  @Test
  void shouldPreventNonSuperAdminFromDeletingUsers() {
    UUID targetUserId = UUID.randomUUID();
    UUID regularUserId = UUID.randomUUID();

    assertThatThrownBy(() -> userService.deleteUser(targetUserId, regularUserId, false))
        .isInstanceOf(SecurityException.class)
        .hasMessageContaining("Only SuperAdmins can delete users");
  }

  @Test
  void shouldChangeUserRoleIfSuperAdmin() {
    UUID targetUserId = UUID.randomUUID();
    UUID superAdminId = UUID.randomUUID();
    User targetUser =
        User.builder()
            .id(targetUserId)
            .username("regularUser")
            .email("user@example.com")
            .role(UserRole.USER)
            .build();

    when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetUser));
    targetUser.setRole(UserRole.ADMIN);
    when(userRepository.save(any(User.class))).thenReturn(targetUser);

    Optional<UserDto> result =
        userService.changeUserRole(targetUserId, UserRole.ADMIN, superAdminId, true);

    assertThat(result).isPresent();
    assertThat(result.get().role()).isEqualTo(UserRole.ADMIN);
  }

  @Test
  void shouldPreventSuperAdminFromChangingOwnRole() {
    UUID superAdminId = UUID.randomUUID();

    assertThatThrownBy(
            () -> userService.changeUserRole(superAdminId, UserRole.USER, superAdminId, true))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("SuperAdmins cannot change their own role");
  }

  @Test
  void shouldPreventNonSuperAdminFromChangingRoles() {
    UUID targetUserId = UUID.randomUUID();
    UUID regularUserId = UUID.randomUUID();

    assertThatThrownBy(
            () -> userService.changeUserRole(targetUserId, UserRole.ADMIN, regularUserId, false))
        .isInstanceOf(SecurityException.class)
        .hasMessageContaining("Only SuperAdmins can change user roles");
  }
}
