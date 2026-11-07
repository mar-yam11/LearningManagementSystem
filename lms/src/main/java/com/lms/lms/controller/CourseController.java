package com.lms.lms.controller;

import com.lms.lms.dto.CourseDTO;
import com.lms.lms.dto.StudentDTO;
import com.lms.lms.model.Course;
import com.lms.lms.service.CourseService;
import com.lms.lms.service.FileUploadService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/course")
public class CourseController {
    private final CourseService courseService;
    private final FileUploadService fileUploadService;

    @Autowired
    public CourseController(CourseService courseService, FileUploadService fileUploadService){
        this.courseService = courseService;
        this.fileUploadService = fileUploadService;
    }

    @GetMapping("/allCourses")
    public ResponseEntity<List<CourseDTO>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @GetMapping("/instructorCourses")
    public ResponseEntity<List<CourseDTO>> getCoursesByInstructor(@RequestParam int instructorId) {
        return ResponseEntity.ok(courseService.getCoursesByInstructor(instructorId));
    }

    @GetMapping("/enrolledStudents")
    public ResponseEntity<List<StudentDTO>> getEnrolledStudents(@RequestParam int courseId) {
        return ResponseEntity.ok(courseService.getEnrolledStudents(courseId));
    }

    @PostMapping("/uploadMedia")
    public ResponseEntity<Course> uploadMedia(
            @RequestParam int courseId,
            @RequestParam("files") List<MultipartFile> files) {
        try {
            List<String> mediaUrls = new ArrayList<>();

            for (MultipartFile file : files) {
                String fileUrl = fileUploadService.uploadFile(file);
                mediaUrls.add(fileUrl);
            }

            Course updatedCourse = courseService.addMediaToCourse(courseId, mediaUrls);
            return ResponseEntity.ok(updatedCourse);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}