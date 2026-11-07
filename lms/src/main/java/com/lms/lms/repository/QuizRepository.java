package com.lms.lms.repository;

import com.lms.lms.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Integer> {
    List<Quiz> findAllByCourseId(Integer courseId); // Fetch quizzes for a course
    // Find quizzes created by a specific instructor
}
