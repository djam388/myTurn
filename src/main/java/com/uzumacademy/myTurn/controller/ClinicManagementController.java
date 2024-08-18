package com.uzumacademy.myTurn.controller;

import com.uzumacademy.myTurn.dto.AppointmentDTO;
import com.uzumacademy.myTurn.dto.AuthRequestDTO;
import com.uzumacademy.myTurn.dto.AuthResponseDTO;
import com.uzumacademy.myTurn.service.AppointmentService;
import com.uzumacademy.myTurn.service.ClinicEmployeeAuthService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/clinic")
public class ClinicManagementController {

    private static final Logger logger = LoggerFactory.getLogger(ClinicManagementController.class);

    private final AppointmentService appointmentService;
    private final ClinicEmployeeAuthService clinicEmployeeAuthService;

    @Autowired
    public ClinicManagementController(AppointmentService appointmentService, ClinicEmployeeAuthService clinicEmployeeAuthService) {
        this.appointmentService = appointmentService;
        this.clinicEmployeeAuthService = clinicEmployeeAuthService;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO authRequest) {
        logger.info("Получен запрос на аутентификацию для пользователя: {}", authRequest.getUsername());
        try {
            String token = clinicEmployeeAuthService.authenticateClinicEmployee(authRequest.getUsername(), authRequest.getPassword());
            logger.info("Аутентификация успешна для пользователя: {}", authRequest.getUsername());
            return ResponseEntity.ok(new AuthResponseDTO(token));
        }
        catch (Exception e) {
            logger.error("Ошибка при аутентификации пользователя: {}", authRequest.getUsername(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponseDTO(null));
        }
    }

    @GetMapping("/appointments")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST') and hasAuthority('VIEW_APPOINTMENTS')")
    public ResponseEntity<List<AppointmentDTO>> getAppointments(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "appointmentTime") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDirection) {

        List<AppointmentDTO> appointments = appointmentService.getFilteredAppointments(startDate, endDate, doctorId, status, sortBy, sortDirection);
        return ResponseEntity.ok(appointments);
    }
}