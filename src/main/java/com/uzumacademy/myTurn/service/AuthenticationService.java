package com.uzumacademy.myTurn.service;

import com.uzumacademy.myTurn.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    private static final int MAX_ATTEMPTS = 3;
    private static final int LOCK_TIME_MINUTES = 15;

    private final UserService userService;
    private final Map<Long, Integer> loginAttempts;
    private final Map<Long, LocalDateTime> lockedAccounts;

    public AuthenticationService(UserService userService) {
        this.userService = userService;
        this.loginAttempts = new HashMap<>();
        this.lockedAccounts = new HashMap<>();
    }

    @Transactional
    public void setPhoneNumber(UserDTO userDTO, String phoneNumber) {
        if (!isValidPhoneNumber(phoneNumber)) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
        userDTO.setPhoneNumber(phoneNumber);
        userDTO.setRegistrationState(UserDTO.RegistrationState.COMPLETED);
        userService.updateUser(userDTO);
        logger.info("Phone number set for user: {}", userDTO.getChatId());
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        // Simple validation: phone number should not be blank
        return !phoneNumber.isBlank();
    }

    private boolean isAccountLocked(Long chatId) {
        LocalDateTime lockTime = lockedAccounts.get(chatId);
        if (lockTime != null) {
            if (LocalDateTime.now().isBefore(lockTime)) {
                return true;
            } else {
                lockedAccounts.remove(chatId);
                loginAttempts.remove(chatId);
            }
        }
        return false;
    }
}