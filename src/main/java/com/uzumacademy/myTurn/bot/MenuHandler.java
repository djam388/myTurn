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
        UserDTO userDTO = userService.getUserByChatId(chatId);

        logger.info("Received callback query: {}", callData);

        if (userDTO == null) {
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
                    handleShowAction(callData, userDTO);
                    break;
                case "MY":
                    handleMyAction(callData, userDTO);
                    break;
                case "DOCTOR":
                    handleDoctorAction(parts, userDTO);
                    break;
                case "DATE":
                    handleDateAction(parts, userDTO);
                    break;
                case "TIME":
                    handleTimeAction(parts, userDTO);
                    break;
                case "UNAVAILABLE_TIME":
                case "UNAVAILABLE":
                    messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Это время уже занято. Пожалуйста, выберите другое время.");
                    break;
                case "CANCEL":
                    handleCancelAction(parts, userDTO);
                    break;
                case "RESCHEDULE":
                    handleRescheduleAction(callData, parts, userDTO);
                    break;
                case "BACK":
                    handleBackAction(callData, parts, userDTO);
                    break;
                default:
                    logger.warn("Unknown action: {}", action);
                    messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Неизвестная команда.");
            }
        } catch (DateTimeParseException e) {
            logger.error("Error parsing date or time: {}", e.getMessage());
            messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Ошибка при обработке даты или времени. Пожалуйста, попробуйте еще раз.");
        } catch (Exception e) {
            logger.error("Error processing callback query", e);
            messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Произошла ошибка при обработке запроса. Пожалуйста, попробуйте еще раз.");
        }
    }

    private void handleShowAction(String callData, UserDTO userDTO) {
        if ("SHOW_DOCTORS".equals(callData)) {
            sendDoctorsList(userDTO.getChatId());
        } else {
            logger.warn("Unknown SHOW action: {}", callData);
            messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Неизвестная команда показа.");
        }
    }

    private void handleMyAction(String callData, UserDTO userDTO) {
        if ("MY_APPOINTMENTS".equals(callData)) {
            logger.info("Handling MY_APPOINTMENTS for user: {}", userDTO.getId());
            appointmentHandler.sendUserAppointments(userDTO);
        } else if ("MY_PROFILE".equals(callData)) {
            sendUserProfile(userDTO);
        } else {
            logger.warn("Unknown MY action: {}", callData);
            messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Неизвестная команда профиля.");
        }
    }

    private void handleDoctorAction(String[] parts, UserDTO userDTO) {
        if (parts.length > 1) {
            appointmentHandler.handleBookingRequest(userDTO, Long.parseLong(parts[1]));
        } else {
            logger.warn("Invalid DOCTOR callback data: {}", String.join("_", parts));
            messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Неверные данные для выбора врача.");
        }
    }

    private void handleDateAction(String[] parts, UserDTO userDTO) {
        if (parts.length > 2) {
            Long id = Long.parseLong(parts[1]);
            LocalDate selectedDate = LocalDate.parse(parts[2]);
            appointmentHandler.handleDateSelection(userDTO, id, selectedDate, false);
        } else {
            logger.warn("Invalid DATE callback data: {}", String.join("_", parts));
            messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Неверные данные для выбора даты.");
        }
    }

    private void handleTimeAction(String[] parts, UserDTO userDTO) {
        if (parts.length > 3) {
            LocalDate selectedDate = LocalDate.parse(parts[2], DateTimeFormatter.ISO_LOCAL_DATE);
            LocalTime selectedTime = LocalTime.parse(parts[3], DateTimeFormatter.ofPattern("HH:mm"));
            boolean isReschedule = parts[0].equals("RESCHEDULE_TIME");
            appointmentHandler.handleTimeSelection(userDTO, Long.parseLong(parts[1]), selectedDate, selectedTime, isReschedule);
        } else {
            logger.warn("Invalid TIME callback data: {}", String.join("_", parts));
            messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Неверные данные для выбора времени.");
        }
    }

    private void handleCancelAction(String[] parts, UserDTO userDTO) {
        if (parts.length > 2 && "APPOINTMENT".equals(parts[1])) {
            Long appointmentId = Long.parseLong(parts[2]);
            appointmentHandler.handleAppointmentCancellation(userDTO, appointmentId);
        } else {
            logger.warn("Invalid CANCEL_APPOINTMENT callback data: {}", String.join("_", parts));
            messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Неверные данные для отмены записи.");
        }
    }

    private void handleRescheduleAction(String callData, String[] parts, UserDTO userDTO) {
        if (callData.startsWith("RESCHEDULE_APPOINTMENT_")) {
            handleRescheduleAppointment(parts, userDTO);
        } else if (callData.startsWith("RESCHEDULE_DATE_")) {
            handleRescheduleDate(parts, userDTO);
        } else if (callData.startsWith("RESCHEDULE_TIME_")) {
            handleRescheduleTime(parts, userDTO);
        } else {
            logger.warn("Unknown RESCHEDULE action: {}", callData);
            messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Неизвестная команда для переноса записи.");
        }
    }

    private void handleRescheduleAppointment(String[] parts, UserDTO userDTO) {
        if (parts.length == 3) {
            try {
                Long appointmentId = Long.parseLong(parts[2]);
                appointmentHandler.startRescheduleProcess(userDTO, appointmentId);
            } catch (NumberFormatException e) {
                logger.error("Error parsing appointment id for reschedule: {}", String.join("_", parts), e);
                messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Ошибка при обработке данных для переноса записи. Пожалуйста, попробуйте еще раз.");
            }
        } else {
            logger.warn("Invalid RESCHEDULE_APPOINTMENT callback data: {}", String.join("_", parts));
            messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Неверные данные для переноса записи.");
        }
    }

    private void handleRescheduleDate(String[] parts, UserDTO userDTO) {
        if (parts.length == 4) {
            try {
                Long appointmentId = Long.parseLong(parts[2]);
                LocalDate newDate = LocalDate.parse(parts[3]);
                appointmentHandler.handleRescheduleDateSelection(userDTO, appointmentId, newDate);
            } catch (NumberFormatException | DateTimeParseException e) {
                logger.error("Error parsing reschedule date data: {}", String.join("_", parts), e);
                messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Ошибка при обработке даты для переноса записи. Пожалуйста, попробуйте еще раз.");
            }
        } else {
            logger.warn("Invalid RESCHEDULE_DATE callback data: {}", String.join("_", parts));
            messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Неверные данные для переноса записи.");
        }
    }

    private void handleRescheduleTime(String[] parts, UserDTO userDTO) {
        if (parts.length == 5) {
            try {
                Long appointmentId = Long.parseLong(parts[2]);
                LocalDate newDate = LocalDate.parse(parts[3]);
                LocalTime newTime = LocalTime.parse(parts[4]);
                appointmentHandler.handleTimeSelection(userDTO, appointmentId, newDate, newTime, true);
            } catch (NumberFormatException | DateTimeParseException e) {
                logger.error("Error parsing reschedule time data: {}", String.join("_", parts), e);
                messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Ошибка при обработке времени для переноса записи. Пожалуйста, попробуйте еще раз.");
            }
        } else {
            logger.warn("Invalid RESCHEDULE_TIME callback data: {}", String.join("_", parts));
            messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Неверные данные для переноса записи.");
        }
    }

    private void handleBackAction(String callData, String[] parts, UserDTO userDTO) {
        if ("BACK_TO_MAIN_MENU".equals(callData)) {
            messageSender.sendMainMenu(userDTO.getChatId());
        } else if ("BACK_TO_DOCTORS".equals(callData)) {
            sendDoctorsList(userDTO.getChatId());
        } else if (callData.startsWith("BACK_TO_DATE_")) {
            handleBackToDate(parts, userDTO);
        } else if (callData.startsWith("BACK_TO_RESCHEDULE_DATE_")) {
            handleBackToRescheduleDate(parts, userDTO);
        } else if ("BACK_TO_APPOINTMENTS".equals(callData)) {
            appointmentHandler.sendUserAppointments(userDTO);
        } else {
            logger.warn("Unknown BACK action: {}", callData);
            messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Неизвестная команда возврата.");
        }
    }

    private void handleBackToDate(String[] parts, UserDTO userDTO) {
        if (parts.length >= 4) {
            try {
                Long doctorId = Long.parseLong(parts[3]);
                DoctorDTO doctorDTO = doctorService.getDoctorById(doctorId);
                if (doctorDTO != null) {
                    List<LocalDate> availableDates = appointmentService.getAvailableDates(doctorDTO, LocalDate.now(), LocalDate.now().plusDays(7));
                    messageSender.sendAvailableDatesWithBack(userDTO.getChatId(), doctorDTO, availableDates);
                } else {
                    logger.warn("Doctor not found for id: {}", doctorId);
                    messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Извините, произошла ошибка. Пожалуйста, начните сначала.");
                }
            } catch (NumberFormatException e) {
                logger.error("Error parsing doctor id from callback data: {}", String.join("_", parts), e);
                messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Произошла ошибка. Пожалуйста, начните сначала.");
            }
        } else {
            logger.warn("Invalid BACK_TO_DATE callback data: {}", String.join("_", parts));
            messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Неверные данные для возврата к выбору даты.");
        }
    }

    private void handleBackToRescheduleDate(String[] parts, UserDTO userDTO) {
        if (parts.length >= 5) {
            try {
                Long appointmentId = Long.parseLong(parts[4]);
                appointmentHandler.startRescheduleProcess(userDTO, appointmentId);
            } catch (NumberFormatException e) {
                logger.error("Error parsing appointment id from callback data: {}", String.join("_", parts), e);
                messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Произошла ошибка. Пожалуйста, начните сначала.");
            }
        } else {
            logger.warn("Invalid BACK_TO_RESCHEDULE_DATE callback data: {}", String.join("_", parts));
            messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Неверные данные для возврата к выбору даты переноса.");
        }
    }

    private void sendDoctorsList(long chatId) {
        List<DoctorDTO> doctors = doctorService.getAllDoctors();
        messageSender.sendDoctorsListWithBackButton(chatId, doctors);
    }

    private void sendUserProfile(UserDTO userDTO) {
        String profileInfo = String.format("Ваш профиль:\nИмя: %s\nФамилия: %s\nТелефон: %s",
                userDTO.getFirstName(), userDTO.getLastName(), userDTO.getPhoneNumber());
        messageSender.sendMessageWithBackButton(userDTO.getChatId(), profileInfo);
    }
}