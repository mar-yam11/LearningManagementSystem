package com.lms.lms.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class StudentAnswer {
    private Integer questionId;  // ID of the question
    private String answer;       // Student's answer
}