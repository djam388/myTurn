package com.uzumacademy.myTurn.bot;

import com.uzumacademy.myTurn.bot.menu.MenuHandler;
import com.uzumacademy.myTurn.config.BotConfig;
import com.uzumacademy.myTurn.service.UserService;
import com.uzumacademy.myTurn.service.AuthenticationService;
import com.uzumacademy.myTurn.service.DoctorService;
import com.uzumacademy.myTurn.service.AppointmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class MyTurnBot extends TelegramLongPollingBot {
    private static final Logger logger = LoggerFactory.getLogger(MyTurnBot.class);

    private final BotConfig config;
    private final MessageHandler messageHandler;
    private final MenuHandler menuHandler;

    public MyTurnBot(BotConfig config, UserService userService, AuthenticationService authService,
                     DoctorService doctorService, AppointmentService appointmentService,
                     KeyboardFactory keyboardFactory) {
        this.config = config;
        MessageSender messageSender = new MessageSender(this, keyboardFactory);
        RegistrationHandler registrationHandler = new RegistrationHandler(userService, authService, messageSender);
        AppointmentHandler appointmentHandler = new AppointmentHandler(userService, doctorService, appointmentService, messageSender);
        this.messageHandler = new MessageHandler(userService, authService, messageSender, registrationHandler,
                new MenuHandler(userService, doctorService, appointmentService, messageSender, appointmentHandler));
        this.menuHandler = new MenuHandler(userService, doctorService, appointmentService, messageSender, appointmentHandler);
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
        try {
            if (update.hasMessage()) {
                messageHandler.handleMessage(update.getMessage());
            } else if (update.hasCallbackQuery()) {
                menuHandler.handleCallbackQuery(update.getCallbackQuery());
            }
        } catch (Exception e) {
            logger.error("Error processing update", e);
        }
    }

    public void execute(SendMessage message) throws TelegramApiException {
        try {
            super.execute(message);
        } catch (TelegramApiException e) {
            logger.error("Failed to send message", e);
            throw e;
        }
    }
}