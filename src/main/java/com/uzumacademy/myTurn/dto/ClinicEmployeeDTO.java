package com.uzumacademy.myTurn.dto;

import com.uzumacademy.myTurn.model.ClinicEmployee;
import java.time.LocalDateTime;

public class ClinicEmployeeDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private ClinicEmployee.Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ClinicEmployeeDTO() {}

    public ClinicEmployeeDTO(Long id, String firstName, String lastName, String username,
                             ClinicEmployee.Role role, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ClinicEmployeeDTO fromClinicEmployee(ClinicEmployee clinicEmployee) {
        ClinicEmployeeDTO dto = new ClinicEmployeeDTO();
        dto.setId(clinicEmployee.getId());
        dto.setFirstName(clinicEmployee.getFirstName());
        dto.setLastName(clinicEmployee.getLastName());
        dto.setUsername(clinicEmployee.getUsername());
        dto.setRole(clinicEmployee.getRole());
        dto.setCreatedAt(clinicEmployee.getCreatedAt());
        dto.setUpdatedAt(clinicEmployee.getUpdatedAt());
        return dto;
    }

    public ClinicEmployee toClinicEmployee() {
        ClinicEmployee clinicEmployee = new ClinicEmployee();
        clinicEmployee.setId(this.getId());
        clinicEmployee.setFirstName(this.getFirstName());
        clinicEmployee.setLastName(this.getLastName());
        clinicEmployee.setUsername(this.getUsername());
        clinicEmployee.setRole(this.getRole());
        clinicEmployee.setCreatedAt(this.getCreatedAt());
        clinicEmployee.setUpdatedAt(this.getUpdatedAt());
        return clinicEmployee;
    }

    // Геттеры
    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getUsername() { return username; }
    public ClinicEmployee.Role getRole() { return role; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Сеттеры
    public void setId(Long id) { this.id = id; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setUsername(String username) { this.username = username; }
    public void setRole(ClinicEmployee.Role role) { this.role = role; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

