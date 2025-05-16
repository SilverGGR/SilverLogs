package com.SilverGGR.SilverLogs.security;

import com.SilverGGR.SilverLogs.entity.AuthUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

// Adapter zwischen UserDetails und Users
public class AuthUserPrincipal implements UserDetails {

    private final String username;
    private final String password;
    private final Collection<GrantedAuthority> authorities;

    // Profilbild wird nicht im Principal gespeichert
    // nur die für die Authentifizierung notwendigen Daten

    public AuthUserPrincipal(AuthUser user) {
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority(user.getRole().toString()));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }



    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    // Zeitlich begrenzte Benutzerkonten
    @Override
    public boolean isAccountNonExpired() {
        return true;
//        return authUser.isAccountNonExpired();

    }

    // Temporäre Sperrung des Benutzerkontos
    @Override
    public boolean isAccountNonLocked() {
        return true;
//        return authUser.isAccountNonLocked();

    }

    // Passwort abgelaufen
    @Override
    public boolean isCredentialsNonExpired() {
//        // Beispiel für eine komplexere Logik
//        if (!authUser.isCredentialsNonExpired()) {
//            return false;
//        }
//        // Prüfen ob Passwort älter als 90 Tage ist
//        if (authUser.getLastPasswordChangeDate() != null) {
//            return ChronoUnit.DAYS.between(authUser.getLastPasswordChangeDate(), LocalDateTime.now()) <= 90;
//        }
        return true;

    }

    // Benutzerkonten deaktivieren
    @Override
    public boolean isEnabled() {
        return true;
//        return authUser.isEnabled();
    }
}
