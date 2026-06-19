package com.rrhh.Modulos.CU6_Solicitud.Application.services;

import com.rrhh.Shared.persistence.EmpleadoModel;
import com.rrhh.Shared.persistence.HistorialAuditoriaModel;
import com.rrhh.Modulos.CU6_Solicitud.Application.dto.DecisionGerenciaRequestDTO;
import com.rrhh.Modulos.CU6_Solicitud.Application.dto.RegistrarSolicitudRequestDTO;
import com.rrhh.Modulos.CU6_Solicitud.Application.dto.RevisionRrhhRequestDTO;
import com.rrhh.Modulos.CU6_Solicitud.Application.dto.SolicitudResponseDTO;
import com.rrhh.Modulos.CU6_Solicitud.Domain.entities.Solicitud;
import com.rrhh.Modulos.CU6_Solicitud.Domain.repository.ISolicitudRepository;
import com.rrhh.Modulos.CU6_Solicitud.Application.factory.ISolicitudFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SolicitudService implements ISolicitudService {

    private static final int DIAS_VACACIONES_ANUALES = 30;

    @PersistenceContext
    private EntityManager em;

    private final ISolicitudRepository solicitudRepository;
    private final ISolicitudFactory solicitudFactory;
    private final com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaEmpleadoRepository jpaEmpleadoRepository;

    public SolicitudService(
            ISolicitudRepository solicitudRepository,
            ISolicitudFactory solicitudFactory,
            com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaEmpleadoRepository jpaEmpleadoRepository) {
        this.solicitudRepository = solicitudRepository;
        this.solicitudFactory = solicitudFactory;
        this.jpaEmpleadoRepository = jpaEmpleadoRepository;
    }

    @Override
    @Transactional
    public SolicitudResponseDTO registrarSolicitud(RegistrarSolicitudRequestDTO request, Long idEmpleado) {

        if (request.getFechaInicio().isAfter(request.getFechaFin())) {
            throw new IllegalArgumentException(
                "La fecha de inicio no puede ser mayor que la fecha fin."
            );
        }

        validarDiasLaborables(request.getFechaInicio(), request.getFechaFin());

        int diasSolicitados = calcularDiasLaborables(request.getFechaInicio(), request.getFechaFin());

        EmpleadoModel empleado = jpaEmpleadoRepository.findById(Objects.requireNonNull(idEmpleado))
            .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado."));
        if (!"ACTIVO".equals(empleado.getEstado())) {
            throw new IllegalStateException("El empleado no esta activo en el sistema.");
        }

        if ("VACACIONES".equals(request.getTipoSolicitud())) {
            int saldoDisponible = empleado.getSaldoVacaciones() != null ? empleado.getSaldoVacaciones() : DIAS_VACACIONES_ANUALES;
            if (diasSolicitados > saldoDisponible) {
                throw new IllegalStateException(
                    "No cuenta con dias disponibles de vacaciones. Saldo disponible: " + saldoDisponible + " dias."
                );
            }
        }

        Solicitud solicitud = solicitudFactory.crearSolicitudPermiso(
                request.getTipoSolicitud(), request.getMotivo());
        solicitud.setFechaInicio(request.getFechaInicio());
        solicitud.setFechaFin(request.getFechaFin());
        solicitud.setDiasSolicitados(diasSolicitados);
        solicitud.setArchivoAdjunto(request.getArchivoAdjunto());
        solicitud.setEstado("PENDIENTE");
        solicitud.setIdEmpleado(idEmpleado);
        solicitud.setCreadoEl(LocalDateTime.now());
        solicitud.setActualizadoEl(LocalDateTime.now());

        Solicitud guardada = solicitudRepository.guardar(solicitud);
        return mapearAResponse(guardada, empleado);
    }

    @Override
    @Transactional
    public SolicitudResponseDTO revisarSolicitudRrhh(RevisionRrhhRequestDTO request, Long idUsuarioRrhh) {

        Solicitud solicitud = solicitudRepository.buscarPorId(request.getIdSolicitud())
            .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada."));

        if (!"PENDIENTE".equals(solicitud.getEstado()) && !"OBSERVADO".equals(solicitud.getEstado())) {
            throw new IllegalStateException("La solicitud no esta en estado valido para revision.");
        }

        if (request.isAprobado()) {
            solicitud.setEstado("PROCESADO");
        } else {
            if (request.getObservacionRrhh() == null || request.getObservacionRrhh().isBlank()) {
                throw new IllegalArgumentException("Debe ingresar una observacion para devolver la solicitud.");
            }
            solicitud.setEstado("OBSERVADO");
            solicitud.setObservacionRrhh(request.getObservacionRrhh());
        }

        solicitud.setRevisadoPor(idUsuarioRrhh);
        solicitud.setFechaRevision(LocalDateTime.now());
        solicitud.setActualizadoEl(LocalDateTime.now());

        Solicitud actualizada = solicitudRepository.guardar(solicitud);
        EmpleadoModel empleado = jpaEmpleadoRepository.findById(Objects.requireNonNull(actualizada.getIdEmpleado())).orElse(null);

        HistorialAuditoriaModel log = new HistorialAuditoriaModel();
        log.setTablaAfectada("SOLICITUD");
        log.setIdRegistroAfectado(actualizada.getIdSolicitud().longValue());
        log.setAccion("REVISION_RRHH");
        log.setValorNuevo("estado=" + actualizada.getEstado() + ", revisadoPor=" + idUsuarioRrhh
                + ", observacion=" + (request.getObservacionRrhh() != null ? request.getObservacionRrhh() : ""));
        em.persist(log);

        return mapearAResponse(actualizada, empleado);
    }

    @Override
    @Transactional
    public SolicitudResponseDTO decidirSolicitudGerencia(DecisionGerenciaRequestDTO request, Long idUsuarioGerencia) {

        Solicitud solicitud = solicitudRepository.buscarPorId(request.getIdSolicitud())
            .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada."));

        if (!"PROCESADO".equals(solicitud.getEstado())) {
            throw new IllegalStateException("La solicitud no esta en estado PROCESADO para decision de Gerencia.");
        }

        if (request.isAprobado()) {
            solicitud.setEstado("APROBADA");
            solicitud.setRespuesta("Su solicitud de vacaciones o permisos ha sido aprobada por Gerencia.");

            // RNF86: Descontar saldo de vacaciones
            if ("VACACIONES".equals(solicitud.getTipoSolicitud())) {
                jpaEmpleadoRepository.findById(Objects.requireNonNull(solicitud.getIdEmpleado())).ifPresent(emp -> {
                    int saldoActual = emp.getSaldoVacaciones() != null ? emp.getSaldoVacaciones() : DIAS_VACACIONES_ANUALES;
                    int dias = solicitud.getDiasSolicitados() != null ? solicitud.getDiasSolicitados() : 0;
                    emp.setSaldoVacaciones(Math.max(0, saldoActual - dias));
                    jpaEmpleadoRepository.save(emp);
                });
            }
        } else {
            if (request.getRespuesta() == null || request.getRespuesta().isBlank()) {
                throw new IllegalArgumentException("Ingrese el motivo del rechazo.");
            }
            solicitud.setEstado("RECHAZADA");
            solicitud.setRespuesta(
                "Su solicitud de vacaciones o permisos ha sido rechazada por Gerencia. Motivo: " + request.getRespuesta()
            );
        }

        solicitud.setAprobadoPor(idUsuarioGerencia);
        solicitud.setFechaDecision(LocalDateTime.now());
        solicitud.setActualizadoEl(LocalDateTime.now());

        Solicitud actualizada = solicitudRepository.guardar(solicitud);
        EmpleadoModel empleado = jpaEmpleadoRepository.findById(Objects.requireNonNull(actualizada.getIdEmpleado())).orElse(null);

        HistorialAuditoriaModel log = new HistorialAuditoriaModel();
        log.setTablaAfectada("SOLICITUD");
        log.setIdRegistroAfectado(actualizada.getIdSolicitud().longValue());
        log.setAccion(request.isAprobado() ? "APROBACION_GERENCIA" : "RECHAZO_GERENCIA");
        log.setValorAnterior("estado=PROCESADO");
        log.setValorNuevo("estado=" + actualizada.getEstado() + ", aprobadoPor=" + idUsuarioGerencia
                + ", respuesta=" + actualizada.getRespuesta());
        em.persist(log);

        return mapearAResponse(actualizada, empleado);
    }

    @Override
    public List<SolicitudResponseDTO> listarPorEstado(String estado) {
        return solicitudRepository.buscarPorEstado(estado).stream()
            .map(s -> {
                EmpleadoModel emp = jpaEmpleadoRepository.findById(Objects.requireNonNull(s.getIdEmpleado())).orElse(null);
                return mapearAResponse(s, emp);
            })
            .collect(Collectors.toList());
    }

    @Override
    public List<SolicitudResponseDTO> listarPorEmpleado(Long idEmpleado) {
        return solicitudRepository.buscarPorEmpleado(idEmpleado).stream()
            .map(s -> {
                EmpleadoModel emp = jpaEmpleadoRepository.findById(Objects.requireNonNull(idEmpleado)).orElse(null);
                return mapearAResponse(s, emp);
            })
            .collect(Collectors.toList());
    }

    @Override
    public SolicitudResponseDTO buscarPorId(Integer idSolicitud) {
        Solicitud solicitud = solicitudRepository.buscarPorId(idSolicitud)
            .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada."));
        EmpleadoModel empleado = jpaEmpleadoRepository.findById(Objects.requireNonNull(solicitud.getIdEmpleado())).orElse(null);
        return mapearAResponse(solicitud, empleado);
    }

    @Override
    @Transactional
    public SolicitudResponseDTO clonarSolicitud(Integer idSolicitud, Long idEmpleado) {
        Solicitud original = solicitudRepository.buscarPorId(idSolicitud)
            .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada."));

        Solicitud clon = original.clonePrototype();

        clon.setIdSolicitud(null);
        clon.setEstado("PENDIENTE");
        clon.setRespuesta(null);
        clon.setObservacionRrhh(null);
        clon.setFechaRevision(null);
        clon.setFechaDecision(null);
        clon.setRevisadoPor(null);
        clon.setAprobadoPor(null);
        clon.setIdEmpleado(idEmpleado);
        clon.setCreadoEl(LocalDateTime.now());
        clon.setActualizadoEl(LocalDateTime.now());

        Solicitud guardada = solicitudRepository.guardar(clon);
        EmpleadoModel empleado = jpaEmpleadoRepository.findById(Objects.requireNonNull(idEmpleado)).orElse(null);
        return mapearAResponse(guardada, empleado);
    }

    public List<SolicitudResponseDTO> listarNotificacionesPorEmpleado(Long idEmpleado) {
        return solicitudRepository.buscarPorEmpleado(idEmpleado).stream()
            .filter(s -> ("APROBADA".equals(s.getEstado()) || "RECHAZADA".equals(s.getEstado())))
            .filter(s -> s.getFechaDecision() != null)
            .sorted((a, b) -> b.getFechaDecision().compareTo(a.getFechaDecision()))
            .map(s -> {
                EmpleadoModel emp = jpaEmpleadoRepository.findById(Objects.requireNonNull(s.getIdEmpleado())).orElse(null);
                return mapearAResponse(s, emp);
            })
            .collect(Collectors.toList());
    }

    // ================================
    // PRIVATE
    // ================================

    private void validarDiasLaborables(LocalDate inicio, LocalDate fin) {
        List<LocalDate> feriados = em.createQuery(
                "SELECT f.fecha FROM FeriadoModel f", LocalDate.class).getResultList();

        LocalDate fecha = inicio;
        while (!fecha.isAfter(fin)) {
            DayOfWeek dia = fecha.getDayOfWeek();
            if (dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY || feriados.contains(fecha)) {
                throw new IllegalArgumentException(
                    "Las fechas seleccionadas incluyen fines de semana o feriados."
                );
            }
            fecha = fecha.plusDays(1);
        }
    }

    private int calcularDiasLaborables(LocalDate inicio, LocalDate fin) {
        List<LocalDate> feriados = em.createQuery(
                "SELECT f.fecha FROM FeriadoModel f", LocalDate.class).getResultList();

        int dias = 0;
        LocalDate fecha = inicio;
        while (!fecha.isAfter(fin)) {
            DayOfWeek dia = fecha.getDayOfWeek();
            if (dia != DayOfWeek.SATURDAY && dia != DayOfWeek.SUNDAY && !feriados.contains(fecha)) {
                dias++;
            }
            fecha = fecha.plusDays(1);
        }
        return dias;
    }

    private SolicitudResponseDTO mapearAResponse(Solicitud solicitud, EmpleadoModel empleado) {
        SolicitudResponseDTO dto = new SolicitudResponseDTO();
        dto.setIdSolicitud(solicitud.getIdSolicitud());
        dto.setTipoSolicitud(solicitud.getTipoSolicitud());
        dto.setFechaInicio(solicitud.getFechaInicio());
        dto.setFechaFin(solicitud.getFechaFin());
        dto.setDiasSolicitados(solicitud.getDiasSolicitados());
        dto.setMotivo(solicitud.getMotivo());
        dto.setEstado(solicitud.getEstado());
        dto.setRespuesta(solicitud.getRespuesta());
        dto.setObservacionRrhh(solicitud.getObservacionRrhh());
        dto.setArchivoAdjunto(solicitud.getArchivoAdjunto());
        dto.setFechaRevision(solicitud.getFechaRevision());
        dto.setFechaDecision(solicitud.getFechaDecision());
        dto.setCreadoEl(solicitud.getCreadoEl());
        dto.setIdEmpleado(solicitud.getIdEmpleado());
        if (empleado != null) {
            dto.setNombreEmpleado(empleado.getNombres() + " " + empleado.getApellidos());
        }
        return dto;
    }
}
