package me.wugs.judy.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import me.wugs.judy.config.SecurityConfig;
import me.wugs.judy.dto.AuthLoginDto;
import me.wugs.judy.dto.AuthRegisterDto;
import me.wugs.judy.dto.UserDto;
import me.wugs.judy.enums.UserRole;
import me.wugs.judy.repository.UserRepository;
import me.wugs.judy.security.JwtUtil;
import me.wugs.judy.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@Import(SecurityConfig.class)
@WebMvcTest(AuthController.class)
public class AuthControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper; // Needed for JSON serialization

  @MockitoBean private UserService userService;
  @MockitoBean private JwtUtil jwtUtil;
  @MockitoBean private UserRepository userRepository;

  private UUID userId;
  private UserDto userDto;
  private String token;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    userDto = new UserDto(userId, "testuser", "test@example.com", UserRole.USER, null, null);
    token = "mocked-jwt-token";

    when(userService.createUser("testuser", "test@example.com", "password")).thenReturn(userDto);
    when(jwtUtil.generateToken(userId)).thenReturn(token);
    when(userService.authenticatePassword("test@example.com", "password")).thenReturn(userId);
  }

  @Test
  void shouldRegisterUserSuccessfully() throws Exception {
    AuthRegisterDto authRegisterDto =
        new AuthRegisterDto(userDto.username(), userDto.email(), "password");

    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRegisterDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value(token));
  }

  @Test
  void shouldLoginSuccessfully() throws Exception {
    AuthLoginDto authLoginDto = new AuthLoginDto("test@example.com", "password");

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authLoginDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value(token));
  }
}
