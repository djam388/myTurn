package com.uzumacademy.myTurn.repository;

import com.uzumacademy.myTurn.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findBySpecialization(String specialization);

    @Query("SELECT DISTINCT d FROM Doctor d WHERE d.isActive = true AND d.id NOT IN " +
            "(SELECT DISTINCT a.doctor.id FROM Appointment a WHERE " +
            "(:startTime BETWEEN a.appointmentTime AND FUNCTION('TIMESTAMPADD', MINUTE, COALESCE(a.duration, 60), a.appointmentTime)) OR " +
            "(:endTime BETWEEN a.appointmentTime AND FUNCTION('TIMESTAMPADD', MINUTE, COALESCE(a.duration, 60), a.appointmentTime)) OR " +
            "(a.appointmentTime BETWEEN :startTime AND :endTime))")
    List<Doctor> findAvailableDoctors(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}