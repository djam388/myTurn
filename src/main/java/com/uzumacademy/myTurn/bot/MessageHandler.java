package com.uzumacademy.myTurn.bot;

import com.uzumacademy.myTurn.bot.menu.MenuHandler;
import com.uzumacademy.myTurn.dto.UserDTO;
import com.uzumacademy.myTurn.service.UserService;
import com.uzumacademy.myTurn.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class MessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

    private final UserService userService;
    private final AuthenticationService authService;
    private final MessageSender messageSender;
    private final RegistrationHandler registrationHandler;
    private final MenuHandler menuHandler;

    public MessageHandler(UserService userService, AuthenticationService authService,
                          MessageSender messageSender, RegistrationHandler registrationHandler,
                          MenuHandler menuHandler) {
        this.userService = userService;
        this.authService = authService;
        this.messageSender = messageSender;
        this.registrationHandler = registrationHandler;
        this.menuHandler = menuHandler;
    }

    public void handleMessage(Message message) {
        long chatId = message.getChatId();
        UserDTO userDTO = userService.getOrCreateUser(chatId);

        if (message.hasText()) {
            String messageText = message.getText();
            handleTextMessage(userDTO, messageText);
        } else if (message.hasContact()) {
            registrationHandler.processPhoneNumber(userDTO, message.getContact().getPhoneNumber());
        }
    }

    private void handleTextMessage(UserDTO userDTO, String messageText) {
        switch (messageText) {
            case "/start":
            case "Начать":
                if (userDTO.getRegistrationState() == UserDTO.RegistrationState.COMPLETED) {
                    messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Вы уже зарегистрированы. Используйте кнопку 'Меню' для доступа к функциям бота.");
                } else {
                    registrationHandler.startRegistration(userDTO);
                }
                break;
            case "/menu":
            case "Меню":
                if (userDTO.getRegistrationState() == UserDTO.RegistrationState.COMPLETED) {
                    messageSender.sendMainMenu(userDTO.getChatId());
                } else {
                    messageSender.sendMessageWithStartButton(userDTO.getChatId(), "Пожалуйста, сначала завершите регистрацию.");
                }
                break;
            default:
                if (userDTO.getRegistrationState() != UserDTO.RegistrationState.COMPLETED) {
                    registrationHandler.processUserInput(userDTO, messageText);
                } else {
                    messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Используйте кнопку 'Меню' для доступа к функциям бота.");
                }
        }
    }
}