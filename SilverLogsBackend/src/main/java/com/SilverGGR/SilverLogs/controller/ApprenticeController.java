package com.SilverGGR.SilverLogs.controller;

import com.SilverGGR.SilverLogs.dtos.ApprenticeDto;
import com.SilverGGR.SilverLogs.dtos.AuthUserDto;
import com.SilverGGR.SilverLogs.service.ApprenticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/apprentice")
@RequiredArgsConstructor
public class ApprenticeController {

    private final ApprenticeService apprenticeService;
    @PostMapping("/admin/createApprentice")
    public ResponseEntity<AuthUserDto> createApprentice(@RequestBody ApprenticeDto userDto) {
        if (apprenticeService.checkIfUsernameExists(userDto.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
        return ResponseEntity.ok(apprenticeService.createApprentice(userDto));
    }
}
