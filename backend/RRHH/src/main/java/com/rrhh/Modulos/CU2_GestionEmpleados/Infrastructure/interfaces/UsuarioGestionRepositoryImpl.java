package com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.interfaces.JpaUsuarioRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.UsuarioGestion;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IUsuarioGestionRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.mappers.UsuarioGestionMapper;
import com.rrhh.Shared.persistence.UsuarioModel;

import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class UsuarioGestionRepositoryImpl implements IUsuarioGestionRepository {

    private final JpaUsuarioRepository jpaRepository;

    public UsuarioGestionRepositoryImpl(JpaUsuarioRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public UsuarioGestion save(UsuarioGestion usuario) {

        UsuarioModel model =
                UsuarioGestionMapper.toModel(usuario);

        UsuarioModel saved =
                jpaRepository.save(Objects.requireNonNull(model));

        return UsuarioGestionMapper.toDomain(saved);
    }

    @Override
    public UsuarioGestion findByEmpleadoId(Long idEmpleado) {

        return jpaRepository.findByEmpleadoIdEmpleado(idEmpleado)
                .map(UsuarioGestionMapper::toDomain)
                .orElse(null);
    }

    @Override
    public boolean existsByNombreUsuario(String nombreUsuario) {
        return jpaRepository.existsByNombreUsuario(nombreUsuario);
    }

    @Override
    public boolean existsByCorreoInst(String correoInst) {
        return jpaRepository.existsByCorreoInst(correoInst);
    }
}