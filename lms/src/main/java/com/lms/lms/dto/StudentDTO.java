package com.lms.lms.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class StudentDTO {
    private Integer id;
    private String name;
    private String email;
}