package com.lms.lms.service;

import com.lms.lms.dto.InstructorDTO;
import com.lms.lms.model.*;
import com.lms.lms.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class InstructorService {
    private final CourseRepository courseRepository;
    private final InstructorRepository instructorRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentLogRepository assignmentLogRepository;
    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;
    private final QuizLogRepository quizLogRepository;
    private final LessonRepository lessonRepository;
    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;
    private final NotificationService notificationService;
    private final LessonService lessonService;

    @Autowired
    public InstructorService(InstructorRepository instructorRepository,
                             CourseRepository courseRepository,
                             AssignmentRepository assignmentRepository,
                             AssignmentLogRepository assignmentLogRepository,
                             QuestionRepository questionRepository,
                             QuizRepository quizRepository,
                             QuizLogRepository quizLogRepository,
                             LessonRepository lessonRepository,
                             StudentRepository studentRepository,
                             AttendanceRepository attendanceRepository,
                             NotificationService notificationService,
                             LessonService lessonService) {
        this.courseRepository = courseRepository;
        this.instructorRepository = instructorRepository;
        this.assignmentRepository = assignmentRepository;
        this.assignmentLogRepository = assignmentLogRepository;
        this.questionRepository = questionRepository;
        this.quizRepository = quizRepository;
        this.quizLogRepository = quizLogRepository;
        this.lessonRepository = lessonRepository;
        this.studentRepository = studentRepository;
        this.attendanceRepository = attendanceRepository;
        this.notificationService = notificationService;
        this.lessonService = lessonService;
    }

    // 🔥 الدالة الجديدة لاستخراج المدرب من التوكن
    public Instructor getAuthenticatedInstructor() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return instructorRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated instructor not found"));
    }

    // ---------- Course management ----------

    public Course createCourse(String courseTitle, int courseDuration) {
        if (courseDuration <= 0) {
            throw new IllegalArgumentException("Invalid course duration.");
        }
        Instructor instructor = getAuthenticatedInstructor(); // استخراج آمن

        Course course = new Course(courseTitle, courseDuration);
        course.setInstructor(instructor);
        return courseRepository.save(course);
    }

    public void deleteCourse(int courseId) {
        Instructor instructor = getAuthenticatedInstructor();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course with ID not found."));

        if (course.getInstructor().getId() != instructor.getId()) {
            throw new IllegalArgumentException("You don't have permission to delete this course.");
        }
        courseRepository.deleteById(courseId);
    }

    public List<InstructorDTO> getAllInstructors() {
        return instructorRepository.findAll().stream()
                .map(inst -> new InstructorDTO(inst.getId(), inst.getName(), inst.getEmail()))
                .collect(Collectors.toList());
    }

    // ---------- Lessons ----------

    public void addLesson(Lesson lesson, int courseId) throws Exception {
        Instructor instructor = getAuthenticatedInstructor();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (course.getInstructor().getId() != instructor.getId()) {
            throw new Exception("Instructor does not have permission to add lessons to this course.");
        }
        lesson.setOtp(lessonService.generateOtp());
        lesson.setCourse(course);
        lessonRepository.save(lesson);
    }

    public List<Lesson> getAllLessons() {
        return lessonRepository.findAll();
    }

    // ---------- Quizzes & questions ----------

    public void createQuiz(Integer courseId, String title, Integer numberOfQuestions) {
        Instructor instructor = getAuthenticatedInstructor();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (course.getInstructor().getId() != instructor.getId()) {
            throw new IllegalArgumentException("Instructor does not have permission to modify this course.");
        }

        List<Question> questions = questionRepository.findByCourseId(courseId);
        Set<Question> questionSet = new HashSet<>();
        while (questionSet.size() < numberOfQuestions) {
            int questionNumber = (int) (Math.random() * questions.size());
            questionSet.add(questions.get(questionNumber));
        }
        questions = new ArrayList<>(questionSet);

        Quiz quiz = new Quiz();
        quiz.setCourse(course);
        quiz.setTitle(title);
        quiz.setNumberOfQuestions(numberOfQuestions);
        quiz.setTotalGrade(numberOfQuestions);
        quiz.setQuestions(questions);
        quizRepository.save(quiz);

        String notification = quiz.getTitle() + " has been added to " + quiz.getCourse().getCourseTitle();
        for (Student student : course.getEnrolledStudents()) {
            notificationService.createNotification(student.getId(), Role.Student, notification);
        }
    }

    public void createQuestion(Question question) {
        Instructor instructor = getAuthenticatedInstructor();
        Course course = courseRepository.findById(question.getCourse().getId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (course.getInstructor().getId() != instructor.getId()) {
            throw new IllegalArgumentException("Instructor does not have permission to modify this course.");
        }
        questionRepository.save(question);
    }

    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    public List<Quiz> getAllQuizzes() {
        return quizRepository.findAll();
    }

    // ---------- Assignments ----------

    public List<Assignment> getAllAssignments() {
        return assignmentRepository.findAll();
    }

    public List<AssignmentLog> getAllAssignmentLogs() {
        return assignmentLogRepository.findAll();
    }

    public int countStudentSubmissions(int courseId, int studentId) {
        List<Assignment> assignments = assignmentRepository.findAllByCourseId(courseId);
        if (assignments.isEmpty()) {
            throw new IllegalArgumentException("No assignments found for the course ID.");
        }
        List<Integer> assignmentIds = assignments.stream().map(Assignment::getId).toList();

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        long submissionCount = assignmentLogRepository.findAll().stream()
                .filter(log -> Objects.equals(log.getStudentId(), student.getId())
                        && assignmentIds.contains(log.getAssignment().getId())
                        && log.getAnsweredPdfPath() != null)
                .count();

        return (int) submissionCount;
    }

    // ---------- Attendance tracking ----------

    public List<Integer> getAttendingStudentIds(long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));

        return attendanceRepository.findAll().stream()
                .filter(attendance -> Objects.equals(attendance.getLesson().getId(), lesson.getId()))
                .filter(Attendance::isAttended)
                .map(attendance -> attendance.getStudent().getId())
                .toList();
    }

    public int countStudentAttendance(int studentId, int courseId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        List<Lesson> courseLessons = lessonRepository.findAll().stream()
                .filter(lesson -> Objects.equals(lesson.getCourse().getId(), course.getId()))
                .toList();

        long attendedLessonCount = attendanceRepository.findAll().stream()
                .filter(attendance -> Objects.equals(attendance.getStudent().getId(), student.getId())
                        && courseLessons.contains(attendance.getLesson())
                        && attendance.isAttended())
                .count();

        return (int) attendedLessonCount;
    }

    // ---------- Quiz performance tracking ----------

    public Map<Integer, Integer> getQuizScoresByStudent(int quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));

        List<QuizLog> quizLogs = quizLogRepository.findAll().stream()
                .filter(quizLog -> Objects.equals(quizLog.getQuiz().getId(), quiz.getId()))
                .toList();

        Map<Integer, Integer> studentGrades = new HashMap<>();
        for (QuizLog quizLog : quizLogs) {
            studentGrades.put(quizLog.getStudent().getId(), quizLog.getScore());
        }
        return studentGrades;
    }

    public Map<String, Integer> getStudentQuizScoresForCourse(int studentId, int courseId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        List<QuizLog> quizLogs = quizLogRepository.findAll().stream()
                .filter(quizLog -> Objects.equals(quizLog.getQuiz().getCourse().getId(), course.getId()))
                .filter(quizLog -> Objects.equals(quizLog.getStudent().getId(), student.getId()))
                .toList();

        Map<String, Integer> studentGrades = new HashMap<>();
        for (QuizLog quizLog : quizLogs) {
            studentGrades.put(quizLog.getQuiz().getTitle(), quizLog.getScore());
        }
        return studentGrades;
    }
}