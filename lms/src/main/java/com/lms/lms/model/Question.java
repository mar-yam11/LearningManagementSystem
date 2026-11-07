package com.lms.lms.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String title;

    @ManyToOne
    @JoinColumn(name = "course_id")
    @JsonBackReference
    private Course course;

    private String correctAnswer;
    private Integer grade;

    @Enumerated(EnumType.STRING)
    private QuestionType type; // نوع السؤال

    @ElementCollection
    private List<String> options;

    public Question(String title, Course course, String correctAnswer, Integer grade, QuestionType type) {
        this.title = title;
        this.course = course;
        this.correctAnswer = correctAnswer;
        this.grade = grade;
        this.type = type;
        this.options = null;
    }

    public boolean isCorrect(String answer) {
        // تجاهل المسافات وحالة الأحرف لزيادة دقة التصحيح الآلي
        return correctAnswer != null && answer != null && correctAnswer.trim().equalsIgnoreCase(answer.trim());
    }


}