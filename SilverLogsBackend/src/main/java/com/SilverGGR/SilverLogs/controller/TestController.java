package com.SilverGGR.SilverLogs.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello World!";
    }

    @GetMapping("/api/admin/checkAdmin")
    public String checkAdmin() {
        return "You are an admin!";
    }

    @GetMapping("/api/checkAdmin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String checkMethod() {
        return "You are somehow an admin!";
    }
}
