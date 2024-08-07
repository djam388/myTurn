package com.uzumacademy.myTurn.bot;

import com.uzumacademy.myTurn.model.Appointment;
import com.uzumacademy.myTurn.model.Doctor;
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
        String messageText = "Выберите время приема на " + selectedDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + ":";
        InlineKeyboardMarkup keyboard = keyboardFactory.createTimeSelectionKeyboard(timeSlots, doctor.getId(), selectedDate);

        SendMessage message = createMessage(chatId, messageText);
        message.setReplyMarkup(keyboard);
        executeMessage(message);
    }

    public void sendUserAppointments(long chatId, List<Appointment> appointments) {
        if (appointments.isEmpty()) {
            sendMessageWithMenuButton(chatId, "У вас нет запланированных приемов.");
            return;
        }

        sendMessage(chatId, "Ваши записи на прием:");
        for (Appointment appointment : appointments) {
            sendSingleAppointment(chatId, appointment);
        }
    }

    public void sendSingleAppointment(long chatId, Appointment appointment) {
        StringBuilder messageText = new StringBuilder("Запись на прием:\n\n");
        messageText.append("Врач: ").append(appointment.getDoctor().getFirstName())
                .append(" ").append(appointment.getDoctor().getLastName())
                .append("\nДата и время: ").append(appointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")))
                .append("\nСтатус: ").append(appointment.getStatus());

        InlineKeyboardMarkup keyboard = keyboardFactory.createAppointmentActionsKeyboard(appointment.getId());

        SendMessage message = createMessage(chatId, messageText.toString());
        message.setReplyMarkup(keyboard);
        executeMessage(message);
    }

    public void sendAppointmentConfirmation(long chatId, Appointment appointment) {
        String message = String.format("Запись подтверждена!\n\nВрач: %s %s\nДата и время: %s",
                appointment.getDoctor().getFirstName(),
                appointment.getDoctor().getLastName(),
                appointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
        sendMessageWithMenuButton(chatId, message);
    }

    public void sendAvailableDatesForReschedule(long chatId, Appointment appointment, List<LocalDate> availableDates) {
        String message = "Выберите новую дату для переноса записи:";
        InlineKeyboardMarkup keyboard = keyboardFactory.createDateSelectionKeyboardForReschedule(availableDates, appointment.getId());

        SendMessage sendMessage = createMessage(chatId, message);
        sendMessage.setReplyMarkup(keyboard);
        executeMessage(sendMessage);
    }

    public void sendAvailableTimeSlotsForReschedule(long chatId, Appointment appointment, LocalDate selectedDate, Map<LocalTime, Boolean> timeSlots) {
        String message = "Выберите новое время для переноса записи на " + selectedDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + ":";
        InlineKeyboardMarkup keyboard = keyboardFactory.createTimeSelectionKeyboardForReschedule(timeSlots, appointment.getId(), selectedDate);

        SendMessage sendMessage = createMessage(chatId, message);
        sendMessage.setReplyMarkup(keyboard);
        executeMessage(sendMessage);
    }

    public void sendRescheduleConfirmation(long chatId, Appointment rescheduledAppointment) {
        String message = String.format("Запись успешно перенесена!\n\nНовое время приема: %s\nВрач: %s %s",
                rescheduledAppointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                rescheduledAppointment.getDoctor().getFirstName(),
                rescheduledAppointment.getDoctor().getLastName());

        sendMessageWithMenuButton(chatId, message);
    }

    public void sendRescheduleConfirmationMessage(long chatId, Appointment appointment) {
        String message = String.format("Вы уверены, что хотите перенести запись?\n\nТекущее время приема: %s\nВрач: %s %s",
                appointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
                appointment.getDoctor().getFirstName(),
                appointment.getDoctor().getLastName());

        InlineKeyboardMarkup keyboard = keyboardFactory.createRescheduleConfirmationKeyboard(appointment.getId());

        SendMessage sendMessage = createMessage(chatId, message);
        sendMessage.setReplyMarkup(keyboard);
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
        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            logger.error("Failed to send message", e);
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