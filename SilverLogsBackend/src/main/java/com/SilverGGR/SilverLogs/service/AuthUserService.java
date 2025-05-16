package com.SilverGGR.SilverLogs.service;

import com.SilverGGR.SilverLogs.entity.AuthUser;
import com.SilverGGR.SilverLogs.repository.AuthUserRepository;
import com.SilverGGR.SilverLogs.security.JWTService;
import com.SilverGGR.SilverLogs.security.MyUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthUserService {

    private final AuthUserRepository authUserRepo;
    private final AuthenticationManager authManager;
    private final JWTService jwtService;
    private final MyUserDetailsService userDetailsService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public AuthUser register(AuthUser authUser) {
        authUser.setPassword(encoder.encode(authUser.getPassword()));
        return authUserRepo.save(authUser);
    }

    public String verify(AuthUser authUser) {
        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authUser.getUsername(), authUser.getPassword()));

            if (authentication.isAuthenticated()) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(authUser.getUsername());
                return jwtService.generateToken(userDetails);
            }
            throw new BadCredentialsException("Invalid credentials");
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Authentication failed: " + e.getMessage());
        }
    }

    @Transactional
    public ResponseEntity<String> uploadProfileImage(MultipartFile file, Authentication authentication) {
        try {
            String username = authentication.getName();
            AuthUser user = authUserRepo.findByUsername(username);
            if (user == null) {
                throw new RuntimeException("Benutzer nicht gefunden");
            }

            // Validierung des Dateityps
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body("Nur Bilddateien sind erlaubt");
            }

            // Größenbeschränkung (z.B. 10MB)
            if (file.getSize() > 2 * 1024 * 1024) {
                return ResponseEntity.badRequest().body("Datei darf nicht größer als 10MB sein");
            }

            // Bild in der Datenbank speichern
            user.setProfileImage(file.getBytes());
            user.setProfileImageType(contentType);
            authUserRepo.save(user);

            return ResponseEntity.ok("Profilbild erfolgreich hochgeladen");
        } catch (Exception e) {
            System.err.println("Fehler beim Hochladen des Profilbilds: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler beim Hochladen: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public AuthUser findByUsername(String username) {
        try {
            return authUserRepo.findByUsername(username);
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional
    public ResponseEntity<String> updateProfile(String username, Map<String, String> updates) {
        AuthUser user = authUserRepo.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Benutzer nicht gefunden");
        }

        // Update nur der erlaubten Felder
        if (updates.containsKey("email")) {
            user.setEmail(updates.get("email"));
        }

        if (updates.containsKey("firstname")) {
            user.setFirstname(updates.get("firstname"));
        }

        if (updates.containsKey("lastname")) {
            user.setLastname(updates.get("lastname"));
        }

        authUserRepo.save(user);
        return ResponseEntity.ok("Profil erfolgreich aktualisiert");
    }


}