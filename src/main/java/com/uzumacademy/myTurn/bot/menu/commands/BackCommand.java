package com.uzumacademy.myTurn.bot.menu.commands;

import com.uzumacademy.myTurn.bot.AppointmentHandler;
import com.uzumacademy.myTurn.bot.MessageSender;
import com.uzumacademy.myTurn.bot.menu.Command;
import com.uzumacademy.myTurn.dto.UserDTO;
import com.uzumacademy.myTurn.dto.DoctorDTO;
import com.uzumacademy.myTurn.service.DoctorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.time.LocalDate;
import java.util.List;

public class BackCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(BackCommand.class);
    private final MessageSender messageSender;
    private final AppointmentHandler appointmentHandler;
    private final DoctorService doctorService;

    public BackCommand(MessageSender messageSender, AppointmentHandler appointmentHandler, DoctorService doctorService) {
        this.messageSender = messageSender;
        this.appointmentHandler = appointmentHandler;
        this.doctorService = doctorService;
    }

    @Override
    public void execute(CallbackQuery callbackQuery, UserDTO userDTO) {
        String callbackData = callbackQuery.getData();
        logger.info("Executing BackCommand for user: {}, callbackData: {}", userDTO.getId(), callbackData);

        switch (callbackData) {
            case "BACK_TO_MAIN_MENU":
                messageSender.sendMainMenu(userDTO.getChatId());
                break;
            case "BACK_TO_DOCTORS":
                sendDoctorsList(userDTO.getChatId());
                break;
            case "BACK_TO_APPOINTMENTS":
                appointmentHandler.sendUserAppointments(userDTO);
                break;
            default:
                if (callbackData.startsWith("BACK_TO_DATE_")) {
                    handleBackToDate(callbackData, userDTO);
                } else if (callbackData.startsWith("BACK_TO_RESCHEDULE_DATE_")) {
                    handleBackToRescheduleDate(callbackData, userDTO);
                } else {
                    logger.warn("Unknown BACK action: {}", callbackData);
                    messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Неизвестная команда возврата.");
                }
        }
    }

    @Override
    public boolean canHandle(String callbackData) {
        return callbackData.startsWith("BACK_");
    }

    private void sendDoctorsList(long chatId) {
        List<DoctorDTO> doctors = doctorService.getActiveDoctors();
        messageSender.sendDoctorsListWithBackButton(chatId, doctors);
    }

    private void handleBackToDate(String callbackData, UserDTO userDTO) {
        String[] parts = callbackData.split("_");
        if (parts.length >= 4) {
            try {
                Long doctorId = Long.parseLong(parts[3]);
                DoctorDTO doctorDTO = doctorService.getDoctorById(doctorId);
                if (doctorDTO != null) {
                    appointmentHandler.handleBookingRequest(userDTO, doctorId);
                } else {
                    logger.warn("Doctor not found for id: {}", doctorId);
                    messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Извините, произошла ошибка. Пожалуйста, начните сначала.");
                }
            } catch (NumberFormatException e) {
                logger.error("Error parsing doctor id from callback data: {}", callbackData, e);
                messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Произошла ошибка. Пожалуйста, начните сначала.");
            }
        } else {
            logger.warn("Invalid BACK_TO_DATE callback data: {}", callbackData);
            messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Неверные данные для возврата к выбору даты.");
        }
    }

    private void handleBackToRescheduleDate(String callbackData, UserDTO userDTO) {
        String[] parts = callbackData.split("_");
        if (parts.length >= 5) {
            try {
                Long appointmentId = Long.parseLong(parts[4]);
                appointmentHandler.startRescheduleProcess(userDTO, appointmentId);
            } catch (NumberFormatException e) {
                logger.error("Error parsing appointment id from callback data: {}", callbackData, e);
                messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Произошла ошибка. Пожалуйста, начните сначала.");
            }
        } else {
            logger.warn("Invalid BACK_TO_RESCHEDULE_DATE callback data: {}", callbackData);
            messageSender.sendMessageWithMenuButton(userDTO.getChatId(), "Неверные данные для возврата к выбору даты переноса.");
        }
    }
}