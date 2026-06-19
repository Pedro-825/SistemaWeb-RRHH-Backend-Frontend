package com.rrhh.Shared.prototype;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Domain.entities.Usuario;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Contrato;
import com.rrhh.Modulos.CU3_Nomina.Domain.entities.Nomina;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Domain.entities.RegistroAsistencia;
import com.rrhh.Modulos.CU5_Reporte.Domain.entities.ReporteGenerado;
import com.rrhh.Modulos.CU6_Solicitud.Domain.entities.Solicitud;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class PrototypeConfig {

    private final PrototypeRegistry registry;

    public PrototypeConfig(PrototypeRegistry registry) {
        this.registry = registry;
    }

    @PostConstruct
    public void init() {
        Usuario usuarioEmpleado = new Usuario();
        usuarioEmpleado.setActivo(true);
        usuarioEmpleado.setDosFAActivado(false);
        usuarioEmpleado.setNombreRol("EMPLEADO");
        registry.register("usuario_empleado", usuarioEmpleado);

        Contrato contratoIndefinido = new Contrato();
        contratoIndefinido.setTipoContrato("INDEFINIDO");
        contratoIndefinido.setEstado("ACTIVO");
        registry.register("contrato_indefinido", contratoIndefinido);

        Contrato contratoPracticas = new Contrato();
        contratoPracticas.setTipoContrato("PRACTICAS");
        contratoPracticas.setEstado("ACTIVO");
        registry.register("contrato_practicas", contratoPracticas);

        Nomina nominaBase = new Nomina();
        nominaBase.setEstadoPago("CALCULADA");
        nominaBase.setSueldoBase(BigDecimal.ZERO);
        registry.register("nomina_base", nominaBase);

        RegistroAsistencia asistenciaPuntual = new RegistroAsistencia();
        asistenciaPuntual.setEstado("PUNTUAL");
        asistenciaPuntual.setMinutosTardanza(0);
        registry.register("asistencia_puntual", asistenciaPuntual);

        ReporteGenerado reporteBase = new ReporteGenerado();
        reporteBase.setTotalRegistros(0);
        registry.register("reporte_base", reporteBase);

        Solicitud solicitudVacaciones = new Solicitud();
        solicitudVacaciones.setTipoSolicitud("VACACIONES");
        solicitudVacaciones.setEstado("PENDIENTE");
        solicitudVacaciones.setDiasSolicitados(30);
        registry.register("solicitud_vacaciones", solicitudVacaciones);

        Solicitud solicitudPermiso = new Solicitud();
        solicitudPermiso.setTipoSolicitud("PERMISO_PERSONAL");
        solicitudPermiso.setEstado("PENDIENTE");
        solicitudPermiso.setDiasSolicitados(1);
        registry.register("solicitud_permiso", solicitudPermiso);
    }
}
