package com.lms.lms.service;

import com.lms.lms.model.*;
import com.lms.lms.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Service
public class ReportService {

    private final CourseRepository courseRepository;
    private final AssignmentLogRepository assignmentLogRepository;
    private final AttendanceRepository attendanceRepository;
    private final AssignmentRepository assignmentRepository;
    private final QuizRepository quizRepository;
    private final QuizLogRepository quizLogRepository;

    @Autowired // توحيد الحقن
    public ReportService(CourseRepository courseRepository, AssignmentLogRepository assignmentLogRepository, AttendanceRepository attendanceRepository, AssignmentRepository assignmentRepository, QuizRepository quizRepository, QuizLogRepository quizLogRepository) {
        this.courseRepository = courseRepository;
        this.assignmentLogRepository = assignmentLogRepository;
        this.attendanceRepository = attendanceRepository;
        this.assignmentRepository = assignmentRepository;
        this.quizRepository = quizRepository;
        this.quizLogRepository = quizLogRepository;
    }

    public byte[] generateExcelReport(int courseId) throws IOException {

        // 1. جلب بيانات الكورس للتأكد من وجوده
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        List<Student> enrolledStudents = course.getEnrolledStudents();
         // 2. جلب بيانات المستخدم الحالي من التوكن
        org.springframework.security.core.Authentication authentication =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();

       // 3. التحقق إذا كان المستخدم أدمن
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Role.Admin.name()));

       // 4. المنع الفوري لو المستخدم مش أدمن ومش هو المدرب صاحب الكورس
        if (!isAdmin && !course.getInstructor().getEmail().equals(currentUserEmail)) {
            throw new RuntimeException("Access Denied: You do not own this course.");
        }

        // ... هنا يكمل باقي كود إنشاء الإكسيل أو الرسم البياني بتاعك عادي جداً ...
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Student Performance");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Student ID");
        headerRow.createCell(1).setCellValue("Name");
        headerRow.createCell(2).setCellValue("Total Grades");
        headerRow.createCell(3).setCellValue("Attendance");

        int rowNum = 1;
        for (Student student : enrolledStudents) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(student.getId());
            row.createCell(1).setCellValue(student.getName());

            int totalGrades = calculateTotalGrades(student.getId(), courseId);
            int attendanceCount = calculateAttendance(student.getId(), courseId);

            row.createCell(2).setCellValue(totalGrades);
            row.createCell(3).setCellValue(attendanceCount);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream.toByteArray();
    }

    public int calculateTotalGrades(int studentId, int courseId) {
        List<Assignment> assignments = assignmentRepository.findAllByCourseId(courseId);
        List<Quiz> quizzes = quizRepository.findAllByCourseId(courseId);

        if ((assignments == null || assignments.isEmpty()) && (quizzes == null || quizzes.isEmpty())) {
            return 0;
        }

        List<Integer> assignmentIds = assignments != null ?
                assignments.stream().map(Assignment::getId).toList() : Collections.emptyList();

        List<Integer> quizIds = quizzes != null ?
                quizzes.stream().map(Quiz::getId).toList() : Collections.emptyList();

        List<AssignmentLog> assignmentLogs = assignmentLogRepository.findAllByStudentIdAndAssignmentIdIn(studentId, assignmentIds);
        List<QuizLog> quizLogs = quizLogRepository.findAllByStudentIdAndQuizIdIn(studentId, quizIds);

        int assignmentTotalGrade = (assignmentLogs != null && !assignmentLogs.isEmpty()) ?
                assignmentLogs.stream().mapToInt(AssignmentLog::getGrade).sum() : 0;

        int quizTotalGrade = (quizLogs != null && !quizLogs.isEmpty()) ?
                quizLogs.stream().mapToInt(QuizLog::getScore).sum() : 0;

        return assignmentTotalGrade + quizTotalGrade;
    }

    public int calculateAttendance(int studentId, int courseId) {
        // الاعتماد على الداتا بيز مباشرة للسرعة القصوى
        return attendanceRepository.countStudentAttendanceInCourse(studentId, courseId);
    }

    public byte[] generatePerformanceChart(int courseId) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // 1. جلب بيانات الكورس للتأكد من وجوده
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

       // 2. جلب بيانات المستخدم الحالي من التوكن
        org.springframework.security.core.Authentication authentication =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();

// 3. التحقق إذا كان المستخدم أدمن (لو عندك رول أدمن مستقبلاً)
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Role.Admin.name()));

// 4. المنع الفوري لو المستخدم مش أدمن ومش هو المدرب صاحب الكورس
        if (!isAdmin && !course.getInstructor().getEmail().equals(currentUserEmail)) {
            throw new RuntimeException("Access Denied: You do not own this course.");
        }

// ... هنا يكمل باقي كود إنشاء الإكسيل أو الرسم البياني بتاعك عادي جداً ...
        List<Student> enrolledStudents = course.getEnrolledStudents();

        for (Student student : enrolledStudents) {
            int totalGrades = calculateTotalGrades(student.getId(), courseId);
            dataset.addValue(totalGrades, "Grades", student.getName());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Student Performance",
                "Students",
                "Total Grades",
                dataset
        );

        ByteArrayOutputStream chartOutputStream = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(chartOutputStream, chart, 800, 600);
        return chartOutputStream.toByteArray();
    }
}