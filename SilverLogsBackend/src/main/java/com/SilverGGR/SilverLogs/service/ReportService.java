package com.SilverGGR.SilverLogs.service;

import com.SilverGGR.SilverLogs.dtos.ReportDto;
import com.SilverGGR.SilverLogs.entity.Report;
import com.SilverGGR.SilverLogs.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final AuthUserService authUserService;

    public Report getReportByUserAndDate(LocalDate weekStartDate, String username) {
       Report report = reportRepository.findByAuthUser_UsernameAndWeekStart(username, weekStartDate);
        if (report == null) {
            throw new RuntimeException("Kein Report gefunden für Benutzer '" + username + "' und Datum '" + weekStartDate + "'.");
        }
        return report;
    }

    public Report getOrCreateReport(ReportDto reportDto, String username) {
        // Versuche, den bestehenden Report zu laden
        Report report = reportRepository.findByAuthUser_UsernameAndWeekStart(username, reportDto.getWeekStart());

        if (report == null) {
            // Falls kein Report existiert, erstelle einen neuen
            report = new Report();
            report.setAuthUser(authUserService.findByUsername(username));
            report.setWeekStart(reportDto.getWeekStart());
            report.setWeekEnd(reportDto.getWeekEnd());
//            report.setReportNumber(reportDto.getReportNumber()); //TODO: Reportnummer muss ich noch anständig klären
        }

        // Update die Felder des Reports (bestehend oder neu)
        report.setWeekText(reportDto.getWeekText());
        report.setInstructionText(reportDto.getInstructionText());
        report.setSchoolText(reportDto.getSchoolText());
        report.setExtraText(reportDto.getExtraText());
        report.setDepartment(reportDto.getDepartment());
        report.setSubmitted(reportDto.getSubmitted());
        report.setApproved(reportDto.getApproved());
        report.setRejected(reportDto.getRejected());
        report.setComment(reportDto.getComment());

        return saveReport(report);
    }

    public Report saveReport(Report report) {
        return reportRepository.save(report);
    }

}
