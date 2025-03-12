package me.wugs.judy.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

  private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000; // 1 day in millis

  private static final SecretKey key = Jwts.SIG.HS256.key().build();

  public String generateToken(UUID userId) {
    return Jwts.builder()
        .subject(userId.toString())
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
        .signWith(key)
        .compact();
  }

  public String validateToken(String token) {
    try {
      Jws<Claims> jws = Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
      return jws.getPayload().getSubject();
    } catch (JwtException e) {
      return null;
    }
  }
}
