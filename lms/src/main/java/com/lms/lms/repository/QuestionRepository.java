package com.lms.lms.repository;

import com.lms.lms.model.Question;
import com.lms.lms.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {
    List<Question> findByCourseId(Integer courseId);
}
