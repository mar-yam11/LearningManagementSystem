package com.lms.lms.service;

import com.lms.lms.model.Course;
import com.lms.lms.model.Role;
import com.lms.lms.model.Student;
import com.lms.lms.repository.CourseRepository;
import com.lms.lms.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    @Autowired
    public AdminService(StudentRepository studentRepository, CourseRepository courseRepository) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    public void addStudent(String name, String email, String password) {
        Student newStudent = new Student(name, email, password, Role.Student);
        studentRepository.save(newStudent);
    }

    /**
     * Previously took Student/Course objects directly, which forced the caller
     * (the controller) to do its own repository lookups first. Now takes IDs
     * and does the lookup itself, like enrollStudent() in StudentService.
     */
    public String addCourseToStudent(int studentId, int courseId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found."));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found."));

        student.addCourse(course);
        studentRepository.save(student);
        return "Course added successfully to the student!";
    }

    public String removeCourseFromStudent(int studentId, int courseId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found."));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found."));

        student.removeCourse(course);
        studentRepository.save(student);
        return "Course removed successfully from the student!";
    }
}
