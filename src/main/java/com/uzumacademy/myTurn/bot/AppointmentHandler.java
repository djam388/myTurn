package com.uzumacademy.myTurn.bot;

import com.uzumacademy.myTurn.dto.UserDTO;
import com.uzumacademy.myTurn.dto.DoctorDTO;
import com.uzumacademy.myTurn.dto.AppointmentDTO;
import com.uzumacademy.myTurn.service.UserService;
import com.uzumacademy.myTurn.service.DoctorService;
import com.uzumacademy.myTurn.service.AppointmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Component
public class AppointmentHandler {
    private static final Logger logger = LoggerFactory.getLogger(AppointmentHandler.class);

    private final UserService userService;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    private final MessageSender messageSender;

    public AppointmentHandler(UserService userService, DoctorService doctorService,
                              AppointmentService appointmentService, MessageSender messageSender) {
        this.userService = userService;
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
        this.messageSender = messageSender;
    }

    public void handleBookingRequest(UserDTO userDTO, Long doctorId) {
        DoctorDTO doctorDTO = doctorService.getDoctorById(doctorId);
        if (doctorDTO == null) {
            messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Врач с указанным ID не найден.");
            return;
        }

        LocalDate currentDate = LocalDate.now();
        List<LocalDate> availableDates = appointmentService.getAvailableDates(doctorDTO, currentDate, currentDate.plusDays(appointmentService.getInitialBookingDaysAhead()));

        messageSender.sendAvailableDatesWithBack(userDTO.getChatId(), doctorDTO, availableDates);
    }

    public void handleDateSelection(UserDTO userDTO, Long id, LocalDate selectedDate, boolean isReschedule) {
        if (isReschedule) {
            handleRescheduleDateSelection(userDTO, id, selectedDate);
        } else {
            DoctorDTO doctorDTO = doctorService.getDoctorById(id);
            if (doctorDTO == null) {
                messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Врач не найден.");
                return;
            }
            Map<LocalTime, Boolean> availableTimeSlots = appointmentService.getAvailableTimeSlots(doctorDTO, selectedDate);
            messageSender.sendAvailableTimeSlotsWithBack(userDTO.getChatId(), doctorDTO, selectedDate, availableTimeSlots);
        }
    }

    public void handleTimeSelection(UserDTO userDTO, Long id, LocalDate selectedDate, LocalTime selectedTime, boolean isReschedule) {
        if (isReschedule) {
            handleRescheduleTimeSelection(userDTO, id, selectedDate, selectedTime);
        } else {
            handleNewAppointmentTimeSelection(userDTO, id, selectedDate, selectedTime);
        }
    }

    private void handleNewAppointmentTimeSelection(UserDTO userDTO, Long doctorId, LocalDate selectedDate, LocalTime selectedTime) {
        DoctorDTO doctorDTO = doctorService.getDoctorById(doctorId);
        if (doctorDTO == null) {
            messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Врач не найден.");
            return;
        }
        LocalDateTime appointmentDateTime = LocalDateTime.of(selectedDate, selectedTime);
        if (appointmentService.isSlotAvailable(doctorDTO, appointmentDateTime)) {
            AppointmentDTO appointment = appointmentService.scheduleAppointment(userDTO, doctorDTO, appointmentDateTime);
            messageSender.sendAppointmentConfirmation(userDTO.getChatId(), appointment);
        } else {
            messageSender.sendMessageWithMenuButton(userDTO.getChatId(),
                    "Извините, этот слот уже занят. Пожалуйста, выберите другое время.");
            handleBookingRequest(userDTO, doctorId);
        }
    }

    public void handleRescheduleDateSelection(UserDTO userDTO, Long appointmentId, LocalDate selectedDate) {
        try {
            AppointmentDTO appointment = appointmentService.getAppointmentById(appointmentId);
            if (appointment == null || !appointment.getUserId().equals(userDTO.getId())) {
                messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Запись не найдена или у вас нет прав на ее изменение.");
                return;
            }

            DoctorDTO doctorDTO = doctorService.getDoctorById(appointment.getDoctor().getId());
            Map<LocalTime, Boolean> availableTimeSlots = appointmentService.getAvailableTimeSlots(doctorDTO, selectedDate);
            messageSender.sendAvailableTimeSlotsForReschedule(userDTO.getChatId(), appointment, selectedDate, availableTimeSlots);
        } catch (Exception e) {
            logger.error("Error handling reschedule date selection", e);
            messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Произошла ошибка при выборе даты. Пожалуйста, попробуйте еще раз.");
        }
    }

    private void handleRescheduleTimeSelection(UserDTO userDTO, Long appointmentId, LocalDate selectedDate, LocalTime selectedTime) {
        try {
            LocalDateTime newAppointmentTime = LocalDateTime.of(selectedDate, selectedTime);
            AppointmentDTO rescheduledAppointment = appointmentService.rescheduleAppointment(appointmentId, newAppointmentTime);
            messageSender.sendRescheduleConfirmation(userDTO.getChatId(), rescheduledAppointment);
        } catch (Exception e) {
            logger.error("Error handling reschedule time selection", e);
            messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Произошла ошибка при выборе времени. Пожалуйста, попробуйте еще раз.");
        }
    }

    public void sendUserAppointments(UserDTO userDTO) {
        logger.info("Fetching appointments for user: {}", userDTO.getId());
        List<AppointmentDTO> userAppointments = appointmentService.getCurrentAndFutureUserAppointments(userDTO);
        logger.info("Found {} current and future appointments for user: {}", userAppointments.size(), userDTO.getId());
        messageSender.sendUserAppointments(userDTO.getChatId(), userAppointments);
    }

    public void handleAppointmentCancellation(UserDTO userDTO, Long appointmentId) {
        try {
            appointmentService.cancelAppointment(appointmentId);
            messageSender.sendMessage(userDTO.getChatId(), "Запись успешно отменена.");

            List<AppointmentDTO> updatedAppointments = appointmentService.getCurrentAndFutureUserAppointments(userDTO);
            if (updatedAppointments.isEmpty()) {
                messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "У вас нет запланированных приемов.");
            } else {
                messageSender.sendUserAppointments(userDTO.getChatId(), updatedAppointments);
            }
        } catch (RuntimeException e) {
            logger.error("Error cancelling appointment", e);
            messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Ошибка при отмене записи. Пожалуйста, попробуйте позже.");
        }
    }

    public void handleRescheduleConfirmation(UserDTO userDTO, Long appointmentId) {
        try {
            AppointmentDTO appointment = appointmentService.getAppointmentById(appointmentId);
            if (appointment == null || !appointment.getUserId().equals(userDTO.getId())) {
                messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Запись не найдена или у вас нет прав на ее изменение.");
                return;
            }

            messageSender.sendRescheduleConfirmationMessage(userDTO.getChatId(), appointment);
        } catch (Exception e) {
            logger.error("Error handling reschedule confirmation", e);
            messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Произошла ошибка при подтверждении переноса. Пожалуйста, попробуйте еще раз.");
        }
    }

    public void startRescheduleProcess(UserDTO userDTO, Long appointmentId) {
        try {
            AppointmentDTO appointment = appointmentService.getAppointmentById(appointmentId);
            if (appointment == null || !appointment.getUserId().equals(userDTO.getId())) {
                messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Запись не найдена или у вас нет прав на ее изменение.");
                return;
            }

            if (appointment.getStatus().equals("CANCELLED")) {
                messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Невозможно перенести отмененную запись. Пожалуйста, создайте новую запись.");
                return;
            }

            LocalDateTime appointmentDateTime = appointment.getAppointmentTime();
            if (appointmentDateTime.minusDays(2).isBefore(LocalDateTime.now())) {
                messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Эту запись нельзя перенести, так как до приема осталось менее 2 дней.");
                return;
            }

            LocalDate currentDate = LocalDate.now();
            DoctorDTO doctorDTO = doctorService.getDoctorById(appointment.getDoctor().getId());
            List<LocalDate> availableDates = appointmentService.getAvailableDates(
                    doctorDTO,
                    currentDate.plusDays(appointmentService.getRescheduleMinDaysAhead()),
                    currentDate.plusDays(appointmentService.getRescheduleMaxDaysAhead())
            );
            messageSender.sendAvailableDatesForReschedule(userDTO.getChatId(), appointment, availableDates);
        } catch (Exception e) {
            logger.error("Error starting reschedule process", e);
            messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Произошла ошибка при попытке переноса записи. Пожалуйста, попробуйте позже.");
        }
    }
}