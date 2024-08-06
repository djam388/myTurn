package com.uzumacademy.myTurn.repository;

import com.uzumacademy.myTurn.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    List<Doctor> findBySpecialization(String specialization);

    @Query("SELECT d FROM Doctor d WHERE d.isActive = true")
    List<Doctor> findAllActiveDoctors();

    @Query("SELECT d FROM Doctor d LEFT JOIN FETCH d.workingHours WHERE d.id = :id")
    Optional<Doctor> findWithWorkingHoursById(@Param("id") Long id);

    @Query("SELECT DISTINCT d FROM Doctor d " +
            "LEFT JOIN d.appointments a " +
            "WHERE d.isActive = true " +
            "AND (a IS NULL OR a.appointmentTime NOT BETWEEN :startTime AND :endTime)")
    List<Doctor> findAvailableDoctors(@Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime);

    @Query("SELECT d FROM Doctor d WHERE d.lastName LIKE %:lastName%")
    List<Doctor> findByLastNameContaining(@Param("lastName") String lastName);

    @Query("SELECT d FROM Doctor d " +
            "WHERE d.isActive = true " +
            "AND EXISTS (SELECT wh FROM d.workingHours wh WHERE :currentTime BETWEEN wh.startTime AND wh.endTime)")
    List<Doctor> findDoctorsCurrentlyWorking(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT COUNT(d) FROM Doctor d WHERE d.isActive = true")
    long countActiveDoctors();

    @Query("SELECT d FROM Doctor d WHERE SIZE(d.appointments) = (" +
            "SELECT MAX(SIZE(d2.appointments)) FROM Doctor d2)")
    List<Doctor> findDoctorsWithMostAppointments();

    @Query("SELECT d FROM Doctor d " +
            "WHERE (:specialization IS NULL OR d.specialization = :specialization) " +
            "AND (:isActive IS NULL OR d.isActive = :isActive) " +
            "AND (:lastName IS NULL OR d.lastName LIKE %:lastName%)")
    List<Doctor> findDoctorsByCriteria(@Param("specialization") String specialization,
                                       @Param("isActive") Boolean isActive,
                                       @Param("lastName") String lastName);
}