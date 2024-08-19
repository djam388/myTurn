package com.uzumacademy.myTurn.bot.menu.commands;

import com.uzumacademy.myTurn.bot.AppointmentHandler;
import com.uzumacademy.myTurn.bot.menu.Command;
import com.uzumacademy.myTurn.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

public class MyAppointmentsCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(MyAppointmentsCommand.class);
    private final AppointmentHandler appointmentHandler;

    public MyAppointmentsCommand(AppointmentHandler appointmentHandler) {
        this.appointmentHandler = appointmentHandler;
    }

    @Override
    public void execute(CallbackQuery callbackQuery, UserDTO userDTO) {
        logger.info("Executing MyAppointmentsCommand for user: {}", userDTO.getId());
        appointmentHandler.sendUserAppointments(userDTO);
    }

    @Override
    public boolean canHandle(String callbackData) {
        return "MY_APPOINTMENTS".equals(callbackData);
    }
}