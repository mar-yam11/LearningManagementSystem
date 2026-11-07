package com.lms.lms.service;

import com.lms.lms.model.Assignment;
import com.lms.lms.model.AssignmentLog;
import com.lms.lms.model.Course;
import com.lms.lms.model.Role;
import com.lms.lms.repository.AssignmentRepository;
import com.lms.lms.repository.AssignmentLogRepository;
import com.lms.lms.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssignmentService {

    private final CourseRepository courseRepository;
    private final AssignmentRepository assignmentRepository;
    private final NotificationService notificationService;
    private final AssignmentLogRepository assignmentLogRepository;

    // تم توحيد الـ Injection هنا (Constructor Injection) وهو الأفضل
    @Autowired
    public AssignmentService(AssignmentLogRepository assignmentLogRepository, CourseRepository courseRepository, NotificationService notificationService, AssignmentRepository assignmentRepository) {
        this.assignmentLogRepository = assignmentLogRepository;
        this.courseRepository = courseRepository;
        this.notificationService = notificationService;
        this.assignmentRepository = assignmentRepository;
    }

    public Assignment addAssignment(Assignment assignment) {
        return assignmentRepository.save(assignment);
    }

    public void editAssignmentGrade(Integer assignmentId, Integer grade) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        assignment.setGrade(grade);
        assignmentRepository.save(assignment);
    }

    public void deleteAssignment(Integer assignmentId) {
        assignmentRepository.deleteById(assignmentId);
    }

    public void submitAssignment(String token, Integer assignmentId, String answeredPdf) {
        Integer studentId = extractStudentIdFromToken(token);
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        AssignmentLog log = new AssignmentLog();
        log.setStudentId(studentId);
        log.setAnsweredPdfPath(answeredPdf);
        log.setAssignment(assignment);
        assignmentLogRepository.save(log);
    }

    // تعديل الاسم والتعامل الآمن مع الـ Optional
    public void createAssignment(Assignment assignment, int instructorId) throws Exception {
        Course course = courseRepository.findById(assignment.getCourseId()) // افترضنا تعديلها لتكون course.getId() لو استخدمتي الكلاس
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if(course.getInstructor().getId() != instructorId) {
            throw new Exception("Instructor doesn't have permission");
        }
        assignmentRepository.save(assignment);
    }

    // تعديل الاسم واستخدام الـ Repository بدلاً من الفلترة في الـ RAM
    public List<Integer> trackAssignmentSubmissions(int assignmentId, int instructorId) throws Exception {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        Course course = courseRepository.findById(assignment.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if(course.getInstructor().getId() != instructorId) {
            throw new Exception("Instructor doesn't have permission");
        }

        // يجب إضافة findByAssignmentId في الـ Repository
        List<AssignmentLog> assignmentLogs = assignmentLogRepository.findByAssignmentId(assignmentId);

        return assignmentLogs.stream().map(AssignmentLog::getGrade).collect(Collectors.toList());
    }

    public List<AssignmentLog> viewAllAssignmentLogs() {
        return assignmentLogRepository.findAll();
    }

    public List<AssignmentLog> viewAllAssignmentLogsForSpecificAssignment(Integer assignmentId) {
        return assignmentLogRepository.findByAssignmentId(assignmentId);
    }

    public List<AssignmentLog> viewUncorrectedAssignmentLogs() {
        return assignmentLogRepository.findByGradeIsNull();
    }

    public List<AssignmentLog> viewUncorrectedAssignmentLogsForSpecificAssignment(Integer assignmentId) {
        return assignmentLogRepository.findByGradeIsNullAndAssignmentId(assignmentId);
    }

    public void correctAssignmentLog(Integer assignmentLogId, Integer grade) {
        AssignmentLog log = assignmentLogRepository.findById(assignmentLogId)
                .orElseThrow(() -> new RuntimeException("AssignmentLog not found"));

        Course crs = courseRepository.findById(log.getAssignment().getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        log.setGrade(grade);
        assignmentLogRepository.save(log);

        String message = "Your assignment for course: " + crs.getCourseTitle() +
                " has been graded. Your grade is: " + grade + ".";
        notificationService.createNotification(log.getStudentId(), Role.Student, message);
    }

    private Integer extractStudentIdFromToken(String token) {
        // Placeholder function
        return 1;
    }
}