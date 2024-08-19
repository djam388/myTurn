package com.uzumacademy.myTurn.bot.menu.commands;

import com.uzumacademy.myTurn.bot.AppointmentHandler;
import com.uzumacademy.myTurn.bot.menu.Command;
import com.uzumacademy.myTurn.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateSelectionCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(DateSelectionCommand.class);
    private final AppointmentHandler appointmentHandler;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public DateSelectionCommand(AppointmentHandler appointmentHandler) {
        this.appointmentHandler = appointmentHandler;
    }

    @Override
    public void execute(CallbackQuery callbackQuery, UserDTO userDTO) {
        String callbackData = callbackQuery.getData();
        String[] parts = callbackData.split("_");
        if (parts.length > 2) {
            try {
                Long id = Long.parseLong(parts[1]);
                LocalDate selectedDate = LocalDate.parse(parts[2], DATE_FORMATTER);
                boolean isReschedule = parts[0].equals("RESCHEDULE");

                logger.info("Executing DateSelectionCommand for user: {}, id: {}, date: {}, isReschedule: {}",
                        userDTO.getId(), id, selectedDate, isReschedule);

                appointmentHandler.handleDateSelection(userDTO, id, selectedDate, isReschedule);
            } catch (NumberFormatException e) {
                logger.error("Invalid ID in callback data: {}", callbackData, e);
            } catch (DateTimeParseException e) {
                logger.error("Invalid date format in callback data: {}", callbackData, e);
            }
        } else {
            logger.warn("Invalid callback data format for DateSelectionCommand: {}", callbackData);
        }
    }

    @Override
    public boolean canHandle(String callbackData) {
        return callbackData.startsWith("DATE_") || callbackData.startsWith("RESCHEDULE_DATE_");
    }
}