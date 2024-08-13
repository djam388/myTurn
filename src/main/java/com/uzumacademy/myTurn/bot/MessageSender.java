package com.uzumacademy.myTurn.bot;

import com.uzumacademy.myTurn.dto.AppointmentDTO;
import com.uzumacademy.myTurn.dto.DoctorDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
public class MessageSender {
    private static final Logger logger = LoggerFactory.getLogger(MessageSender.class);

    private final MyTurnBot bot;
    private final KeyboardFactory keyboardFactory;

    public MessageSender(MyTurnBot bot, KeyboardFactory keyboardFactory) {
        this.bot = bot;
        this.keyboardFactory = keyboardFactory;
    }

    public void sendMessageWithMenuButton(long chatId, String text) {
        SendMessage message = createMessage(chatId, text);
        message.setReplyMarkup(keyboardFactory.createSingleButtonKeyboard("Меню"));
        executeMessage(message);
    }

    public void sendMainMenu(long chatId) {
        SendMessage message = createMessage(chatId, "Главное меню:");
        message.setReplyMarkup(keyboardFactory.createMainMenuKeyboard());
        executeMessage(message);
    }

    public void sendDoctorsList(long chatId, List<DoctorDTO> doctors) {
        sendDoctorsListWithBackButton(chatId, doctors);
    }

    public void sendDoctorsListWithBackButton(long chatId, List<DoctorDTO> doctors) {
        SendMessage message = createMessage(chatId, "Выберите врача:");
        message.setReplyMarkup(keyboardFactory.createDoctorsListKeyboardWithBack(doctors));
        executeMessage(message);
    }


    public void sendAvailableDatesWithBack(long chatId, DoctorDTO doctor, List<LocalDate> availableDates) {
        String message = String.format("Выберите дату приема к врачу %s %s:", doctor.getFirstName(), doctor.getLastName());
        InlineKeyboardMarkup keyboard = keyboardFactory.createDateSelectionKeyboardWithBack(availableDates, doctor.getId());
        SendMessage sendMessage = createMessage(chatId, message);
        sendMessage.setReplyMarkup(keyboard);
        executeMessage(sendMessage);
    }

    public void sendAvailableTimeSlotsWithBack(long chatId, DoctorDTO doctor, LocalDate selectedDate, Map<LocalTime, Boolean> timeSlots) {
        String messageText = "Выберите время приема на " + selectedDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + ":";
        InlineKeyboardMarkup keyboard = keyboardFactory.createTimeSelectionKeyboardWithBack(timeSlots, doctor.getId(), selectedDate);
        SendMessage message = createMessage(chatId, messageText);
        message.setReplyMarkup(keyboard);
        executeMessage(message);
    }

    public void sendUserAppointments(long chatId, List<AppointmentDTO> appointments) {
        if (appointments.isEmpty()) {
            sendMessageWithBackButton(chatId, "У вас нет запланированных приемов.");
            return;
        }

        for (AppointmentDTO appointment : appointments) {
            sendSingleAppointment(chatId, appointment);
        }

        sendMessageWithBackButton(chatId, "Для возврата в главное меню нажмите кнопку ниже:");
    }

    public void sendSingleAppointment(long chatId, AppointmentDTO appointment) {
        StringBuilder messageText = new StringBuilder("Запись на прием:\n\n");
        messageText.append("Врач: ").append(appointment.getDoctor().getFirstName())
                .append(" ").append(appointment.getDoctor().getLastName())
                .append("\nДата и время: ").append(appointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")))
                .append("\nСтатус: ").append(appointment.getStatus());

        InlineKeyboardMarkup keyboard = keyboardFactory.createAppointmentActionsKeyboard(appointment);

        SendMessage message = createMessage(chatId, messageText.toString());
        message.setReplyMarkup(keyboard);
        executeMessage(message);
    }

    public void sendMessageWithBackButton(long chatId, String text) {
        SendMessage message = createMessage(chatId, text);
        message.setReplyMarkup(keyboardFactory.createBackButton());
        executeMessage(message);
    }

    public void sendAppointmentConfirmation(long chatId, AppointmentDTO appointment) {
        String message = String.format("Запись подтверждена!\n\nВрач: %s %s\nДата и время: %s\n\nЧто бы вы хотели сделать дальше?",
                appointment.getDoctor().getFirstName(),
                appointment.getDoctor().getLastName(),
                appointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));

        SendMessage sendMessage = createMessage(chatId, message);
        sendMessage.setReplyMarkup(keyboardFactory.createMainMenuKeyboard());
        executeMessage(sendMessage);
    }

    public void sendAvailableDatesForReschedule(long chatId, AppointmentDTO appointment, List<LocalDate> availableDates) {
        String message = String.format("Выберите новую дату для переноса записи к врачу %s %s:",
                appointment.getDoctor().getFirstName(), appointment.getDoctor().getLastName());
        InlineKeyboardMarkup keyboard = keyboardFactory.createDateSelectionKeyboardForReschedule(availableDates, appointment.getId());
        SendMessage sendMessage = createMessage(chatId, message);
        sendMessage.setReplyMarkup(keyboard);
        executeMessage(sendMessage);
    }

    public void sendAvailableTimeSlotsForReschedule(long chatId, AppointmentDTO appointment, LocalDate selectedDate, Map<LocalTime, Boolean> timeSlots) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        String message = String.format("Выберите новое время для переноса записи на %s:",
                selectedDate.format(dateFormatter));
        InlineKeyboardMarkup keyboard = keyboardFactory.createTimeSelectionKeyboardForReschedule(timeSlots, appointment.getId(), selectedDate);

        SendMessage sendMessage = createMessage(chatId, message);
        sendMessage.setReplyMarkup(keyboard);
        executeMessage(sendMessage);
    }

    public void sendRescheduleConfirmation(long chatId, AppointmentDTO rescheduledAppointment) {
        String message = String.format("Запись успешно перенесена!\n\nНовое время приема: %s\nВрач: %s %s\n\nЧто бы вы хотели сделать дальше?",
                rescheduledAppointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                rescheduledAppointment.getDoctor().getFirstName(),
                rescheduledAppointment.getDoctor().getLastName());

        SendMessage sendMessage = createMessage(chatId, message);
        sendMessage.setReplyMarkup(keyboardFactory.createMainMenuKeyboard());
        executeMessage(sendMessage);
    }

    public void sendMessage(long chatId, String text) {
        SendMessage message = createMessage(chatId, text);
        executeMessage(message);
    }

    private SendMessage createMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        return message;
    }

    private void executeMessage(SendMessage message) {
        int maxRetries = 3;
        int retryDelay = 5000; // 5 seconds

        for (int i = 0; i < maxRetries; i++) {
            try {
                bot.execute(message);
                return; // If successful, exit the method
            } catch (TelegramApiException e) {
                if (i == maxRetries - 1) {
                    logger.error("Failed to send message after {} attempts", maxRetries, e);
                } else {
                    logger.warn("Failed to send message, retrying in {} ms. Attempt {}/{}", retryDelay, i + 1, maxRetries);
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        logger.error("Thread interrupted while waiting to retry", ie);
                    }
                }
            }
        }
    }

    public void sendMessageWithStartButton(long chatId, String text) {
        SendMessage message = createMessage(chatId, text);
        message.setReplyMarkup(keyboardFactory.createSingleButtonKeyboard("Начать"));
        executeMessage(message);
    }

    public void sendMessageWithoutKeyboard(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setReplyMarkup(new ReplyKeyboardRemove(true));
        executeMessage(message);
    }

    public void requestPhoneNumber(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Пожалуйста, предоставьте ваш номер телефона, нажав на кнопку ниже или введите его вручную в формате +XXXXXXXXXXX.");
        message.setReplyMarkup(keyboardFactory.createPhoneNumberKeyboard());
        executeMessage(message);
    }
}