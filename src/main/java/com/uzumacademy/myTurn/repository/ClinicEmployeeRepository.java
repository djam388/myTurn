package com.uzumacademy.myTurn.repository;

import com.uzumacademy.myTurn.model.ClinicEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClinicEmployeeRepository extends JpaRepository<ClinicEmployee, Long> {

    Optional<ClinicEmployee> findByUsername(String username);
}