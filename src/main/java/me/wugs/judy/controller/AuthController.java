package me.wugs.judy.controller;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import me.wugs.judy.dto.AuthLoginDto;
import me.wugs.judy.dto.AuthRegisterDto;
import me.wugs.judy.dto.TokenResponseDto;
import me.wugs.judy.dto.UserDto;
import me.wugs.judy.security.JwtUtil;
import me.wugs.judy.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final JwtUtil jwtUtil;
  private final UserService userService;

  @GetMapping
  public ResponseEntity<String> getMyAuth(Authentication auth) {
    if (auth == null) {
      return ResponseEntity.ok("");
    } else {
      return ResponseEntity.ok(auth.getName());
    }
  }

  @PostMapping("/login")
  public ResponseEntity<TokenResponseDto> login(@RequestBody @NotNull AuthLoginDto authLoginDto) {
    UUID userId = userService.authenticatePassword(authLoginDto.email(), authLoginDto.password());
    String token = jwtUtil.generateToken(userId);

    return ResponseEntity.ok(new TokenResponseDto(token));
  }

  @PostMapping("/register")
  public ResponseEntity<TokenResponseDto> registerUser(
      @RequestBody @NotNull AuthRegisterDto authRegisterDto) {
    UserDto user =
        userService.createUser(
            authRegisterDto.username(), authRegisterDto.email(), authRegisterDto.password());
    String token = jwtUtil.generateToken(user.id());

    return ResponseEntity.ok(new TokenResponseDto(token));
  }
}
