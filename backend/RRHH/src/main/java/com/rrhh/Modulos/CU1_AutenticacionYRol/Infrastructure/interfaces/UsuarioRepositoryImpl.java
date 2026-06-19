package com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.interfaces;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Domain.entities.Usuario;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Domain.repository.IUsuarioRepository;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.mappers.UsuarioMapper;

import com.rrhh.Shared.persistence.UsuarioModel;

import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class UsuarioRepositoryImpl implements IUsuarioRepository {

    private final JpaUsuarioRepository jpaRepository;

    public UsuarioRepositoryImpl(JpaUsuarioRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Usuario findByUsername(String username) {

        return jpaRepository.findByNombreUsuario(username)
                .map(UsuarioMapper::toDomain)
                .orElse(null);
    }

    @Override
    public Usuario findByNombreUsuarioOrCorreoInst(
        String nombreUsuario,
        String correoInst
    ) {

    return jpaRepository
            .findByNombreUsuarioOrCorreoInst(
                    nombreUsuario,
                    correoInst
            )
            .map(UsuarioMapper::toDomain)
            .orElse(null);
    }

    @Override
public Usuario save(Usuario usuario) {

    if (usuario.getId() != null) {

        UsuarioModel model = jpaRepository.findById(Objects.requireNonNull(usuario.getId()))
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        model.setIntentosFallidos(usuario.getIntentosFallidos());
        model.setCuentaBloqueada(usuario.isCuentaBloqueada());
        model.setFechaBloqueo(usuario.getFechaBloqueo());

        model.setDosfaActivo(usuario.isDosFAActivado());
        model.setDosfaSecret(usuario.getSecret2FA());

        model.setIntentosFalla2fa(usuario.getIntentosFalla2fa());
        model.setBloqueo2faHasta(usuario.getBloqueo2faHasta());

        model.setTokenInvalidoDesde(usuario.getTokenInvalidoDesde());
        model.setContrasenia(usuario.getContrasenia());

        UsuarioModel saved = jpaRepository.save(model);

        return UsuarioMapper.toDomain(saved);
    }

    UsuarioModel model = UsuarioMapper.toModel(usuario);

    UsuarioModel saved = jpaRepository.save(Objects.requireNonNull(model));

    return UsuarioMapper.toDomain(saved);
}
}