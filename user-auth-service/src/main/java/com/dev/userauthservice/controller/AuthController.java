package com.dev.userauthservice.controller;

import com.dev.userauthservice.dto.request.LoginRequest;
import com.dev.userauthservice.dto.request.RefreshTokenRequest;
import com.dev.userauthservice.dto.request.SignupRequest;
import com.dev.userauthservice.dto.response.APIResponse;
import com.dev.userauthservice.dto.response.AuthResponse;
import com.dev.userauthservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController extends BaseController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<APIResponse<AuthResponse>> signup(@Valid @RequestBody SignupRequest request) {
        log.info("Signup request received for username: {}", request.getUsername());
        AuthResponse response = authService.registerUser(request);
        log.info("User registered successfully: {}", request.getUsername());
        return created(response);
    }

    @PostMapping("/login")
    public ResponseEntity<APIResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for username: {}", request.getUsername());
        AuthResponse response = authService.authenticateUser(request);
        log.info("User logged in successfully: {}", request.getUsername());
        return success(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<APIResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.debug("Token refresh request received");
        AuthResponse response = authService.refreshAccessToken(request);
        log.debug("Access token refreshed successfully");
        return success(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<APIResponse<Void>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Logout request received");
        authService.logout(request);
        log.info("User logged out successfully");
        return success(null, "Logged out successfully");
    }
}