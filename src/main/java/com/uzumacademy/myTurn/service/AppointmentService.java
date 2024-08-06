package com.uzumacademy.myTurn.service;

import com.uzumacademy.myTurn.model.Appointment;
import com.uzumacademy.myTurn.model.Doctor;
import com.uzumacademy.myTurn.model.User;
import com.uzumacademy.myTurn.repository.AppointmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class AppointmentService {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentService.class);

    private final AppointmentRepository appointmentRepository;

    @Autowired
    public AppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional(readOnly = true)
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @Transactional
    public Appointment scheduleAppointment(User user, Doctor doctor, LocalDateTime appointmentTime) {
        Appointment appointment = new Appointment();
        appointment.setUser(user);
        appointment.setDoctor(doctor);
        appointment.setAppointmentTime(appointmentTime);
        appointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);
        Appointment savedAppointment = appointmentRepository.save(appointment);
        logger.info("Appointment scheduled and saved: id={}, userId={}, doctorId={}, dateTime={}, status={}",
                savedAppointment.getId(), user.getId(), doctor.getId(), appointmentTime, savedAppointment.getStatus());

        Appointment checkAppointment = appointmentRepository.findById(savedAppointment.getId()).orElse(null);
        if (checkAppointment != null) {
            logger.info("Appointment found in database after save: id={}, userId={}, doctorId={}, dateTime={}, status={}",
                    checkAppointment.getId(), checkAppointment.getUser().getId(), checkAppointment.getDoctor().getId(),
                    checkAppointment.getAppointmentTime(), checkAppointment.getStatus());
        } else {
            logger.error("Appointment not found in database immediately after save!");
        }

        return savedAppointment;
    }

    @Transactional(readOnly = true)
    public List<Appointment> getCurrentAndFutureUserAppointments(User user) {
        LocalDateTime currentTime = LocalDateTime.now();
        logger.info("Fetching appointments for user {} after {}", user.getId(), currentTime);

        List<Appointment> allAppointments = appointmentRepository.findAll();
        logger.info("Total appointments in database: {}", allAppointments.size());

        List<Appointment> appointments = appointmentRepository.findCurrentAndFutureAppointmentsByUser(user, currentTime);
        logger.info("Retrieved {} current and future appointments for user {}", appointments.size(), user.getId());

        // Добавьте следующие строки для отладки
        List<Appointment> manualFilteredAppointments = allAppointments.stream()
                .filter(a -> a.getUser().getId().equals(user.getId()))
                .filter(a -> a.getAppointmentTime().isAfter(currentTime) || a.getAppointmentTime().isEqual(currentTime))
                .filter(a -> a.getStatus() != Appointment.AppointmentStatus.CANCELLED)
                .collect(Collectors.toList());
        logger.info("Manually filtered appointments: {}", manualFilteredAppointments.size());

        appointments.forEach(appointment ->
                logger.info("Appointment: id={}, userId={}, doctorId={}, dateTime={}, status={}",
                        appointment.getId(), appointment.getUser().getId(), appointment.getDoctor().getId(),
                        appointment.getAppointmentTime(), appointment.getStatus())
        );

        return appointments;
    }

    @Transactional(readOnly = true)
    public List<Appointment> getAllAppointmentsForUser(User user) {
        List<Appointment> appointments = appointmentRepository.findByUser(user);
        logger.info("All appointments for user {}: {}", user.getId(), appointments.size());
        appointments.forEach(appointment ->
                logger.info("User Appointment: id={}, userId={}, doctorId={}, dateTime={}, status={}",
                        appointment.getId(), appointment.getUser().getId(), appointment.getDoctor().getId(),
                        appointment.getAppointmentTime(), appointment.getStatus())
        );
        return appointments;
    }


    public List<LocalDate> getAvailableDates(Doctor doctor, LocalDate startDate, LocalDate endDate) {
        return startDate.datesUntil(endDate.plusDays(1))
                .filter(date -> !getAvailableTimeSlots(doctor, date).isEmpty())
                .collect(Collectors.toList());
    }

    public Map<LocalTime, Boolean> getAvailableTimeSlots(Doctor doctor, LocalDate selectedDate) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();

        LocalDateTime startOfDay = selectedDate.atStartOfDay();
        LocalDateTime endOfDay = selectedDate.atTime(LocalTime.MAX);

        List<Appointment> existingAppointments = appointmentRepository.findByDoctorAndAppointmentTimeBetweenAndStatusNot(
                doctor, startOfDay, endOfDay, Appointment.AppointmentStatus.CANCELLED);

        Map<LocalTime, Boolean> timeSlots = new TreeMap<>();
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(18, 0);

        while (startTime.isBefore(endTime)) {
            LocalDateTime slotDateTime = LocalDateTime.of(selectedDate, startTime);


            boolean isPastTimeForToday = selectedDate.equals(today) && startTime.isBefore(currentTime);

            if (!isPastTimeForToday) {
                boolean isAvailable = existingAppointments.stream()
                        .noneMatch(appointment -> appointment.getAppointmentTime().equals(slotDateTime));
                timeSlots.put(startTime, isAvailable);
            }

            startTime = startTime.plusHours(1);
        }

        return timeSlots;
    }

    @Transactional(readOnly = true)
    public boolean isSlotAvailable(Doctor doctor, LocalDateTime appointmentDateTime) {
        return appointmentRepository.findByDoctorAndAppointmentTime(doctor, appointmentDateTime).isEmpty();
    }

    @Transactional
    public void cancelAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Запись не найдена"));
        appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
        logger.info("Appointment cancelled: id={}", appointmentId);
    }
}