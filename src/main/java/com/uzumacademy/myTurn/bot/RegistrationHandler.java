package com.uzumacademy.myTurn.bot;

import com.uzumacademy.myTurn.dto.UserDTO;
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

    public void startRegistration(UserDTO userDTO) {
        userDTO.setRegistrationState(UserDTO.RegistrationState.AWAITING_FIRST_NAME);
        userService.updateUser(userDTO);
        messageSender.sendMessageWithoutKeyboard(userDTO.getChatId(), "Добро пожаловать в сервис записи к врачу! Пожалуйста, введите ваше имя.");
    }

    public void processUserInput(UserDTO userDTO, String messageText) {
        switch (userDTO.getRegistrationState()) {
            case AWAITING_FIRST_NAME:
                processFirstName(userDTO, messageText);
                break;
            case AWAITING_LAST_NAME:
                processLastName(userDTO, messageText);
                break;
            case AWAITING_PHONE_NUMBER:
                processPhoneNumber(userDTO, messageText);
                break;
        }
    }

    private void processFirstName(UserDTO userDTO, String firstName) {
        userDTO.setFirstName(firstName);
        userDTO.setRegistrationState(UserDTO.RegistrationState.AWAITING_LAST_NAME);
        userService.updateUser(userDTO);
        messageSender.sendMessageWithoutKeyboard(userDTO.getChatId(), "Спасибо! Теперь введите вашу фамилию.");
    }

    private void processLastName(UserDTO userDTO, String lastName) {
        userDTO.setLastName(lastName);
        userDTO.setRegistrationState(UserDTO.RegistrationState.AWAITING_PHONE_NUMBER);
        userService.updateUser(userDTO);
        messageSender.requestPhoneNumber(userDTO.getChatId());
    }

    public void processPhoneNumber(UserDTO userDTO, String phoneNumber) {
        try {
            authService.setPhoneNumber(userDTO, phoneNumber);
            completeRegistration(userDTO);
        } catch (IllegalArgumentException e) {
            messageSender.sendMessageWithoutKeyboard(userDTO.getChatId(), "Неверный формат номера телефона. Пожалуйста, попробуйте еще раз.");
            messageSender.requestPhoneNumber(userDTO.getChatId());
        }
    }

    private void completeRegistration(UserDTO userDTO) {
        userDTO.setRegistrationState(UserDTO.RegistrationState.COMPLETED);
        userService.updateUser(userDTO);
        messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Регистрация завершена! Теперь вы можете использовать кнопку 'Меню' для доступа к функциям бота.");
        messageSender.sendMainMenu(userDTO.getChatId());
    }
}