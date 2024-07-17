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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
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
                logger.info("Received message from chat ID: {}", chatId);

                if ("/start".equals(messageText)) {
                    startRegistration(user);
                } else if ("/doctors".equals(messageText)) {
                    if (user.getRegistrationState() == User.RegistrationState.COMPLETED) {
                        sendDoctorsList(chatId);
                    } else {
                        sendMessage(chatId, "Пожалуйста, завершите регистрацию для просмотра списка врачей.");
                    }
                } else if (messageText.startsWith("/book")) {
                    if (user.getRegistrationState() == User.RegistrationState.COMPLETED) {
                        handleBookingRequest(user, messageText);
                    } else {
                        sendMessage(chatId, "Пожалуйста, завершите регистрацию для записи к врачу.");
                    }
                } else if ("/myappointments".equals(messageText)) {
                    if (user.getRegistrationState() == User.RegistrationState.COMPLETED) {
                        sendUserAppointments(user);
                    } else {
                        sendMessage(chatId, "Пожалуйста, завершите регистрацию для просмотра ваших записей.");
                    }
                } else {
                    processUserInput(user, messageText);
                }
            } else if (update.getMessage().hasContact()) {
                processPhoneNumber(user, update.getMessage().getContact().getPhoneNumber());
            }
        }
    }

    private void startRegistration(User user) {
        user.setRegistrationState(User.RegistrationState.AWAITING_FIRST_NAME);
        userService.updateUser(user);
        sendMessage(user.getChatId(), "Добро пожаловать в сервис записи к врачу! Пожалуйста, введите ваше имя.");
    }

    private void processUserInput(User user, String messageText) {
        switch (user.getRegistrationState()) {
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
                sendMessage(user.getChatId(), "Пожалуйста, используйте кнопку 'Отправить номер телефона' для предоставления вашего номера.");
                break;
            case COMPLETED:
                sendMessage(user.getChatId(), "Вы уже зарегистрированы. Используйте команды /doctors, /book или /myappointments.");
                break;
        }
    }

    private void processPhoneNumber(User user, String phoneNumber) {
        if (user.getRegistrationState() == User.RegistrationState.AWAITING_PHONE_NUMBER) {
            try {
                authService.setPhoneNumber(user, phoneNumber);
                user.setRegistrationState(User.RegistrationState.COMPLETED);
                userService.updateUser(user);
                sendMessage(user.getChatId(), "Регистрация завершена! Теперь вы можете использовать сервис записи к врачу.");
            } catch (IllegalArgumentException e) {
                sendMessage(user.getChatId(), "Неверный формат номера телефона. Пожалуйста, попробуйте еще раз.");
                requestPhoneNumber(user.getChatId());
            }
        }
    }

    private void requestPhoneNumber(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Пожалуйста, предоставьте ваш номер телефона, нажав на кнопку ниже.");

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

    private void sendDoctorsList(long chatId) {
        List<Doctor> doctors = doctorService.getAllDoctors();
        StringBuilder message = new StringBuilder("Список доступных врачей:\n\n");
        for (Doctor doctor : doctors) {
            message.append(doctor.getFirstName()).append(" ")
                    .append(doctor.getLastName()).append(" - ")
                    .append(doctor.getSpecialization()).append("\n")
                    .append("Для записи используйте команду: /book ")
                    .append(doctor.getId()).append("\n\n");
        }
        sendMessage(chatId, message.toString());
    }

    private void handleBookingRequest(User user, String messageText) {
        String[] parts = messageText.split(" ");
        if (parts.length != 2) {
            sendMessage(user.getChatId(), "Неверный формат команды. Используйте /book <id врача>");
            return;
        }

        try {
            Long doctorId = Long.parseLong(parts[1]);
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
        } catch (NumberFormatException e) {
            sendMessage(user.getChatId(), "Неверный формат ID врача.");
        }
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
}