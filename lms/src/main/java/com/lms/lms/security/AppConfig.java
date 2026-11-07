package com.lms.lms.security;

import com.lms.lms.model.Role;
import com.lms.lms.repository.InstructorRepository;
import com.lms.lms.repository.StudentRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

@Configuration
public class AppConfig {

    private final StudentRepository studentRepository;
    private final InstructorRepository instructorRepository;

    public AppConfig(StudentRepository studentRepository, InstructorRepository instructorRepository) {
        this.studentRepository = studentRepository;
        this.instructorRepository = instructorRepository;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            // 1. البحث في جدول الطلاب
            var student = studentRepository.findByEmail(username);
            if (student.isPresent()) {
                return new User(student.get().getEmail(), student.get().getPassword(),
                        Collections.singleton(new SimpleGrantedAuthority(Role.Student.name())));
            }

            // 2. البحث في جدول المدربين
            var instructor = instructorRepository.findByEmail(username);
            if (instructor.isPresent()) {
                return new User(instructor.get().getEmail(), instructor.get().getPassword(),
                        Collections.singleton(new SimpleGrantedAuthority(Role.Instructor.name())));
            }

            throw new UsernameNotFoundException("User not found with email: " + username);
        };
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}