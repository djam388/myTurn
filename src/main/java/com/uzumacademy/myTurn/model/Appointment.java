package com.uzumacademy.myTurn.model;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "appointments")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    private LocalDateTime appointmentTime;

    private Integer duration;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;

    public enum AppointmentStatus {
        SCHEDULED, COMPLETED, CANCELLED
    }

    @Column(name = "last_rescheduled_at")
    private LocalDateTime lastRescheduledAt;

    @Column(name = "reschedule_count")
    private int rescheduleCount = 0;

    public boolean canBeRescheduled() {
        return this.appointmentTime.minusDays(2).isAfter(LocalDateTime.now());
    }

    public void reschedule(LocalDateTime newAppointmentTime) {
        this.lastRescheduledAt = LocalDateTime.now();
        this.rescheduleCount++;
        this.appointmentTime = newAppointmentTime;
    }
}
