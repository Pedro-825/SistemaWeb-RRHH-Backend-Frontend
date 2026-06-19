package com.rrhh.Modulos.CU5_Reporte.Application.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rrhh.Modulos.CU5_Reporte.Application.dto.*;
import com.rrhh.Modulos.CU5_Reporte.Domain.entities.ReporteGenerado;
import com.rrhh.Modulos.CU5_Reporte.Domain.repository.IReporteGeneradoRepository;
import com.rrhh.Modulos.CU5_Reporte.Infrastructure.interfaces.JpaReporteEmpleadoRepository;
import com.rrhh.Modulos.CU5_Reporte.Infrastructure.interfaces.JpaReporteAsistenciaRepository;
import com.rrhh.Modulos.CU5_Reporte.Infrastructure.interfaces.JpaReporteNominaRepository;
import com.rrhh.Modulos.CU5_Reporte.Infrastructure.interfaces.JpaReporteSolicitudRepository;
import com.rrhh.Modulos.CU5_Reporte.Application.factory.IReporteFactory;
import com.rrhh.Shared.export.ExportAbstractFactory;
import com.rrhh.Shared.persistence.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReporteService implements IReporteService {

    private static final Map<String, String> FACTORY_BY_TIPO = Map.of(
        "EMPLEADOS", "gestionEmpleadosExportFactory",
        "ASISTENCIA", "registroAsistenciaExportFactory",
        "NOMINA", "nominaExportFactory",
        "SOLICITUDES", "solicitudExportFactory"
    );

    private final IReporteGeneradoRepository reporteGeneradoRepository;
    private final JpaReporteEmpleadoRepository jpaEmpleadoRepository;
    private final JpaReporteAsistenciaRepository jpaAsistenciaRepository;
    private final JpaReporteNominaRepository jpaNominaRepository;
    private final JpaReporteSolicitudRepository jpaSolicitudRepository;
    private final ObjectMapper objectMapper;
    private final Map<String, ExportAbstractFactory> exportFactories;
    private final IReporteFactory reporteFactory;

    @PersistenceContext
    private EntityManager em;

    public ReporteService(
            IReporteGeneradoRepository reporteGeneradoRepository,
            JpaReporteEmpleadoRepository jpaEmpleadoRepository,
            JpaReporteAsistenciaRepository jpaAsistenciaRepository,
            JpaReporteNominaRepository jpaNominaRepository,
            JpaReporteSolicitudRepository jpaSolicitudRepository,
            ObjectMapper objectMapper,
            Map<String, ExportAbstractFactory> exportFactories,
            IReporteFactory reporteFactory) {
        this.reporteGeneradoRepository = reporteGeneradoRepository;
        this.jpaEmpleadoRepository = jpaEmpleadoRepository;
        this.jpaAsistenciaRepository = jpaAsistenciaRepository;
        this.jpaNominaRepository = jpaNominaRepository;
        this.jpaSolicitudRepository = jpaSolicitudRepository;
        this.objectMapper = objectMapper;
        this.exportFactories = exportFactories;
        this.reporteFactory = reporteFactory;
    }

    @Override
    @Transactional(timeout = 30)
    public ReporteResponseDTO generarReporteEmpleados(FiltroEmpleadoDTO filtro, Long idUsuario) {

        validarFiltroEmpleado(filtro);

        List<EmpleadoModel> empleados = jpaEmpleadoRepository.buscarConFiltros(
            filtro.getEstado(),
            filtro.getArea(),
            filtro.getCargo(),
            filtro.getTipoContrato(),
            filtro.getIdEmpleado()
        );

        if (empleados.isEmpty()) {
            return guardarYRetornar("EMPLEADOS", filtro, java.util.List.of(), idUsuario);
        }

        validarConsistenciaDatos("EMPLEADOS", empleados);

        List<Map<String, Object>> datos = empleados.stream().map(e -> {
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("id", e.getIdEmpleado());
            fila.put("nombres", e.getNombres() + " " + e.getApellidos());
            fila.put("documento", e.getNumeroDi());
            fila.put("correo", e.getCorreo());
            fila.put("estado", e.getEstado());
            fila.put("fechaIngreso", e.getCreadoEl() != null ? e.getCreadoEl().toLocalDate() : null);
            return fila;
        }).collect(Collectors.toList());

        return guardarYRetornar("EMPLEADOS", filtro, datos, idUsuario);
    }

    @Override
    @Transactional(timeout = 30)
    public ReporteResponseDTO generarReporteAsistencia(FiltroAsistenciaDTO filtro, Long idUsuario) {

        validarFiltroFechas(filtro.getFechaInicio(), filtro.getFechaFin());

        List<RegistroAsistenciaModel> registros = jpaAsistenciaRepository.buscarConFiltros(
            filtro.getIdEmpleado(),
            filtro.getArea(),
            filtro.getFechaInicio(),
            filtro.getFechaFin()
        );

        if (registros.isEmpty()) {
            return guardarYRetornar("ASISTENCIA", filtro, java.util.List.of(), idUsuario);
        }

        validarConsistenciaDatos("ASISTENCIA", registros);

        List<Map<String, Object>> datos = registros.stream().map(r -> {
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("empleado", r.getEmpleado() != null ?
                r.getEmpleado().getNombres() + " " + r.getEmpleado().getApellidos() : "");
            fila.put("fecha", r.getFecha());
            fila.put("horaEntrada", r.getHoraEntrada());
            fila.put("horaSalida", r.getHoraSalida());
            fila.put("minutosTardanza", r.getMinutosTardanza());
            fila.put("minutosExtra", r.getMinutosExtra());
            fila.put("estado", r.getEstado());
            fila.put("tipoRegistro", r.getTipoRegistro() != null ? r.getTipoRegistro() : "ORIGINAL");
            return fila;
        }).collect(Collectors.toList());

        return guardarYRetornar("ASISTENCIA", filtro, datos, idUsuario);
    }

    @Override
    @Transactional(timeout = 30)
    public ReporteResponseDTO generarReporteNomina(FiltroNominaDTO filtro, Long idUsuario) {

        validarFiltroFechas(filtro.getFechaInicio(), filtro.getFechaFin());

        List<NominaModel> nominas = jpaNominaRepository.buscarConFiltros(
            filtro.getIdEmpleado(),
            filtro.getArea(),
            filtro.getFechaInicio(),
            filtro.getFechaFin(),
            filtro.getSalarioMin(),
            filtro.getSalarioMax()
        );

        if (nominas.isEmpty()) {
            return guardarYRetornar("NOMINA", filtro, java.util.List.of(), idUsuario);
        }

        validarConsistenciaDatos("NOMINA", nominas);

        List<Map<String, Object>> datos = nominas.stream().map(n -> {
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("empleado", n.getEmpleado() != null ?
                n.getEmpleado().getNombres() + " " + n.getEmpleado().getApellidos() : "");
            fila.put("periodo", n.getPeriodo());
            fila.put("sueldoBase", n.getSueldoBase());
            fila.put("horasExtra", n.getTotalHorasExtra());
            fila.put("bonificaciones", sumaBonificaciones(n));
            fila.put("descuentos", n.getDescuentoTardanzas());
            fila.put("sueldoBruto", n.getSueldoBruto());
            fila.put("sueldoNeto", n.getSueldoNeto());
            fila.put("estado", n.getEstadoPago());
            return fila;
        }).collect(Collectors.toList());

        return guardarYRetornar("NOMINA", filtro, datos, idUsuario);
    }

    @Override
    @Transactional(timeout = 30)
    public ReporteResponseDTO generarReporteSolicitudes(FiltroSolicitudDTO filtro, Long idUsuario) {

        validarFiltroFechas(filtro.getFechaInicio(), filtro.getFechaFin());

        List<SolicitudModel> solicitudes = jpaSolicitudRepository.buscarConFiltros(
            filtro.getIdEmpleado(),
            filtro.getArea(),
            filtro.getTipoSolicitud(),
            filtro.getEstado(),
            filtro.getCargo(),
            filtro.getFechaInicio(),
            filtro.getFechaFin()
        );

        if (solicitudes.isEmpty()) {
            return guardarYRetornar("SOLICITUDES", filtro, java.util.List.of(), idUsuario);
        }

        validarConsistenciaDatos("SOLICITUDES", solicitudes);

        List<Map<String, Object>> datos = solicitudes.stream().map(s -> {
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("empleado", s.getEmpleado() != null ?
                s.getEmpleado().getNombres() + " " + s.getEmpleado().getApellidos() : "");
            fila.put("tipoSolicitud", s.getTipoSolicitud());
            fila.put("fechaInicio", s.getFechaInicio());
            fila.put("fechaFin", s.getFechaFin());
            fila.put("diasSolicitados", s.getDiasSolicitados());
            fila.put("estado", s.getEstado());
            fila.put("motivo", s.getMotivo());
            return fila;
        }).collect(Collectors.toList());

        return guardarYRetornar("SOLICITUDES", filtro, datos, idUsuario);
    }

    // ================================
    // METODOS PRIVADOS
    // ================================

    private void validarFiltroEmpleado(FiltroEmpleadoDTO filtro) {
        if (filtro == null) {
            throw new IllegalArgumentException(
                "Los filtros ingresados no son validos, verifique la informacion."
            );
        }
    }

    private void validarFiltroFechas(java.time.LocalDate inicio, java.time.LocalDate fin) {
        if (inicio != null && fin != null && inicio.isAfter(fin)) {
            throw new IllegalArgumentException(
                "Los filtros ingresados no son validos, verifique la informacion."
            );
        }
    }

    private void validarConsistenciaDatos(String tipoReporte, List<?> datos) {
        if (datos == null || datos.isEmpty()) {
            return;
        }

        int size = datos.size();

        String tabla = switch (tipoReporte) {
            case "EMPLEADOS" -> "empleado";
            case "ASISTENCIA" -> "registro_asistencia";
            case "NOMINA" -> "nomina";
            case "SOLICITUDES" -> "solicitud";
            default -> null;
        };

        if (tabla != null) {
            try {
                Number dbCount = (Number) em.createNativeQuery(
                        "SELECT COUNT(*) FROM " + tabla).getSingleResult();
                if (size > dbCount.intValue()) {
                    throw new IllegalStateException("Error de consistencia en reporte " + tipoReporte +
                            ": " + size + " registros reportados superan los " + dbCount.intValue() +
                            " registros en la base de datos.");
                }
            } catch (Exception ignore) {
                // Si falla la consulta de conteo, se omite la validación
            }
        }
    }

    private java.math.BigDecimal sumaBonificaciones(NominaModel n) {
        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        if (n.getBonifFamiliar() != null) total = total.add(n.getBonifFamiliar());
        if (n.getBonifTurnoNocturno() != null) total = total.add(n.getBonifTurnoNocturno());
        if (n.getBonifGuardia() != null) total = total.add(n.getBonifGuardia());
        if (n.getBonifRiesgo() != null) total = total.add(n.getBonifRiesgo());
        if (n.getBonifCargo() != null) total = total.add(n.getBonifCargo());
        return total;
    }

    private ReporteResponseDTO guardarYRetornar(
            String tipo, Object filtro,
            List<Map<String, Object>> datos, Long idUsuario) {
        return guardarYRetornar(tipo, filtro, datos, idUsuario, null);
    }

    private ReporteResponseDTO guardarYRetornar(
            String tipo, Object filtro,
            List<Map<String, Object>> datos, Long idUsuario, String formato) {

        ReporteGenerado reporte = reporteFactory.crearReporteBase();
        reporte.setTipoReporte(tipo);
        reporte.setTotalRegistros(datos.size());
        reporte.setGeneradoPor(idUsuario);
        reporte.setFormatoExportacion(formato);
        reporte.setCreadoEl(LocalDateTime.now());
        try {
            reporte.setFiltrosAplicados(objectMapper.writeValueAsString(filtro));
        } catch (Exception e) {
            reporte.setFiltrosAplicados("{}");
        }
        ReporteGenerado guardado = reporteGeneradoRepository.guardar(reporte);

        ReporteResponseDTO response = new ReporteResponseDTO();
        response.setIdReporte(guardado.getIdReporte());
        response.setTipoReporte(tipo);
        response.setTotalRegistros(datos.size());
        response.setGeneradoEl(guardado.getCreadoEl());
        response.setDatos(datos);
        return response;
    }

    @Override
    public byte[] exportarReporte(String tipo, String formato,
                                   List<Map<String, Object>> datos) {
        String factoryBeanName = FACTORY_BY_TIPO.getOrDefault(tipo, tipo.toLowerCase() + "ExportFactory");
        ExportAbstractFactory factory = exportFactories.get(factoryBeanName);
        if (factory == null) {
            throw new IllegalArgumentException("No existe fabrica de exportacion para: " + tipo);
        }

        String titulo = "Reporte de " + tipo;

        if ("PDF".equalsIgnoreCase(formato)) {
            return factory.createPDFExporter().export(titulo, datos, factory.getHeaders(), factory.getKeys());
        }
        if ("EXCEL".equalsIgnoreCase(formato)) {
            return factory.createExcelExporter().export(titulo, datos, factory.getHeaders(), factory.getKeys());
        }
        if ("CSV".equalsIgnoreCase(formato)) {
            return factory.createCSVExporter().export(titulo, datos, factory.getHeaders(), factory.getKeys());
        }
        throw new IllegalArgumentException("Formato no soportado: " + formato + ". Use PDF, EXCEL o CSV.");
    }
}
