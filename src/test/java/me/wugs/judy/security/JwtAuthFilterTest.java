package me.wugs.judy.security;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;
import java.util.UUID;
import me.wugs.judy.config.SecurityConfig;
import me.wugs.judy.entity.User;
import me.wugs.judy.enums.UserRole;
import me.wugs.judy.repository.UserRepository;
import me.wugs.judy.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@Import(SecurityConfig.class)
@WebMvcTest
@AutoConfigureMockMvc
class JwtAuthFilterTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private UserService userService;
  @MockitoBean private JwtUtil jwtUtil;
  @MockitoBean private UserRepository userRepository;

  private String token;
  private UUID userId;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    token = "Bearer mock.jwt.token";

    User user =
        new User(userId, "testuser", "test@example.com", "hashedpass", UserRole.USER, null, null);
    when(jwtUtil.validateToken("mock.jwt.token")).thenReturn(userId.toString());
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
  }

  @Test
  void shouldAllowRequestWithoutToken() throws Exception {
    // Relies on AuthController GET /api/auth (200 OK, UUID or empty string is returned)
    mockMvc.perform(get("/api/auth")).andExpect(status().isOk()); // Public route should pass
  }

  @Test
  void shouldAuthenticateWithValidToken() throws Exception {
    // Relies on UserController GET /api/users (must be an authorized user to GET)
    mockMvc.perform(get("/api/users").header("Authorization", token)).andExpect(status().isOk());
  }

  @Test
  void shouldRejectInvalidToken() throws Exception {
    // Relies on UserController GET /api/users (must be an authorized user to GET)
    when(jwtUtil.validateToken("invalid.token")).thenReturn(null);

    mockMvc
        .perform(get("/api/users").header("Authorization", "Bearer invalid.token"))
        .andExpect(status().isForbidden());
  }
}
