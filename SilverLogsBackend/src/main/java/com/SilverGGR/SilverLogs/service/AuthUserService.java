package com.SilverGGR.SilverLogs.service;

import com.SilverGGR.SilverLogs.entity.AuthUser;
import com.SilverGGR.SilverLogs.repository.AuthUserRepository;
import com.SilverGGR.SilverLogs.security.JWTService;
import com.SilverGGR.SilverLogs.security.MyUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthUserService {

    private final AuthUserRepository authUserRepo;
    private final AuthenticationManager authManager;
    private final JWTService jwtService;
    private final MyUserDetailsService userDetailsService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public AuthUser register(AuthUser authUser) {
        authUser.setPassword(encoder.encode(authUser.getPassword()));
        return authUserRepo.save(authUser);
    }

    public String verify(AuthUser authUser) {
        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authUser.getUsername(), authUser.getPassword()));

            if (authentication.isAuthenticated()) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(authUser.getUsername());
                return jwtService.generateToken(userDetails);
            }
            throw new BadCredentialsException("Invalid credentials");
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Authentication failed: " + e.getMessage());
        }
    }
}