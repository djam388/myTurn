package com.uzumacademy.myTurn.bot;

import com.uzumacademy.myTurn.model.User;
import com.uzumacademy.myTurn.service.UserService;
import com.uzumacademy.myTurn.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RegistrationHandler {
    private static final Logger logger = LoggerFactory.getLogger(RegistrationHandler.class);

    private final UserService userService;
    private final AuthenticationService authService;
    private final MessageSender messageSender;

    public RegistrationHandler(UserService userService, AuthenticationService authService, MessageSender messageSender) {
        this.userService = userService;
        this.authService = authService;
        this.messageSender = messageSender;
    }

    public void startRegistration(User user) {
        user.setRegistrationState(User.RegistrationState.AWAITING_FIRST_NAME);
        userService.updateUser(user);
        messageSender.sendMessageWithoutKeyboard(user.getChatId(), "Добро пожаловать в сервис записи к врачу! Пожалуйста, введите ваше имя.");
    }

    public void processUserInput(User user, String messageText) {
        switch (user.getRegistrationState()) {
            case AWAITING_FIRST_NAME:
                processFirstName(user, messageText);
                break;
            case AWAITING_LAST_NAME:
                processLastName(user, messageText);
                break;
            case AWAITING_PHONE_NUMBER:
                processPhoneNumber(user, messageText);
                break;
        }
    }

    private void processFirstName(User user, String firstName) {
        user.setFirstName(firstName);
        user.setRegistrationState(User.RegistrationState.AWAITING_LAST_NAME);
        userService.updateUser(user);
        messageSender.sendMessageWithoutKeyboard(user.getChatId(), "Спасибо! Теперь введите вашу фамилию.");
    }

    private void processLastName(User user, String lastName) {
        user.setLastName(lastName);
        user.setRegistrationState(User.RegistrationState.AWAITING_PHONE_NUMBER);
        userService.updateUser(user);
        messageSender.requestPhoneNumber(user.getChatId());
    }

    public void processPhoneNumber(User user, String phoneNumber) {
        try {
            authService.setPhoneNumber(user, phoneNumber);
            completeRegistration(user);
        } catch (IllegalArgumentException e) {
            messageSender.sendMessageWithoutKeyboard(user.getChatId(), "Неверный формат номера телефона. Пожалуйста, попробуйте еще раз.");
            messageSender.requestPhoneNumber(user.getChatId());
        }
    }

    private void completeRegistration(User user) {
        user.setRegistrationState(User.RegistrationState.COMPLETED);
        userService.updateUser(user);
        messageSender.sendMessageWithMenuButton(user.getChatId(), "Регистрация завершена! Теперь вы можете использовать кнопку 'Меню' для доступа к функциям бота.");
        messageSender.sendMainMenu(user.getChatId());
    }
}