package com.lms.lms.dto;

import com.lms.lms.model.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private Role role; // "Student" أو "Instructor"
}