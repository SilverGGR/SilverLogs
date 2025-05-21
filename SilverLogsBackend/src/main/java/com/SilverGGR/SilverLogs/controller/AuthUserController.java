package com.SilverGGR.SilverLogs.controller;

import com.SilverGGR.SilverLogs.entity.AuthUser;
import com.SilverGGR.SilverLogs.repository.AuthUserRepository;
import com.SilverGGR.SilverLogs.security.AuthUserPrincipal;
import com.SilverGGR.SilverLogs.service.AuthUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/authUser")
@RequiredArgsConstructor
public class AuthUserController {

    private final AuthUserService authUserService;
    private final AuthUserRepository userRepository;

    @PostMapping("/upload-image")
    public ResponseEntity<String> uploadProfileImage(@RequestParam("file") MultipartFile file,
                                                     Authentication authentication) {

        return authUserService.uploadProfileImage(file, authentication);
    }

    @GetMapping("/image")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> getProfileImage(@AuthenticationPrincipal AuthUserPrincipal authUser) {
        AuthUser user = userRepository.findByUsername(authUser.getUsername());
        if (user == null) {
            throw new RuntimeException("Benutzer nicht gefunden");
        }

        if (user.getProfileImage() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(user.getProfileImageType()))
                .body(user.getProfileImage());
    }

    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentUserProfile(@AuthenticationPrincipal AuthUserPrincipal authUser) {
        AuthUser user = authUserService.findByUsername(authUser.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("firstname", user.getFirstname());
        response.put("lastname", user.getLastname());
        response.put("role", user.getRole());
        response.put("hasProfileImage", user.getProfileImage() != null);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateProfile(
            @RequestBody Map<String, String> updates,
            Authentication authentication) {

        String username = authentication.getName();
        return authUserService.updateProfile(username, updates);
    }
}