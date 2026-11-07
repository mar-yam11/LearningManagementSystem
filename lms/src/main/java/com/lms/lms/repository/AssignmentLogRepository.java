package com.lms.lms.repository;

import com.lms.lms.model.AssignmentLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentLogRepository extends JpaRepository<AssignmentLog, Integer> {
    List<AssignmentLog> findAllByStudentIdAndAssignmentIdIn(Integer studentId, List<Integer> assignmentIds);

    // التعديلات الاحترافية لفلترة البيانات مباشرة داخل قاعدة البيانات (Database Level)
    List<AssignmentLog> findByAssignmentId(Integer assignmentId);
    List<AssignmentLog> findByGradeIsNull();
    List<AssignmentLog> findByGradeIsNullAndAssignmentId(Integer assignmentId);
}