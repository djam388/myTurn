package com.uzumacademy.myTurn.controller;

import com.uzumacademy.myTurn.dto.ClinicEmployeeDTO;
import com.uzumacademy.myTurn.service.ClinicEmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/clinic-employees")
@PreAuthorize("hasRole('ADMIN')")  // Только администраторы имеют доступ к этим эндпоинтам
public class ClinicEmployeeController {

    private final ClinicEmployeeService clinicEmployeeService;

    @Autowired
    public ClinicEmployeeController(ClinicEmployeeService clinicEmployeeService) {
        this.clinicEmployeeService = clinicEmployeeService;
    }

    @PostMapping
    public ResponseEntity<ClinicEmployeeDTO> createClinicEmployee(@Valid @RequestBody ClinicEmployeeDTO clinicEmployeeDTO,
                                                                  @RequestParam String password) {
        ClinicEmployeeDTO createdEmployee = clinicEmployeeService.createClinicEmployee(clinicEmployeeDTO, password);
        return new ResponseEntity<>(createdEmployee, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClinicEmployeeDTO> getClinicEmployeeById(@PathVariable Long id) {
        ClinicEmployeeDTO employee = clinicEmployeeService.getClinicEmployeeById(id);
        return ResponseEntity.ok(employee);
    }

    @GetMapping
    public ResponseEntity<List<ClinicEmployeeDTO>> getAllClinicEmployees() {
        List<ClinicEmployeeDTO> employees = clinicEmployeeService.getAllClinicEmployees();
        return ResponseEntity.ok(employees);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClinicEmployeeDTO> updateClinicEmployee(@PathVariable Long id,
                                                                  @Valid @RequestBody ClinicEmployeeDTO clinicEmployeeDTO,
                                                                  @RequestParam(required = false) String newPassword) {
        ClinicEmployeeDTO updatedEmployee = clinicEmployeeService.updateClinicEmployee(id, clinicEmployeeDTO, newPassword);
        return ResponseEntity.ok(updatedEmployee);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClinicEmployee(@PathVariable Long id) {
        clinicEmployeeService.deleteClinicEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<ClinicEmployeeDTO> getClinicEmployeeByUsername(@PathVariable String username) {
        ClinicEmployeeDTO employee = clinicEmployeeService.getClinicEmployeeByUsername(username);
        return ResponseEntity.ok(employee);
    }

    @ExceptionHandler(javax.persistence.EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFoundException(javax.persistence.EntityNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }
}