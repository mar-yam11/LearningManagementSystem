package com.lms.lms.service;

import com.lms.lms.model.Notification;
import com.lms.lms.model.Role;
import com.lms.lms.model.Student;
import com.lms.lms.model.Instructor;
import com.lms.lms.repository.NotificationRepository;
import com.lms.lms.repository.StudentRepository;
import com.lms.lms.repository.InstructorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final StudentRepository studentRepository;
    private final InstructorRepository instructorRepository;
    private final JavaMailSender mailSender;

    @Autowired
    public NotificationService(StudentRepository studentRepository, InstructorRepository instructorRepository, JavaMailSender mailSender, NotificationRepository notificationRepository) {
        this.studentRepository = studentRepository;
        this.instructorRepository = instructorRepository;
        this.notificationRepository = notificationRepository;
        this.mailSender = mailSender;
    }

    // 🔥 دالة مساعدة لتحديد المستخدم الحالي و الـ ID بتاعه والـ Role من الـ Token
    public UserContext getAuthenticatedUserContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        String roleStr = authentication.getAuthorities().iterator().next().getAuthority();
        Role role = Role.valueOf(roleStr);

        int userId;
        if (role == Role.Student) {
            userId = studentRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Student not found")).getId();
        } else {
            userId = instructorRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Instructor not found")).getId();
        }

        return new UserContext(userId, role);
    }

    // كلاس مساعد (Inner Class) لترجيع الـ ID والـ Role مع بعض
    public static class UserContext {
        public int userId;
        public Role role;

        public UserContext(int userId, Role role) {
            this.userId = userId;
            this.role = role;
        }
    }


    // 🔥 الدوال دي دلوقتي بتستخدم الـ Context بدل ما تاخد الـ ID كـ Parameter
    public List<Notification> getMyUnReadNotifications() {
        UserContext context = getAuthenticatedUserContext();
        return notificationRepository.findByUserIdAndUserTypeAndIsReadFalse(context.userId, context.role);
    }

    public List<Notification> getMyAllNotifications() {
        UserContext context = getAuthenticatedUserContext();
        return notificationRepository.findByUserIdAndUserType(context.userId, context.role);
    }


    public String getEmailForUser(int userId, Role userType) {
        if (userType == Role.Student) {
            Optional<Student> studentOptional = studentRepository.findById(userId);
            return studentOptional.map(Student::getEmail).orElse(null);
        } else if (userType == Role.Instructor) {
            Optional<Instructor> instructorOptional = instructorRepository.findById(userId);
            return instructorOptional.map(Instructor::getEmail).orElse(null);
        }
        return null;
    }

    public String sendEmailNotification(int userId, Role userType, String message) {
        String email = getEmailForUser(userId, userType);

        if (email != null) {
            try {
                SimpleMailMessage mailMessage = new SimpleMailMessage();
                mailMessage.setTo(email);
                mailMessage.setSubject("LMS Notification");
                mailMessage.setText(message);
                mailSender.send(mailMessage);
                return "Notification sent successfully to your email.";
            } catch (Exception e) {
                System.err.println("Error sending email: " + e.getMessage());
                return "Error sending email notification.";
            }
        }
        return "User email not found.";
    }

    public void createNotification(int userId, Role userType, String message) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setUserType(userType);
        notification.setMessage(message);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);

        sendEmailNotification(userId, userType, message);
    }

    // 🔥 تأمين عملية القراءة: نتأكد إن الإشعار يخص الشخص اللي بيحاول يعمله Read
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        UserContext context = getAuthenticatedUserContext();

        // التحقق من الصلاحية
        if (notification.getUserId() != context.userId || notification.getUserType() != context.role) {
            throw new RuntimeException("You do not have permission to mark this notification as read.");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }
}