package me.wugs.judy.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import me.wugs.judy.dto.ErrorResponseDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  private final DataSource dataSource;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/api/auth/**")
                    .permitAll()
                    .requestMatchers("/api/users/exists/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
        .exceptionHandling(
            exception ->
                exception.authenticationEntryPoint(
                    (request, response, authException) -> {
                      response.setContentType("application/json");
                      response.setCharacterEncoding("UTF-8");

                      int status;
                      String errorType;
                      String message;

                      if (authException instanceof BadCredentialsException) {
                        status = HttpServletResponse.SC_UNAUTHORIZED;
                        errorType = "Unauthorized";
                        message = "Invalid credentials.";
                      } else if (authException instanceof InsufficientAuthenticationException) {
                        status = HttpServletResponse.SC_UNAUTHORIZED;
                        errorType = "Unauthorized";
                        message = "Please log in.";
                      } else {
                        status = HttpServletResponse.SC_FORBIDDEN;
                        errorType = "Forbidden";
                        message = "Access denied.";
                      }

                      // Set error status response and create error response
                      response.setStatus(status);
                      ErrorResponseDto errorResponseDto = new ErrorResponseDto(errorType, message);

                      // Write error record as JSON
                      ObjectMapper mapper = new ObjectMapper();
                      String json = mapper.writeValueAsString(errorResponseDto);
                      response.getWriter().write(json);
                    }));

    return http.build();
  }

  @Bean
  public UserDetailsService userDetailsService() {
    JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);
    manager.setUsersByUsernameQuery(
        "SELECT username, password, enabled FROM \"user\" WHERE username = ?");
    manager.setAuthoritiesByUsernameQuery("SELECT username, role FROM \"user\" WHERE username = ?");
    return manager;
  }

  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
