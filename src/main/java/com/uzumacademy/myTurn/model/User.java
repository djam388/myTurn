package com.uzumacademy.myTurn.model;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private Long chatId;

    private String username;
    private String firstName;
    private String lastName;

    @Column(unique = true)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private RegistrationState registrationState = RegistrationState.NEW;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_active")
    private LocalDateTime lastActive;

    public User() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastActive = LocalDateTime.now();
    }

    public enum RegistrationState {
        NEW, AWAITING_FIRST_NAME, AWAITING_LAST_NAME, AWAITING_PHONE_NUMBER, COMPLETED
    }

    public void updateLastActive() {
        this.lastActive = LocalDateTime.now();
    }

    public boolean isRegistrationCompleted() {
        return this.registrationState == RegistrationState.COMPLETED;
    }
}