package com.uzumacademy.myTurn.bot;

import com.uzumacademy.myTurn.model.Appointment;
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
import java.util.*;

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

    public InlineKeyboardMarkup createDoctorsListKeyboardWithBack(List<Doctor> doctors) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        for (Doctor doctor : doctors) {
            rowsInline.add(List.of(InlineKeyboardButton.builder()
                    .text(doctor.getFirstName() + " " + doctor.getLastName() + " (" + doctor.getSpecialization() + ")")
                    .callbackData("DOCTOR_" + doctor.getId())
                    .build()));
        }
        rowsInline.add(List.of(InlineKeyboardButton.builder()
                .text("Назад")
                .callbackData("BACK_TO_MAIN_MENU")
                .build()));
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    public InlineKeyboardMarkup createDateSelectionKeyboardWithBack(List<LocalDate> availableDates, Long doctorId) {
        return createDateSelectionKeyboard(availableDates, doctorId, "DATE", "BACK_TO_DOCTORS");
    }


    private InlineKeyboardMarkup createDateSelectionKeyboard(List<LocalDate> availableDates, Long id, String prefix, String backAction) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("d MMM (EEE)", new Locale("ru"));
        DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        List<InlineKeyboardButton> currentRow = new ArrayList<>();
        for (LocalDate date : availableDates) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            String formattedDate = date.format(displayFormatter).toLowerCase();
            button.setText(formattedDate);
            button.setCallbackData(prefix + "_" + id + "_" + date.format(fullFormatter));
            currentRow.add(button);

            if (currentRow.size() == 4) {
                rowsInline.add(new ArrayList<>(currentRow));
                currentRow.clear();
            }
        }

        if (!currentRow.isEmpty()) {
            rowsInline.add(currentRow);
        }

        rowsInline.add(List.of(InlineKeyboardButton.builder()
                .text("Назад")
                .callbackData(backAction)
                .build()));

        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    public InlineKeyboardMarkup createTimeSelectionKeyboardWithBack(Map<LocalTime, Boolean> timeSlots, Long doctorId, LocalDate selectedDate) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        List<InlineKeyboardButton> currentRow = new ArrayList<>();

        for (Map.Entry<LocalTime, Boolean> entry : timeSlots.entrySet()) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            if (entry.getValue()) {
                button.setText(entry.getKey().format(formatter));
                button.setCallbackData("TIME_" + doctorId + "_" + selectedDate + "_" + entry.getKey());
            } else {
                button.setText(entry.getKey().format(formatter) + " ❌");
                button.setCallbackData("UNAVAILABLE");
            }
            currentRow.add(button);

            if (currentRow.size() == 3) {
                rowsInline.add(new ArrayList<>(currentRow));
                currentRow.clear();
            }
        }

        if (!currentRow.isEmpty()) {
            rowsInline.add(currentRow);
        }

        rowsInline.add(List.of(InlineKeyboardButton.builder()
                .text("Назад")
                .callbackData("BACK_TO_DATE_" + doctorId + "_" + selectedDate)
                .build()));

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

    public InlineKeyboardMarkup createAppointmentActionsKeyboard(Appointment appointment) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        if (appointment.getStatus() != Appointment.AppointmentStatus.CANCELLED) {
            InlineKeyboardButton rescheduleButton = new InlineKeyboardButton();
            rescheduleButton.setText("Перенести запись");
            rescheduleButton.setCallbackData("RESCHEDULE_APPOINTMENT_" + appointment.getId());
            rowsInline.add(Collections.singletonList(rescheduleButton));
        }

        if (appointment.getStatus() == Appointment.AppointmentStatus.SCHEDULED) {
            InlineKeyboardButton cancelButton = new InlineKeyboardButton();
            cancelButton.setText("Отменить запись");
            cancelButton.setCallbackData("CANCEL_APPOINTMENT_" + appointment.getId());
            rowsInline.add(Collections.singletonList(cancelButton));
        }

        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    public InlineKeyboardMarkup createRescheduleConfirmationKeyboard(Long appointmentId) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        InlineKeyboardButton confirmButton = new InlineKeyboardButton();
        confirmButton.setText("Подтвердить перенос");
        confirmButton.setCallbackData("RESCHEDULE_CONFIRM_" + appointmentId);

        InlineKeyboardButton cancelButton = new InlineKeyboardButton();
        cancelButton.setText("Отменить перенос");
        cancelButton.setCallbackData("RESCHEDULE_CANCEL_" + appointmentId);

        rowsInline.add(Arrays.asList(confirmButton, cancelButton));
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    public InlineKeyboardMarkup createBackButton() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        rowsInline.add(Collections.singletonList(InlineKeyboardButton.builder()
                .text("Назад")
                .callbackData("BACK_TO_MAIN_MENU")
                .build()));

        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    public InlineKeyboardMarkup createDateSelectionKeyboardForReschedule(List<LocalDate> availableDates, Long appointmentId) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd.MM (EE)", new Locale("ru"));
        DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        List<InlineKeyboardButton> currentRow = new ArrayList<>();
        for (LocalDate date : availableDates) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            String formattedDate = date.format(displayFormatter);
            button.setText(formattedDate);
            button.setCallbackData("RESCHEDULE_DATE_" + appointmentId + "_" + date.format(fullFormatter));
            currentRow.add(button);

            if (currentRow.size() == 4) {
                rowsInline.add(new ArrayList<>(currentRow));
                currentRow.clear();
            }
        }

        if (!currentRow.isEmpty()) {
            rowsInline.add(currentRow);
        }

        rowsInline.add(List.of(InlineKeyboardButton.builder()
                .text("Назад")
                .callbackData("BACK_TO_APPOINTMENTS")
                .build()));

        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    public InlineKeyboardMarkup createTimeSelectionKeyboardForReschedule(Map<LocalTime, Boolean> timeSlots, Long appointmentId, LocalDate selectedDate) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        List<InlineKeyboardButton> currentRow = new ArrayList<>();

        for (Map.Entry<LocalTime, Boolean> entry : timeSlots.entrySet()) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            if (entry.getValue()) {
                button.setText(entry.getKey().format(formatter));
                button.setCallbackData("RESCHEDULE_TIME_" + appointmentId + "_" + selectedDate + "_" + entry.getKey());
            } else {
                button.setText(entry.getKey().format(formatter) + " ❌");
                button.setCallbackData("UNAVAILABLE_TIME");
            }
            currentRow.add(button);

            if (currentRow.size() == 3) {
                rowsInline.add(new ArrayList<>(currentRow));
                currentRow.clear();
            }
        }

        if (!currentRow.isEmpty()) {
            rowsInline.add(currentRow);
        }

        rowsInline.add(List.of(InlineKeyboardButton.builder()
                .text("Назад")
                .callbackData("BACK_TO_RESCHEDULE_DATE_" + appointmentId)
                .build()));

        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }
}