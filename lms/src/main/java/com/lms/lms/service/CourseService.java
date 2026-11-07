package com.lms.lms.service;

import java.util.List;
import java.util.stream.Collectors;

import com.lms.lms.dto.CourseDTO;
import com.lms.lms.dto.StudentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lms.lms.model.Course;
import com.lms.lms.model.Instructor;
import com.lms.lms.repository.CourseRepository;
import com.lms.lms.repository.InstructorRepository;

@Service
public class CourseService {
    private final CourseRepository courseRepository;
    private final InstructorRepository instructorRepository;

    @Autowired
    public CourseService(CourseRepository courseRepository, InstructorRepository instructorRepository){
        this.courseRepository = courseRepository;
        this.instructorRepository = instructorRepository;
    }

    // إرجاع CourseDTO بدلاً من Course
    public List<CourseDTO> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(course -> new CourseDTO(
                        course.getId(),
                        course.getCourseTitle(),
                        course.getCourseDuration(),
                        course.getInstructor() != null ? course.getInstructor().getName() : "No Instructor",
                        course.getMediaFiles()
                ))
                .collect(Collectors.toList());
    }

    // إرجاع CourseDTO بدلاً من Course
    public List<CourseDTO> getCoursesByInstructor(int instructorId) {
        Instructor instructor = instructorRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found with id: " + instructorId));

        return courseRepository.findByInstructor(instructor).stream()
                .map(course -> new CourseDTO(
                        course.getId(),
                        course.getCourseTitle(),
                        course.getCourseDuration(),
                        instructor.getName(),
                        course.getMediaFiles()
                ))
                .collect(Collectors.toList());
    }

    // إرجاع StudentDTO لمنع تسريب بيانات الطلاب
    public List<StudentDTO> getEnrolledStudents(int courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        return course.getEnrolledStudents().stream()
                .map(student -> new StudentDTO(student.getId(), student.getName(), student.getEmail()))
                .collect(Collectors.toList());
    }

    public Course addMediaToCourse(int courseId, List<String> mediaUrls) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + courseId));

        course.getMediaFiles().addAll(mediaUrls);
        return courseRepository.save(course);
    }
}