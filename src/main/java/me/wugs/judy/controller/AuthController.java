package me.wugs.judy.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import me.wugs.judy.dto.*;
import me.wugs.judy.enums.RateLimitedEndpoint;
import me.wugs.judy.exception.UnauthorizedException;
import me.wugs.judy.security.RateLimitingInterceptor;
import me.wugs.judy.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final RateLimitingInterceptor rateLimiter;
  private final UserService userService;
  private final AuthenticationManager authenticationManager;

  // TODO: ignore case in login, ignore case in check exists, preserve casing from registration for
  // display
  @PostMapping("/login")
  public ResponseEntity<UserDto> login(
      @RequestBody @NotNull AuthLoginDto authLoginDto, HttpSession session) {

    UserDto user =
        userService
            .getUserByIdentifier(authLoginDto.identifier())
            .orElseThrow(() -> new UnauthorizedException("Invalid credentials."));

    try {
      Authentication authentication =
          authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(user.username(), authLoginDto.password()));

      SecurityContextHolder.getContext().setAuthentication(authentication);
      session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
      session.setAttribute("userId", user.id());

      return ResponseEntity.ok(user);

    } catch (AuthenticationException e) {
      throw new UnauthorizedException("Invalid credentials.");
    }
  }

  @PostMapping("/logout")
  public ResponseEntity<StatusResponseDto> logout(HttpSession session) {
    session.invalidate();
    return ResponseEntity.ok(new StatusResponseDto("Logged out."));
  }

  @PostMapping("/register")
  public ResponseEntity<UserDto> registerUser(
      @RequestBody @NotNull AuthRegisterDto authRegisterDto,
      HttpServletRequest request,
      HttpSession session) {
    // Only one registration is permitted per 5 minutes
    rateLimiter.checkRateLimit(request.getRemoteAddr(), RateLimitedEndpoint.register);
    UserDto user =
        userService.createUser(
            authRegisterDto.username(), authRegisterDto.email(), authRegisterDto.password());

    // Not wrapped in try-catch since we expect this to succeed; if it throws,
    // GlobalExceptionHandler's got us
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(user.username(), authRegisterDto.password()));
    SecurityContextHolder.getContext().setAuthentication(authentication);
    session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
    session.setAttribute("userId", user.id());

    return ResponseEntity.ok(user);
  }
}
