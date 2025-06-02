package com.SilverGGR.SilverLogs.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ReportBadgeDto {
    private LocalDate weekStart;
    private LocalDate weekEnd;
    private Integer reportNumber;
    private Boolean submitted;
    private Boolean approved;
    private Boolean rejected;
}
