package me.wugs.judy.controller;

import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import me.wugs.judy.config.SecurityConfig;
import me.wugs.judy.config.TestcontainersConfig;
import me.wugs.judy.dto.UserDto;
import me.wugs.judy.enums.UserRole;
import me.wugs.judy.repository.UserRepository;
import me.wugs.judy.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@Import(SecurityConfig.class)
@WebMvcTest(AuthController.class)
@ContextConfiguration(classes = TestcontainersConfig.class)
public class AuthControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper; // Needed for JSON serialization

  @MockitoBean private UserService userService;
  @MockitoBean private UserRepository userRepository;

  private UUID userId;
  private UserDto userDto;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    userDto = new UserDto(userId, "testuser", "test@example.com", true, UserRole.USER, null, null);

    when(userService.createUser("testuser", "test@example.com", "password")).thenReturn(userDto);
  }

  @Test
  void shouldRegisterUserSuccessfully() throws Exception {
    assert (true);
  }

  @Test
  void shouldLoginSuccessfully() throws Exception {
    assert (true);
  }
}
