package com.ayhan.fleet_management.controller;

import com.ayhan.fleet_management.dto.AuthResponseDto;
import com.ayhan.fleet_management.dto.LoginRequestDto;
import com.ayhan.fleet_management.dto.RegisterRequestDto;
import com.ayhan.fleet_management.entity.User;
import com.ayhan.fleet_management.exception.InvalidCredentialsException;
import com.ayhan.fleet_management.security.JwtService;
import com.ayhan.fleet_management.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequestDto requestDto) {
        User user = userService.register(requestDto);
        String token = jwtService.generateToken(user);

        AuthResponseDto responseDto = AuthResponseDto.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto requestDto) {
        Authentication authentication;

        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requestDto.getUsername(), requestDto.getPassword())
            );
        } catch (BadCredentialsException exception) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        User user = (User) authentication.getPrincipal();
        String token = jwtService.generateToken(user);

        AuthResponseDto responseDto = AuthResponseDto.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();

        return ResponseEntity.ok(responseDto);
    }
}
