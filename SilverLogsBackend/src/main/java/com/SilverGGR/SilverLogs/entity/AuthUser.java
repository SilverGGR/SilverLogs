package com.SilverGGR.SilverLogs.entity;

import com.SilverGGR.SilverLogs.enums.Role;
import jakarta.persistence.Basic;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class AuthUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String firstname;
    private String lastname;
    private String email;
    private String password;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] profileImage;

    private String profileImageType;

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
