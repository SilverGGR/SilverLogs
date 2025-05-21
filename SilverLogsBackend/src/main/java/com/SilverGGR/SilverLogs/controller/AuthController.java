package com.SilverGGR.SilverLogs.controller;

import com.SilverGGR.SilverLogs.entity.AuthUser;
import com.SilverGGR.SilverLogs.service.AuthUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthUserService service;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody AuthUser authUser) {
        String token = service.verify(authUser);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody AuthUser authUser) {
        AuthUser registeredUser = service.register(authUser);
        return ResponseEntity.ok("User registered successfully");
    }

    @GetMapping("/setup")
    public String setup() {
        return service.setup();
    }
}