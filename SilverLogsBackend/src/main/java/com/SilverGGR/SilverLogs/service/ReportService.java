package com.SilverGGR.SilverLogs.service;

import com.SilverGGR.SilverLogs.dtos.ReportBadgeDto;
import com.SilverGGR.SilverLogs.dtos.ReportDto;
import com.SilverGGR.SilverLogs.entity.Apprentice;
import com.SilverGGR.SilverLogs.entity.Report;
import com.SilverGGR.SilverLogs.repository.ApprenticeRepository;
import com.SilverGGR.SilverLogs.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final AuthUserService authUserService;
    private final DtoMapper dtoMapper;

    public ReportDto getReportByUserAndDate(LocalDate weekStartDate, String username) {
       Report report = reportRepository.findByAuthUser_UsernameAndWeekStart(username, weekStartDate);
        if (report == null) {
            ReportDto newReport = new ReportDto();

            newReport.setWeekStart(weekStartDate);
            newReport.setWeekEnd(weekStartDate.plusDays(6));
            newReport.setWeekText("");
            newReport.setInstructionText("");
            newReport.setSchoolText("");
            newReport.setExtraText(null);
            newReport.setDepartment(null);
            newReport.setSubmitted(false);
            newReport.setApproved(false);
            newReport.setRejected(false);
            newReport.setComment(null);
            return newReport;
        }

        return dtoMapper.convertReportToDto(report);
    }

    public ReportDto getOrCreateReport(ReportDto reportDto, String username) {
        // Versuche, den bestehenden Report zu laden
        Report report = reportRepository.findByAuthUser_UsernameAndWeekStart(username, reportDto.getWeekStart());

        if (report == null) {
            // Falls kein Report existiert, erstelle einen neuen
            report = new Report();
            report.setAuthUser(authUserService.findByUsername(username));
            report.setWeekStart(reportDto.getWeekStart());
            report.setWeekEnd(reportDto.getWeekEnd());
        }
        dtoMapper.convertDtoToReport(report, reportDto);

        Report savedReport = saveReport(report);
        return dtoMapper.convertReportToDto(savedReport);
    }

    public Report saveReport(Report report) {
        return reportRepository.save(report);
    }

    public List<ReportBadgeDto> getAllBadges(String username) {
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusWeeks(1);

        List<Report> reports = reportRepository.findAllByAuthUser_Username(username);
        return reports.stream()
                .filter(report -> {
                    // Include reports where the report week is within [today, nextWeek]
                    return !report.getWeekStart().isAfter(nextWeek);
                })
                .sorted(Comparator.comparing(Report::getReportNumber))
                .map(dtoMapper::convertReportToBadgeDto)
                .toList();
    }

    public void createEmptyReports(Apprentice apprentice) {
        LocalDate startDate = apprentice.getStartingDate();
        LocalDate endDate = apprentice.getEndingDate();

        // Falls der Start nicht ein Montag ist, gehe zum Montag der Woche zurück
        if (startDate.getDayOfWeek().getValue() != 1) {
            startDate = startDate.minusDays(startDate.getDayOfWeek().getValue() - 1);
        }

        // Iteriere durch Wochen und erstelle leere Berichte
        int count = 1;
        while (!startDate.isAfter(endDate)) {
            Report report = new Report();
            report.setReportNumber(count++);
            report.setAuthUser(apprentice);
            report.setWeekStart(startDate);
            report.setWeekEnd(startDate.plusDays(6));
            report.setSubmitted(false);
            report.setApproved(false);
            report.setRejected(false);
            reportRepository.save(report);

            // Zur nächsten Woche
            startDate = startDate.plusWeeks(1);
        }
    }

    public ResponseEntity<?> setApproved(LocalDate weekStartDate, String username, String comment, String approvedByUsername) {
        Report report = reportRepository.findByAuthUser_UsernameAndWeekStart(username, weekStartDate);
        if (report == null) {
            return ResponseEntity.notFound().build();
        }
        report.setComment(comment);
        report.setApproved(true);
        report.setApprovedBy(approvedByUsername);
        reportRepository.save(report);
        return ResponseEntity.ok("Report approved successfully");
    }

    public ResponseEntity<?> setRejected(LocalDate weekStartDate, String username, String comment) {
        Report report = reportRepository.findByAuthUser_UsernameAndWeekStart(username, weekStartDate);
        if (report == null) {
            return ResponseEntity.notFound().build();
        }
        report.setComment(comment);
        report.setRejected(true);
        reportRepository.save(report);
        return ResponseEntity.ok("Report rejected successfully");
    }

}
