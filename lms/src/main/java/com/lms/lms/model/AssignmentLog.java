package com.lms.lms.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "assignment_Logs")
@Getter
@Setter
@NoArgsConstructor
public class AssignmentLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer studentId;
    private String answeredPdfPath;
    private Integer grade;

    @ManyToOne
    @JoinColumn(name = "assignment_Id")
    private Assignment assignment;

    public AssignmentLog(Integer studentId, String answeredPdfPath, Integer grade) {
        this.studentId = studentId;
        this.answeredPdfPath = answeredPdfPath;
        this.grade = grade;
    }
}