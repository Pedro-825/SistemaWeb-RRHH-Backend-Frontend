package com.rrhh.Modulos.CU3_Nomina;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Domain.repository.IHistorialRepository;
import com.rrhh.Modulos.CU3_Nomina.Application.dto.CalcularNominaRequestDTO;
import com.rrhh.Modulos.CU3_Nomina.Application.dto.NominaResponseDTO;
import com.rrhh.Modulos.CU3_Nomina.Application.factory.NominaFactory;
import com.rrhh.Modulos.CU3_Nomina.Application.services.NominaService;
import com.rrhh.Modulos.CU3_Nomina.Domain.entities.Nomina;
import com.rrhh.Modulos.CU3_Nomina.Domain.repository.IDetalleNominaRepository;
import com.rrhh.Modulos.CU3_Nomina.Domain.repository.INominaRepository;
import com.rrhh.Modulos.CU3_Nomina.Infrastructure.export.NominaComprobantePDFGenerator;
import com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaContratoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaEmpleadoDerechoHabientesRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaEmpleadoRepository;
import com.rrhh.Modulos.CU3_Nomina.Infrastructure.interfaces.JpaFamiliaInfoRepository;
import com.rrhh.Modulos.CU3_Nomina.Infrastructure.interfaces.JpaRegistroAsistenciaRepository;
import com.rrhh.Shared.config.RRHHProperties;
import com.rrhh.Shared.persistence.ContratoModel;
import com.rrhh.Shared.persistence.EmpleadoModel;
import com.rrhh.Shared.persistence.FamiliaInfoModel;
import com.rrhh.Shared.persistence.RegistroAsistenciaModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NominaServiceUpsertTest {

    @Mock INominaRepository nominaRepository;
    @Mock IDetalleNominaRepository detalleNominaRepository;
    @Mock IHistorialRepository historialRepository;
    @Mock NominaComprobantePDFGenerator comprobantePDFGenerator;
    @Mock JpaEmpleadoRepository jpaEmpleadoRepository;
    @Mock JpaRegistroAsistenciaRepository jpaRegistroAsistenciaRepository;
    @Mock JpaContratoRepository jpaContratoRepository;
    @Mock JpaFamiliaInfoRepository jpaFamiliaInfoRepository;
    @Mock JpaEmpleadoDerechoHabientesRepository jpaEmpleadoDerechoHabientesRepository;

    NominaService service;

    private static final Long EMPLEADO_ID = 1L;
    private static final Long USUARIO_ID  = 99L;

    // Período de prueba: abril 2026 (mes pasado respecto a 2026-06 no aplica — usamos mes anterior)
    private static final LocalDate INICIO_ABRIL = LocalDate.of(2025, 4, 1);
    private static final LocalDate FIN_ABRIL    = LocalDate.of(2025, 4, 30);
    private static final String PERIODO_ABRIL   = "2025-04";

    private static final LocalDate INICIO_MAYO = LocalDate.of(2025, 5, 1);
    private static final LocalDate FIN_MAYO    = LocalDate.of(2025, 5, 31);
    private static final String PERIODO_MAYO   = "2025-05";

    @BeforeEach
    void setUp() {
        RRHHProperties props = new RRHHProperties();
        props.setSalarioMinimo(new BigDecimal("1025.00"));
        props.setTasaHoraExtra1_5(new BigDecimal("1.5"));
        props.setTasaHoraExtra2_0(new BigDecimal("2.0"));
        props.setUmbralHorasDobles(2);
        props.setRmv(new BigDecimal("1025.00"));
        props.setBonifFamiliar(new BigDecimal("0.10"));
        props.setBonifNocturno(new BigDecimal("0.35"));
        props.setBonifGuardia(new BigDecimal("10.00"));
        props.setBonifRiesgo(new BigDecimal("0.10"));
        props.setBonifCargo(new BigDecimal("0.10"));
        props.setTasaDescuentoLey(new BigDecimal("0.13"));
        props.setTasaDescuentoAfp(new BigDecimal("0.10"));

        service = new NominaService(
                props,
                nominaRepository,
                detalleNominaRepository,
                historialRepository,
                new NominaFactory(),
                comprobantePDFGenerator,
                jpaEmpleadoRepository,
                jpaRegistroAsistenciaRepository,
                jpaContratoRepository,
                jpaFamiliaInfoRepository,
                jpaEmpleadoDerechoHabientesRepository
        );
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private EmpleadoModel empleadoActivo() {
        EmpleadoModel e = new EmpleadoModel();
        e.setIdEmpleado(EMPLEADO_ID);
        e.setNombres("Juan");
        e.setApellidos("Perez");
        e.setEstado("ACTIVO");
        return e;
    }

    private ContratoModel contratoActivo() {
        ContratoModel c = new ContratoModel();
        c.setEmpleado(empleadoActivo());
        c.setSueldo(new BigDecimal("2000.00"));
        c.setTipoPension("ONP");
        return c;
    }

    private RegistroAsistenciaModel registroNormal(LocalDate fecha) {
        RegistroAsistenciaModel r = new RegistroAsistenciaModel();
        r.setFecha(fecha);
        r.setHoraEntrada(LocalTime.of(8, 0));
        r.setHoraSalida(LocalTime.of(17, 0));
        r.setMinutosTardanza(0);
        r.setMinutosExtra(0);
        r.setTipoRegistro("ORIGINAL");
        EmpleadoModel emp = new EmpleadoModel();
        emp.setIdEmpleado(EMPLEADO_ID);
        r.setEmpleado(emp);
        return r;
    }

    /** Configura mocks comunes para calcularNomina en un período dado */
    private void configurarMocksCalculo(LocalDate inicio, LocalDate fin, String periodo) {
        EmpleadoModel emp = empleadoActivo();
        List<Long> idsEmpleados = List.of(EMPLEADO_ID);
        when(jpaEmpleadoRepository.findByEstado("ACTIVO")).thenReturn(List.of(emp));
        when(jpaRegistroAsistenciaRepository
                .findByEmpleadoIdEmpleadoInAndFechaBetween(eq(idsEmpleados), eq(inicio), eq(fin)))
                .thenReturn(List.of(registroNormal(inicio)));
        when(jpaContratoRepository
                .findByEmpleadoIdEmpleadoInAndEstado(eq(idsEmpleados), eq("ACTIVO")))
                .thenReturn(List.of(contratoActivo()));
        when(jpaFamiliaInfoRepository.findByEmpleadoIdEmpleadoInAndActivoTrue(eq(idsEmpleados)))
                .thenReturn(List.of());
        when(jpaEmpleadoDerechoHabientesRepository.findByEmpleadoIdEmpleadoInAndActivoTrue(eq(idsEmpleados)))
                .thenReturn(List.of());
        doNothing().when(historialRepository).save(any());
        lenient().doNothing().when(detalleNominaRepository).eliminarPorNomina(anyInt());
        lenient().when(detalleNominaRepository.guardarTodos(anyList())).thenReturn(List.of());
    }

    /** Simula que el repositorio guarda y asigna el id dado */
    private void configurarGuardarConId(Integer idNomina) {
        when(nominaRepository.guardar(any(Nomina.class))).thenAnswer(inv -> {
            Nomina n = inv.getArgument(0);
            return copiaGuardada(n, idNomina);
        });
    }

    private Nomina copiaGuardada(Nomina n, Integer idNomina) {
        return new Nomina(
                idNomina,
                n.getPeriodo(),
                n.getFechaInicio(),
                n.getFechaFin(),
                n.getFechaEmision(),
                n.getSueldoContrato(),
                n.getSueldoBase(),
                n.getTotalHorasTrabajadas(),
                n.getTotalHorasExtra(),
                n.getTotalMinutosTardanza(),
                n.getBonifFamiliar(),
                n.getBonifTurnoNocturno(),
                n.getBonifGuardia(),
                n.getBonifRiesgo(),
                n.getBonifCargo(),
                n.getDescuentoTardanzas(),
                n.getDescuentoLey(),
                n.getAsignacionFamiliar(),
                n.getSueldoBruto(),
                n.getSueldoNeto(),
                n.getCantidadHijos(),
                n.getTieneHijos(),
                n.getCantidadGuardias(),
                n.getTotalHorasNocturnas(),
                n.getTipoPensionAplicada(),
                n.getEstadoPago(),
                n.getVersion(),
                n.getNominaAnteriorId(),
                n.getIdEmpleado(),
                n.getCalculadoPor(),
                n.getEsEmpleadoClinico(),
                n.getTieneHorarioNocturno(),
                n.getDetalles()
        );
    }

    // ---------------------------------------------------------------
    // Test 1: primer cálculo → crea 1 nómina por empleado (versión 1)
    // ---------------------------------------------------------------

    @Test
    void primerCalculo_creaUnaNominaPorEmpleado_conVersion1() {
        configurarMocksCalculo(INICIO_ABRIL, FIN_ABRIL, PERIODO_ABRIL);
        when(nominaRepository.buscarPorEmpleadoYPeriodo(EMPLEADO_ID, PERIODO_ABRIL))
                .thenReturn(List.of());
        configurarGuardarConId(101);

        CalcularNominaRequestDTO request = new CalcularNominaRequestDTO(
                INICIO_ABRIL, FIN_ABRIL, null, null);
        List<NominaResponseDTO> resultados = service.calcularNomina(request, USUARIO_ID);

        assertThat(resultados).hasSize(1);
        assertThat(resultados.get(0).getIdNomina()).isEqualTo(101);

        ArgumentCaptor<Nomina> captor = ArgumentCaptor.forClass(Nomina.class);
        verify(nominaRepository).guardar(captor.capture());
        assertThat(captor.getValue().getVersion()).isEqualTo(1);
        assertThat(captor.getValue().getIdNomina()).isNull();
    }

    // ---------------------------------------------------------------
    // Test 2: segundo cálculo del mismo período → conserva el mismo ID
    // ---------------------------------------------------------------

    @Test
    void segundoCalculo_mismoPeriodo_conservaIdNominaEIncrementaVersion() {
        configurarMocksCalculo(INICIO_ABRIL, FIN_ABRIL, PERIODO_ABRIL);

        Nomina existente = new Nomina();
        existente.setIdNomina(101);
        existente.setVersion(1);
        when(nominaRepository.buscarPorEmpleadoYPeriodo(EMPLEADO_ID, PERIODO_ABRIL))
                .thenReturn(List.of(existente));
        configurarGuardarConId(101);

        CalcularNominaRequestDTO request = new CalcularNominaRequestDTO(
                INICIO_ABRIL, FIN_ABRIL, null, null);
        List<NominaResponseDTO> resultados = service.calcularNomina(request, USUARIO_ID);

        assertThat(resultados).hasSize(1);
        assertThat(resultados.get(0).getIdNomina()).isEqualTo(101);

        ArgumentCaptor<Nomina> captor = ArgumentCaptor.forClass(Nomina.class);
        verify(nominaRepository).guardar(captor.capture());
        assertThat(captor.getValue().getIdNomina()).isEqualTo(101);
        assertThat(captor.getValue().getVersion()).isEqualTo(2);
    }

    // ---------------------------------------------------------------
    // Test 3: cálculo de otro período → crea un ID nuevo (idNomina null al guardar)
    // ---------------------------------------------------------------

    @Test
    void calculoPeriodoDiferente_creaIdNuevoSinReutilizar() {
        configurarMocksCalculo(INICIO_MAYO, FIN_MAYO, PERIODO_MAYO);
        when(nominaRepository.buscarPorEmpleadoYPeriodo(EMPLEADO_ID, PERIODO_MAYO))
                .thenReturn(List.of());
        configurarGuardarConId(202);

        CalcularNominaRequestDTO request = new CalcularNominaRequestDTO(
                INICIO_MAYO, FIN_MAYO, null, null);
        List<NominaResponseDTO> resultados = service.calcularNomina(request, USUARIO_ID);

        assertThat(resultados).hasSize(1);
        assertThat(resultados.get(0).getIdNomina()).isEqualTo(202);

        ArgumentCaptor<Nomina> captor = ArgumentCaptor.forClass(Nomina.class);
        verify(nominaRepository).guardar(captor.capture());
        assertThat(captor.getValue().getIdNomina()).isNull();
        assertThat(captor.getValue().getVersion()).isEqualTo(1);
    }

    // ---------------------------------------------------------------
    // Test 4: el repositorio rechaza duplicados de empleado+período
    //         (simula la excepción que lanzaría la restricción UNIQUE de BD)
    // ---------------------------------------------------------------

    @Test
    void repositorio_rechazaDuplicadoEmpleadoPeriodo_lanzaExcepcion() {
        configurarMocksCalculo(INICIO_ABRIL, FIN_ABRIL, PERIODO_ABRIL);
        when(nominaRepository.buscarPorEmpleadoYPeriodo(EMPLEADO_ID, PERIODO_ABRIL))
                .thenReturn(List.of());
        when(nominaRepository.guardar(any(Nomina.class)))
                .thenThrow(new DataIntegrityViolationException(
                        "duplicate key value violates unique constraint \"uq_nomina_empleado_periodo\""));

        CalcularNominaRequestDTO request = new CalcularNominaRequestDTO(
                INICIO_ABRIL, FIN_ABRIL, null, null);

        assertThatThrownBy(() -> service.calcularNomina(request, USUARIO_ID))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("uq_nomina_empleado_periodo");
    }

    // ---------------------------------------------------------------
    // Test 5: buscarPorPeriodo devuelve solo las nóminas del período exacto
    // ---------------------------------------------------------------

    @Test
    void buscarPorPeriodo_devuelveSoloNominasDelPeriodoExacto() {
        Nomina nominaAbril = new Nomina();
        nominaAbril.setIdNomina(101);
        nominaAbril.setIdEmpleado(EMPLEADO_ID);

        when(nominaRepository.buscarPorPeriodo(PERIODO_ABRIL)).thenReturn(List.of(nominaAbril));
        when(jpaEmpleadoRepository.findAllById(List.of(EMPLEADO_ID))).thenReturn(List.of(empleadoActivo()));

        List<NominaResponseDTO> resultados = service.buscarPorPeriodo(PERIODO_ABRIL);

        assertThat(resultados).hasSize(1);
        assertThat(resultados.get(0).getIdNomina()).isEqualTo(101);
        verify(nominaRepository).buscarPorPeriodo(PERIODO_ABRIL);
        verify(nominaRepository, never()).buscarPorPeriodo(PERIODO_MAYO);
    }
}
