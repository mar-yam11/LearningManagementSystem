package com.lms.lms.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int userId;
    private String message;

    @Column(name = "is_read")
    private boolean isRead = false;

    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING) // لتخزين نوع المستخدم كنص واضح
    private Role userType;
}