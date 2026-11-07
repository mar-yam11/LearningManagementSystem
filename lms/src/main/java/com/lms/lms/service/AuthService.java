package com.lms.lms.service;

import com.lms.lms.dto.AuthRequest;
import com.lms.lms.dto.AuthResponse;
import com.lms.lms.dto.RegisterRequest;
import com.lms.lms.model.Instructor;
import com.lms.lms.model.Role;
import com.lms.lms.model.Student;
import com.lms.lms.repository.InstructorRepository;
import com.lms.lms.repository.StudentRepository;
import com.lms.lms.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final StudentRepository studentRepository;
    private final InstructorRepository instructorRepository;

    public AuthService(AuthenticationManager authenticationManager, UserDetailsService userDetailsService,
                       JwtUtil jwtUtil, PasswordEncoder passwordEncoder,
                       StudentRepository studentRepository, InstructorRepository instructorRepository) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.studentRepository = studentRepository;
        this.instructorRepository = instructorRepository;
    }

    public AuthResponse register(RegisterRequest request) {
        if (studentRepository.findByEmail(request.getEmail()).isPresent() ||
                instructorRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        UserDetails userDetails;

        // تشفير الباسورد وحفظ المستخدم حسب دوره (Role)
        if (request.getRole() == Role.Student) {
            Student student = new Student();
            student.setName(request.getName());
            student.setEmail(request.getEmail());
            student.setPassword(passwordEncoder.encode(request.getPassword())); // التشفير هنا
            student.setRole(Role.Student);
            studentRepository.save(student);

            userDetails = new org.springframework.security.core.userdetails.User(
                    student.getEmail(), student.getPassword(),
                    Collections.singleton(new SimpleGrantedAuthority(Role.Student.name()))
            );
        } else {
            Instructor instructor = new Instructor();
            instructor.setName(request.getName());
            instructor.setEmail(request.getEmail());
            instructor.setPassword(passwordEncoder.encode(request.getPassword())); // التشفير هنا
            instructor.setRole(Role.Instructor);
            instructorRepository.save(instructor);

            userDetails = new org.springframework.security.core.userdetails.User(
                    instructor.getEmail(), instructor.getPassword(),
                    Collections.singleton(new SimpleGrantedAuthority(Role.Instructor.name()))
            );
        }

        // توليد التوكن للمستخدم الجديد
        String jwtToken = jwtUtil.generateToken(userDetails, new HashMap<>());
        return new AuthResponse(jwtToken);
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDetails user = userDetailsService.loadUserByUsername(request.getEmail());
        String jwtToken = jwtUtil.generateToken(user, new HashMap<>());

        return new AuthResponse(jwtToken);
    }
}