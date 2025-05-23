package com.SilverGGR.SilverLogs.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Apprentice extends AuthUser{

    private LocalDate startingDate;
    private LocalDate endingDate;

    @OneToMany(
            mappedBy = "authUser",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Report> reports = new ArrayList<>();

    // Beziehung zu Supervisors (überschreibt die Beziehung in AuthUser)
    @OneToMany(mappedBy = "apprentice", cascade = CascadeType.ALL)
    private List<SupervisorApprenticeMapping> supervisors = new ArrayList<>();

}
