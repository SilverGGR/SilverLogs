package com.SilverGGR.SilverLogs.controller;

import com.SilverGGR.SilverLogs.entity.AuthUser;
import com.SilverGGR.SilverLogs.repository.AuthUserRepository;
import com.SilverGGR.SilverLogs.service.AuthUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    public ResponseEntity<byte[]> getProfileImage(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            String username = authentication.getName();
            AuthUser user = userRepository.findByUsername(username);
            if (user == null) {
                throw new RuntimeException("Benutzer nicht gefunden");
            }

            if (user.getProfileImage() == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(user.getProfileImageType()))
                    .body(user.getProfileImage());
        } catch (Exception e) {
            System.err.println("Fehler beim Laden des Profilbilds: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentUserProfile(Authentication authentication) {
        try {
            String username = authentication.getName();
            AuthUser user = authUserService.findByUsername(username);

            Map<String, Object> response = new HashMap<>();
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("firstname", user.getFirstname());
            response.put("lastname", user.getLastname());
            response.put("role", user.getRole());
            response.put("hasProfileImage", user.getProfileImage() != null);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateProfile(
            @RequestBody Map<String, String> updates,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            return authUserService.updateProfile(username, updates);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler bei der Aktualisierung: " + e.getMessage());
        }
    }
}