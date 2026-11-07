package com.lms.lms.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Student extends User {

    @ManyToMany
    @JoinTable(
            name = "student_courses",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    @Builder.Default
    private List<Course> courses = new ArrayList<>(); // initialized to prevent NullPointerException

    // Role field removed — inherited from User

    public Student(String name, String email, String password, Role role) {
        super(name, email, password, Role.Student);
    }

    public void addCourse(Course course) {
        this.courses.add(course);
    }

    public void removeCourse(Course course) {
        this.courses.remove(course);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Fixed: was returning List.of() which broke Spring Security role checks
        return List.of(new SimpleGrantedAuthority(getRole().name()));
    }
}
