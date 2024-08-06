package com.uzumacademy.myTurn.bot;

import com.uzumacademy.myTurn.model.Doctor;
import com.uzumacademy.myTurn.model.Appointment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
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

    public void sendMessageWithStartButton(long chatId, String text) {
        SendMessage message = createMessage(chatId, text);
        message.setReplyMarkup(keyboardFactory.createSingleButtonKeyboard("Начать"));
        executeMessage(message);
    }

    public void sendMessageWithMenuButton(long chatId, String text) {
        SendMessage message = createMessage(chatId, text);
        message.setReplyMarkup(keyboardFactory.createSingleButtonKeyboard("Меню"));
        executeMessage(message);
    }

    public void sendMessageWithoutKeyboard(long chatId, String text) {
        SendMessage message = createMessage(chatId, text);
        ReplyKeyboardRemove keyboardRemove = new ReplyKeyboardRemove();
        keyboardRemove.setRemoveKeyboard(true);
        message.setReplyMarkup(keyboardRemove);
        executeMessage(message);
    }

    public void sendMainMenu(long chatId) {
        SendMessage message = createMessage(chatId, "Выберите действие:");
        message.setReplyMarkup(keyboardFactory.createMainMenuKeyboard());
        executeMessage(message);
    }

    public void sendDoctorsList(long chatId, List<Doctor> doctors) {
        SendMessage message = createMessage(chatId, "Выберите врача:");
        message.setReplyMarkup(keyboardFactory.createDoctorsListKeyboard(doctors));
        executeMessage(message);
    }

    public void sendAvailableDates(long chatId, Doctor doctor, List<LocalDate> availableDates) {
        SendMessage message = createMessage(chatId, "Выберите дату приема:");
        message.setReplyMarkup(keyboardFactory.createDateSelectionKeyboard(availableDates, doctor.getId()));
        executeMessage(message);
    }

    public void sendAvailableTimeSlots(long chatId, Doctor doctor, LocalDate selectedDate, Map<LocalTime, Boolean> timeSlots) {
        StringBuilder message = new StringBuilder("Выберите время приема:\n");
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        for (Map.Entry<LocalTime, Boolean> entry : timeSlots.entrySet()) {
            LocalTime time = entry.getKey();
            boolean isAvailable = entry.getValue();

            String buttonText = time.format(DateTimeFormatter.ofPattern("HH:mm"));
            if (!isAvailable) {
                buttonText += " (занято)";
            }

            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(buttonText);

            if (isAvailable) {
                button.setCallbackData("TIME_" + doctor.getId() + "_" + selectedDate + "_" + time);
            } else {
                button.setCallbackData("UNAVAILABLE_TIME");
            }

            rowsInline.add(Collections.singletonList(button));
        }

        markupInline.setKeyboard(rowsInline);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(message.toString());
        sendMessage.setReplyMarkup(markupInline);

        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            logger.error("Failed to send available time slots message", e);
        }
    }

    public void sendUserAppointments(long chatId, List<Appointment> appointments) {
        if (appointments.isEmpty()) {
            sendMessageWithMenuButton(chatId, "У вас нет запланированных приемов.");
        } else {
            StringBuilder messageText = new StringBuilder("Ваши записи на прием:\n\n");
            for (Appointment appointment : appointments) {
                messageText.append("Врач: ").append(appointment.getDoctor().getFirstName())
                        .append(" ").append(appointment.getDoctor().getLastName())
                        .append("\nДата и время: ").append(appointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")))
                        .append("\nСтатус: ").append(appointment.getStatus())
                        .append("\n\n");
            }
            sendMessageWithMenuButton(chatId, messageText.toString());
        }
    }

    public void sendAppointmentConfirmation(long chatId, Appointment appointment) {
        String message = String.format("Запись подтверждена!\n\nВрач: %s %s\nДата и время: %s",
                appointment.getDoctor().getFirstName(),
                appointment.getDoctor().getLastName(),
                appointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
        sendMessageWithMenuButton(chatId, message);
    }

    public void requestPhoneNumber(Long chatId) {
        SendMessage message = createMessage(chatId, "Пожалуйста, предоставьте ваш номер телефона, нажав на кнопку ниже или введите его вручную в формате +XXXXXXXXXXX.");
        message.setReplyMarkup(keyboardFactory.createPhoneNumberKeyboard());
        executeMessage(message);
    }

    private SendMessage createMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        return message;
    }

    public void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            logger.error("Failed to send message", e);
        }
    }

    private void executeMessage(SendMessage message) {
        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            logger.error("Failed to send message", e);
        }
    }
}