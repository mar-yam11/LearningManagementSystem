package com.lms.lms.controller;

import com.lms.lms.dto.InstructorDTO;
import com.lms.lms.model.*;
import com.lms.lms.service.*;

import java.util.List;
import java.util.Map;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/instructor")
public class InstructorController {

    private final InstructorService instructorService;
    private final LessonService lessonService;
    private final AssignmentService assignmentService;
    private final NotificationService notificationService;

    @Autowired
    public InstructorController(InstructorService instructorService,
                                LessonService lessonService,
                                AssignmentService assignmentService,
                                NotificationService notificationService) {
        this.instructorService = instructorService;
        this.lessonService = lessonService;
        this.assignmentService = assignmentService;
        this.notificationService = notificationService;
    }

    @PostMapping(path = "/create-course")
    public ResponseEntity<String> createCourse(@RequestParam String courseTitle,
                                               @RequestParam int courseDuration) {
        try {
            instructorService.createCourse(courseTitle, courseDuration);
            return ResponseEntity.ok("Course created successfully and assigned to you.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/add-lesson/{courseId}")
    public ResponseEntity<String> addLesson(@RequestBody Lesson lesson, @PathVariable int courseId) {
        try {
            instructorService.addLesson(lesson, courseId);
            return ResponseEntity.ok("Lesson added successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping(path = "/create-assignment")
    public String createAssignment(@RequestBody Assignment assignment) {
        try {
            // نستخرج الـ ID من الـ Service لتمريره لـ AssignmentService
            int instructorId = instructorService.getAuthenticatedInstructor().getId();
            assignmentService.createAssignment(assignment, instructorId);
            return "Assignment created successfully";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @DeleteMapping(path = "/delete-course")
    public ResponseEntity<String> deleteCourse(@RequestParam int courseId) {
        try {
            instructorService.deleteCourse(courseId);
            return ResponseEntity.ok("Course deleted successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting course.");
        }
    }

    @GetMapping("/getall")
    public List<InstructorDTO> getall() {
        return instructorService.getAllInstructors();
    }

    @PostMapping("/generate-otp/{lessonId}")
    public String generateOtp(@PathVariable Long lessonId) {
        try {
            lessonService.setOtpForLesson(lessonId);
            return "OTP generated for lesson ID: " + lessonId;
        } catch (Exception e) {
            return "Error generating OTP.";
        }
    }

    @GetMapping(path = "/track-student-assignment")
    public ResponseEntity<Integer> trackAssignmentStudents(@RequestParam int courseid, @RequestParam int studentId) {
        try {
            return ResponseEntity.ok(instructorService.countStudentSubmissions(courseid, studentId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(0);
        }
    }

    @GetMapping(path = "/track-attendance")
    public ResponseEntity<List<Integer>> trackAttendance(@RequestParam int lessonId) {
        try {
            return ResponseEntity.ok(instructorService.getAttendingStudentIds(lessonId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }

    @GetMapping(path = "/track-student-attendance")
    public ResponseEntity<Integer> trackStudentAttendance(@RequestParam int studentid, @RequestParam int courseid) {
        try {
            return ResponseEntity.ok(instructorService.countStudentAttendance(studentid, courseid));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(0);
        }
    }

    @PostMapping(path = "/correct-assignment")
    public String correctAssignment(@RequestParam Integer assignmentLogId, @RequestParam Integer grade) {
        if (assignmentLogId == null || grade == null || grade < 0) {
            return "Invalid assignment log ID or grade.";
        }
        try {
            assignmentService.correctAssignmentLog(assignmentLogId, grade);
            return "Assignment graded successfully!";
        } catch (Exception e) {
            return "Error correcting assignment.";
        }
    }

    @GetMapping(path = "/getlessons")
    public List<Lesson> get_lessons() {
        return instructorService.getAllLessons();
    }

    @GetMapping(path = "assignments")
    public List<Assignment> get_assignments() {
        return instructorService.getAllAssignments();
    }

    @GetMapping(path = "assignmentlogs")
    public List<AssignmentLog> get_logs() {
        return instructorService.getAllAssignmentLogs();
    }


    @PostMapping("/create-quiz")
    public ResponseEntity<String> createQuiz(@RequestParam Integer courseId,
                                             @RequestParam String title, @RequestParam Integer numberOfQuestions) {
        try {
            instructorService.createQuiz(courseId, title, numberOfQuestions);
            return ResponseEntity.ok("Quiz created.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/create-question")
    public ResponseEntity<String> createQuestion(@RequestBody Question question) {
        try {
            instructorService.createQuestion(question);
            return ResponseEntity.ok("Question created.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping(path = "/track-quiz")
    public ResponseEntity<Map<Integer, Integer>> trackQuiz(@RequestParam int quizId) {
        try {
            return ResponseEntity.ok(instructorService.getQuizScoresByStudent(quizId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Collections.emptyMap());
        }
    }

    @GetMapping(path = "/track-student-quiz")
    public ResponseEntity<Map<String, Integer>> trackStudentQuiz(@RequestParam int studentid, @RequestParam int courseid) {
        try {
            return ResponseEntity.ok(instructorService.getStudentQuizScoresForCourse(studentid, courseid));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Collections.emptyMap());
        }
    }

    @GetMapping("/questions")
    public List<Question> getQuestions() {
        return instructorService.getAllQuestions();
    }

    @GetMapping("/quizzes")
    public List<Quiz> getQuizzes() {
        return instructorService.getAllQuizzes();
    }
}