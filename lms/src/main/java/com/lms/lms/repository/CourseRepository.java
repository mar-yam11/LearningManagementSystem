package com.lms.lms.repository;

import com.lms.lms.model.Course;
import com.lms.lms.model.Instructor;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.repository.query.Param;
// import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Integer> {
    List<Course> findByInstructor(Instructor instructor);
}