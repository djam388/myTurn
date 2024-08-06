package com.uzumacademy.myTurn.bot;

import com.uzumacademy.myTurn.model.Doctor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class KeyboardFactory {

    public ReplyKeyboardMarkup createSingleButtonKeyboard(String buttonText) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton(buttonText));
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);
        return keyboardMarkup;
    }

    public InlineKeyboardMarkup createMainMenuKeyboard() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(List.of(InlineKeyboardButton.builder().text("Список врачей").callbackData("SHOW_DOCTORS").build()));
        rowsInline.add(List.of(InlineKeyboardButton.builder().text("Мои записи").callbackData("MY_APPOINTMENTS").build()));
        rowsInline.add(List.of(InlineKeyboardButton.builder().text("Мой профиль").callbackData("MY_PROFILE").build()));
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    public InlineKeyboardMarkup createDoctorsListKeyboard(List<Doctor> doctors) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        for (Doctor doctor : doctors) {
            rowsInline.add(List.of(InlineKeyboardButton.builder()
                    .text(doctor.getFirstName() + " " + doctor.getLastName() + " (" + doctor.getSpecialization() + ")")
                    .callbackData("DOCTOR_" + doctor.getId())
                    .build()));
        }
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    public InlineKeyboardMarkup createDateSelectionKeyboard(List<LocalDate> availableDates, Long doctorId) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        for (LocalDate date : availableDates) {
            rowsInline.add(List.of(InlineKeyboardButton.builder()
                    .text(date.format(formatter))
                    .callbackData("DATE_" + doctorId + "_" + date.format(formatter))
                    .build()));
        }
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    public InlineKeyboardMarkup createTimeSelectionKeyboard(Map<LocalTime, Boolean> timeSlots, Long doctorId, LocalDate selectedDate) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        for (Map.Entry<LocalTime, Boolean> entry : timeSlots.entrySet()) {
            LocalTime time = entry.getKey();
            Boolean isAvailable = entry.getValue();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(time.format(formatter));
            if (isAvailable) {
                button.setCallbackData("TIME_" + doctorId + "_" + selectedDate + "_" + time.format(formatter));
            } else {
                button.setCallbackData("UNAVAILABLE");
            }
            rowsInline.add(List.of(button));
        }
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    public ReplyKeyboardMarkup createPhoneNumberKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        KeyboardButton button = new KeyboardButton("Отправить номер телефона");
        button.setRequestContact(true);
        row.add(button);
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);
        return keyboardMarkup;
    }
}