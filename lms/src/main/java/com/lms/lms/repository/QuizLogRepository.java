package com.lms.lms.repository;

import com.lms.lms.model.AssignmentLog;
import com.lms.lms.model.QuizLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizLogRepository extends JpaRepository<QuizLog, Integer> {
    List<QuizLog> findAllByStudentIdAndQuizIdIn(int studentId, List<Integer> quizIds);
    QuizLog findByStudentIdAndQuizId(int studentId, int quizId); // تم تصحيح اسم المتغير لـ quizId

    // الدالة المطلوبة للـ Controller
    List<QuizLog> findByQuizId(Integer quizId);
}