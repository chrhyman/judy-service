package me.wugs.judy.security;

import static org.assertj.core.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtUtilTest {

  private JwtUtil jwtUtil;
  private UUID userId;

  @BeforeEach
  void setUp() {
    jwtUtil = new JwtUtil();
    userId = UUID.randomUUID();
  }

  @Test
  void shouldGenerateAndValidateToken() {
    String token = jwtUtil.generateToken(userId);

    assertThat(token).isNotNull();
    assertThat(jwtUtil.validateToken(token)).isEqualTo(userId.toString());
  }

  @Test
  void shouldReturnNullForInvalidToken() {
    assertThat(jwtUtil.validateToken("invalid.token.here")).isNull();
  }
}
