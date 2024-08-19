package com.uzumacademy.myTurn.bot.menu.commands;

import com.uzumacademy.myTurn.bot.MessageSender;
import com.uzumacademy.myTurn.bot.menu.Command;
import com.uzumacademy.myTurn.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

public class MyProfileCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(MyProfileCommand.class);
    private final MessageSender messageSender;

    public MyProfileCommand(MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    @Override
    public void execute(CallbackQuery callbackQuery, UserDTO userDTO) {
        logger.info("Executing MyProfileCommand for user: {}", userDTO.getId());
        String profileInfo = String.format("Ваш профиль:\nИмя: %s\nФамилия: %s\nТелефон: %s",
                userDTO.getFirstName(), userDTO.getLastName(), userDTO.getPhoneNumber());
        messageSender.sendMessageWithBackButton(userDTO.getChatId(), profileInfo);
    }

    @Override
    public boolean canHandle(String callbackData) {
        return "MY_PROFILE".equals(callbackData);
    }
}