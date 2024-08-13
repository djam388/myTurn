package com.uzumacademy.myTurn.service;

import com.uzumacademy.myTurn.dto.UserDTO;
import com.uzumacademy.myTurn.model.User;
import com.uzumacademy.myTurn.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserDTO getOrCreateUser(Long chatId) {
        return userRepository.findByChatId(chatId)
                .map(UserDTO::fromUser)
                .orElseGet(() -> {
                    UserDTO newUserDTO = new UserDTO();
                    newUserDTO.setChatId(chatId);
                    newUserDTO.setRegistrationState(UserDTO.RegistrationState.NEW);
                    User savedUser = userRepository.save(newUserDTO.toUser());
                    logger.info("New user created with chatId: {}", chatId);
                    return UserDTO.fromUser(savedUser);
                });
    }

    @Transactional
    public UserDTO updateUser(UserDTO userDTO) {
        User user = userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        User updatedUser = userDTO.toUser();
        updatedUser.updateLastActive();
        User savedUser = userRepository.save(updatedUser);
        logger.info("User updated: {}", savedUser.getChatId());
        return UserDTO.fromUser(savedUser);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserByChatId(Long chatId) {
        Optional<User> user = userRepository.findByChatId(chatId);
        if (user.isPresent()) {
            return UserDTO.fromUser(user.get());
        } else {
            logger.warn("User not found for chatId: {}", chatId);
            return null;
        }
    }

}