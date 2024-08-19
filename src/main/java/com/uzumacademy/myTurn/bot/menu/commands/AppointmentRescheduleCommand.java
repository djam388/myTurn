package com.uzumacademy.myTurn.bot.menu.commands;

import com.uzumacademy.myTurn.bot.AppointmentHandler;
import com.uzumacademy.myTurn.bot.menu.Command;
import com.uzumacademy.myTurn.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

public class AppointmentRescheduleCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(AppointmentRescheduleCommand.class);
    private final AppointmentHandler appointmentHandler;

    public AppointmentRescheduleCommand(AppointmentHandler appointmentHandler) {
        this.appointmentHandler = appointmentHandler;
    }

    @Override
    public void execute(CallbackQuery callbackQuery, UserDTO userDTO) {
        String callbackData = callbackQuery.getData();
        String[] parts = callbackData.split("_");
        if (parts.length > 2 && "APPOINTMENT".equals(parts[1])) {
            try {
                Long appointmentId = Long.parseLong(parts[2]);
                logger.info("Executing AppointmentRescheduleCommand for user: {} and appointment: {}", userDTO.getId(), appointmentId);
                appointmentHandler.startRescheduleProcess(userDTO, appointmentId);
            } catch (NumberFormatException e) {
                logger.error("Invalid appointment ID in callback data: {}", callbackData, e);
            }
        } else {
            logger.warn("Invalid callback data format for AppointmentRescheduleCommand: {}", callbackData);
        }
    }

    @Override
    public boolean canHandle(String callbackData) {
        return callbackData.startsWith("RESCHEDULE_APPOINTMENT_");
    }
}