package me.wugs.judy.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import me.wugs.judy.repository.UserRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Requires that a JWT (JSON Web Token) is present in the Authorization header of requests to
 * secured endpoints. The subject of the JWT contains the userId. This ID is stored with the user
 * role in Spring's SecurityContext.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final UserRepository userRepository;

  protected void doFilterInternal(
      @NotNull HttpServletRequest request,
      @NotNull HttpServletResponse response,
      @NotNull FilterChain chain)
      throws ServletException, IOException {
    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      chain.doFilter(request, response);
      return;
    }

    String token = authHeader.substring(7);
    String userId = jwtUtil.validateToken(token);

    if (userId != null) {
      Optional<me.wugs.judy.entity.User> maybeUser =
          userRepository.findById(UUID.fromString(userId));
      maybeUser.ifPresent(
          user -> {
            // Treat unique UUID as the "Username" in Spring Security
            UserDetails userDetails =
                User.withUsername(maybeUser.get().getId().toString())
                    .password("")
                    .authorities(
                        Collections.singletonList(
                            new SimpleGrantedAuthority("ROLE_" + maybeUser.get().getRole().name())))
                    .build();
            UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
          });
    }

    chain.doFilter(request, response);
  }
}
