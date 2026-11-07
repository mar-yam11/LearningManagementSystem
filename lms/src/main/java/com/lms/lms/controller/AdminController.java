package com.lms.lms.controller;

import com.lms.lms.service.AdminService;
import com.lms.lms.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final ReportService reportService;

    @Autowired
    AdminController(AdminService adminService, ReportService reportService) {
        this.adminService = adminService;
        this.reportService = reportService;
    }

    @PostMapping("/add-student")
    public String addStudent(@RequestParam String name, @RequestParam String email, @RequestParam String password) {
        adminService.addStudent(name, email, password);
        return "Student added successfully!";
    }

    @PostMapping("/add-course-to-student")
    public ResponseEntity<String> addCourseToStudent(@RequestParam int studentId, @RequestParam int courseId) {
        try {
            return ResponseEntity.ok(adminService.addCourseToStudent(studentId, courseId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/remove-course-from-student")
    public ResponseEntity<String> removeCourseFromStudent(@RequestParam int studentId, @RequestParam int courseId) {
        try {
            return ResponseEntity.ok(adminService.removeCourseFromStudent(studentId, courseId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
