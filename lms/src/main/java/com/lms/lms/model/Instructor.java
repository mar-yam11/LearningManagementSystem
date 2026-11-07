package com.lms.lms.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "instructors")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Instructor extends User {

    @OneToMany(mappedBy = "instructor")
    private List<Course> createdCourses;

    // Role field removed — inherited from User

    public Instructor(String name, String email, String password) {
        super(name, email, password, Role.Instructor);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(getRole().name()));
    }
}
