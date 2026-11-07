package com.lms.lms.repository;

import com.lms.lms.model.Notification;
import com.lms.lms.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdAndUserType(int userId, Role userType);
    List<Notification> findByUserIdAndUserTypeAndIsReadFalse(int userId, Role userType);
}
