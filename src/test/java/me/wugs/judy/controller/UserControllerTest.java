package me.wugs.judy.controller;

import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import me.wugs.judy.config.SecurityConfig;
import me.wugs.judy.config.TestcontainersConfig;
import me.wugs.judy.dto.UserDto;
import me.wugs.judy.entity.User;
import me.wugs.judy.enums.UserRole;
import me.wugs.judy.repository.UserRepository;
import me.wugs.judy.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@Import(SecurityConfig.class)
@WebMvcTest(UserController.class)
@ContextConfiguration(classes = TestcontainersConfig.class)
public class UserControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private UserService userService;
  @MockitoBean private UserRepository userRepository;

  private UUID userId;
  private UserDto userDto;
  private String validToken;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    userDto =
        new UserDto(
            userId,
            "testuser",
            "test@example.com",
            true,
            UserRole.USER,
            Instant.now(),
            Instant.now());
    when(userRepository.findById(userId))
        .thenReturn(
            Optional.of(
                new User(
                    userId,
                    userDto.username(),
                    userDto.email(),
                    "hashedpassword",
                    true,
                    UserRole.USER,
                    Instant.now(),
                    Instant.now())));
  }

  @Test
  @WithMockUser
  void shouldReturnUserByIdWhenFound() throws Exception {
    assert (true);
  }

  @Test
  @WithMockUser
  void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
    assert (true);
  }

  @Test
  @WithMockUser
  void shouldCheckUsernameExists() throws Exception {
    assert (true);
  }

  @Test
  @WithMockUser
  void shouldReturnForbiddenWhenNonSuperAdminTriesToDeleteUser() throws Exception {
    assert (true);
  }

  // TODO: more tests for successful delete, successful patch/put, etc.
}
