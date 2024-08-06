package com.uzumacademy.myTurn.repository;

import com.uzumacademy.myTurn.model.Appointment;
import com.uzumacademy.myTurn.model.Doctor;
import com.uzumacademy.myTurn.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByUser(User user);

    List<Appointment> findByDoctor(Doctor doctor);

    List<Appointment> findByDoctorAndAppointmentTimeBetween(Doctor doctor, LocalDateTime start, LocalDateTime end);

    Optional<Appointment> findByDoctorAndAppointmentTime(Doctor doctor, LocalDateTime appointmentTime);

    List<Appointment> findByAppointmentTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.doctor = :doctor AND a.appointmentTime BETWEEN :start AND :end")
    long countByDoctorAndAppointmentTimeBetween(@Param("doctor") Doctor doctor, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT a FROM Appointment a WHERE a.user = :user AND a.appointmentTime >= :currentTime AND a.status != 'CANCELLED' ORDER BY a.appointmentTime ASC")
    List<Appointment> findCurrentAndFutureAppointmentsByUser(@Param("user") User user, @Param("currentTime") LocalDateTime currentTime);
}