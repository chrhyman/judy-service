package me.wugs.judy.dto;

import java.time.Instant;
import java.util.UUID;
import me.wugs.judy.entity.User;
import me.wugs.judy.enums.UserRole;

/**
 * A DTO that excludes the password from the entity result to prevent accidental leakage of (hashed)
 * passwords
 *
 * @param id UUID
 * @param username String
 * @param email String
 * @param enabled boolean
 * @param role UserRole
 * @param createdAt Instant
 * @param updatedAt Instant
 */
public record UserDto(
    UUID id,
    String username,
    String email,
    boolean enabled,
    UserRole role,
    Instant createdAt,
    Instant updatedAt) {
  public static UserDto fromEntity(User user) {
    return new UserDto(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        user.isEnabled(),
        user.getRole(),
        user.getCreatedAt(),
        user.getUpdatedAt());
  }
}
