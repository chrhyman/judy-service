package me.wugs.judy.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;
import me.wugs.judy.config.TestcontainersConfig;
import me.wugs.judy.entity.User;
import me.wugs.judy.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = TestcontainersConfig.class)
public class UserRepositoryTest {

  @Autowired private UserRepository userRepository;

  private final String SUPERADMIN_USERNAME = "SuperAdmin";
  private final String SUPERADMIN_EMAIL = "superadmin@wugs.me";

  @Test
  void shouldFindSuperAdminByEmail() {
    Optional<User> user = userRepository.findByEmail(SUPERADMIN_EMAIL);

    assertThat(user).isPresent();
    assertThat(user.get().getUsername()).isEqualTo(SUPERADMIN_USERNAME);
    assertThat(user.get().getRole()).isEqualTo(UserRole.SUPERADMIN);
  }

  @Test
  void shouldFindSuperAdminByUsername() {
    Optional<User> user = userRepository.findByUsername(SUPERADMIN_USERNAME);

    assertThat(user).isPresent();
    assertThat(user.get().getEmail()).isEqualTo(SUPERADMIN_EMAIL);
    assertThat(user.get().getRole()).isEqualTo(UserRole.SUPERADMIN);
  }

  @Test
  void existsByUsername_findSeededUser() {
    boolean seededUser = userRepository.existsByUsername(SUPERADMIN_USERNAME);
    boolean unknownUser = userRepository.existsByUsername("unknown");

    assertThat(seededUser).isTrue();
    assertThat(unknownUser).isFalse();
  }

  @Test
  void existsByEmail_findSeededUser() {
    boolean seededUser = userRepository.existsByEmail(SUPERADMIN_EMAIL);
    boolean unknownUser = userRepository.existsByEmail("unknown");

    assertThat(seededUser).isTrue();
    assertThat(unknownUser).isFalse();
  }

  @Test
  void shouldSaveAndRetrieveUser() {
    User user =
        User.builder()
            .username("testuser")
            .email("test@example.com")
            .password("hashedpassword")
            .role(UserRole.USER)
            .build();

    User savedUser = userRepository.save(user);
    Optional<User> retrievedUserById = userRepository.findById(savedUser.getId());
    Optional<User> retrievedUserByUsername = userRepository.findByUsername(savedUser.getUsername());
    boolean existsByUsername = userRepository.existsByUsername(savedUser.getUsername());
    boolean existsByEmail = userRepository.existsByEmail(savedUser.getEmail());

    assertThat(retrievedUserById).isPresent();
    assertThat(retrievedUserById.get().getUsername()).isEqualTo("testuser");
    assertThat(retrievedUserById.get().getEmail()).isEqualTo("test@example.com");
    assertThat(retrievedUserByUsername).isPresent();
    assertThat(existsByUsername).isTrue();
    assertThat(existsByEmail).isTrue();
  }

  @Test
  void shouldDeleteUser() {
    User user =
        User.builder()
            .username("deletableUser")
            .email("delete@example.com")
            .password("hashedpassword")
            .role(UserRole.USER)
            .build();

    User savedUser = userRepository.save(user);
    UUID userId = savedUser.getId();
    userRepository.deleteById(userId);

    assertThat(userRepository.findById(userId)).isEmpty();
  }
}
