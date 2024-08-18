package com.uzumacademy.myTurn.service;

import com.uzumacademy.myTurn.dto.ClinicEmployeeDTO;
import com.uzumacademy.myTurn.model.ClinicEmployee;
import com.uzumacademy.myTurn.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ClinicEmployeeAuthService {
    private static final Logger logger = LoggerFactory.getLogger(ClinicEmployeeAuthService.class);

    private final JwtTokenProvider tokenProvider;
    private final ClinicEmployeeService clinicEmployeeService;

    @Autowired
    public ClinicEmployeeAuthService(JwtTokenProvider tokenProvider,
                                     ClinicEmployeeService clinicEmployeeService) {
        this.tokenProvider = tokenProvider;
        this.clinicEmployeeService = clinicEmployeeService;
    }

    public String authenticateClinicEmployee(String username, String password) {
        logger.info("Попытка аутентификации пользователя: {}", username);
        try {
            if (!clinicEmployeeService.verifyPassword(username, password)) {
                throw new BadCredentialsException("Invalid username or password");
            }

            ClinicEmployeeDTO employeeDTO = clinicEmployeeService.getClinicEmployeeByUsername(username);
            List<SimpleGrantedAuthority> authorities = getAuthoritiesForEmployee(employeeDTO);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);

            logger.info("Аутентификация успешна, генерация токена для пользователя: {}", username);
            return tokenProvider.generateToken(authentication, authorities);
        } catch (Exception e) {
            logger.error("Ошибка аутентификации для пользователя: {}", username, e);
            throw new RuntimeException("Authentication failed: " + e.getMessage(), e);
        }
    }

    private List<SimpleGrantedAuthority> getAuthoritiesForEmployee(ClinicEmployeeDTO employeeDTO) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + employeeDTO.getRole().name()));

        if (employeeDTO.getRole().equals(ClinicEmployee.Role.ADMIN)) {
            authorities.add(new SimpleGrantedAuthority("MANAGE_EMPLOYEES"));
        }
        authorities.add(new SimpleGrantedAuthority("VIEW_APPOINTMENTS"));

        return authorities;
    }
}
