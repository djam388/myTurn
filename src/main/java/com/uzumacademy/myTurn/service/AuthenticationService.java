package com.uzumacademy.myTurn.service;

import com.uzumacademy.myTurn.model.User;
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
    public boolean authenticateByPhoneNumber(Long chatId, String phoneNumber) {
        User user = userService.getUserByChatId(chatId);
        if (user == null) {
            logger.warn("Authentication attempt for non-existent user: {}", chatId);
            return false;
        }

        if (isAccountLocked(chatId)) {
            logger.warn("Authentication attempt for locked account: {}", chatId);
            return false;
        }

        if (user.getPhoneNumber() != null && user.getPhoneNumber().equals(phoneNumber)) {
            user.updateLastActive();
            userService.updateUser(user);
            resetLoginAttempts(chatId);
            logger.info("User authenticated successfully: {}", chatId);
            return true;
        } else {
            incrementLoginAttempts(chatId);
            logger.warn("Failed authentication attempt for user: {}", chatId);
            return false;
        }
    }

    @Transactional
    public void setPhoneNumber(User user, String phoneNumber) {
        if (!isValidPhoneNumber(phoneNumber)) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
        user.setPhoneNumber(phoneNumber);
        user.setRegistrationState(User.RegistrationState.COMPLETED);
        userService.updateUser(user);
        logger.info("Phone number set for user: {}", user.getChatId());
    }

    public boolean isUserRegistered(Long chatId) {
        User user = userService.getUserByChatId(chatId);
        return user != null && user.isRegistrationCompleted();
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        // Simple validation: phone number should contain only digits and be of certain length
//        return phoneNumber.matches("\\d{10,12}");
        return !phoneNumber.isBlank();
    }

    private void incrementLoginAttempts(Long chatId) {
        int attempts = loginAttempts.getOrDefault(chatId, 0) + 1;
        loginAttempts.put(chatId, attempts);
        if (attempts >= MAX_ATTEMPTS) {
            lockAccount(chatId);
        }
    }

    private void resetLoginAttempts(Long chatId) {
        loginAttempts.remove(chatId);
        lockedAccounts.remove(chatId);
    }

    private void lockAccount(Long chatId) {
        lockedAccounts.put(chatId, LocalDateTime.now().plusMinutes(LOCK_TIME_MINUTES));
        logger.warn("Account locked due to multiple failed attempts: {}", chatId);
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