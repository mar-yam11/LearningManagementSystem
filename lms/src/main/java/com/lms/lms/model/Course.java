package com.lms.lms.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "courseID")
    protected int id;

    @Column(nullable = false)
    protected String courseTitle;

    @Column(nullable = true)
    protected String courseDescription;

    @Column(nullable = true)
    protected int courseDuration;

    @ManyToOne
    @JoinColumn(name = "instructor_id")
    @JsonIgnoreProperties({"courses", "authorities", "accountNonExpired", "accountNonLocked", "credentialsNonExpired", "enabled", "password"})
    @JsonIgnore
    private Instructor instructor;

    @ElementCollection
    @CollectionTable(name = "course_media", joinColumns = @JoinColumn(name = "course_id"))
    @Column(name = "media_url")
    private List<String> mediaFiles = new ArrayList<>();

    @ManyToMany(mappedBy = "courses")
    @JsonIgnoreProperties({"courses", "authorities", "accountNonExpired", "accountNonLocked", "credentialsNonExpired", "enabled", "password"})
    @JsonIgnore
    private List<Student> enrolledStudents = new ArrayList<>();

    @OneToMany(mappedBy = "course")
    @JsonManagedReference
    private List<Lesson> lessons;

    public Course(String courseTitle, String courseDescription, int courseDuration, Instructor instructor) {
        this.courseTitle = courseTitle;
        this.courseDescription = courseDescription;
        this.courseDuration = courseDuration;
        this.instructor = instructor;
    }

    public Course(String courseTitle, int courseDuration) {
        this.courseTitle = courseTitle;
        this.courseDuration = courseDuration;
    }

    public void addStudent(Student student){
        enrolledStudents.add(student);
    }
}