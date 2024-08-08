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
                        Long id = Long.parseLong(parts[1]);
                        LocalDate selectedDate = LocalDate.parse(parts[2]);
                        appointmentHandler.handleDateSelection(user, id, selectedDate, false);
                    } else {
                        logger.warn("Invalid DATE callback data: {}", callData);
                        messageSender.sendMessageWithMenuButton(chatId, "Неверные данные для выбора даты.");
                    }
                    break;
                case "TIME":
                    if (parts.length > 3) {
                        LocalDate selectedDate = LocalDate.parse(parts[2], DateTimeFormatter.ISO_LOCAL_DATE);
                        LocalTime selectedTime = LocalTime.parse(parts[3], DateTimeFormatter.ofPattern("HH:mm"));
                        boolean isReschedule = parts[0].equals("RESCHEDULE_TIME");
                        appointmentHandler.handleTimeSelection(user, Long.parseLong(parts[1]), selectedDate, selectedTime, isReschedule);
                    } else {
                        logger.warn("Invalid TIME callback data: {}", callData);
                        messageSender.sendMessageWithMenuButton(chatId, "Неверные данные для выбора времени.");
                    }
                    break;
                case "UNAVAILABLE_TIME":
                    messageSender.sendMessageWithMenuButton(chatId, "Это время уже занято. Пожалуйста, выберите другое время.");
                    break;
                case "UNAVAILABLE":
                    messageSender.sendMessage(chatId, "Это время уже занято. Пожалуйста, выберите другое время.");
                    break;
                case "CANCEL":
                    if (parts.length > 2 && "APPOINTMENT".equals(parts[1])) {
                        Long appointmentId = Long.parseLong(parts[2]);
                        appointmentHandler.handleAppointmentCancellation(user, appointmentId);
                    } else {
                        logger.warn("Invalid CANCEL_APPOINTMENT callback data: {}", callData);
                        messageSender.sendMessageWithMenuButton(chatId, "Неверные данные для отмены записи.");
                    }
                    break;
                case "RESCHEDULE":
                    if (callData.startsWith("RESCHEDULE_APPOINTMENT_")) {
                        String[] rescheduleParts = callData.split("_");
                        if (rescheduleParts.length == 3) {
                            try {
                                Long appointmentId = Long.parseLong(rescheduleParts[2]);
                                appointmentHandler.startRescheduleProcess(user, appointmentId);
                            } catch (NumberFormatException e) {
                                logger.error("Error parsing appointment id for reschedule: {}", callData, e);
                                messageSender.sendMessageWithMenuButton(chatId, "Ошибка при обработке данных для переноса записи. Пожалуйста, попробуйте еще раз.");
                            }
                        } else {
                            logger.warn("Invalid RESCHEDULE_APPOINTMENT callback data: {}", callData);
                            messageSender.sendMessageWithMenuButton(chatId, "Неверные данные для переноса записи.");
                        }
                    } else if (callData.startsWith("RESCHEDULE_DATE_")) {
                        String[] rescheduleParts = callData.split("_");
                        if (rescheduleParts.length == 4) {
                            try {
                                Long appointmentId = Long.parseLong(rescheduleParts[2]);
                                LocalDate newDate = LocalDate.parse(rescheduleParts[3]);
                                appointmentHandler.handleRescheduleDateSelection(user, appointmentId, newDate);
                            } catch (NumberFormatException | DateTimeParseException e) {
                                logger.error("Error parsing reschedule date data: {}", callData, e);
                                messageSender.sendMessageWithMenuButton(chatId, "Ошибка при обработке даты для переноса записи. Пожалуйста, попробуйте еще раз.");
                            } catch (Exception e) {
                                logger.error("Error handling reschedule date selection", e);
                                messageSender.sendMessageWithMenuButton(chatId, "Произошла ошибка при выборе даты для переноса записи. Пожалуйста, попробуйте еще раз.");
                            }
                        } else {
                            logger.warn("Invalid RESCHEDULE_DATE callback data: {}", callData);
                            messageSender.sendMessageWithMenuButton(chatId, "Неверные данные для переноса записи.");
                        }
                    } else if (callData.startsWith("RESCHEDULE_TIME_")) {
                        String[] rescheduleParts = callData.split("_");
                        if (rescheduleParts.length == 5) {
                            try {
                                Long appointmentId = Long.parseLong(rescheduleParts[2]);
                                LocalDate newDate = LocalDate.parse(rescheduleParts[3]);
                                LocalTime newTime = LocalTime.parse(rescheduleParts[4]);
                                appointmentHandler.handleTimeSelection(user, appointmentId, newDate, newTime, true);
                            } catch (NumberFormatException | DateTimeParseException e) {
                                logger.error("Error parsing reschedule time data: {}", callData, e);
                                messageSender.sendMessageWithMenuButton(chatId, "Ошибка при обработке времени для переноса записи. Пожалуйста, попробуйте еще раз.");
                            } catch (Exception e) {
                                logger.error("Error handling reschedule time selection", e);
                                messageSender.sendMessageWithMenuButton(chatId, "Произошла ошибка при выборе времени для переноса записи. Пожалуйста, попробуйте еще раз.");
                            }
                        } else {
                            logger.warn("Invalid RESCHEDULE_TIME callback data: {}", callData);
                            messageSender.sendMessageWithMenuButton(chatId, "Неверные данные для переноса записи.");
                        }
                    } else {
                        logger.warn("Unknown RESCHEDULE action: {}", callData);
                        messageSender.sendMessageWithMenuButton(chatId, "Неизвестная команда для переноса записи.");
                    }
                    break;
                case "BACK":
                    if ("BACK_TO_MAIN_MENU".equals(callData)) {
                        messageSender.sendMainMenu(chatId);
                    } else if ("BACK_TO_DOCTORS".equals(callData)) {
                        sendDoctorsList(chatId);
                    } else if (callData.startsWith("BACK_TO_DATE_")) {
                        String[] backParts = callData.split("_");
                        if (backParts.length >= 4) {
                            try {
                                Long doctorId = Long.parseLong(backParts[3]);
                                Doctor doctor = doctorService.getDoctorById(doctorId);
                                if (doctor != null) {
                                    List<LocalDate> availableDates = appointmentService.getAvailableDates(doctor, LocalDate.now(), LocalDate.now().plusDays(7));
                                    messageSender.sendAvailableDatesWithBack(chatId, doctor, availableDates);
                                } else {
                                    logger.warn("Doctor not found for id: {}", doctorId);
                                    messageSender.sendMessageWithMenuButton(chatId, "Извините, произошла ошибка. Пожалуйста, начните сначала.");
                                }
                            } catch (NumberFormatException e) {
                                logger.error("Error parsing doctor id from callback data: {}", callData, e);
                                messageSender.sendMessageWithMenuButton(chatId, "Произошла ошибка. Пожалуйста, начните сначала.");
                            }
                        } else {
                            logger.warn("Invalid BACK_TO_DATE callback data: {}", callData);
                            messageSender.sendMessageWithMenuButton(chatId, "Неверные данные для возврата к выбору даты.");
                        }
                    } else if (callData.startsWith("BACK_TO_RESCHEDULE_DATE_")) {
                        String[] backParts = callData.split("_");
                        if (backParts.length >= 5) {
                            try {
                                Long appointmentId = Long.parseLong(backParts[4]);
                                appointmentHandler.startRescheduleProcess(user, appointmentId);
                            } catch (NumberFormatException e) {
                                logger.error("Error parsing appointment id from callback data: {}", callData, e);
                                messageSender.sendMessageWithMenuButton(chatId, "Произошла ошибка. Пожалуйста, начните сначала.");
                            }
                        } else {
                            logger.warn("Invalid BACK_TO_RESCHEDULE_DATE callback data: {}", callData);
                            messageSender.sendMessageWithMenuButton(chatId, "Неверные данные для возврата к выбору даты переноса.");
                        }
                    } else if ("BACK_TO_APPOINTMENTS".equals(callData)) {
                        appointmentHandler.sendUserAppointments(user);
                    } else {
                        logger.warn("Unknown BACK action: {}", callData);
                        messageSender.sendMessageWithMenuButton(chatId, "Неизвестная команда возврата.");
                    }
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
        messageSender.sendDoctorsListWithBackButton(chatId, doctors);
    }

    private void sendUserProfile(long chatId) {
        User user = userService.getUserByChatId(chatId);
        if (user != null) {
            String profileInfo = String.format("Ваш профиль:\nИмя: %s\nФамилия: %s\nТелефон: %s",
                    user.getFirstName(), user.getLastName(), user.getPhoneNumber());
            messageSender.sendMessageWithBackButton(chatId, profileInfo);
        } else {
            messageSender.sendMessageWithBackButton(chatId, "Ошибка: пользователь не найден.");
        }
    }

    private void handleAppointmentReschedule(User user, Long appointmentId) {
        appointmentHandler.startRescheduleProcess(user, appointmentId);
    }
}