package me.wugs.judy.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import me.wugs.judy.dto.*;
import me.wugs.judy.exception.UnauthorizedException;
import me.wugs.judy.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final UserService userService;
  private final AuthenticationManager authenticationManager;

  @GetMapping
  public ResponseEntity<String> getMyAuth(Authentication auth) {
    if (auth == null) {
      return ResponseEntity.ok("");
    } else {
      return ResponseEntity.ok(auth.getName());
    }
  }

  @PostMapping("/login")
  public ResponseEntity<UserDto> login(
      @RequestBody @NotNull AuthLoginDto authLoginDto, HttpSession session) {

    UserDto user =
        userService
            .getUserByIdentifier(authLoginDto.identifier())
            .orElseThrow(() -> new UnauthorizedException("Invalid credentials."));

    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(user.username(), authLoginDto.password()));

    session.setAttribute("userId", user.id());

    return ResponseEntity.ok(user);
  }

  @PostMapping("/logout")
  public ResponseEntity<StatusResponseDto> logout(
      @CookieValue("refreshToken") String refreshToken, HttpServletResponse response) {

    return ResponseEntity.ok(new StatusResponseDto("Logged out."));
  }

  @PostMapping("/register")
  public ResponseEntity<UserDto> registerUser(
      @RequestBody @NotNull AuthRegisterDto authRegisterDto, HttpServletResponse response) {
    UserDto user =
        userService.createUser(
            authRegisterDto.username(), authRegisterDto.email(), authRegisterDto.password());

    return ResponseEntity.ok(user);
  }
}
