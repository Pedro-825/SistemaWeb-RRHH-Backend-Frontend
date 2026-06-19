package com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.interfaces;


import com.rrhh.Shared.persistence.PasswordResetTokenModel;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaPasswordResetTokenRepository
        extends JpaRepository<PasswordResetTokenModel, Long> {

    Optional<PasswordResetTokenModel> findByToken(String token);
}