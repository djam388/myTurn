package com.uzumacademy.myTurn.repository;

import com.uzumacademy.myTurn.model.Appointment;
import com.uzumacademy.myTurn.model.Doctor;
import com.uzumacademy.myTurn.model.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query("SELECT a FROM Appointment a WHERE a.user = :user AND a.appointmentTime >= :currentTime AND a.status != 'CANCELLED' ORDER BY a.appointmentTime ASC")
    List<Appointment> findCurrentAndFutureAppointmentsByUser(@Param("user") User user, @Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT a FROM Appointment a WHERE a.id = :appointmentId AND a.appointmentTime > :twoDateBeforeAppointment")
    Optional<Appointment> findReschedulableAppointment(@Param("appointmentId") Long appointmentId, @Param("twoDateBeforeAppointment") LocalDateTime twoDateBeforeAppointment);

    Optional<Appointment> findById(Long id);

    List<Appointment> findByDoctorAndAppointmentTimeBetweenAndStatusNot(Doctor doctor, LocalDateTime start, LocalDateTime end, Appointment.AppointmentStatus status);

    Optional<Appointment> findByDoctorAndAppointmentTimeAndStatusNot(Doctor doctor, LocalDateTime appointmentTime, Appointment.AppointmentStatus status);


    List<Appointment> findAll(Sort sort);

    @Query("SELECT a FROM Appointment a WHERE " +
            "a.appointmentTime BETWEEN :startDate AND :endDate " +
            "AND (:doctorId IS NULL OR a.doctor.id = :doctorId) " +
            "AND (:status IS NULL OR a.status = :status)")
    List<Appointment> findFilteredAppointments(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("doctorId") Long doctorId,
            @Param("status") Appointment.AppointmentStatus status
    );
}