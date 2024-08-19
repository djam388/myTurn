package com.uzumacademy.myTurn.bot.menu.commands;

import com.uzumacademy.myTurn.bot.AppointmentHandler;
import com.uzumacademy.myTurn.bot.menu.Command;
import com.uzumacademy.myTurn.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

public class DoctorAppointmentCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(DoctorAppointmentCommand.class);
    private final AppointmentHandler appointmentHandler;

    public DoctorAppointmentCommand(AppointmentHandler appointmentHandler) {
        this.appointmentHandler = appointmentHandler;
    }

    @Override
    public void execute(CallbackQuery callbackQuery, UserDTO userDTO) {
        String callbackData = callbackQuery.getData();
        String[] parts = callbackData.split("_");
        if (parts.length > 1) {
            try {
                Long doctorId = Long.parseLong(parts[1]);
                logger.info("Executing DoctorAppointmentCommand for user: {} and doctor: {}", userDTO.getId(), doctorId);
                appointmentHandler.handleBookingRequest(userDTO, doctorId);
            } catch (NumberFormatException e) {
                logger.error("Invalid doctor ID in callback data: {}", callbackData, e);
            }
        } else {
            logger.warn("Invalid callback data format for DoctorAppointmentCommand: {}", callbackData);
        }
    }

    @Override
    public boolean canHandle(String callbackData) {
        return callbackData.startsWith("DOCTOR_");
    }
}