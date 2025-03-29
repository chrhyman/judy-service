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
import me.wugs.judy.exception.BadRequestException;
import me.wugs.judy.exception.ForbiddenException;
import me.wugs.judy.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private static final String alphaNumericPlusUnderscore = "^[a-zA-Z0-9_]+$";

  public List<UserDto> getAllUsers() {
    return userRepository.findAll().stream().map(UserDto::fromEntity).toList();
  }

  public Optional<UserDto> getUserById(UUID id) {
    return userRepository.findById(id).map(UserDto::fromEntity);
  }

  public Optional<UserDto> getUserByIdentifier(String identifier) {
    Optional<User> userByUsername = userRepository.findByUsername(identifier);
    Optional<User> userByEmail = userRepository.findByEmail(identifier);
    return userByUsername.or(() -> userByEmail).map(UserDto::fromEntity);
  }

  public boolean existsByUsername(String username) {
    return userRepository.existsByUsername(username);
  }

  public boolean existsByEmail(String email) {
    return userRepository.existsByEmail(email);
  }

  @Transactional
  public UserDto createUser(String username, String email, String rawPassword) {
    if (existsByUsername(username) || existsByEmail(email))
      throw new BadRequestException("Username or identifier already exists.");

    if (username.isBlank()) throw new BadRequestException("Username cannot be blank.");
    if (email.isBlank()) throw new BadRequestException("Email cannot be blank.");
    if (rawPassword.isBlank()) throw new BadRequestException("Password cannot be blank.");

    if (!username.matches(alphaNumericPlusUnderscore))
      throw new BadRequestException(
          "Username may only include alphanumeric and underscore characters.");

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
                throw new ForbiddenException("You are not authorized to update this user.");
              }

              // Ensure the user is CHANGING their identifier/username
              // by verifying the found user doesn't match the request
              if (!user.getUsername().equals(username) && existsByUsername(username)) {
                throw new BadRequestException("Username already taken.");
              }

              if (!user.getEmail().equals(email) && existsByEmail(email)) {
                throw new BadRequestException("Email already taken.");
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

    Optional<User> requester = userRepository.findById(requesterId);
    if (!isSuperAdmin || requester.isEmpty() || requester.get().getRole() != UserRole.SUPERADMIN) {
      throw new ForbiddenException("Only SuperAdmins can delete users.");
    }
    if (id.equals(requesterId)) {
      throw new BadRequestException("SuperAdmins cannot delete themselves.");
    }
    userRepository.deleteById(id);
  }

  @Transactional
  public Optional<UserDto> changeUserRole(
      UUID id, UserRole newRole, UUID requesterId, boolean isSuperAdmin) {

    Optional<User> requester = userRepository.findById(requesterId);
    if (!isSuperAdmin || requester.isEmpty() || requester.get().getRole() != UserRole.SUPERADMIN) {
      throw new ForbiddenException("Only SuperAdmins can change user roles.");
    }
    if (id.equals(requesterId)) {
      throw new BadRequestException("SuperAdmins cannot change their own role.");
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
