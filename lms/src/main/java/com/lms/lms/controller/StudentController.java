package com.lms.lms.controller;

import com.lms.lms.dto.StudentDTO;
import com.lms.lms.model.Notification;
import com.lms.lms.model.Role;
import com.lms.lms.model.Student;
import com.lms.lms.model.AssignmentLog;
import com.lms.lms.service.AttendanceService;
import com.lms.lms.service.NotificationService;
import com.lms.lms.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping(path = "/students")
public class StudentController {

    private final StudentService studentService;
    private final AttendanceService attendanceService;
    private final NotificationService notificationService;

    @Autowired
    public StudentController(StudentService studentService,
                             AttendanceService attendanceService,
                             NotificationService notificationService) {
        this.studentService = studentService;
        this.attendanceService = attendanceService;
        this.notificationService = notificationService;
    }

    // يرجع DTO بدل الـ Entity الكامل
    @GetMapping(path = "/get-students")
    public ResponseEntity<List<StudentDTO>> getStudents() {
        try {
            return ResponseEntity.ok(studentService.getStudents());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @PostMapping(path = "/add-course")
    public ResponseEntity<String> addCourse(@RequestParam int courseId) {
        try {
            String message = studentService.enrollStudent(courseId);
            return ResponseEntity.ok(message);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error enrolling student.");
        }
    }

    @PostMapping("/attend-lesson/{lessonId}")
    public ResponseEntity<String> attendLesson(@PathVariable Long lessonId, @RequestParam String otp) {
        try {
            if (otp == null) {
                return ResponseEntity.badRequest().body("Invalid OTP provided.");
            }
            // نستخرج الـ ID الآمن من التوكن
            Student student = studentService.getAuthenticatedStudent();
            boolean isAttended = attendanceService.markAttendance(lessonId, otp, student.getId());
            return ResponseEntity.ok(isAttended ? "Attendance marked successfully." : "Invalid OTP.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error marking attendance.");
        }
    }

    @PostMapping("/submit-assignment")
    public ResponseEntity<String> submitAssignment(@RequestBody AssignmentLog assignmentLog) {
        try {
            String message = studentService.submitAssignment(assignmentLog);
            return ResponseEntity.ok(message);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error submitting assignment.");
        }
    }

    @PostMapping("/take-quiz")
    public ResponseEntity<String> takeQuiz(@RequestParam Integer quizId, @RequestBody List<String> answers) {
        try {
            String message = studentService.takeQuiz(quizId, answers);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/get-quiz")
    public ResponseEntity<String> getQuiz(@RequestParam Integer quizId) {
        try {
            String quiz = studentService.getQuizWithRandomQuestions(quizId);
            return ResponseEntity.ok(quiz);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping(path = "/get_otp")
    public ResponseEntity<String> getOtp(@RequestParam long lessonId) {
        try {
            return ResponseEntity.ok(studentService.getOtpForLesson(lessonId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}