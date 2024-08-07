package com.uzumacademy.myTurn.service;

import com.uzumacademy.myTurn.model.User;
import com.uzumacademy.myTurn.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User getOrCreateUser(Long chatId) {
        return userRepository.findByChatId(chatId)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setChatId(chatId);
                    newUser.setRegistrationState(User.RegistrationState.NEW);
                    User savedUser = userRepository.save(newUser);
                    logger.info("New user created with chatId: {}", chatId);
                    return savedUser;
                });
    }

    @Transactional
    public User updateUser(User user) {
        user.updateLastActive();
        User savedUser = userRepository.save(user);
        logger.info("User updated: {}", user.getChatId());
        return savedUser;
    }

    @Transactional(readOnly = true)
    public User getUserByChatId(Long chatId) {
        Optional<User> user = userRepository.findByChatId(chatId);
        if (user.isPresent()) {
            return user.get();
        } else {
            logger.warn("User not found for chatId: {}", chatId);
            return null;
        }
    }

    @Transactional(readOnly = true)
    public User getUserByPhoneNumber(String phoneNumber) {
        Optional<User> user = userRepository.findByPhoneNumber(phoneNumber);
        if (user.isPresent()) {
            return user.get();
        } else {
            logger.warn("User not found for phone number: {}", phoneNumber);
            return null;
        }
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public void deleteUser(Long chatId) {
        User user = getUserByChatId(chatId);
        if (user != null) {
            userRepository.delete(user);
            logger.info("User deleted: {}", chatId);
        } else {
            logger.warn("Attempt to delete non-existent user: {}", chatId);
        }
    }

    @Transactional
    public void updateUserRegistrationState(Long chatId, User.RegistrationState state) {
        User user = getUserByChatId(chatId);
        if (user != null) {
            user.setRegistrationState(state);
            updateUser(user);
            logger.info("Updated registration state for user {}: {}", chatId, state);
        } else {
            logger.warn("Attempt to update registration state for non-existent user: {}", chatId);
        }
    }

    @Transactional(readOnly = true)
    public boolean isUserRegistrationCompleted(Long chatId) {
        User user = getUserByChatId(chatId);
        return user != null && user.isRegistrationCompleted();
    }

    @Transactional
    public void setUserPhoneNumber(Long chatId, String phoneNumber) {
        User user = getUserByChatId(chatId);
        if (user != null) {
            user.setPhoneNumber(phoneNumber);
            user.setRegistrationState(User.RegistrationState.COMPLETED);
            updateUser(user);
            logger.info("Phone number set and registration completed for user: {}", chatId);
        } else {
            logger.warn("Attempt to set phone number for non-existent user: {}", chatId);
        }
    }
}