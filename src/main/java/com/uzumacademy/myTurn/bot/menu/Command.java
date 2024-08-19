package com.uzumacademy.myTurn.bot.menu;

import com.uzumacademy.myTurn.dto.UserDTO;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

public interface Command {
    void execute(CallbackQuery callbackQuery, UserDTO userDTO);
    boolean canHandle(String callbackData);
}