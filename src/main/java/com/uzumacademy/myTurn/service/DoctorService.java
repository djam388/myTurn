package com.uzumacademy.myTurn.service;

import com.uzumacademy.myTurn.dto.DoctorDTO;
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
import java.util.stream.Collectors;

@Service
public class DoctorService {

    private static final Logger logger = LoggerFactory.getLogger(DoctorService.class);

    private final DoctorRepository doctorRepository;

    @Autowired
    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    @Transactional(readOnly = true)
    public List<DoctorDTO> getAllDoctors() {
        logger.info("Fetching all doctors");
        return doctorRepository.findAll().stream()
                .map(DoctorDTO::fromDoctor)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DoctorDTO> getActiveDoctors() {
        logger.info("Fetching all active doctors");
        return doctorRepository.findAllActiveDoctors().stream()
                .map(DoctorDTO::fromDoctor)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DoctorDTO getDoctorById(Long id) {
        logger.info("Fetching doctor by id: {}", id);
        return doctorRepository.findById(id)
                .map(DoctorDTO::fromDoctor)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public DoctorDTO getDoctorWithWorkingHours(Long id) {
        logger.info("Fetching doctor with working hours by id: {}", id);
        return doctorRepository.findWithWorkingHoursById(id)
                .map(DoctorDTO::fromDoctor)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<DoctorDTO> getDoctorsBySpecialization(String specialization) {
        logger.info("Fetching doctors by specialization: {}", specialization);
        return doctorRepository.findBySpecialization(specialization).stream()
                .map(DoctorDTO::fromDoctor)
                .collect(Collectors.toList());
    }

    @Transactional
    public DoctorDTO updateDoctor(DoctorDTO doctorDTO) {
        logger.info("Updating doctor with id: {}", doctorDTO.getId());
        if (!doctorRepository.existsById(doctorDTO.getId())) {
            throw new EntityNotFoundException("Doctor not found with id: " + doctorDTO.getId());
        }
        Doctor doctor = doctorDTO.toDoctor();
        Doctor updatedDoctor = doctorRepository.save(doctor);
        return DoctorDTO.fromDoctor(updatedDoctor);
    }

    @Transactional
    public void deleteDoctor(Long id) {
        logger.info("Deleting doctor with id: {}", id);
        doctorRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<DoctorDTO> getAvailableDoctors(LocalDateTime startTime, LocalDateTime endTime) {
        logger.info("Fetching available doctors between {} and {}", startTime, endTime);
        return doctorRepository.findAvailableDoctors(startTime, endTime).stream()
                .map(DoctorDTO::fromDoctor)
                .collect(Collectors.toList());
    }

    @Transactional
    public void setDoctorWorkingHours(Long doctorId, List<DoctorDTO.WorkingHoursDTO> workingHoursDTO) {
        logger.info("Setting working hours for doctor with id: {}", doctorId);
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found with id: " + doctorId));
        doctor.setWorkingHours(workingHoursDTO.stream()
                .map(DoctorDTO.WorkingHoursDTO::toWorkingHours)
                .collect(Collectors.toList()));
        doctorRepository.save(doctor);
    }

    @Transactional(readOnly = true)
    public List<DoctorDTO.WorkingHoursDTO> getDoctorWorkingHours(Long doctorId) {
        logger.info("Fetching working hours for doctor with id: {}", doctorId);
        Doctor doctor = getDoctorWithWorkingHours(doctorId).toDoctor();
        return doctor.getWorkingHours().stream()
                .map(DoctorDTO.WorkingHoursDTO::fromWorkingHours)
                .collect(Collectors.toList());
    }

    @Transactional
    public void setDoctorActiveStatus(Long doctorId, boolean isActive) {
        logger.info("Setting active status for doctor with id: {} to {}", doctorId, isActive);
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found with id: " + doctorId));
        doctor.setActive(isActive);
        doctorRepository.save(doctor);
    }

    @Transactional(readOnly = true)
    public List<DoctorDTO> searchDoctors(String specialization, Boolean isActive, String lastName) {
        logger.info("Searching doctors with criteria - specialization: {}, isActive: {}, lastName: {}",
                specialization, isActive, lastName);
        return doctorRepository.findDoctorsByCriteria(specialization, isActive, lastName).stream()
                .map(DoctorDTO::fromDoctor)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long countActiveDoctors() {
        logger.info("Counting active doctors");
        return doctorRepository.countActiveDoctors();
    }

    @Transactional(readOnly = true)
    public List<DoctorDTO> getDoctorsWithMostAppointments() {
        logger.info("Fetching doctors with most appointments");
        return doctorRepository.findDoctorsWithMostAppointments().stream()
                .map(DoctorDTO::fromDoctor)
                .collect(Collectors.toList());
    }
}