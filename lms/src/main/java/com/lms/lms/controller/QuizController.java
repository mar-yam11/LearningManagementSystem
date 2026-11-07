package com.lms.lms.controller;

import com.lms.lms.model.Quiz;
import com.lms.lms.model.QuizLog;
import com.lms.lms.model.StudentAnswer; // استيراد الـ DTO
import com.lms.lms.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/quiz")
public class QuizController {

    private final QuizService quizService;

    @Autowired
    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping("/add")
    public Quiz addQuiz(@RequestBody Quiz quiz) {
        return quizService.addQuiz(quiz);
    }

    @DeleteMapping("/delete/{quizId}")
    public void deleteQuiz(@PathVariable Integer quizId) {
        quizService.deleteQuiz(quizId);
    }

    @PutMapping("/update-grade/{quizId}/{totalGrade}")
    public void updateQuizTotalGrade(@PathVariable Integer quizId, @PathVariable Integer totalGrade) {
        quizService.updateQuizTotalGrade(quizId, totalGrade);
    }

    // تم تعديل الاستقبال ليكون قائمة من كائنات StudentAnswer
    @PostMapping("/answer")
    public void answerQuiz(@RequestParam String token, @RequestParam Integer quizId, @RequestBody List<StudentAnswer> studentAnswers) {
        quizService.answerQuiz(token, quizId, studentAnswers);
    }

    @GetMapping("/logs")
    public List<QuizLog> viewAllQuizLogs() {
        return quizService.getAllQuizLogs();
    }

    @GetMapping("/logs/{quizId}")
    public List<QuizLog> viewQuizLogsForSpecificQuiz(@PathVariable Integer quizId) {
        return quizService.getQuizLogsByQuizId(quizId);
    }

    @DeleteMapping("/logs/delete/{quizLogId}")
    public void deleteQuizLog(@PathVariable Integer quizLogId) {
        quizService.deleteQuizLog(quizLogId);
    }
}