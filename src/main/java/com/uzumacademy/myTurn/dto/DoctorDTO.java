package com.uzumacademy.myTurn.dto;

import com.uzumacademy.myTurn.model.Doctor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class DoctorDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String specialization;
    private String phoneNumber;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isActive;
    private List<WorkingHoursDTO> workingHours = new ArrayList<>();

    public static DoctorDTO fromDoctor(Doctor doctor) {
        DoctorDTO dto = new DoctorDTO();
        dto.setId(doctor.getId());
        dto.setFirstName(doctor.getFirstName());
        dto.setLastName(doctor.getLastName());
        dto.setSpecialization(doctor.getSpecialization());
        dto.setPhoneNumber(doctor.getPhoneNumber());
        dto.setEmail(doctor.getEmail());
        dto.setCreatedAt(doctor.getCreatedAt());
        dto.setUpdatedAt(doctor.getUpdatedAt());
        dto.setActive(doctor.isActive());
        dto.setWorkingHours(doctor.getWorkingHours().stream()
                .map(WorkingHoursDTO::fromWorkingHours)
                .collect(Collectors.toList()));
        return dto;
    }

    public Doctor toDoctor() {
        Doctor doctor = new Doctor();
        doctor.setId(this.getId());
        doctor.setFirstName(this.getFirstName());
        doctor.setLastName(this.getLastName());
        doctor.setSpecialization(this.getSpecialization());
        doctor.setPhoneNumber(this.getPhoneNumber());
        doctor.setEmail(this.getEmail());
        doctor.setCreatedAt(this.getCreatedAt());
        doctor.setUpdatedAt(this.getUpdatedAt());
        doctor.setActive(this.isActive());
        doctor.setWorkingHours(this.getWorkingHours().stream()
                .map(WorkingHoursDTO::toWorkingHours)
                .collect(Collectors.toList()));
        return doctor;
    }

    @Data
    public static class WorkingHoursDTO {
        private String dayOfWeek;
        private LocalDateTime startTime;
        private LocalDateTime endTime;

        public static WorkingHoursDTO fromWorkingHours(Doctor.WorkingHours workingHours) {
            WorkingHoursDTO dto = new WorkingHoursDTO();
            dto.setDayOfWeek(workingHours.getDayOfWeek());
            dto.setStartTime(workingHours.getStartTime());
            dto.setEndTime(workingHours.getEndTime());
            return dto;
        }

        public Doctor.WorkingHours toWorkingHours() {
            Doctor.WorkingHours workingHours = new Doctor.WorkingHours();
            workingHours.setDayOfWeek(this.getDayOfWeek());
            workingHours.setStartTime(this.getStartTime());
            workingHours.setEndTime(this.getEndTime());
            return workingHours;
        }
    }
}