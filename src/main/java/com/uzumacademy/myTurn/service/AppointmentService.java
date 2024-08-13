package com.uzumacademy.myTurn.service;

import com.uzumacademy.myTurn.dto.AppointmentDTO;
import com.uzumacademy.myTurn.dto.DoctorDTO;
import com.uzumacademy.myTurn.dto.UserDTO;
import com.uzumacademy.myTurn.model.Appointment;
import com.uzumacademy.myTurn.repository.AppointmentRepository;
import javax.persistence.EntityNotFoundException;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
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
    private final UserService userService;
    private final DoctorService doctorService;

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
    public AppointmentService(AppointmentRepository appointmentRepository, UserService userService, DoctorService doctorService) {
        this.appointmentRepository = appointmentRepository;
        this.userService = userService;
        this.doctorService = doctorService;
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> getAllAppointments() {
        return appointmentRepository.findAll().stream()
                .map(AppointmentDTO::fromAppointment)
                .collect(Collectors.toList());
    }

    @Transactional
    public AppointmentDTO scheduleAppointment(UserDTO userDTO, DoctorDTO doctorDTO, LocalDateTime appointmentTime) {
        Appointment appointment = new Appointment();
        appointment.setUser(userDTO.toUser());
        appointment.setDoctor(doctorDTO.toDoctor());
        appointment.setAppointmentTime(appointmentTime);
        appointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);
        Appointment savedAppointment = appointmentRepository.save(appointment);
        logger.info("Appointment scheduled and saved: id={}, userId={}, doctorId={}, dateTime={}, status={}",
                savedAppointment.getId(), userDTO.getId(), doctorDTO.getId(), appointmentTime, savedAppointment.getStatus());

        return AppointmentDTO.fromAppointment(savedAppointment);
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> getCurrentAndFutureUserAppointments(UserDTO userDTO) {
        LocalDateTime currentTime = LocalDateTime.now();
        logger.info("Fetching appointments for user {} after {}", userDTO.getId(), currentTime);

        List<Appointment> appointments = appointmentRepository.findCurrentAndFutureAppointmentsByUser(userDTO.toUser(), currentTime);
        logger.info("Retrieved {} current and future appointments for user {}", appointments.size(), userDTO.getId());

        return appointments.stream()
                .map(AppointmentDTO::fromAppointment)
                .collect(Collectors.toList());
    }

    public List<LocalDate> getAvailableDates(DoctorDTO doctorDTO, LocalDate startDate, LocalDate endDate) {
        return startDate.datesUntil(endDate.plusDays(1))
                .filter(date -> !getAvailableTimeSlots(doctorDTO, date).isEmpty())
                .collect(Collectors.toList());
    }

    public Map<LocalTime, Boolean> getAvailableTimeSlots(DoctorDTO doctorDTO, LocalDate selectedDate) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();

        LocalDateTime startOfDay = selectedDate.atStartOfDay();
        LocalDateTime endOfDay = selectedDate.atTime(LocalTime.MAX);

        List<Appointment> existingAppointments = appointmentRepository.findByDoctorAndAppointmentTimeBetweenAndStatusNot(
                doctorDTO.toDoctor(), startOfDay, endOfDay, Appointment.AppointmentStatus.CANCELLED);

        logger.info("Found {} non-cancelled appointments for doctor {} on {}",
                existingAppointments.size(), doctorDTO.getId(), selectedDate);

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
    }

    @Transactional
    public AppointmentDTO rescheduleAppointment(Long appointmentId, LocalDateTime newAppointmentTime) {
        Appointment appointment = getReschedulableAppointment(appointmentId);

        if (!isSlotAvailable(DoctorDTO.fromDoctor(appointment.getDoctor()), newAppointmentTime)) {
            throw new RuntimeException("Выбранное время уже занято");
        }

        appointment.reschedule(newAppointmentTime);
        Appointment savedAppointment = appointmentRepository.save(appointment);
        return AppointmentDTO.fromAppointment(savedAppointment);
    }

    @Transactional(readOnly = true)
    public AppointmentDTO getAppointmentById(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Appointment not found with id: " + appointmentId));
        return AppointmentDTO.fromAppointment(appointment);
    }

    public boolean isSlotAvailable(DoctorDTO doctorDTO, LocalDateTime appointmentDateTime) {
        return appointmentRepository.findByDoctorAndAppointmentTimeAndStatusNot(
                doctorDTO.toDoctor(), appointmentDateTime, Appointment.AppointmentStatus.CANCELLED).isEmpty();
    }

    public List<AppointmentDTO> getFilteredAppointments(LocalDateTime startDate, LocalDateTime endDate, Long doctorId, String status, String sortBy, String sortDirection) {
        Sort sort = Sort.by(sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);

        List<Appointment> appointments = appointmentRepository.findAll(sort);

//        return appointments.stream()
//                .filter(appointment -> (startDate == null || appointment.getAppointmentTime().isAfter(startDate))
//                        && (endDate == null || appointment.getAppointmentTime().isBefore(endDate))
//                        && (doctorId == null || appointment.getDoctor().getId().equals(doctorId))
//                        && (status == null || appointment.getStatus().toString().equalsIgnoreCase(status)))
//                .map(AppointmentDTO::fromAppointment)
//                .collect(Collectors.toList());

        return appointments.stream()
                .filter(appointment -> (startDate == null || !appointment.getAppointmentTime().isBefore(startDate))
                        && (endDate == null || !appointment.getAppointmentTime().isAfter(endDate))
                        && (doctorId == null || appointment.getDoctor().getId().equals(doctorId))
                        && (status == null || appointment.getStatus().toString().equalsIgnoreCase(status)))
                .map(AppointmentDTO::fromAppointment)
                .collect(Collectors.toList());
    }

    private Appointment getReschedulableAppointment(Long appointmentId) {
        LocalDateTime twoDateBeforeAppointment = LocalDateTime.now().plusDays(2);
        return appointmentRepository.findReschedulableAppointment(appointmentId, twoDateBeforeAppointment)
                .orElseThrow(() -> new RuntimeException("Запись не может быть перенесена или не существует"));
    }
}