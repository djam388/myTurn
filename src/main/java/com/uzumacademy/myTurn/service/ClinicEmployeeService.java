package com.uzumacademy.myTurn.service;

import com.uzumacademy.myTurn.dto.ClinicEmployeeDTO;
import com.uzumacademy.myTurn.model.ClinicEmployee;
import com.uzumacademy.myTurn.repository.ClinicEmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClinicEmployeeService {
    private static final Logger logger = LoggerFactory.getLogger(ClinicEmployeeService.class);

    private final ClinicEmployeeRepository clinicEmployeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public ClinicEmployeeService(ClinicEmployeeRepository clinicEmployeeRepository, PasswordEncoder passwordEncoder) {
        this.clinicEmployeeRepository = clinicEmployeeRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Transactional
    public ClinicEmployeeDTO createClinicEmployee(ClinicEmployeeDTO clinicEmployeeDTO, String password) {
        ClinicEmployee clinicEmployee = clinicEmployeeDTO.toClinicEmployee();
        clinicEmployee.setPassword(passwordEncoder.encode(password));
        ClinicEmployee savedEmployee = clinicEmployeeRepository.save(clinicEmployee);
        logger.info("Created new clinic employee: {}", savedEmployee.getUsername());
        return ClinicEmployeeDTO.fromClinicEmployee(savedEmployee);
    }

    @Transactional(readOnly = true)
    public ClinicEmployeeDTO getClinicEmployeeById(Long id) {
        ClinicEmployee clinicEmployee = clinicEmployeeRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Clinic employee not found with id: {}", id);
                    return new EntityNotFoundException("ClinicEmployee not found with id: " + id);
                });
        return ClinicEmployeeDTO.fromClinicEmployee(clinicEmployee);
    }

    @Transactional(readOnly = true)
    public ClinicEmployeeDTO getClinicEmployeeByUsername(String username) {
        logger.info("Получение сотрудника клиники по имени пользователя: {}", username);
        ClinicEmployee clinicEmployee = clinicEmployeeRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("Сотрудник клиники не найден с именем пользователя: {}", username);
                    return new EntityNotFoundException("ClinicEmployee not found with username: " + username);
                });
        return ClinicEmployeeDTO.fromClinicEmployee(clinicEmployee);
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return clinicEmployeeRepository.findByUsername(username).isPresent();
    }

    @Transactional(readOnly = true)
    public List<ClinicEmployeeDTO> getAllClinicEmployees() {
        return clinicEmployeeRepository.findAll().stream()
                .map(ClinicEmployeeDTO::fromClinicEmployee)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClinicEmployeeDTO updateClinicEmployee(Long id, ClinicEmployeeDTO clinicEmployeeDTO, String newPassword) {
        ClinicEmployee existingEmployee = clinicEmployeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ClinicEmployee not found with id: " + id));

        existingEmployee.setFirstName(clinicEmployeeDTO.getFirstName());
        existingEmployee.setLastName(clinicEmployeeDTO.getLastName());
        existingEmployee.setUsername(clinicEmployeeDTO.getUsername());
        existingEmployee.setRole(clinicEmployeeDTO.getRole());

        if (newPassword != null && !newPassword.isEmpty()) {
            existingEmployee.setPassword(passwordEncoder.encode(newPassword));
        }

        ClinicEmployee updatedEmployee = clinicEmployeeRepository.save(existingEmployee);
        logger.info("Updated clinic employee: {}", updatedEmployee.getUsername());
        return ClinicEmployeeDTO.fromClinicEmployee(updatedEmployee);
    }

    @Transactional
    public void deleteClinicEmployee(Long id) {
        if (!clinicEmployeeRepository.existsById(id)) {
            logger.error("Clinic employee not found with id: {} for deletion", id);
            throw new EntityNotFoundException("ClinicEmployee not found with id: " + id);
        }
        clinicEmployeeRepository.deleteById(id);
        logger.info("Deleted clinic employee with id: {}", id);
    }

    public boolean verifyPassword(String username, String password) {
        logger.info("Проверка пароля для пользователя: {}", username);
        ClinicEmployee employee = clinicEmployeeRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        return passwordEncoder.matches(password, employee.getPassword());
    }
}
