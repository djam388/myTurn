package com.uzumacademy.myTurn.bot.menu.commands;

import com.uzumacademy.myTurn.bot.MessageSender;
import com.uzumacademy.myTurn.bot.menu.Command;
import com.uzumacademy.myTurn.dto.DoctorDTO;
import com.uzumacademy.myTurn.dto.UserDTO;
import com.uzumacademy.myTurn.service.DoctorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.List;

public class ShowDoctorsCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(ShowDoctorsCommand.class);
    private final DoctorService doctorService;
    private final MessageSender messageSender;

    public ShowDoctorsCommand(DoctorService doctorService, MessageSender messageSender) {
        this.doctorService = doctorService;
        this.messageSender = messageSender;
    }

    @Override
    public void execute(CallbackQuery callbackQuery, UserDTO userDTO) {
        logger.info("Executing ShowDoctorsCommand for user: {}", userDTO.getId());
        List<DoctorDTO> doctors = doctorService.getActiveDoctors();
        logger.debug("Retrieved {} active doctors", doctors.size());
        messageSender.sendDoctorsListWithBackButton(userDTO.getChatId(), doctors);
    }

    @Override
    public boolean canHandle(String callbackData) {
        return "SHOW_DOCTORS".equals(callbackData);
    }
}