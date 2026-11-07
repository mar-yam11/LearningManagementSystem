package com.lms.lms.service;

import com.lms.lms.model.*;
import com.lms.lms.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

@Service
public class AttendanceService {

    private final LessonRepository lessonRepository;
    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository; // تم إضافة الريبوزيتوري لجلب الطالب الحقيقي

    @Autowired
    public AttendanceService(LessonRepository lessonRepository, AttendanceRepository attendanceRepository, StudentRepository studentRepository) {
        this.lessonRepository = lessonRepository;
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
    }

    public boolean markAttendance(Long lessonId, String otp, int studentId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        // التأكد من أن الـ OTP ليس فارغاً ومطابق
        if (lesson.getOtp() != null && lesson.getOtp().equals(otp)) {
            Attendance attendance = new Attendance();
            attendance.setLesson(lesson);

            // جلب الطالب الفعلي من قاعدة البيانات بدلاً من الكود الثابت
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            attendance.setStudent(student);
            attendance.setAttended(true);
            attendance.setTimestamp(LocalDateTime.now());
            attendanceRepository.save(attendance);
            return true;
        }
        return false; // Invalid OTP
    }
}