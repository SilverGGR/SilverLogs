package com.SilverGGR.SilverLogs.service;

import com.SilverGGR.SilverLogs.dtos.ApprenticeDto;
import com.SilverGGR.SilverLogs.entity.Apprentice;
import com.SilverGGR.SilverLogs.repository.ApprenticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApprenticeService {

    private final ReportService reportService;
    private final ApprenticeRepository apprenticeRepo;
    private final DtoMapper dtoMapper;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public ApprenticeDto createApprentice(ApprenticeDto apprenticeDto) {
        Apprentice user = dtoMapper.convertApprenticeFromDto(apprenticeDto);
        user.setPassword(encoder.encode(apprenticeDto.getPassword()));

        Apprentice savedUser = apprenticeRepo.save(user);
        reportService.createEmptyReports(savedUser);

        return dtoMapper.convertApprenticeToDto(savedUser);
    }

    @Transactional(readOnly = true)
    public Apprentice findByUsername(String username) {
        try {
            return apprenticeRepo.findByUsername(username);
        } catch (Exception e) {
            return null;
        }
    }

}
