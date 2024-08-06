package com.uzumacademy.myTurn.bot;

import com.uzumacademy.myTurn.model.User;
import com.uzumacademy.myTurn.model.Doctor;
import com.uzumacademy.myTurn.service.UserService;
import com.uzumacademy.myTurn.service.DoctorService;
import com.uzumacademy.myTurn.service.AppointmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Component
public class MenuHandler {
    private static final Logger logger = LoggerFactory.getLogger(MenuHandler.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final UserService userService;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    private final MessageSender messageSender;
    private final AppointmentHandler appointmentHandler;

    public MenuHandler(UserService userService, DoctorService doctorService,
                       AppointmentService appointmentService, MessageSender messageSender,
                       AppointmentHandler appointmentHandler) {
        this.userService = userService;
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
        this.messageSender = messageSender;
        this.appointmentHandler = appointmentHandler;
    }

    public void handleCallbackQuery(CallbackQuery callbackQuery) {
        String callData = callbackQuery.getData();
        long chatId = callbackQuery.getMessage().getChatId();
        User user = userService.getUserByChatId(chatId);

        logger.info("Received callback query: {}", callData);

        if (user == null) {
            logger.error("User not found for chat ID: {}", chatId);
            messageSender.sendMessageWithMenuButton(chatId, "Ошибка: пользователь не найден.");
            return;
        }

        String[] parts = callData.split("_");
        String action = parts[0];

        logger.info("Parsed action: {}", action);

        try {
            switch (action) {
                case "SHOW":
                    if ("SHOW_DOCTORS".equals(callData)) {
                        sendDoctorsList(chatId);
                    } else {
                        logger.warn("Unknown SHOW action: {}", callData);
                        messageSender.sendMessageWithMenuButton(chatId, "Неизвестная команда показа.");
                    }
                    break;
                case "MY":
                    if ("MY_APPOINTMENTS".equals(callData)) {
                        logger.info("Handling MY_APPOINTMENTS for user: {}", user.getId());
                        appointmentHandler.sendUserAppointments(user);
                    } else if ("MY_PROFILE".equals(callData)) {
                        sendUserProfile(chatId);
                    } else {
                        logger.warn("Unknown MY action: {}", callData);
                        messageSender.sendMessageWithMenuButton(chatId, "Неизвестная команда профиля.");
                    }
                    break;
                case "DOCTOR":
                    if (parts.length > 1) {
                        appointmentHandler.handleBookingRequest(user, Long.parseLong(parts[1]));
                    } else {
                        logger.warn("Invalid DOCTOR callback data: {}", callData);
                        messageSender.sendMessageWithMenuButton(chatId, "Неверные данные для выбора врача.");
                    }
                    break;
                case "DATE":
                    if (parts.length > 2) {
                        LocalDate selectedDate = LocalDate.parse(parts[2], DATE_FORMATTER);
                        appointmentHandler.handleDateSelection(user, Long.parseLong(parts[1]), selectedDate);
                    } else {
                        logger.warn("Invalid DATE callback data: {}", callData);
                        messageSender.sendMessageWithMenuButton(chatId, "Неверные данные для выбора даты.");
                    }
                    break;

                case "TIME":
                    if (parts.length > 3) {
                        LocalDate selectedDate = LocalDate.parse(parts[2], DateTimeFormatter.ISO_LOCAL_DATE);
                        LocalTime selectedTime = LocalTime.parse(parts[3], DateTimeFormatter.ofPattern("HH:mm"));
                        appointmentHandler.handleTimeSelection(user, Long.parseLong(parts[1]), selectedDate, selectedTime);
                    } else {
                        logger.warn("Invalid TIME callback data: {}", callData);
                        messageSender.sendMessageWithMenuButton(chatId, "Неверные данные для выбора времени.");
                    }
                    break;
                case "UNAVAILABLE_TIME":
                    messageSender.sendMessageWithMenuButton(chatId, "Это время уже занято. Пожалуйста, выберите другое время.");
                    break;
                case "UNAVAILABLE":
                    if ("UNAVAILABLE_TIME".equals(callData)) {
                        messageSender.sendMessage(chatId, "Это время уже занято. Пожалуйста, выберите другое время.");
                    } else {
                        logger.warn("Unknown UNAVAILABLE action: {}", callData);
                        messageSender.sendMessageWithMenuButton(chatId, "Неизвестная команда.");
                    }
                    break;

                default:
                    logger.warn("Unknown callback query: {}", callData);
                    messageSender.sendMessageWithMenuButton(chatId, "Неизвестная команда.");
                    break;
            }
        } catch (DateTimeParseException e) {
            logger.error("Error parsing date or time: {}", e.getMessage());
            messageSender.sendMessageWithMenuButton(chatId, "Ошибка при обработке даты или времени. Пожалуйста, попробуйте еще раз.");
        } catch (Exception e) {
            logger.error("Error processing callback query", e);
            messageSender.sendMessageWithMenuButton(chatId, "Произошла ошибка при обработке запроса. Пожалуйста, попробуйте еще раз.");
        }
    }

    private void sendDoctorsList(long chatId) {
        List<Doctor> doctors = doctorService.getAllDoctors();
        messageSender.sendDoctorsList(chatId, doctors);
    }

    private void sendUserProfile(long chatId) {
        User user = userService.getUserByChatId(chatId);
        if (user != null) {
            String profileInfo = String.format("Ваш профиль:\nИмя: %s\nФамилия: %s\nТелефон: %s",
                    user.getFirstName(), user.getLastName(), user.getPhoneNumber());
            messageSender.sendMessageWithMenuButton(chatId, profileInfo);
        } else {
            messageSender.sendMessageWithMenuButton(chatId, "Ошибка: пользователь не найден.");
        }
    }
}