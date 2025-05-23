package com.SilverGGR.SilverLogs.service;

import com.SilverGGR.SilverLogs.entity.Apprentice;
import com.SilverGGR.SilverLogs.entity.AuthUser;
import com.SilverGGR.SilverLogs.entity.SupervisorApprenticeMapping;
import com.SilverGGR.SilverLogs.enums.Role;
import com.SilverGGR.SilverLogs.repository.ApprenticeRepository;
import com.SilverGGR.SilverLogs.repository.AuthUserRepository;
import com.SilverGGR.SilverLogs.repository.SupervisorApprenticeMappingRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupervisorApprenticeService {

    private final AuthUserRepository authUserRepository;
    private final ApprenticeRepository apprenticeRepository;
    private final SupervisorApprenticeMappingRepository mappingRepository;

    /**
     * Weist einen Apprentice einem Supervisor zu
     */
    @Transactional
    public void assignApprenticeToSupervisor(String apprenticeUsername, String supervisorUsername) {
        Apprentice apprentice = apprenticeRepository.findByUsername(apprenticeUsername);

        AuthUser supervisor = authUserRepository.findByUsername(supervisorUsername);

        if (mappingRepository.findByApprenticeAndSupervisor(apprentice, supervisor).isPresent()) {
            return; // Beziehung existiert bereits
        }

        SupervisorApprenticeMapping mapping = new SupervisorApprenticeMapping(apprentice, supervisor);
        mappingRepository.save(mapping);
    }

    /**
     * Entfernt die Zuordnung eines Apprentice zu einem Supervisor
     */
    @Transactional
    public void deleteConnection(String apprenticeUsername, String supervisorUsername) {
        Apprentice apprentice = apprenticeRepository.findByUsername(apprenticeUsername);

        AuthUser supervisor = authUserRepository.findByUsername(supervisorUsername);

        mappingRepository.deleteByApprenticeAndSupervisor(apprentice, supervisor);
    }

    /**
     * Liefert alle Apprentices, die einem Supervisor zugewiesen sind
     */
    public List<Apprentice> getApprenticesBySupervisor(Long supervisorId) {
        AuthUser supervisor = authUserRepository.findById(supervisorId)
                .orElseThrow(() -> new EntityNotFoundException("Supervisor nicht gefunden"));

        return mappingRepository.findBySupervisor(supervisor).stream()
                .map(SupervisorApprenticeMapping::getApprentice)
                .collect(Collectors.toList());
    }

    /**
     * Liefert alle Supervisors, die einem Apprentice zugewiesen sind
     */
    public List<AuthUser> getSupervisorsByApprentice(Long apprenticeId) {
        Apprentice apprentice = apprenticeRepository.findById(apprenticeId)
                .orElseThrow(() -> new EntityNotFoundException("Auszubildender nicht gefunden"));

        return mappingRepository.findByApprentice(apprentice).stream()
                .map(SupervisorApprenticeMapping::getSupervisor)
                .collect(Collectors.toList());
    }

}
