package com.SilverGGR.SilverLogs.service;

import com.SilverGGR.SilverLogs.dtos.AuthUserDto;
import com.SilverGGR.SilverLogs.entity.Apprentice;
import com.SilverGGR.SilverLogs.entity.AuthUser;
import com.SilverGGR.SilverLogs.enums.Role;
import com.SilverGGR.SilverLogs.repository.ApprenticeRepository;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthUserService {

    private final AuthUserRepository authUserRepo;
    private final ApprenticeRepository apprenticeRepo;
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

            // Größenbeschränkung (z. B. 10 MB)
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

    public String setup() {
        if (authUserRepo.count() > 0 && apprenticeRepo.count() > 0) {
            return "Database is not empty";
        } else {
            Apprentice apprentice = new Apprentice();
            apprentice.setUsername("user");
            apprentice.setFirstname("Erster");
            apprentice.setLastname("Guy");
            apprentice.setEmail("user@email.com");
            apprentice.setRole(Role.USER);
            apprentice.setPassword(encoder.encode("user"));
            apprentice.setStartingDate(LocalDate.of(2024, 7, 1));
            apprentice.setEndingDate(LocalDate.of(2027, 7, 1));
            apprenticeRepo.save(apprentice);

            AuthUser adminUser = new AuthUser();
            adminUser.setUsername("admin");
            adminUser.setFirstname("Erster");
            adminUser.setLastname("Dude");
            adminUser.setEmail("admin@email.com");
            adminUser.setRole(Role.ADMIN);
            adminUser.setPassword(encoder.encode("admin"));
            authUserRepo.save(adminUser);

            return "Setup successful";
        }
    }

    @Transactional(readOnly = true)
    public AuthUserDto[] getAllApprentice() {
        List<Apprentice> apprenticeList = apprenticeRepo.findAll();
        return apprenticeList.stream()
                .map(this::convertToDto)
                .toArray(AuthUserDto[]::new);
    }

    @Transactional(readOnly = true)
    public AuthUserDto[] getAllSupervisors() {
        List<AuthUser> supervisorList = authUserRepo.findByRole(Role.SUPERVISOR);
        return supervisorList.stream()
                .map(this::convertToDto)
                .toArray(AuthUserDto[]::new);
    }

    // Hilfsmethode zum Konvertieren von AuthUser/Apprentice zu AuthUserDto
    private AuthUserDto convertToDto(AuthUser user) {
        AuthUserDto dto = new AuthUserDto();
        dto.setUsername(user.getUsername());
        dto.setFirstname(user.getFirstname());
        dto.setLastname(user.getLastname());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        return dto;
    }


}