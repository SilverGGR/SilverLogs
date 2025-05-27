package com.SilverGGR.SilverLogs.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ApprenticeDto extends AuthUserDto {
    private LocalDate startingDate;
    private LocalDate endingDate;
}
