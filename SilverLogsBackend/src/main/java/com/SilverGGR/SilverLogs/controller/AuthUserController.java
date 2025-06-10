package com.SilverGGR.SilverLogs.controller;

import com.SilverGGR.SilverLogs.dtos.ApprenticeDto;
import com.SilverGGR.SilverLogs.dtos.AuthUserDto;
import com.SilverGGR.SilverLogs.entity.AuthUser;
import com.SilverGGR.SilverLogs.repository.AuthUserRepository;
import com.SilverGGR.SilverLogs.security.AuthUserPrincipal;
import com.SilverGGR.SilverLogs.service.AuthUserService;
import com.SilverGGR.SilverLogs.service.SupervisorApprenticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/authUser")
@RequiredArgsConstructor
public class AuthUserController {

    private final AuthUserService authUserService;
    private final SupervisorApprenticeService supervisorApprenticeService;
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
    public ResponseEntity<String> updateProfile(@RequestBody AuthUserDto authUserDto) {
        return authUserService.updateProfile(authUserDto);
    }

    @GetMapping("/all")
    public ResponseEntity<List<AuthUserDto>> getAllUsers() {
        return ResponseEntity.ok(authUserService.getAll());
    }

    @PostMapping("/admin/createUser")
    public ResponseEntity<AuthUserDto> createUser(@RequestBody AuthUserDto userDto) {
        if (authUserService.checkIfUsernameExists(userDto.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
        return ResponseEntity.ok(authUserService.createAuthUser(userDto));
    }

    @PutMapping("/admin/update")
    public ResponseEntity<AuthUserDto> updateUser(@RequestBody AuthUserDto authUserDto) {
        return ResponseEntity.ok(authUserService.updateUser(authUserDto));
    }

    @DeleteMapping("/admin/delete/{username}")
    public ResponseEntity<Void> deleteUser(@PathVariable String username) {
        authUserService.deleteUser(username);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/apprentice/all")
    public ResponseEntity<List<AuthUserDto>> getAllApprentices() {
        return ResponseEntity.ok(authUserService.getAllApprentice());
    }
    @GetMapping("/supervisors/all")
    public ResponseEntity<List<AuthUserDto>> getAllSupervisorsAndAdmins() {
        return ResponseEntity.ok(authUserService.getAllSupervisorsAndAdmins());
    }

    @GetMapping("/supervisors-for-apprentice")
    public ResponseEntity<List<AuthUserDto>> getSupervisorsForApprentice(@RequestParam String apprenticeUsername) {
        return ResponseEntity.ok(supervisorApprenticeService.getSupervisorsByApprentice(apprenticeUsername));
    }

    @GetMapping("/apprentice-for-supervisors")
    public ResponseEntity<List<AuthUserDto>> getApprenticeForSupervisor(@AuthenticationPrincipal AuthUserPrincipal authUserPrincipal) {
        return ResponseEntity.ok(supervisorApprenticeService.getApprenticesBySupervisor(authUserPrincipal.getUsername()));
    }

    @PostMapping("/set-supervisor")
    public ResponseEntity<String> setSupervisor(@RequestParam String apprenticeUsername, @RequestParam String supervisorUsername) {
        supervisorApprenticeService.assignApprenticeToSupervisor(apprenticeUsername, supervisorUsername);
        return ResponseEntity.ok("Supervisor successfully assigned");
    }

    @DeleteMapping("/delete-connection")
    public ResponseEntity<String> deleteConnection(@RequestParam String apprenticeUsername, @RequestParam String supervisorUsername) {
        supervisorApprenticeService.deleteConnection(apprenticeUsername, supervisorUsername);
        return ResponseEntity.ok("Supervisor successfully deleted");
    }

    @GetMapping("/start-end-date")
    public ResponseEntity<List<LocalDate>> getStartAndEndDate(@AuthenticationPrincipal AuthUserPrincipal authUser) {
        return ResponseEntity.ok(authUserService.getStartAndEndDate(authUser));
    }
}