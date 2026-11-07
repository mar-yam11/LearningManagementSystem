package com.lms.lms.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "assignments")
@Getter
@Setter
@NoArgsConstructor
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // تم تصحيح الـ Annotation هنا لأن المتغير عبارة عن Integer وليس كائن كلاس كامل
    @Column(name = "course_id", nullable = false)
    private Integer courseId;

    private String pdfPath;
    private Integer grade;

    public Assignment(Integer courseId, String pdfPath, Integer grade) {
        this.courseId = courseId;
        this.pdfPath = pdfPath;
        this.grade = grade;
    }
}