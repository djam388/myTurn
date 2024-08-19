package com.uzumacademy.myTurn.bot.menu.commands;

import com.uzumacademy.myTurn.bot.AppointmentHandler;
import com.uzumacademy.myTurn.bot.menu.Command;
import com.uzumacademy.myTurn.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TimeSelectionCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(TimeSelectionCommand.class);
    private final AppointmentHandler appointmentHandler;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;

    public TimeSelectionCommand(AppointmentHandler appointmentHandler) {
        this.appointmentHandler = appointmentHandler;
    }

    @Override
    public void execute(CallbackQuery callbackQuery, UserDTO userDTO) {
        String callbackData = callbackQuery.getData();
        String[] parts = callbackData.split("_");
        if (parts.length > 3) {
            try {
                Long id = Long.parseLong(parts[1]);
                LocalDate selectedDate = LocalDate.parse(parts[2], DATE_FORMATTER);
                LocalTime selectedTime = LocalTime.parse(parts[3], TIME_FORMATTER);
                boolean isReschedule = parts[0].equals("RESCHEDULE");

                logger.info("Executing TimeSelectionCommand for user: {}, id: {}, date: {}, time: {}, isReschedule: {}",
                        userDTO.getId(), id, selectedDate, selectedTime, isReschedule);

                appointmentHandler.handleTimeSelection(userDTO, id, selectedDate, selectedTime, isReschedule);
            } catch (NumberFormatException e) {
                logger.error("Invalid ID in callback data: {}", callbackData, e);
            } catch (DateTimeParseException e) {
                logger.error("Invalid date or time format in callback data: {}", callbackData, e);
            }
        } else {
            logger.warn("Invalid callback data format for TimeSelectionCommand: {}", callbackData);
        }
    }

    @Override
    public boolean canHandle(String callbackData) {
        return callbackData.startsWith("TIME_") || callbackData.startsWith("RESCHEDULE_TIME_");
    }
}