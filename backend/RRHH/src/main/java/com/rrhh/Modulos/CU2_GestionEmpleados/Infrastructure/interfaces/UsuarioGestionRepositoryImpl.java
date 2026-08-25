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
        Objects.requireNonNull(usuario);

        // UsuarioGestion solo representa un subconjunto de columnas de UsuarioModel
        // (no conoce dosfaSecret, intentosFallidos, cuentaBloqueada, appMovilInstalada,
        // tokenInvalidoDesde, etc.). Si se construyera un UsuarioModel desde cero con
        // UsuarioGestionMapper.toModel(...) y se guardara asi, el merge de JPA
        // sobreescribiria esas columnas con sus valores por defecto (null/false) --
        // por ejemplo borrando el secreto 2FA cada vez que se sanciona/asciende/
        // desactiva a un empleado, sin que esa fuera la intencion de esas acciones.
        // Por eso se carga primero el modelo existente y solo se le aplican los
        // campos que este dominio realmente gestiona.
        UsuarioModel model = usuario.getIdUsuario() != null
                ? jpaRepository.findById(usuario.getIdUsuario()).orElseGet(() -> UsuarioGestionMapper.toModel(usuario))
                : UsuarioGestionMapper.toModel(usuario);

        model.setNombreUsuario(usuario.getNombreUsuario());
        model.setCorreoInst(usuario.getCorreoInst());
        model.setContrasenia(usuario.getContrasenia());
        model.setActivo(usuario.getActivo());
        model.setDosfaActivo(usuario.getDosfaActivo());

        UsuarioModel saved = jpaRepository.save(model);

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