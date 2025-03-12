package me.wugs.judy.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import me.wugs.judy.config.SecurityConfig;
import me.wugs.judy.dto.UserDto;
import me.wugs.judy.entity.User;
import me.wugs.judy.enums.UserRole;
import me.wugs.judy.repository.UserRepository;
import me.wugs.judy.security.JwtUtil;
import me.wugs.judy.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@Import(SecurityConfig.class)
@WebMvcTest(UserController.class)
public class UserControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private UserService userService;
  @MockitoBean private JwtUtil jwtUtil;
  @MockitoBean private UserRepository userRepository;

  private UUID userId;
  private UserDto userDto;
  private String validToken;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    userDto =
        new UserDto(
            userId, "testuser", "test@example.com", UserRole.USER, Instant.now(), Instant.now());
    validToken = "mocked-jwt-token";
    when(jwtUtil.validateToken(validToken)).thenReturn(userId.toString());
    when(userRepository.findById(userId))
        .thenReturn(
            Optional.of(
                new User(
                    userId,
                    userDto.username(),
                    userDto.email(),
                    "hashedpassword",
                    UserRole.USER,
                    Instant.now(),
                    Instant.now())));
  }

  @Test
  @WithMockUser
  void shouldReturnUserByIdWhenFound() throws Exception {
    when(userService.getUserById(userId)).thenReturn(Optional.of(userDto));

    mockMvc
        .perform(
            get("/api/users/" + userId).header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(userId.toString()))
        .andExpect(jsonPath("$.username").value(userDto.username()))
        .andExpect(jsonPath("$.email").value(userDto.email()));
  }

  @Test
  @WithMockUser
  void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
    when(userService.getUserById(userId)).thenReturn(Optional.empty());

    mockMvc
        .perform(
            get("/api/users/" + userId).header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser
  void shouldCheckUsernameExists() throws Exception {
    when(userService.existsByUsername("testuser")).thenReturn(true);

    mockMvc
        .perform(
            get("/api/users/exists/username/testuser")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
        .andExpect(status().isOk())
        .andExpect(content().string("true"));
  }

  @Test
  @WithMockUser
  void shouldReturnForbiddenWhenNonSuperAdminTriesToDeleteUser() throws Exception {
    doThrow(new SecurityException("Only SuperAdmins can delete users."))
        .when(userService)
        .deleteUser(eq(userId), any(UUID.class), eq(false));

    mockMvc
        .perform(
            delete("/api/users/" + userId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken))
        .andExpect(status().isForbidden());
  }

  // TODO: more tests for successful delete, successful patch/put, etc.
}
