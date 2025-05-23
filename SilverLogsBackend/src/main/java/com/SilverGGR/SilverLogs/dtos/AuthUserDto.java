package com.SilverGGR.SilverLogs.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthUserDto {
    private String username;
    private String firstname;
    private String lastname;
    private String email;
    private String phone;
    private String department;
    private String role;
    private byte[] profileImage;
    private String profileImageType;
}