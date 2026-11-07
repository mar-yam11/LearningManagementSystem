package com.lms.lms.repository;

import com.lms.lms.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {

    // استخدمنا @Query عشان نكتب اللوجيك بشكل صريح ونمنع أي لخبطة من Spring
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student.id = :studentId AND a.lesson.course.id = :courseId AND a.attended = true")
    int countStudentAttendanceInCourse(@Param("studentId") int studentId, @Param("courseId") int courseId);
}

