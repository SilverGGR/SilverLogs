package com.SilverGGR.SilverLogs.repository;

import com.SilverGGR.SilverLogs.entity.Apprentice;
import com.SilverGGR.SilverLogs.entity.AuthUser;
import com.SilverGGR.SilverLogs.entity.SupervisorApprenticeMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupervisorApprenticeMappingRepository extends JpaRepository<SupervisorApprenticeMapping, Long> {
    List<SupervisorApprenticeMapping> findBySupervisor(AuthUser supervisor);

    List<SupervisorApprenticeMapping> findByApprentice(Apprentice apprentice);

    Optional<SupervisorApprenticeMapping> findByApprenticeAndSupervisor(Apprentice apprentice, AuthUser supervisor);

    void deleteByApprenticeAndSupervisor(Apprentice apprentice, AuthUser supervisor);
}
