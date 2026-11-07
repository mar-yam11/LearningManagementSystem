package com.lms.lms.repository;

import com.lms.lms.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
// تم تعديل Integer إلى Long ليتطابق مع الـ Entity
public interface LessonRepository extends JpaRepository<Lesson, Long> {
}