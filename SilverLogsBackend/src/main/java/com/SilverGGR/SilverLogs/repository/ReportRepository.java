package com.SilverGGR.SilverLogs.repository;

import com.SilverGGR.SilverLogs.entity.AuthUser;
import com.SilverGGR.SilverLogs.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    Report findByAuthUser_UsernameAndWeekStart(String username, LocalDate weekStart);

}
