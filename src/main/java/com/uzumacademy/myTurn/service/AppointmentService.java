package com.uzumacademy.myTurn.service;

import com.uzumacademy.myTurn.model.Appointment;
import com.uzumacademy.myTurn.model.Doctor;
import com.uzumacademy.myTurn.model.User;
import com.uzumacademy.myTurn.repository.AppointmentRepository;
import javax.persistence.EntityNotFoundException;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentService.class);

    private final AppointmentRepository appointmentRepository;

    @Getter
    @Value("${appointment.initial-booking-days-ahead}")
    private int initialBookingDaysAhead;

    @Getter
    @Value("${appointment.reschedule-min-days-ahead}")
    private int rescheduleMinDaysAhead;

    @Getter
    @Value("${appointment.reschedule-max-days-ahead}")
    private int rescheduleMaxDaysAhead;

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

//        List<Appointment> manualFilteredAppointments = allAppointments.stream()
//                .filter(a -> a.getUser().getId().equals(user.getId()))
//                .filter(a -> a.getAppointmentTime().isAfter(currentTime) || a.getAppointmentTime().isEqual(currentTime))
//                .filter(a -> a.getStatus() != Appointment.AppointmentStatus.CANCELLED)
//                .collect(Collectors.toList());
//        logger.info("Manually filtered appointments: {}", manualFilteredAppointments.size());

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

        logger.info("Found {} non-cancelled appointments for doctor {} on {}",
                existingAppointments.size(), doctor.getId(), selectedDate);
        existingAppointments.forEach(appointment ->
                logger.info("Appointment: id={}, time={}, status={}",
                        appointment.getId(), appointment.getAppointmentTime(), appointment.getStatus()));

        Map<LocalTime, Boolean> timeSlots = new TreeMap<>();
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(18, 0);

        while (startTime.isBefore(endTime)) {
            LocalDateTime slotDateTime = LocalDateTime.of(selectedDate, startTime);
            boolean isPastTimeForToday = selectedDate.equals(today) && startTime.isBefore(currentTime);

            if (!isPastTimeForToday) {
                boolean isAvailable = existingAppointments.stream()
                        .noneMatch(appointment -> appointment.getAppointmentTime().equals(slotDateTime) &&
                                appointment.getStatus() == Appointment.AppointmentStatus.SCHEDULED);
                timeSlots.put(startTime, isAvailable);
                logger.info("Time slot: {}, isAvailable: {}", startTime, isAvailable);
            }

            startTime = startTime.plusHours(1);
        }

        return timeSlots;
    }

    @Transactional
    public void cancelAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Запись не найдена"));
        appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
        logger.info("Appointment cancelled: id={}, status={}", appointmentId, appointment.getStatus());

        Appointment checkAppointment = appointmentRepository.findById(appointmentId).orElse(null);
        if (checkAppointment != null) {
            logger.info("Appointment status after cancellation: id={}, status={}",
                    checkAppointment.getId(), checkAppointment.getStatus());
        } else {
            logger.error("Cancelled appointment not found in database!");
        }
    }

    @Transactional
    public Appointment getReschedulableAppointment(Long appointmentId) {
        LocalDateTime twoDateBeforeAppointment = LocalDateTime.now().plusDays(2);
        return appointmentRepository.findReschedulableAppointment(appointmentId, twoDateBeforeAppointment)
                .orElseThrow(() -> new RuntimeException("Запись не может быть перенесена или не существует"));
    }

    @Transactional
    public Appointment rescheduleAppointment(Long appointmentId, LocalDateTime newAppointmentTime) {
        Appointment appointment = getReschedulableAppointment(appointmentId);

        if (!isSlotAvailable(appointment.getDoctor(), newAppointmentTime)) {
            throw new RuntimeException("Выбранное время уже занято");
        }

        appointment.reschedule(newAppointmentTime);
        return appointmentRepository.save(appointment);
    }

    @Transactional(readOnly = true)
    public Appointment getAppointmentById(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found with id: " + appointmentId));
    }

    public boolean isSlotAvailable(Doctor doctor, LocalDateTime appointmentDateTime) {
        return appointmentRepository.findByDoctorAndAppointmentTimeAndStatusNot(
                doctor, appointmentDateTime, Appointment.AppointmentStatus.CANCELLED).isEmpty();
    }
}