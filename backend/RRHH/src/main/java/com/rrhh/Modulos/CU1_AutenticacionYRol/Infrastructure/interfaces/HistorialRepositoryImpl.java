package com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.interfaces;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Domain.entities.Historial;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Domain.repository.IHistorialRepository;
import com.rrhh.Shared.persistence.HistorialAuditoriaModel;
import com.rrhh.Shared.persistence.UsuarioModel;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

@Component
public class HistorialRepositoryImpl implements IHistorialRepository {

    private final JpaHistorialRepository jpaRepository;

    public HistorialRepositoryImpl(JpaHistorialRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Historial historial) {

        HistorialAuditoriaModel model = new HistorialAuditoriaModel();

        // =========================
        // USUARIO QUE REALIZA LA ACCIÓN
        // =========================
        if (historial.getIdUsuario() != null) {
            UsuarioModel usuario = new UsuarioModel();
            usuario.setIdUsuario(historial.getIdUsuario());

            model.setUsuario(usuario);
        }

        // =========================
        // DATOS DE AUDITORÍA
        // =========================
        model.setAccion(historial.getAccion());
        model.setIpMaquina(historial.getIpMaquina());
        model.setTablaAfectada(historial.getTablaAfectada());

        model.setIdRegistroAfectado(
                historial.getIdRegistroAfectado()
        );

        model.setValorAnterior(
                historial.getValorAnterior()
        );

        model.setValorNuevo(
                historial.getValorNuevo()
        );

        model.setFechaDeCambio(
                historial.getFechaCambio() != null
                        ? historial.getFechaCambio()
                        : LocalDateTime.now()
        );

        model.setCreadoEl(
                LocalDateTime.now()
        );

        jpaRepository.save(Objects.requireNonNull(model));
    }
}