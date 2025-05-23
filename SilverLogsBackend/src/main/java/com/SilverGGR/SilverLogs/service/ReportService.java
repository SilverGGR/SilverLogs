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
//            newReport.setReportNumber(null); //TODO: Ohje
            newReport.setDepartment(null);
            newReport.setSubmitted(false);
            newReport.setApproved(false);
            newReport.setRejected(false);
            newReport.setComment(null);
            return newReport;
        }

        return getReportDto(report);
    }

    private static ReportDto getReportDto(Report report) {
        ReportDto reportDto = new ReportDto();
        reportDto.setWeekStart(report.getWeekStart());
        reportDto.setWeekEnd(report.getWeekEnd());
        reportDto.setWeekText(report.getWeekText());
        reportDto.setInstructionText(report.getInstructionText());
        reportDto.setSchoolText(report.getSchoolText());
        reportDto.setExtraText(report.getExtraText());
        reportDto.setDepartment(report.getDepartment());
        reportDto.setSubmitted(report.getSubmitted());
        reportDto.setApproved(report.getApproved());
        reportDto.setRejected(report.getRejected());
        reportDto.setComment(report.getComment());
        return reportDto;
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

        Report savedReport = saveReport(report);
        return getReportDto(savedReport);
    }

    public Report saveReport(Report report) {
        return reportRepository.save(report);
    }

}
