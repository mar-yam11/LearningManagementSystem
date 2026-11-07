package com.lms.lms.service;

import com.lms.lms.dto.StudentDTO;
import com.lms.lms.model.*;
import com.lms.lms.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentLogRepository assignmentLogRepository;
    private final LessonRepository lessonRepository;
    private final NotificationService notificationService;
    private final QuizRepository quizRepository;
    private final QuizLogRepository quizLogRepository;

    @Autowired
    public StudentService(StudentRepository studentRepository,
                          CourseRepository courseRepository,
                          AssignmentRepository assignmentRepository,
                          AssignmentLogRepository assignmentLogRepository,
                          LessonRepository lessonRepository,
                          NotificationService notificationService,
                          QuizRepository quizRepository,
                          QuizLogRepository quizLogRepository) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.assignmentRepository = assignmentRepository;
        this.assignmentLogRepository = assignmentLogRepository;
        this.lessonRepository = lessonRepository;
        this.notificationService = notificationService;
        this.quizRepository = quizRepository;
        this.quizLogRepository = quizLogRepository;
    }

    // 🔥 الدالة الجديدة لاستخراج الطالب من الـ Token
    public Student getAuthenticatedStudent() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated student not found"));
    }

    // 🔥 تم التعديل لإرجاع DTO بدلاً من Entity كامل
    public List<StudentDTO> getStudents() {
        return studentRepository.findAll().stream()
                .map(student -> new StudentDTO(student.getId(), student.getName(), student.getEmail()))
                .collect(Collectors.toList());
    }

    // 🔥 تم حذف studentId والاعتماد على التوكن
    public String enrollStudent(int courseId) {
        Student student = getAuthenticatedStudent();

        Optional<Course> courseOptional = courseRepository.findById(courseId);
        if (courseOptional.isEmpty()) {
            throw new IllegalArgumentException("Enrollment failed: Course not found.");
        }

        Course course = courseOptional.get();
        student.addCourse(course);
        course.addStudent(student);
        studentRepository.save(student);

        String successMessageForStudent = "Enrollment successful for course: " + course.getCourseTitle();
        notificationService.createNotification(student.getId(), Role.Student, successMessageForStudent);

        Instructor instructor = course.getInstructor();
        String successMessageForInstructor = "A student (" + student.getName()
                + ") has enrolled in your course: " + course.getCourseTitle();
        notificationService.createNotification(instructor.getId(), Role.Instructor, successMessageForInstructor);

        return successMessageForStudent;
    }

    public String submitAssignment(AssignmentLog assignmentLog) {
        // 🔥 إجبار الـ Log على استخدام الـ ID بتاع صاحب التوكن لمنع التلاعب
        Student student = getAuthenticatedStudent();
        assignmentLog.setStudentId(student.getId());

        if (assignmentRepository.findById(assignmentLog.getAssignment().getId()).isEmpty()) {
            throw new IllegalArgumentException("Assignment not found.");
        }

        assignmentLogRepository.save(assignmentLog);
        return "Assignment submitted successfully.";
    }

    public String getOtpForLesson(long lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found."))
                .getOtp();
    }

    // 🔥 تم حذف studentId
    public String takeQuiz(Integer quizId, List<String> answers) {
        Student student = getAuthenticatedStudent();

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found."));

        List<Question> questions = quiz.getQuestions();

        if (answers.size() != questions.size()) {
            throw new IllegalArgumentException("Number of answers does not match the number of questions.");
        }

        int score = 0;
        for (int i = 0; i < questions.size(); i++) {
            if (questions.get(i).isCorrect(answers.get(i))) {
                score += questions.get(i).getGrade();
            }
        }

        QuizLog quizLog = new QuizLog();
        quizLog.setStudent(student);
        quizLog.setQuiz(quiz);
        quizLog.setScore(score);
        quizLogRepository.save(quizLog);

        String resultMessage = "You have completed the quiz: " + quiz.getTitle() + ". Your score is: " + score;
        notificationService.createNotification(student.getId(), Role.Student, resultMessage);

        return resultMessage;
    }

    // 🔥 تم حذف studentId
    public String getQuizWithRandomQuestions(Integer quizId) {
        Student student = getAuthenticatedStudent(); // للتأكد أن من يطلب الكويز طالب مسجل
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found."));

        List<Question> questions = quiz.getQuestions();
        if (questions == null || questions.isEmpty()) {
            return "No questions available for this quiz yet.";
        }

        StringBuilder quizContent = new StringBuilder();
        quizContent.append("Quiz Title: ").append(quiz.getTitle()).append("\n");
        quizContent.append("Total Questions: ").append(quiz.getNumberOfQuestions()).append("\n");
        quizContent.append("--------------------------------------------------\n\n");

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            quizContent.append("Question ").append(i + 1).append(": ").append(q.getTitle()).append("\n");

            if (q.getOptions() != null) {
                for (int j = 0; j < q.getOptions().size(); j++) {
                    char optionLetter = (char) ('A' + j);
                    quizContent.append(" ").append(optionLetter).append(") ").append(q.getOptions().get(j)).append("\n");
                }
            }
            quizContent.append("\n");
        }

        return quizContent.toString();
    }
}