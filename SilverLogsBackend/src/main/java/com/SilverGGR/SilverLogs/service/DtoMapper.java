package com.SilverGGR.SilverLogs.service;

import com.SilverGGR.SilverLogs.dtos.ApprenticeDto;
import com.SilverGGR.SilverLogs.dtos.AuthUserDto;
import com.SilverGGR.SilverLogs.dtos.DocumentBadgeDto;
import com.SilverGGR.SilverLogs.dtos.DocumentDto;
import com.SilverGGR.SilverLogs.dtos.ReportBadgeDto;
import com.SilverGGR.SilverLogs.dtos.ReportDto;
import com.SilverGGR.SilverLogs.entity.Apprentice;
import com.SilverGGR.SilverLogs.entity.AuthUser;
import com.SilverGGR.SilverLogs.entity.Document;
import com.SilverGGR.SilverLogs.entity.Report;
import com.SilverGGR.SilverLogs.enums.Role;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Service zur Umwandlung zwischen Entity- und DTO-Objekten
 */
@Service
public class DtoMapper {

    // ---------- AuthUser Mapping ----------

    /**
     * Konvertiert eine AuthUser-Entity in ein AuthUserDto ohne Profilbild
     *
     * @param user Die zu konvertierende AuthUser-Entity
     * @return Das konvertierte AuthUserDto
     */
    public AuthUserDto convertUserToDto(AuthUser user) {
        if (user == null) {
            return null;
        }

        AuthUserDto dto = new AuthUserDto();
        dto.setUsername(user.getUsername());
        dto.setFirstname(user.getFirstname());
        dto.setLastname(user.getLastname());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setDepartment(user.getDepartment());
        dto.setRole(user.getRole().toString());
        return dto;
    }

    /**
     * Konvertiert eine AuthUser-Entity in ein AuthUserDto mit Profilbild
     *
     * @param user Die zu konvertierende AuthUser-Entity
     * @return Das konvertierte AuthUserDto mit Profilbild
     */
    public AuthUserDto convertToDtoWithImage(AuthUser user) {
        if (user == null) {
            return null;
        }

        AuthUserDto dto = convertUserToDto(user);
        dto.setProfileImage(user.getProfileImage());
        dto.setProfileImageType(user.getProfileImageType());
        return dto;
    }

    /**
     * Konvertiert ein AuthUserDto in eine AuthUser-Entity
     *
     * @param dto Das zu konvertierende AuthUserDto
     * @return Die konvertierte AuthUser-Entity
     */
    public AuthUser convertUserFromDto(AuthUserDto dto) {
        if (dto == null) {
            return null;
        }

        AuthUser user = new AuthUser();
        user.setUsername(dto.getUsername());
        user.setFirstname(dto.getFirstname());
        user.setLastname(dto.getLastname());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setDepartment(dto.getDepartment());

        // Rolle setzen, falls vorhanden
        if (StringUtils.hasText(dto.getRole())) {
            try {
                user.setRole(Role.valueOf(dto.getRole()));
            } catch (IllegalArgumentException e) {
                // Standardrolle USER setzen, falls ungültige Rolle
                user.setRole(Role.USER);
            }
        } else {
            user.setRole(Role.USER);
        }

        // Passwort nur setzen, wenn es im DTO vorhanden ist
        if (StringUtils.hasText(dto.getPassword())) {
            user.setPassword(dto.getPassword());
        }

        // Profilbild nur setzen, wenn es im DTO vorhanden ist
        if (dto.getProfileImage() != null) {
            user.setProfileImage(dto.getProfileImage());
            user.setProfileImageType(dto.getProfileImageType());
        }

        return user;
    }

    /**
     * Aktualisiert eine bestehende AuthUser-Entity mit Werten aus einem DTO
     * Überschreibt nur die Felder, die im DTO gesetzt sind
     *
     * @param user Die zu aktualisierende AuthUser-Entity
     * @param dto Das DTO mit den neuen Werten
     * @return Die aktualisierte AuthUser-Entity
     */
    public AuthUser updateUserFromDto(AuthUser user, AuthUserDto dto) {
        if (user == null || dto == null) {
            return user;
        }

        if (StringUtils.hasText(dto.getFirstname())) {
            user.setFirstname(dto.getFirstname());
        }

        if (StringUtils.hasText(dto.getLastname())) {
            user.setLastname(dto.getLastname());
        }

        if (StringUtils.hasText(dto.getEmail())) {
            user.setEmail(dto.getEmail());
        }

        if (StringUtils.hasText(dto.getPhone())) {
            user.setPhone(dto.getPhone());
        }

        if (StringUtils.hasText(dto.getDepartment())) {
            user.setDepartment(dto.getDepartment());
        }

        if (StringUtils.hasText(dto.getRole())) {
            try {
                user.setRole(Role.valueOf(dto.getRole()));
            } catch (IllegalArgumentException e) {
                // Rolle nicht ändern, wenn ungültig
            }
        }

        if (StringUtils.hasText(dto.getPassword())) {
            user.setPassword(dto.getPassword());
        }

        return user;
    }

    // ---------- Apprentice Mapping ----------

    /**
     * Konvertiert eine Apprentice-Entity in ein ApprenticeDto
     *
     * @param apprentice Die zu konvertierende Apprentice-Entity
     * @return Das konvertierte ApprenticeDto
     */
    public ApprenticeDto convertApprenticeToDto(Apprentice apprentice) {
        if (apprentice == null) {
            return null;
        }

        ApprenticeDto dto = new ApprenticeDto();
        dto.setUsername(apprentice.getUsername());
        dto.setFirstname(apprentice.getFirstname());
        dto.setLastname(apprentice.getLastname());
        dto.setEmail(apprentice.getEmail());
        dto.setPhone(apprentice.getPhone());
        dto.setDepartment(apprentice.getDepartment());
        dto.setStartingDate(apprentice.getStartingDate());
        dto.setEndingDate(apprentice.getEndingDate());
        return dto;
    }

    /**
     * Konvertiert ein ApprenticeDto in eine Apprentice-Entity
     *
     * @param dto Das zu konvertierende ApprenticeDto
     * @return Die konvertierte Apprentice-Entity
     */
    public Apprentice convertApprenticeFromDto(ApprenticeDto dto) {
        if (dto == null) {
            return null;
        }

        Apprentice apprentice = new Apprentice();
        apprentice.setUsername(dto.getUsername());
        apprentice.setFirstname(dto.getFirstname());
        apprentice.setLastname(dto.getLastname());
        apprentice.setEmail(dto.getEmail());
        apprentice.setPhone(dto.getPhone());
        apprentice.setDepartment(dto.getDepartment());
        apprentice.setStartingDate(dto.getStartingDate());
        apprentice.setEndingDate(dto.getEndingDate());

        // Passwort nur setzen, wenn es im DTO vorhanden ist
        if (StringUtils.hasText(dto.getPassword())) {
            apprentice.setPassword(dto.getPassword());
        }

        return apprentice;
    }

    /**
     * Aktualisiert eine bestehende Apprentice-Entity mit Werten aus einem DTO
     * Überschreibt nur die Felder, die im DTO gesetzt sind
     *
     * @param apprentice Die zu aktualisierende Apprentice-Entity
     * @param dto Das DTO mit den neuen Werten
     * @return Die aktualisierte Apprentice-Entity
     */
    public Apprentice updateApprenticeFromDto(Apprentice apprentice, ApprenticeDto dto) {
        if (apprentice == null || dto == null) {
            return apprentice;
        }

        if (StringUtils.hasText(dto.getFirstname())) {
            apprentice.setFirstname(dto.getFirstname());
        }

        if (StringUtils.hasText(dto.getLastname())) {
            apprentice.setLastname(dto.getLastname());
        }

        if (StringUtils.hasText(dto.getEmail())) {
            apprentice.setEmail(dto.getEmail());
        }

        if (StringUtils.hasText(dto.getDepartment())) {
            apprentice.setDepartment(dto.getDepartment());
        }

        if (dto.getStartingDate() != null) {
            apprentice.setStartingDate(dto.getStartingDate());
        }

        if (dto.getEndingDate() != null) {
            apprentice.setEndingDate(dto.getEndingDate());
        }

        if (StringUtils.hasText(dto.getPassword())) {
            apprentice.setPassword(dto.getPassword());
        }

        return apprentice;
    }

    /**
     * Konvertiert eine AuthUser-Entity in eine Apprentice-Entity
     *
     * @param user Die zu konvertierende AuthUser-Entity
     * @return Die konvertierte Apprentice-Entity
     */
    public Apprentice convertUserToApprentice(AuthUser user) {
        if (user == null) {
            return null;
        }

        Apprentice apprentice = new Apprentice();
        apprentice.setUsername(user.getUsername());
        apprentice.setFirstname(user.getFirstname());
        apprentice.setLastname(user.getLastname());
        apprentice.setEmail(user.getEmail());
        apprentice.setDepartment(user.getDepartment());
        apprentice.setPassword(user.getPassword());

        // Profilbild übernehmen, falls vorhanden
        apprentice.setProfileImage(user.getProfileImage());
        apprentice.setProfileImageType(user.getProfileImageType());

        return apprentice;
    }

    // ---------- Apprentice Mapping ----------

    public ReportDto convertReportToDto(Report report) {
        if (report == null) return null;
        ReportDto dto = new ReportDto();
        dto.setWeekStart(report.getWeekStart());
        dto.setWeekEnd(report.getWeekEnd());
        dto.setReportNumber(report.getReportNumber());
        dto.setWeekText(report.getWeekText());
        dto.setInstructionText(report.getInstructionText());
        dto.setSchoolText(report.getSchoolText());
        dto.setExtraText(report.getExtraText());
        dto.setDepartment(report.getDepartment());
        dto.setSubmitted(report.getSubmitted());
        dto.setApproved(report.getApproved());
        dto.setRejected(report.getRejected());
        dto.setComment(report.getComment());
        return dto;
    }

    public Report convertDtoToReport(Report report, ReportDto dto) {
        if (dto == null) return null;
        report.setReportNumber(dto.getReportNumber());
        report.setWeekText(dto.getWeekText());
        report.setInstructionText(dto.getInstructionText());
        report.setSchoolText(dto.getSchoolText());
        report.setExtraText(dto.getExtraText());
        report.setDepartment(dto.getDepartment());
        report.setSubmitted(dto.getSubmitted());
        report.setApproved(dto.getApproved());
        report.setRejected(dto.getRejected());
        report.setComment(dto.getComment());
        return report;
    }

    public ReportBadgeDto convertReportToBadgeDto(Report report) {
        if (report == null) return null;
        ReportBadgeDto dto = new ReportBadgeDto();
        dto.setWeekStart(report.getWeekStart());
        dto.setWeekEnd(report.getWeekEnd());
        dto.setReportNumber(report.getReportNumber());
        dto.setSubmitted(Boolean.TRUE.equals(report.getSubmitted()));
        dto.setApproved(Boolean.TRUE.equals(report.getApproved()));
        dto.setRejected(Boolean.TRUE.equals(report.getRejected()));
        return dto;
    }

    public DocumentDto convertDocumentToDto(Document document) {
        if (document == null) return null;
        DocumentDto dto = new DocumentDto();
        dto.setId(document.getId());
        dto.setFileContent(document.getFileContent());
        dto.setFileName(document.getFileName());
        dto.setFileType(document.getFileType());
        return dto;
    }

    public DocumentBadgeDto convertDocumentToBadgeDto(Document document) {
        if (document == null) return null;
        DocumentBadgeDto dto = new DocumentBadgeDto();
        dto.setId(document.getId());
        dto.setFileName(document.getFileName());
        dto.setFileType(document.getFileType());
        return dto;
    }

}