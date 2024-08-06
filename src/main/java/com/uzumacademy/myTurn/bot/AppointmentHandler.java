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

    public void handleDateSelection(User user, Long doctorId, LocalDate selectedDate) {
        Doctor doctor = doctorService.getDoctorById(doctorId);
        if (doctor == null) {
            messageSender.sendMessageWithMenuButton(user.getChatId(), "Врач не найден.");
            return;
        }
        Map<LocalTime, Boolean> availableTimeSlots = appointmentService.getAvailableTimeSlots(doctor, selectedDate);
        messageSender.sendAvailableTimeSlots(user.getChatId(), doctor, selectedDate, availableTimeSlots);
    }

    public void handleTimeSelection(User user, Long doctorId, LocalDate selectedDate, LocalTime selectedTime) {
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

    public void sendUserAppointments(User user) {
        logger.info("Fetching appointments for user: {}", user.getId());
        List<Appointment> allAppointments = appointmentService.getAllAppointments();
        logger.info("Total appointments in system: {}", allAppointments.size());
        allAppointments.forEach(appointment ->
                logger.info("All appointments: id={}, userId={}, doctorId={}, dateTime={}, status={}",
                        appointment.getId(), appointment.getUser().getId(), appointment.getDoctor().getId(),
                        appointment.getAppointmentTime(), appointment.getStatus())
        );

        List<Appointment> allUserAppointments = appointmentService.getAllAppointmentsForUser(user);
        List<Appointment> userAppointments = appointmentService.getCurrentAndFutureUserAppointments(user);
        logger.info("Found {} current and future appointments out of {} total appointments for user: {}",
                userAppointments.size(), allUserAppointments.size(), user.getId());
        messageSender.sendUserAppointments(user.getChatId(), userAppointments);
    }

    public void handleAppointmentCancellation(User user, Long appointmentId) {
        try {
            appointmentService.cancelAppointment(appointmentId);
            messageSender.sendMessage(user.getChatId(), "Запись успешно отменена.");

            // Получаем обновленный список записей и отправляем его пользователю
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
}