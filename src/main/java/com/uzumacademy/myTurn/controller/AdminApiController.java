package com.uzumacademy.myTurn.controller;

import com.uzumacademy.myTurn.dto.AppointmentDTO;
import com.uzumacademy.myTurn.dto.DoctorDTO;
import com.uzumacademy.myTurn.service.AppointmentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminApiController {

    private final AppointmentService appointmentService;

    public AdminApiController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping("/appointments")
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