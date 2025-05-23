package com.SilverGGR.SilverLogs.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class SupervisorApprenticeMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "supervisor_id")
    private AuthUser supervisor;

    @ManyToOne
    @JoinColumn(name = "apprentice_id")
    private Apprentice apprentice;

    public SupervisorApprenticeMapping(Apprentice apprentice, AuthUser supervisor) {
        this.apprentice = apprentice;
        this.supervisor = supervisor;
    }


}
