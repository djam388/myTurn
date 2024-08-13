package com.uzumacademy.myTurn.dto;

import com.uzumacademy.myTurn.model.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDTO {
    private Long id;
    private Long chatId;
    private String username;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private RegistrationState registrationState;
    private LocalDateTime createdAt;
    private LocalDateTime lastActive;

    public enum RegistrationState {
        NEW, AWAITING_FIRST_NAME, AWAITING_LAST_NAME, AWAITING_PHONE_NUMBER, COMPLETED
    }

    public static UserDTO fromUser(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setChatId(user.getChatId());
        dto.setUsername(user.getUsername());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setRegistrationState(mapToDTOState(user.getRegistrationState()));
        dto.setCreatedAt(user.getCreatedAt());
        dto.setLastActive(user.getLastActive());
        return dto;
    }

    private static UserDTO.RegistrationState mapToDTOState(User.RegistrationState state) {
        switch (state) {
            case NEW: return UserDTO.RegistrationState.NEW;
            case AWAITING_FIRST_NAME: return UserDTO.RegistrationState.AWAITING_FIRST_NAME;
            case AWAITING_LAST_NAME: return UserDTO.RegistrationState.AWAITING_LAST_NAME;
            case AWAITING_PHONE_NUMBER: return UserDTO.RegistrationState.AWAITING_PHONE_NUMBER;
            case COMPLETED: return UserDTO.RegistrationState.COMPLETED;
            default: throw new IllegalArgumentException("Unknown state: " + state);
        }
    }
    public User toUser() {
        User user = new User();
        user.setId(this.getId());
        user.setChatId(this.getChatId());
        user.setUsername(this.getUsername());
        user.setFirstName(this.getFirstName());
        user.setLastName(this.getLastName());
        user.setPhoneNumber(this.getPhoneNumber());
        user.setRegistrationState(mapToUserState(this.getRegistrationState()));
        user.setCreatedAt(this.getCreatedAt());
        user.setLastActive(this.getLastActive());
        return user;
    }
    private static User.RegistrationState mapToUserState(UserDTO.RegistrationState state) {
        switch (state) {
            case NEW: return User.RegistrationState.NEW;
            case AWAITING_FIRST_NAME: return User.RegistrationState.AWAITING_FIRST_NAME;
            case AWAITING_LAST_NAME: return User.RegistrationState.AWAITING_LAST_NAME;
            case AWAITING_PHONE_NUMBER: return User.RegistrationState.AWAITING_PHONE_NUMBER;
            case COMPLETED: return User.RegistrationState.COMPLETED;
            default: throw new IllegalArgumentException("Unknown state: " + state);
        }
    }
}