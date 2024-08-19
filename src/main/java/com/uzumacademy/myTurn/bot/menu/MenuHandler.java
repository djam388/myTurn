package com.uzumacademy.myTurn.bot.menu;

import com.uzumacademy.myTurn.bot.AppointmentHandler;
import com.uzumacademy.myTurn.bot.MessageSender;
import com.uzumacademy.myTurn.bot.menu.commands.*;
import com.uzumacademy.myTurn.dto.UserDTO;
import com.uzumacademy.myTurn.service.UserService;
import com.uzumacademy.myTurn.service.DoctorService;
import com.uzumacademy.myTurn.service.AppointmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@Component
public class MenuHandler {
    private static final Logger logger = LoggerFactory.getLogger(MenuHandler.class);

    private final CommandChain commandChain;
    private final UserService userService;
    private final MessageSender messageSender;

    public MenuHandler(UserService userService,
                       DoctorService doctorService,
                       AppointmentService appointmentService,
                       MessageSender messageSender,
                       AppointmentHandler appointmentHandler) {
        this.userService = userService;
        this.messageSender = messageSender;
        this.commandChain = new CommandChain();


        this.commandChain.addCommand(new ShowDoctorsCommand(doctorService, messageSender));
        this.commandChain.addCommand(new MyAppointmentsCommand(appointmentHandler));
        this.commandChain.addCommand(new DoctorAppointmentCommand(appointmentHandler));
        this.commandChain.addCommand(new DateSelectionCommand(appointmentHandler));
        this.commandChain.addCommand(new TimeSelectionCommand(appointmentHandler));
        this.commandChain.addCommand(new AppointmentCancelCommand(appointmentHandler));
        this.commandChain.addCommand(new AppointmentRescheduleCommand(appointmentHandler));
        this.commandChain.addCommand(new BackCommand(messageSender, appointmentHandler, doctorService));
        this.commandChain.addCommand(new MyProfileCommand(messageSender));
    }

    public void handleCallbackQuery(CallbackQuery callbackQuery) {
        String callData = callbackQuery.getData();
        long chatId = callbackQuery.getMessage().getChatId();
        UserDTO userDTO = userService.getUserByChatId(chatId);

        logger.info("Received callback query: {}", callData);

        if (userDTO == null) {
            logger.error("User not found for chat ID: {}", chatId);
            messageSender.sendMessageWithMenuButton(chatId, "Ошибка: пользователь не найден.");
            return;
        }

        try {
            commandChain.processCommand(callbackQuery, userDTO);
        } catch (UnsupportedOperationException e) {
            logger.warn("Unknown action: {}", callData);
            messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Неизвестная команда.");
        } catch (Exception e) {
            logger.error("Error processing callback query", e);
            messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Произошла ошибка при обработке запроса. Пожалуйста, попробуйте еще раз.");
        }
    }
}