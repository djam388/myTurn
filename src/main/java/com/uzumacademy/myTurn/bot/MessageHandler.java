package com.uzumacademy.myTurn.bot;

import com.uzumacademy.myTurn.model.User;
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
        User user = userService.getOrCreateUser(chatId);

        if (message.hasText()) {
            String messageText = message.getText();
            handleTextMessage(user, messageText);
        } else if (message.hasContact()) {
            registrationHandler.processPhoneNumber(user, message.getContact().getPhoneNumber());
        }
    }

    private void handleTextMessage(User user, String messageText) {
        switch (messageText) {
            case "/start":
            case "Начать":
                if (user.isRegistrationCompleted()) {
                    messageSender.sendMessageWithMenuButton(user.getChatId(), "Вы уже зарегистрированы. Используйте кнопку 'Меню' для доступа к функциям бота.");
                } else {
                    registrationHandler.startRegistration(user);
                }
                break;
            case "/menu":
            case "Меню":
                if (user.isRegistrationCompleted()) {
                    messageSender.sendMainMenu(user.getChatId());
                } else {
                    messageSender.sendMessageWithStartButton(user.getChatId(), "Пожалуйста, сначала завершите регистрацию.");
                }
                break;
            default:
                if (!user.isRegistrationCompleted()) {
                    registrationHandler.processUserInput(user, messageText);
                } else {
                    messageSender.sendMessageWithMenuButton(user.getChatId(), "Используйте кнопку 'Меню' для доступа к функциям бота.");
                }
        }
    }
}