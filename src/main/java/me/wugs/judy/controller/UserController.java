package me.wugs.judy.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import me.wugs.judy.dto.UserDto;
import me.wugs.judy.enums.UserRole;
import me.wugs.judy.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  private boolean hasSuperAdminAuthority(Authentication auth) {
    return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"));
  }

  @GetMapping
  public ResponseEntity<List<UserDto>> getAllUsers() {
    return ResponseEntity.ok(userService.getAllUsers());
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserDto> getUserById(@PathVariable UUID id) {
    Optional<UserDto> user = userService.getUserById(id);

    return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping("/exists/username/{username}")
  public ResponseEntity<String> checkUsernameExists(@PathVariable String username) {
    return ResponseEntity.ok(String.valueOf(userService.existsByUsername(username)));
  }

  @GetMapping("/exists/email/{email}")
  public ResponseEntity<Boolean> checkEmailExists(@PathVariable String email) {
    return ResponseEntity.ok(userService.existsByEmail(email));
  }

  // TODO: re-write this to take a DTO for the update request and test it properly
  @PutMapping("/{id}")
  public ResponseEntity<UserDto> updateUser(
      @PathVariable UUID id,
      @RequestParam String username,
      @RequestParam String email,
      @RequestParam(required = false) String password,
      Authentication auth) {
    // JwtAuthFilter supplies user UUID as the "name" in auth, which is required
    if (auth == null) {
      throw new SecurityException("Only SuperAdmins can delete users.");
    }
    UUID requesterId = UUID.fromString(auth.getName());

    return userService
        .updateUser(id, username, email, password, requesterId, hasSuperAdminAuthority(auth))
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(@PathVariable UUID id, Authentication auth) {
    if (auth == null) {
      throw new SecurityException("Only SuperAdmins can delete users.");
    }
    UUID requesterId = UUID.fromString(auth.getName());

    userService.deleteUser(id, requesterId, hasSuperAdminAuthority(auth));
    return ResponseEntity.noContent().build();
  }

  // TODO: update how this is handled, maybe use DTO, unsure, but also needs to be tested
  @PatchMapping("/{id}/role")
  public ResponseEntity<UserDto> changeUserRole(
      @PathVariable UUID id, @RequestParam UserRole newRole, Authentication auth) {
    if (auth == null) {
      throw new SecurityException("Only SuperAdmins can change user roles.");
    }
    UUID requesterId = UUID.fromString(auth.getName());

    return userService
        .changeUserRole(id, newRole, requesterId, hasSuperAdminAuthority(auth))
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }
}
