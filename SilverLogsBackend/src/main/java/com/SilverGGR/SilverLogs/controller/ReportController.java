package com.SilverGGR.SilverLogs.controller;

import com.SilverGGR.SilverLogs.dtos.ReportBadgeDto;
import com.SilverGGR.SilverLogs.dtos.ReportDto;
import com.SilverGGR.SilverLogs.entity.Report;
import com.SilverGGR.SilverLogs.repository.AuthUserRepository;
import com.SilverGGR.SilverLogs.security.AuthUserPrincipal;
import com.SilverGGR.SilverLogs.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/{weekStartDate}")
    public ResponseEntity<ReportDto> getReportByUserAndDate(@PathVariable LocalDate weekStartDate, @AuthenticationPrincipal AuthUserPrincipal authUserPrincipal) {
        return ResponseEntity.ok(reportService.getReportByUserAndDate(weekStartDate, authUserPrincipal.getUsername()));
    }

    @GetMapping("/apprentice/{weekStartDate}/{username}")
    public ResponseEntity<ReportDto> getApprenticeReportByUserAndDate(@PathVariable LocalDate weekStartDate, @PathVariable String username) {
        return ResponseEntity.ok(reportService.getReportByUserAndDate(weekStartDate, username));
    }

    @GetMapping("/getAllBadges")
    public ResponseEntity<List<ReportBadgeDto>> getBadge(@AuthenticationPrincipal AuthUserPrincipal authUserPrincipal) {
        return ResponseEntity.ok(reportService.getAllBadges(authUserPrincipal.getUsername()));
    }

    @GetMapping("/apprentice/getAllBadges/{username}")
    public ResponseEntity<List<ReportBadgeDto>> getApprenticeBadge(@PathVariable String username) {
        return ResponseEntity.ok(reportService.getAllBadges(username));
    }

    @PostMapping("/save")
    public ResponseEntity<ReportDto> saveReport(@RequestBody ReportDto reportDto, @AuthenticationPrincipal AuthUserPrincipal authUserPrincipal) {
        return ResponseEntity.ok(reportService.getOrCreateReport(reportDto, authUserPrincipal.getUsername()));
    }

    @PostMapping("/approve/{weekStart}/{username}")
    public ResponseEntity<?> approveReport(@PathVariable LocalDate weekStart, @PathVariable String username, @RequestBody Map<String, String> body, @AuthenticationPrincipal AuthUserPrincipal authUserPrincipal) {
        return reportService.setApproved(weekStart, username, body.get("comment"), authUserPrincipal.getUsername());
    }

    @PostMapping("/reject/{weekStartDate}/{username}")
    public ResponseEntity<?> rejectReport(@PathVariable LocalDate weekStartDate, @PathVariable String username, @RequestBody Map<String, String> body) {
        return reportService.setRejected(weekStartDate, username, body.get("comment"));
    }
}
