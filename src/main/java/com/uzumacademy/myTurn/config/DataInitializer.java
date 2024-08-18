package com.uzumacademy.myTurn.config;

import com.uzumacademy.myTurn.model.Doctor;
import com.uzumacademy.myTurn.model.ClinicEmployee;
import com.uzumacademy.myTurn.repository.DoctorRepository;
import com.uzumacademy.myTurn.repository.ClinicEmployeeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(DoctorRepository doctorRepository,
                                   ClinicEmployeeRepository clinicEmployeeRepository,
                                   PasswordEncoder passwordEncoder) {
        return args -> {
            if (doctorRepository.count() == 0) {
                Doctor doctor1 = new Doctor();
                doctor1.setFirstName("Иван");
                doctor1.setLastName("Петров");
                doctor1.setSpecialization("Терапевт");
                doctor1.setPhoneNumber("+00001234567");
                doctor1.setEmail("ivan.petrov@example.com");
                doctor1.setActive(true);

                Doctor doctor2 = new Doctor();
                doctor2.setFirstName("Елена");
                doctor2.setLastName("Сидорова");
                doctor2.setSpecialization("Кардиолог");
                doctor2.setPhoneNumber("+00009876543");
                doctor2.setEmail("elena.sidorova@example.com");
                doctor2.setActive(true);

                Doctor doctor3 = new Doctor();
                doctor3.setFirstName("Алексей");
                doctor3.setLastName("Иванов");
                doctor3.setSpecialization("Невролог");
                doctor3.setPhoneNumber("+00005554433");
                doctor3.setEmail("alexey.ivanov@example.com");
                doctor3.setActive(true);

                Doctor doctor4 = new Doctor();
                doctor4.setFirstName("Ольга");
                doctor4.setLastName("Николаева");
                doctor4.setSpecialization("Педиатр");
                doctor4.setPhoneNumber("+00007778899");
                doctor4.setEmail("olga.kozlova@example.com");
                doctor4.setActive(true);

                Doctor doctor5 = new Doctor();
                doctor5.setFirstName("Дмитрий");
                doctor5.setLastName("Смирнов");
                doctor5.setSpecialization("Хирург");
                doctor5.setPhoneNumber("+00003332211");
                doctor5.setEmail("dmitry.smirnov@example.com");
                doctor5.setActive(true);

                doctorRepository.saveAll(Arrays.asList(doctor1, doctor2, doctor3, doctor4, doctor5));
            }
            if (clinicEmployeeRepository.count() == 0) {
                LocalDateTime now = LocalDateTime.now();

                ClinicEmployee admin = new ClinicEmployee();
                admin.setFirstName("Админ");
                admin.setLastName("Администраторов");
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("adminPassword"));
                admin.setRole(ClinicEmployee.Role.ADMIN);
                admin.setCreatedAt(now);
                admin.setUpdatedAt(now);

                ClinicEmployee receptionist = new ClinicEmployee();
                receptionist.setFirstName("Регина");
                receptionist.setLastName("Регистраторова");
                receptionist.setUsername("receptionist");
                receptionist.setPassword(passwordEncoder.encode("receptionistPassword"));
                receptionist.setRole(ClinicEmployee.Role.RECEPTIONIST);
                receptionist.setCreatedAt(now);
                receptionist.setUpdatedAt(now);

                clinicEmployeeRepository.saveAll(Arrays.asList(admin, receptionist));
            }
        };
    }
}
