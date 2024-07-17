package com.uzumacademy.myTurn.service;


import com.uzumacademy.myTurn.model.Appointment;
import com.uzumacademy.myTurn.model.Doctor;
import com.uzumacademy.myTurn.model.User;
import com.uzumacademy.myTurn.repository.AppointmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    public AppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional
    public Appointment scheduleAppointment(User user, Doctor doctor, LocalDateTime appointmentTime) {
        Appointment appointment = new Appointment();
        appointment.setUser(user);
        appointment.setDoctor(doctor);
        appointment.setAppointmentTime(appointmentTime);
        appointment.setStatus("SCHEDULED");
        return appointmentRepository.save(appointment);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getUserAppointments(User user) {
        return appointmentRepository.findByUser(user);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getDoctorAppointments(Doctor doctor, LocalDateTime start, LocalDateTime end) {
        return appointmentRepository.findByDoctorAndAppointmentTimeBetween(doctor, start, end);
    }

    @Transactional
    public void cancelAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow();
        appointment.setStatus("CANCELLED");
        appointmentRepository.save(appointment);
    }
}
