package com.uzumacademy.myTurn.bot;

import com.uzumacademy.myTurn.config.BotConfig;
import com.uzumacademy.myTurn.model.Appointment;
import com.uzumacademy.myTurn.model.Doctor;
import com.uzumacademy.myTurn.model.User;
import com.uzumacademy.myTurn.service.AppointmentService;
import com.uzumacademy.myTurn.service.DoctorService;
import com.uzumacademy.myTurn.service.UserService;
import com.uzumacademy.myTurn.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class MyTurnBot extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(MyTurnBot.class);

    private final BotConfig config;
    private final UserService userService;
    private final AuthenticationService authService;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;

    public MyTurnBot(BotConfig config, UserService userService, AuthenticationService authService,
                     DoctorService doctorService, AppointmentService appointmentService) {
        this.config = config;
        this.userService = userService;
        this.authService = authService;
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
    }

    @Override
    public String getBotUsername() {
        return config.getUsername();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            long chatId = update.getMessage().getChatId();
            User user = userService.getOrCreateUser(chatId);

            if (update.getMessage().hasText()) {
                String messageText = update.getMessage().getText();
                handleIncomingMessage(user, messageText);
            } else if (update.getMessage().hasContact()) {
                processPhoneNumber(user, update.getMessage().getContact().getPhoneNumber());
            }
        } else if (update.hasCallbackQuery()) {
            String callData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            handleCallbackQuery(chatId, callData);
        }
    }

    private void handleIncomingMessage(User user, String messageText) {
        if ("/start".equals(messageText)) {
            if (user.isRegistrationCompleted()) {
                sendMessage(user.getChatId(), "Вы уже зарегистрированы. Используйте /menu для доступа к функциям бота.");
            } else {
                startRegistration(user);
            }
        } else if ("/menu".equals(messageText)) {
            if (user.isRegistrationCompleted()) {
                sendMainMenu(user.getChatId());
            } else {
                sendMessage(user.getChatId(), "Пожалуйста, сначала завершите регистрацию.");
            }
        } else {
            processUserInput(user, messageText);
        }
    }

    private void handleCallbackQuery(long chatId, String callData) {
        switch (callData) {
            case "SHOW_DOCTORS":
                sendDoctorsList(chatId);
                break;
            case "MY_APPOINTMENTS":
                sendUserAppointments(userService.getUserByChatId(chatId));
                break;
            case "MY_PROFILE":
                sendUserProfile(chatId);
                break;
            default:
                if (callData.startsWith("DOCTOR_")) {
                    Long doctorId = Long.parseLong(callData.split("_")[1]);
                    sendDoctorDetails(chatId, doctorId);
                } else if (callData.startsWith("BOOK_")) {
                    Long doctorId = Long.parseLong(callData.split("_")[1]);
                    handleBookingRequest(userService.getUserByChatId(chatId), doctorId);
                }
                break;
        }
    }

    private void sendUserProfile(long chatId) {
        User user = userService.getUserByChatId(chatId);
        if (user != null) {
            String profileInfo = String.format("Ваш профиль:\nИмя: %s\nФамилия: %s\nТелефон: %s",
                    user.getFirstName(), user.getLastName(), user.getPhoneNumber());
            sendMessage(chatId, profileInfo);
        } else {
            sendMessage(chatId, "Ошибка: пользователь не найден.");
        }
    }

    private void startRegistration(User user) {
        user.setRegistrationState(User.RegistrationState.AWAITING_FIRST_NAME);
        userService.updateUser(user);
        sendMessage(user.getChatId(), "Добро пожаловать в сервис записи к врачу! Пожалуйста, введите ваше имя.");
    }

    private void processUserInput(User user, String messageText) {
        switch (user.getRegistrationState()) {
            case NEW:
            case COMPLETED:
                sendMessage(user.getChatId(), "Используйте команду /menu для доступа к функциям бота.");
                break;
            case AWAITING_FIRST_NAME:
                user.setFirstName(messageText);
                user.setRegistrationState(User.RegistrationState.AWAITING_LAST_NAME);
                userService.updateUser(user);
                sendMessage(user.getChatId(), "Спасибо! Теперь введите вашу фамилию.");
                break;
            case AWAITING_LAST_NAME:
                user.setLastName(messageText);
                user.setRegistrationState(User.RegistrationState.AWAITING_PHONE_NUMBER);
                userService.updateUser(user);
                requestPhoneNumber(user.getChatId());
                break;
            case AWAITING_PHONE_NUMBER:
                try {
                    authService.setPhoneNumber(user, messageText);
                    completeRegistration(user);
                } catch (IllegalArgumentException e) {
                    sendMessage(user.getChatId(), "Неверный формат номера телефона. Пожалуйста, используйте кнопку 'Отправить номер телефона' или введите номер в формате +XXXXXXXXXXX.");
                }
                break;
        }
    }

    private void requestPhoneNumber(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Пожалуйста, предоставьте ваш номер телефона, нажав на кнопку ниже или введите его вручную в формате +XXXXXXXXXXX.");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        KeyboardButton button = new KeyboardButton("Отправить номер телефона");
        button.setRequestContact(true);
        row.add(button);
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setOneTimeKeyboard(true);
        keyboardMarkup.setResizeKeyboard(true);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            logger.error("Failed to send message to chat ID: {}", chatId, e);
        }
    }

    private void processPhoneNumber(User user, String phoneNumber) {
        if (user.getRegistrationState() == User.RegistrationState.AWAITING_PHONE_NUMBER) {
            try {
                authService.setPhoneNumber(user, phoneNumber);
                completeRegistration(user);
            } catch (IllegalArgumentException e) {
                sendMessage(user.getChatId(), "Неверный формат номера телефона. Пожалуйста, попробуйте еще раз.");
                requestPhoneNumber(user.getChatId());
            }
        }
    }

    private void completeRegistration(User user) {
        user.setRegistrationState(User.RegistrationState.COMPLETED);
        userService.updateUser(user);
        sendMessageWithKeyboardRemove(user.getChatId(), "Регистрация завершена!");
        sendMainMenu(user.getChatId());
    }

    private void sendMainMenu(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выберите действие:");

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder().text("Список врачей").callbackData("SHOW_DOCTORS").build());

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(InlineKeyboardButton.builder().text("Мои записи").callbackData("MY_APPOINTMENTS").build());

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(InlineKeyboardButton.builder().text("Мой профиль").callbackData("MY_PROFILE").build());

        rowsInline.add(row1);
        rowsInline.add(row2);
        rowsInline.add(row3);

        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            logger.error("Error sending main menu", e);
        }
    }

    private void sendDoctorsList(long chatId) {
        List<Doctor> doctors = doctorService.getAllDoctors();
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выберите врача:");

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        for (Doctor doctor : doctors) {
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            rowInline.add(InlineKeyboardButton.builder()
                    .text(doctor.getFirstName() + " " + doctor.getLastName() + " (" + doctor.getSpecialization() + ")")
                    .callbackData("DOCTOR_" + doctor.getId())
                    .build());
            rowsInline.add(rowInline);
        }

        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            logger.error("Error sending doctors list", e);
        }
    }

    private void sendDoctorDetails(long chatId, Long doctorId) {
        Doctor doctor = doctorService.getDoctorById(doctorId);
        if (doctor != null) {
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText("Врач: " + doctor.getFirstName() + " " + doctor.getLastName() + "\n" +
                    "Специализация: " + doctor.getSpecialization() + "\n" +
                    "Телефон: " + doctor.getPhoneNumber() + "\n" +
                    "Email: " + doctor.getEmail());

            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            rowInline.add(InlineKeyboardButton.builder().text("Записаться на прием").callbackData("BOOK_" + doctorId).build());
            rowsInline.add(rowInline);

            markupInline.setKeyboard(rowsInline);
            message.setReplyMarkup(markupInline);

            try {
                execute(message);
            } catch (TelegramApiException e) {
                logger.error("Error sending doctor details", e);
            }
        } else {
            sendMessage(chatId, "Врач не найден.");
        }
    }

    private void handleBookingRequest(User user, Long doctorId) {
        Doctor doctor = doctorService.getDoctorById(doctorId);
        if (doctor == null) {
            sendMessage(user.getChatId(), "Врач с указанным ID не найден.");
            return;
        }

        // Здесь должна быть логика выбора времени приема
        LocalDateTime appointmentTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);

        Appointment appointment = appointmentService.scheduleAppointment(user, doctor, appointmentTime);
        sendMessage(user.getChatId(), "Вы успешно записаны к врачу " + doctor.getFirstName() + " " +
                doctor.getLastName() + " на " + appointmentTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
    }

    private void sendUserAppointments(User user) {
        List<Appointment> appointments = appointmentService.getUserAppointments(user);
        if (appointments.isEmpty()) {
            sendMessage(user.getChatId(), "У вас нет запланированных приемов.");
        } else {
            StringBuilder message = new StringBuilder("Ваши записи на прием:\n\n");
            for (Appointment appointment : appointments) {
                message.append("Врач: ").append(appointment.getDoctor().getFirstName()).append(" ")
                        .append(appointment.getDoctor().getLastName()).append("\n")
                        .append("Время: ").append(appointment.getAppointmentTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))).append("\n")
                        .append("Статус: ").append(appointment.getStatus()).append("\n\n");
            }
            sendMessage(user.getChatId(), message.toString());
        }
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        try {
            execute(message);
            logger.debug("Message sent to chat ID: {}", chatId);
        } catch (TelegramApiException e) {
            logger.error("Failed to send message to chat ID: {}", chatId, e);
        }
    }

    private void sendMessageWithKeyboardRemove(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        ReplyKeyboardRemove keyboardRemove = new ReplyKeyboardRemove();
        keyboardRemove.setRemoveKeyboard(true);
        message.setReplyMarkup(keyboardRemove);

        try {
            execute(message);
            logger.debug("Message sent with keyboard remove to chat ID: {}", chatId);
        } catch (TelegramApiException e) {
            logger.error("Failed to send message with keyboard remove to chat ID: {}", chatId, e);
        }
    }
}