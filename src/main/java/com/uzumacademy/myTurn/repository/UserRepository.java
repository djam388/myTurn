package com.uzumacademy.myTurn.repository;


import com.uzumacademy.myTurn.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByChatId(Long chatId);
    Optional<User> findByPhoneNumber(String phoneNumber);
    boolean existsByPhoneNumber(String phoneNumber);
    List<User> findByRegistrationState(User.RegistrationState registrationState);
    void deleteByChatId(Long chatId);
    long countByRegistrationState(User.RegistrationState registrationState);

}