package com.SilverGGR.SilverLogs.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity
public class MethodSecurityConfig {
    // Leere Konfigurationsklasse - aktiviert @PreAuthorize und ähnliche Annotationen
}
