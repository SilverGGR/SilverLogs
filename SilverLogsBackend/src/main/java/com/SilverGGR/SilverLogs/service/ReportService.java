package com.SilverGGR.SilverLogs.service;

import com.SilverGGR.SilverLogs.dtos.ReportBadgeDto;
import com.SilverGGR.SilverLogs.dtos.ReportDto;
import com.SilverGGR.SilverLogs.entity.Report;
import com.SilverGGR.SilverLogs.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
        return dtoMapper.convertReportToDto(savedReport);
    }

    public Report saveReport(Report report) {
        return reportRepository.save(report);
    }

    public List<ReportBadgeDto> getAllBadges(String username) {
        List<Report> reports = reportRepository.findAllByAuthUser_Username(username);
        return reports.stream()
                .map(dtoMapper::convertReportToBadgeDto)
                .toList();
    }


}
