package com.SilverGGR.SilverLogs.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Document {

    @Id
    @GeneratedValue
    private UUID id;

    private String fileName;
    private String fileType;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] fileContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auth_user_id")
    private AuthUser authUser;
}
