package com.lms.lms.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class CourseDTO {
    private Integer id;
    private String courseTitle;
    private int courseDuration;
    private String instructorName;
    private List<String> mediaFiles;
}