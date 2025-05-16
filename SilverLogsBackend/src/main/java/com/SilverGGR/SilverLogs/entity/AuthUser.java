package com.SilverGGR.SilverLogs.entity;

import com.SilverGGR.SilverLogs.enums.Role;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class AuthUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String password;


    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

//    private boolean enabled = true;
//    private boolean accountNonExpired = true;
//    private boolean accountNonLocked = true;
//    private boolean credentialsNonExpired = true;
//
//    // Optional: Timestamps
//    private LocalDateTime lastPasswordChangeDate;
//    private LocalDateTime accountLockDate;

}
