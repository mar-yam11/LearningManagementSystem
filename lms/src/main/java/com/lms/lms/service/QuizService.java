package com.lms.lms.service;

import com.lms.lms.model.*;
import com.lms.lms.model.StudentAnswer;
import com.lms.lms.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizLogRepository quizLogRepository;
    private final QuestionRepository questionRepository;
    private final StudentRepository studentRepository;

    @Autowired
    public QuizService(QuizRepository quizRepository, QuizLogRepository quizLogRepository,
                       QuestionRepository questionRepository, StudentRepository studentRepository) {
        this.quizRepository = quizRepository;
        this.quizLogRepository = quizLogRepository;
        this.questionRepository = questionRepository;
        this.studentRepository = studentRepository;
    }

    public Quiz addQuiz(Quiz quiz) {

        List<Question> questions = new ArrayList<>();

        for (Question question : quiz.getQuestions()) {

            Question existingQuestion = questionRepository.findById(question.getId())
                    .orElseThrow(() ->
                            new RuntimeException("Question not found with ID: " + question.getId())
                    );

            questions.add(existingQuestion);
        }

        quiz.setQuestions(questions);

        return quizRepository.save(quiz);
    }

    public void deleteQuiz(Integer quizId) {
        quizRepository.deleteById(quizId);
    }

    public void updateQuizTotalGrade(Integer quizId, Integer totalGrade) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        quiz.setTotalGrade(totalGrade);
        quizRepository.save(quiz);
    }

    // اللوجيك الجديد بعد استخدام الـ StudentAnswer DTO
    public void answerQuiz(String token, Integer quizId, List<StudentAnswer> studentAnswers) {

        Integer studentId = extractStudentIdFromToken(token);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        int score = 0;
        List<Question> answeredQuestions = new ArrayList<>();
        List<String> textAnswers = new ArrayList<>();

        // المرور على إجابات الطالب، استخراج السؤال وتصحيحه
        for (StudentAnswer sa : studentAnswers) {
            Question question = questionRepository.findById(sa.getQuestionId())
                    .orElseThrow(() -> new RuntimeException("Question not found with ID: " + sa.getQuestionId()));

            answeredQuestions.add(question);
            textAnswers.add(sa.getAnswer());

            if (question.isCorrect(sa.getAnswer())) {
                score += question.getGrade() != null ? question.getGrade() : 1;
            }
        }

        // حفظ المحاولة
        QuizLog quizLog = new QuizLog();
        quizLog.setStudent(student);
        quizLog.setQuiz(quiz);
        quizLog.setAttemptDate(new Date());
        quizLog.setScore(score);
        quizLog.setStudentAnswers(textAnswers);
        quizLog.setQuestions(answeredQuestions);
        quizLog.setFeedback("Auto-graded successfully. Your score is: " + score);

        quizLogRepository.save(quizLog);
    }

    public List<QuizLog> getAllQuizLogs() {
        return quizLogRepository.findAll();
    }

    public List<QuizLog> getQuizLogsByQuizId(Integer quizId) {
        return quizLogRepository.findByQuizId(quizId);
    }

    public void deleteQuizLog(Integer quizLogId) {
        quizLogRepository.deleteById(quizLogId);
    }

    private Integer extractStudentIdFromToken(String token) {
        return 1;
    }
}