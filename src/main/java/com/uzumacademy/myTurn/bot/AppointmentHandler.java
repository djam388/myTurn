package com.uzumacademy.myTurn.bot;

import com.uzumacademy.myTurn.model.User;
import com.uzumacademy.myTurn.model.Doctor;
import com.uzumacademy.myTurn.model.Appointment;
import com.uzumacademy.myTurn.service.UserService;
import com.uzumacademy.myTurn.service.DoctorService;
import com.uzumacademy.myTurn.service.AppointmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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

    public void handleBookingRequest(User user, Long doctorId) {
        Doctor doctor = doctorService.getDoctorById(doctorId);
        if (doctor == null) {
            messageSender.sendMessageWithMenuButton(user.getChatId(), "Врач с указанным ID не найден.");
            return;
        }

        LocalDate currentDate = LocalDate.now();
        List<LocalDate> availableDates = appointmentService.getAvailableDates(doctor, currentDate, currentDate.plusDays(7));
        messageSender.sendAvailableDates(user.getChatId(), doctor, availableDates);
    }

    public void handleDateSelection(User user, Long id, LocalDate selectedDate, boolean isReschedule) {
        if (isReschedule) {
            handleRescheduleDateSelection(user, id, selectedDate);
        } else {
            Doctor doctor = doctorService.getDoctorById(id);
            if (doctor == null) {
                messageSender.sendMessageWithMenuButton(user.getChatId(), "Врач не найден.");
                return;
            }
            Map<LocalTime, Boolean> availableTimeSlots = appointmentService.getAvailableTimeSlots(doctor, selectedDate);
            messageSender.sendAvailableTimeSlots(user.getChatId(), doctor, selectedDate, availableTimeSlots);
        }
    }

    public void handleTimeSelection(User user, Long id, LocalDate selectedDate, LocalTime selectedTime, boolean isReschedule) {
        if (isReschedule) {
            handleRescheduleTimeSelection(user, id, selectedDate, selectedTime);
        } else {
            handleNewAppointmentTimeSelection(user, id, selectedDate, selectedTime);
        }
    }

    private void handleNewAppointmentTimeSelection(User user, Long doctorId, LocalDate selectedDate, LocalTime selectedTime) {
        Doctor doctor = doctorService.getDoctorById(doctorId);
        if (doctor == null) {
            messageSender.sendMessageWithMenuButton(user.getChatId(), "Врач не найден.");
            return;
        }
        LocalDateTime appointmentDateTime = LocalDateTime.of(selectedDate, selectedTime);
        if (appointmentService.isSlotAvailable(doctor, appointmentDateTime)) {
            Appointment appointment = appointmentService.scheduleAppointment(user, doctor, appointmentDateTime);
            messageSender.sendAppointmentConfirmation(user.getChatId(), appointment);
        } else {
            messageSender.sendMessageWithMenuButton(user.getChatId(),
                    "Извините, этот слот уже занят. Пожалуйста, выберите другое время.");
            handleBookingRequest(user, doctor.getId());
        }
    }

    public void handleRescheduleDateSelection(User user, Long appointmentId, LocalDate selectedDate) {
        try {
            Appointment appointment = appointmentService.getAppointmentById(appointmentId);
            if (appointment == null || !appointment.getUser().getId().equals(user.getId())) {
                messageSender.sendMessageWithMenuButton(user.getChatId(), "Запись не найдена или у вас нет прав на ее изменение.");
                return;
            }

            Map<LocalTime, Boolean> availableTimeSlots = appointmentService.getAvailableTimeSlots(appointment.getDoctor(), selectedDate);
            messageSender.sendAvailableTimeSlotsForReschedule(user.getChatId(), appointment, selectedDate, availableTimeSlots);
        } catch (Exception e) {
            logger.error("Error handling reschedule date selection", e);
            messageSender.sendMessageWithMenuButton(user.getChatId(), "Произошла ошибка при выборе даты. Пожалуйста, попробуйте еще раз.");
        }
    }

    private void handleRescheduleTimeSelection(User user, Long appointmentId, LocalDate selectedDate, LocalTime selectedTime) {
        try {
            LocalDateTime newAppointmentTime = LocalDateTime.of(selectedDate, selectedTime);
            Appointment rescheduledAppointment = appointmentService.rescheduleAppointment(appointmentId, newAppointmentTime);
            messageSender.sendRescheduleConfirmation(user.getChatId(), rescheduledAppointment);
        } catch (Exception e) {
            logger.error("Error handling reschedule time selection", e);
            messageSender.sendMessageWithMenuButton(user.getChatId(), "Произошла ошибка при выборе времени. Пожалуйста, попробуйте еще раз.");
        }
    }

    public void sendUserAppointments(User user) {
        logger.info("Fetching appointments for user: {}", user.getId());
        List<Appointment> allAppointments = appointmentService.getAllAppointments();
        logger.info("Total appointments in system: {}", allAppointments.size());
        allAppointments.forEach(appointment ->
                logger.info("All appointments: id={}, userId={}, doctorId={}, dateTime={}, status={}",
                        appointment.getId(), appointment.getUser().getId(), appointment.getDoctor().getId(),
                        appointment.getAppointmentTime(), appointment.getStatus())
        );

        List<Appointment> userAppointments = appointmentService.getCurrentAndFutureUserAppointments(user);
        logger.info("Found {} current and future appointments for user: {}", userAppointments.size(), user.getId());
        messageSender.sendUserAppointments(user.getChatId(), userAppointments);
    }

    public void handleAppointmentCancellation(User user, Long appointmentId) {
        try {
            appointmentService.cancelAppointment(appointmentId);
            messageSender.sendMessage(user.getChatId(), "Запись успешно отменена.");

            List<Appointment> updatedAppointments = appointmentService.getCurrentAndFutureUserAppointments(user);
            if (updatedAppointments.isEmpty()) {
                messageSender.sendMessageWithMenuButton(user.getChatId(), "У вас нет запланированных приемов.");
            } else {
                messageSender.sendUserAppointments(user.getChatId(), updatedAppointments);
            }
        } catch (RuntimeException e) {
            logger.error("Error cancelling appointment", e);
            messageSender.sendMessageWithMenuButton(user.getChatId(), "Ошибка при отмене записи. Пожалуйста, попробуйте позже.");
        }
    }

    public void handleRescheduleConfirmation(User user, Long appointmentId) {
        try {
            Appointment appointment = appointmentService.getAppointmentById(appointmentId);
            if (appointment == null || !appointment.getUser().getId().equals(user.getId())) {
                messageSender.sendMessageWithMenuButton(user.getChatId(), "Запись не найдена или у вас нет прав на ее изменение.");
                return;
            }

            // Здесь можно добавить дополнительную логику подтверждения переноса
            // Например, отправку сообщения с деталями переноса и кнопками подтверждения/отмены

            messageSender.sendRescheduleConfirmationMessage(user.getChatId(), appointment);
        } catch (Exception e) {
            logger.error("Error handling reschedule confirmation", e);
            messageSender.sendMessageWithMenuButton(user.getChatId(), "Произошла ошибка при подтверждении переноса. Пожалуйста, попробуйте еще раз.");
        }
    }

    public void startRescheduleProcess(User user, Long appointmentId) {
        try {
            Appointment appointment = appointmentService.getAppointmentById(appointmentId);
            if (appointment == null || !appointment.getUser().getId().equals(user.getId())) {
                messageSender.sendMessageWithMenuButton(user.getChatId(), "Запись не найдена или у вас нет прав на ее изменение.");
                return;
            }

            if (!appointment.canBeRescheduled()) {
                messageSender.sendMessageWithMenuButton(user.getChatId(), "Эту запись нельзя перенести, так как до приема осталось менее 2 дней.");
                return;
            }

            LocalDate currentDate = LocalDate.now();
            List<LocalDate> availableDates = appointmentService.getAvailableDates(appointment.getDoctor(), currentDate.plusDays(2), currentDate.plusDays(30));
            messageSender.sendAvailableDatesForReschedule(user.getChatId(), appointment, availableDates);
        } catch (Exception e) {
            logger.error("Error starting reschedule process", e);
            messageSender.sendMessageWithMenuButton(user.getChatId(), "Произошла ошибка при попытке переноса записи. Пожалуйста, попробуйте позже.");
        }
    }
}