package com.uzumacademy.myTurn.dto;

import com.uzumacademy.myTurn.model.Appointment;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppointmentDTO {
    private Long id;
    private Long userId;
    private String userFirstName;
    private String userLastName;
    private String userPhoneNumber;
    private DoctorDTO doctor;
    private LocalDateTime appointmentTime;
    private String status;

    public static AppointmentDTO fromAppointment(Appointment appointment) {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(appointment.getId());
        dto.setUserId(appointment.getUser().getId());
        dto.setUserFirstName(appointment.getUser().getFirstName());
        dto.setUserLastName(appointment.getUser().getLastName());
        dto.setUserPhoneNumber(appointment.getUser().getPhoneNumber());
        dto.setDoctor(DoctorDTO.fromDoctor(appointment.getDoctor()));
        dto.setAppointmentTime(appointment.getAppointmentTime());
        dto.setStatus(appointment.getStatus().toString());
        return dto;
    }

    public Appointment toAppointment() {
        Appointment appointment = new Appointment();
        appointment.setId(this.getId());
        appointment.setAppointmentTime(this.getAppointmentTime());
        appointment.setStatus(Appointment.AppointmentStatus.valueOf(this.getStatus()));

        return appointment;
    }
}