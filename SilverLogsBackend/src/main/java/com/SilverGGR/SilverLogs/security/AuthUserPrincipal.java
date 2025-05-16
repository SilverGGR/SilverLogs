package com.SilverGGR.SilverLogs.security;

import com.SilverGGR.SilverLogs.entity.AuthUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// Adapter zwischen UserDetails und Users
public class AuthUserPrincipal implements UserDetails {

    private final AuthUser authUser;

    public AuthUserPrincipal(AuthUser authUser) {
        this.authUser = authUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(authUser.getRole().name()));

        return authorities;
    }


    @Override
    public String getPassword() {
        return authUser.getPassword();
    }

    @Override
    public String getUsername() {
        return authUser.getUsername();
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
