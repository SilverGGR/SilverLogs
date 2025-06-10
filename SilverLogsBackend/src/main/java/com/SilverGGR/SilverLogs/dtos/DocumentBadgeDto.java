package com.SilverGGR.SilverLogs.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class DocumentBadgeDto {
    private UUID id;
    private String fileName;
    private String fileType;
}
