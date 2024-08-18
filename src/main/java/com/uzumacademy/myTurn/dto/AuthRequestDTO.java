package com.uzumacademy.myTurn.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AuthRequestDTO {
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
