package com.uzumacademy.myTurn.bot.menu;

import com.uzumacademy.myTurn.dto.UserDTO;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CommandChain {
    private static final Logger logger = LoggerFactory.getLogger(CommandChain.class);
    private final List<Command> commands = new ArrayList<>();

    public void addCommand(Command command) {
        commands.add(command);
    }

    public void processCommand(CallbackQuery callbackQuery, UserDTO userDTO) {
        String callbackData = callbackQuery.getData();
        logger.debug("Processing command: {}", callbackData);

        for (Command command : commands) {
            if (command.canHandle(callbackData)) {
                logger.debug("Executing command: {}", command.getClass().getSimpleName());
                command.execute(callbackQuery, userDTO);
                return;
            }
        }

        logger.warn("No command found to handle: {}", callbackData);
        throw new UnsupportedOperationException("Неизвестная команда: " + callbackData);
    }
}