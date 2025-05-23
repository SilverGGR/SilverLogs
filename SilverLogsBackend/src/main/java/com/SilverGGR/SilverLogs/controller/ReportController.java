package com.SilverGGR.SilverLogs.controller;

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

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/{weekStartDate}")
    public ResponseEntity<ReportDto> getReportByUserAndDate(@PathVariable LocalDate weekStartDate, @AuthenticationPrincipal AuthUserPrincipal authUserPrincipal) {
        return ResponseEntity.ok(reportService.getReportByUserAndDate(weekStartDate, authUserPrincipal.getUsername()));
    }

    @PostMapping("/save")
    public ResponseEntity<ReportDto> saveReport(@RequestBody ReportDto reportDto, @AuthenticationPrincipal AuthUserPrincipal authUserPrincipal) {
        return ResponseEntity.ok(reportService.getOrCreateReport(reportDto, authUserPrincipal.getUsername()));
    }
}
