package com.SilverGGR.SilverLogs.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class DocumentDto {
    private UUID id;
    private String fileName;
    private String fileType;
    private byte[] fileContent;
}
