package com.uzumacademy.myTurn.controller;

import com.uzumacademy.myTurn.dto.AuthRequestDTO;
import com.uzumacademy.myTurn.dto.AuthResponseDTO;
import com.uzumacademy.myTurn.service.ClinicEmployeeAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/clinic-auth")
public class ClinicEmployeeAuthController {

    private final ClinicEmployeeAuthService clinicEmployeeAuthService;

    @Autowired
    public ClinicEmployeeAuthController(ClinicEmployeeAuthService clinicEmployeeAuthService) {
        this.clinicEmployeeAuthService = clinicEmployeeAuthService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> authenticateClinicEmployee(
            @Valid @RequestBody AuthRequestDTO authRequest) {
        String token = clinicEmployeeAuthService.authenticateClinicEmployee(
                authRequest.getUsername(),
                authRequest.getPassword()
        );
        return ResponseEntity.ok(new AuthResponseDTO(token));
    }
}