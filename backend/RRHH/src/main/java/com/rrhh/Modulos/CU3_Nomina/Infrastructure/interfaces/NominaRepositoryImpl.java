package com.rrhh.Modulos.CU3_Nomina.Infrastructure.interfaces;

import com.rrhh.Modulos.CU3_Nomina.Domain.entities.DetalleNomina;
import com.rrhh.Modulos.CU3_Nomina.Domain.entities.Nomina;
import com.rrhh.Modulos.CU3_Nomina.Domain.repository.INominaRepository;
import com.rrhh.Shared.persistence.DetalleNominaModel;
import com.rrhh.Shared.persistence.EmpleadoModel;
import com.rrhh.Shared.persistence.NominaModel;
import com.rrhh.Shared.persistence.UsuarioModel;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class NominaRepositoryImpl implements INominaRepository {

    private final JpaNominaRepository jpaNominaRepository;
    private final JpaDetalleNominaRepository jpaDetalleNominaRepository;
    private final DetalleNominaRepositoryImpl detalleMapper;

    public NominaRepositoryImpl(
            JpaNominaRepository jpaNominaRepository,
            JpaDetalleNominaRepository jpaDetalleNominaRepository,
            DetalleNominaRepositoryImpl detalleMapper) {
        this.jpaNominaRepository = jpaNominaRepository;
        this.jpaDetalleNominaRepository = jpaDetalleNominaRepository;
        this.detalleMapper = detalleMapper;
    }

    @Override
    public Nomina guardar(Nomina nomina) {
        NominaModel model = mapearAModel(nomina);
        NominaModel guardado = jpaNominaRepository.save(Objects.requireNonNull(model));
        nomina.setIdNomina(guardado.getIdNomina());
        return nomina;
    }

    @Override
    public Optional<Nomina> buscarPorId(Integer idNomina) {
        return jpaNominaRepository.findById(Objects.requireNonNull(idNomina))
            .map(this::mapearAEntidad);
    }

    @Override
    public List<Nomina> buscarPorEmpleadoYPeriodo(Long idEmpleado, String periodo) {
        List<NominaModel> models;
        if (periodo != null) {
            models = jpaNominaRepository.findByEmpleadoIdEmpleadoAndPeriodo(idEmpleado, periodo);
        } else {
            models = jpaNominaRepository.findByEmpleadoIdEmpleado(idEmpleado);
        }
        return models.stream()
            .map(this::mapearAEntidadConDetalles)
            .collect(Collectors.toList());
    }

    @Override
    public List<Nomina> buscarPorPeriodo(String periodo) {
        return jpaNominaRepository.findByPeriodo(periodo)
            .stream().map(this::mapearAEntidadConDetalles).collect(Collectors.toList());
    }

    @Override
    public List<Nomina> buscarPorEmpleadoYRango(Long idEmpleado, LocalDate fechaInicio, LocalDate fechaFin) {
        return jpaNominaRepository.findByEmpleadoIdEmpleado(idEmpleado)
            .stream()
            .filter(n -> n.getFechaInicio() != null &&
                !n.getFechaInicio().isBefore(fechaInicio) &&
                !n.getFechaFin().isAfter(fechaFin))
            .map(this::mapearAEntidadConDetalles)
            .collect(Collectors.toList());
    }

    @Override
    public boolean existePorEmpleadoYPeriodo(Long idEmpleado, String periodo) {
        return jpaNominaRepository.existsByEmpleadoIdEmpleadoAndPeriodo(idEmpleado, periodo);
    }

    @Override
    public List<Nomina> buscarPorNombreEmpleado(String nombre) {
        return jpaNominaRepository.findAll().stream()
            .map(this::mapearAEntidadConDetalles)
            .collect(Collectors.toList());
    }

    private NominaModel mapearAModel(Nomina nomina) {
        NominaModel model = new NominaModel();
        model.setIdNomina(nomina.getIdNomina());
        model.setPeriodo(nomina.getPeriodo());
        model.setFechaInicio(nomina.getFechaInicio());
        model.setFechaFin(nomina.getFechaFin());
        model.setFechaEmision(nomina.getFechaEmision() != null ? nomina.getFechaEmision() : LocalDate.now());
        model.setSueldoBase(nomina.getSueldoBase());
        model.setTotalHorasTrabajadas(nomina.getTotalHorasTrabajadas());
        model.setTotalHorasExtra(nomina.getTotalHorasExtra());
        model.setTotalMinutosTardanza(nomina.getTotalMinutosTardanza());
        model.setBonifFamiliar(nomina.getBonifFamiliar());
        model.setBonifTurnoNocturno(nomina.getBonifTurnoNocturno());
        model.setBonifGuardia(nomina.getBonifGuardia());
        model.setBonifRiesgo(nomina.getBonifRiesgo());
        model.setBonifCargo(nomina.getBonifCargo());
        model.setDescuentoTardanzas(nomina.getDescuentoTardanzas());
        model.setDescuentoLey(nomina.getDescuentoLey());
        model.setAsignacionFamiliar(nomina.getAsignacionFamiliar());
        model.setSueldoBruto(nomina.getSueldoBruto());
        model.setSueldoNeto(nomina.getSueldoNeto());
        model.setEstadoPago(nomina.getEstadoPago());
        model.setVersion(nomina.getVersion() != null ? nomina.getVersion() : 1);
        model.setNominaAnteriorId(nomina.getNominaAnteriorId());
        model.setCantidadHijos(nomina.getCantidadHijos() != null ? nomina.getCantidadHijos() : 0);
        model.setTieneHijos(nomina.getTieneHijos() != null ? nomina.getTieneHijos() : false);
        model.setCantidadGuardias(nomina.getCantidadGuardias() != null ? nomina.getCantidadGuardias() : 0);
        model.setTotalHorasNocturnas(nomina.getTotalHorasNocturnas() != null ? nomina.getTotalHorasNocturnas() : java.math.BigDecimal.ZERO);
        model.setTipoPensionAplicada(nomina.getTipoPensionAplicada() != null ? nomina.getTipoPensionAplicada() : "ONP");

        EmpleadoModel emp = new EmpleadoModel();
        emp.setIdEmpleado(nomina.getIdEmpleado());
        model.setEmpleado(emp);

        if (nomina.getCalculadoPor() != null) {
            UsuarioModel usuario = new UsuarioModel();
            usuario.setIdUsuario(nomina.getCalculadoPor());
            model.setCalculadoPor(usuario);
        }

        return model;
    }

    private Nomina mapearAEntidad(NominaModel model) {
        Nomina nomina = new Nomina();
        nomina.setIdNomina(model.getIdNomina());
        nomina.setPeriodo(model.getPeriodo());
        nomina.setFechaInicio(model.getFechaInicio());
        nomina.setFechaFin(model.getFechaFin());
        nomina.setFechaEmision(model.getFechaEmision());
        nomina.setSueldoBase(model.getSueldoBase());
        nomina.setTotalHorasTrabajadas(model.getTotalHorasTrabajadas());
        nomina.setTotalHorasExtra(model.getTotalHorasExtra());
        nomina.setTotalMinutosTardanza(model.getTotalMinutosTardanza());
        nomina.setBonifFamiliar(model.getBonifFamiliar());
        nomina.setBonifTurnoNocturno(model.getBonifTurnoNocturno());
        nomina.setBonifGuardia(model.getBonifGuardia());
        nomina.setBonifRiesgo(model.getBonifRiesgo());
        nomina.setBonifCargo(model.getBonifCargo());
        nomina.setDescuentoTardanzas(model.getDescuentoTardanzas());
        nomina.setDescuentoLey(model.getDescuentoLey());
        nomina.setAsignacionFamiliar(model.getAsignacionFamiliar());
        nomina.setSueldoBruto(model.getSueldoBruto());
        nomina.setSueldoNeto(model.getSueldoNeto());
        nomina.setEstadoPago(model.getEstadoPago());
        nomina.setVersion(model.getVersion());
        nomina.setNominaAnteriorId(model.getNominaAnteriorId());
        nomina.setCantidadHijos(model.getCantidadHijos());
        nomina.setTieneHijos(model.getTieneHijos());
        nomina.setCantidadGuardias(model.getCantidadGuardias());
        nomina.setTotalHorasNocturnas(model.getTotalHorasNocturnas());
        nomina.setTipoPensionAplicada(model.getTipoPensionAplicada());
        if (model.getEmpleado() != null) {
            nomina.setIdEmpleado(model.getEmpleado().getIdEmpleado());
        }
        if (model.getCalculadoPor() != null) {
            nomina.setCalculadoPor(model.getCalculadoPor().getIdUsuario());
        }
        return nomina;
    }

    private Nomina mapearAEntidadConDetalles(NominaModel model) {
        Nomina nomina = mapearAEntidad(model);
        List<DetalleNominaModel> detalleModels = jpaDetalleNominaRepository.findByNominaIdNomina(model.getIdNomina());
        List<DetalleNomina> detalles = detalleModels.stream()
            .map(detalleMapper::mapearAEntidad)
            .collect(Collectors.toList());
        nomina.setDetalles(detalles);
        return nomina;
    }
}