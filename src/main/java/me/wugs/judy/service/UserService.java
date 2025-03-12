package me.wugs.judy.service;

import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import me.wugs.judy.dto.UserDto;
import me.wugs.judy.entity.User;
import me.wugs.judy.enums.UserRole;
import me.wugs.judy.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public List<UserDto> getAllUsers() {
    return userRepository.findAll().stream().map(UserDto::fromEntity).toList();
  }

  public Optional<UserDto> getUserById(UUID id) {
    return userRepository.findById(id).map(UserDto::fromEntity);
  }

  public boolean existsByUsername(String username) {
    return userRepository.existsByUsername(username);
  }

  public boolean existsByEmail(String email) {
    return userRepository.existsByEmail(email);
  }

  public UUID authenticatePassword(String email, String rawPassword) {
    if (email == null || email.isBlank() || rawPassword == null || rawPassword.isBlank()) {
      throw new IllegalArgumentException("Email and password are required.");
    }
    Optional<User> maybeUser = userRepository.findByEmail(email);
    if (maybeUser.isPresent()
        && passwordEncoder.matches(rawPassword, maybeUser.get().getPassword())) {
      return maybeUser.get().getId();
    }
    throw new SecurityException("Invalid credentials.");
  }

  @Transactional
  public UserDto createUser(String username, String email, String rawPassword) {
    if (existsByUsername(username) || existsByEmail(email)) {
      throw new IllegalArgumentException("Username or email already exists.");
    }

    User userRequest =
        User.builder()
            .username(username)
            .email(email)
            .password(passwordEncoder.encode(rawPassword))
            .role(UserRole.USER)
            .build();

    User repositoryResponse = userRepository.save(userRequest);
    return UserDto.fromEntity(repositoryResponse);
  }

  @Transactional
  public Optional<UserDto> updateUser(
      UUID id,
      String username,
      String email,
      String rawPassword,
      UUID requesterId,
      boolean isSuperAdmin) {
    return userRepository
        .findById(id)
        .map(
            user -> {
              if (!user.getId().equals(requesterId) && !isSuperAdmin) {
                throw new SecurityException("You are not authorized to update this user.");
              }

              // Ensure the user is CHANGING their email/username
              // by verifying the found user doesn't match the request
              if (!user.getUsername().equals(username) && existsByUsername(username)) {
                throw new IllegalArgumentException("Username already taken.");
              }

              if (!user.getEmail().equals(email) && existsByEmail(email)) {
                throw new IllegalArgumentException("Email already taken.");
              }

              if (username != null && !username.isBlank()) {
                user.setUsername(username);
              }
              if (email != null && !email.isBlank()) {
                user.setEmail(email);
              }
              if (rawPassword != null && !rawPassword.isBlank()) {
                user.setPassword(passwordEncoder.encode(rawPassword));
              }
              user.setUpdatedAt(Instant.now());

              User repositoryResponse = userRepository.save(user);
              return UserDto.fromEntity(repositoryResponse);
            });
  }

  @Transactional
  public void deleteUser(UUID id, UUID requesterId, boolean isSuperAdmin) {
    if (!isSuperAdmin) {
      throw new SecurityException("Only SuperAdmins can delete users.");
    }
    if (id.equals(requesterId)) {
      throw new IllegalArgumentException("SuperAdmins cannot delete themselves.");
    }
    userRepository.deleteById(id);
  }

  @Transactional
  public Optional<UserDto> changeUserRole(
      UUID id, UserRole newRole, UUID requesterId, boolean isSuperAdmin) {
    if (!isSuperAdmin) {
      throw new SecurityException("Only SuperAdmins can change user roles.");
    }
    if (id.equals(requesterId)) {
      throw new IllegalArgumentException("SuperAdmins cannot change their own role.");
    }

    return userRepository
        .findById(id)
        .map(
            user -> {
              user.setRole(newRole);
              user.setUpdatedAt(Instant.now());
              User repositoryResponse = userRepository.save(user);
              return UserDto.fromEntity(repositoryResponse);
            });
  }
}
