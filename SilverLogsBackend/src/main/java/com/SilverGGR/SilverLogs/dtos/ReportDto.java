package com.SilverGGR.SilverLogs.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ReportDto {
    private LocalDate weekStart;
    private LocalDate weekEnd;
    private String weekText;
    private String instructionText;
    private String schoolText;
    private String extraText;
    private Integer reportNumber;
    private String department;
    private Boolean submitted;
    private Boolean approved;
    private Boolean rejected;
    private String comment;
}
