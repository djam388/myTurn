package com.uzumacademy.myTurn.service;

import com.uzumacademy.myTurn.model.Doctor;
import com.uzumacademy.myTurn.repository.DoctorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DoctorService {

    private static final Logger logger = LoggerFactory.getLogger(DoctorService.class);

    private final DoctorRepository doctorRepository;

    @Autowired
    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    @Transactional(readOnly = true)
    public List<Doctor> getAllDoctors() {
        logger.info("Fetching all doctors");
        return doctorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Doctor> getActiveDoctors() {
        logger.info("Fetching all active doctors");
        return doctorRepository.findAllActiveDoctors();
    }

    @Transactional(readOnly = true)
    public Doctor getDoctorById(Long id) {
        logger.info("Fetching doctor by id: {}", id);
        return doctorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Doctor getDoctorWithWorkingHours(Long id) {
        logger.info("Fetching doctor with working hours by id: {}", id);
        return doctorRepository.findWithWorkingHoursById(id)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Doctor> getDoctorsBySpecialization(String specialization) {
        logger.info("Fetching doctors by specialization: {}", specialization);
        return doctorRepository.findBySpecialization(specialization);
    }

    @Transactional
    public Doctor addDoctor(Doctor doctor) {
        logger.info("Adding new doctor: {} {}", doctor.getFirstName(), doctor.getLastName());
        return doctorRepository.save(doctor);
    }

    @Transactional
    public Doctor updateDoctor(Doctor doctor) {
        logger.info("Updating doctor with id: {}", doctor.getId());
        if (!doctorRepository.existsById(doctor.getId())) {
            throw new EntityNotFoundException("Doctor not found with id: " + doctor.getId());
        }
        return doctorRepository.save(doctor);
    }

    @Transactional
    public void deleteDoctor(Long id) {
        logger.info("Deleting doctor with id: {}", id);
        doctorRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Doctor> getAvailableDoctors(LocalDateTime startTime, LocalDateTime endTime) {
        logger.info("Fetching available doctors between {} and {}", startTime, endTime);
        return doctorRepository.findAvailableDoctors(startTime, endTime);
    }

    @Transactional
    public void setDoctorWorkingHours(Long doctorId, List<Doctor.WorkingHours> workingHours) {
        logger.info("Setting working hours for doctor with id: {}", doctorId);
        Doctor doctor = getDoctorById(doctorId);
        doctor.setWorkingHours(workingHours);
        doctorRepository.save(doctor);
    }

    @Transactional(readOnly = true)
    public List<Doctor.WorkingHours> getDoctorWorkingHours(Long doctorId) {
        logger.info("Fetching working hours for doctor with id: {}", doctorId);
        Doctor doctor = getDoctorWithWorkingHours(doctorId);
        return doctor.getWorkingHours();
    }

    @Transactional
    public void setDoctorActiveStatus(Long doctorId, boolean isActive) {
        logger.info("Setting active status for doctor with id: {} to {}", doctorId, isActive);
        Doctor doctor = getDoctorById(doctorId);
        doctor.setActive(isActive);
        doctorRepository.save(doctor);
    }

    @Transactional(readOnly = true)
    public List<Doctor> searchDoctors(String specialization, Boolean isActive, String lastName) {
        logger.info("Searching doctors with criteria - specialization: {}, isActive: {}, lastName: {}",
                specialization, isActive, lastName);
        return doctorRepository.findDoctorsByCriteria(specialization, isActive, lastName);
    }

    @Transactional(readOnly = true)
    public long countActiveDoctors() {
        logger.info("Counting active doctors");
        return doctorRepository.countActiveDoctors();
    }

    @Transactional(readOnly = true)
    public List<Doctor> getDoctorsWithMostAppointments() {
        logger.info("Fetching doctors with most appointments");
        return doctorRepository.findDoctorsWithMostAppointments();
    }
}