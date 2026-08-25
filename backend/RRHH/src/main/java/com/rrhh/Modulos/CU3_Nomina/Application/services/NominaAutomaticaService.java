package com.rrhh.Modulos.CU3_Nomina.Application.services;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Domain.entities.Historial;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Domain.repository.IHistorialRepository;
import com.rrhh.Modulos.CU3_Nomina.Application.dto.CalcularNominaRequestDTO;
import com.rrhh.Modulos.CU3_Nomina.Domain.repository.INominaRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class NominaAutomaticaService {

    private static final ZoneId ZONA_PERU = ZoneId.of("America/Lima");
    private static final Long ID_USUARIO_SISTEMA = 1L;

    private final NominaService nominaService;
    private final INominaRepository nominaRepository;
    private final IHistorialRepository historialRepository;

    public NominaAutomaticaService(NominaService nominaService, INominaRepository nominaRepository,
                                    IHistorialRepository historialRepository) {
        this.nominaService = nominaService;
        this.nominaRepository = nominaRepository;
        this.historialRepository = historialRepository;
    }

    /**
     * Corre el día 1 de cada mes a las 23:50, justo antes de que se cierre la ventana
     * de recálculo del mes anterior. Si RRHH nunca calculó la nómina de ese período,
     * la calcula automáticamente para que no quede sin ningún registro.
     */
    @Scheduled(cron = "${nomina.auto-calculo.cron:0 50 23 1 * *}", zone = "${sistema.zona-horaria:America/Lima}")
    @Transactional
    public void calcularNominaPendiente() {
        LocalDate hoy = LocalDate.now(ZONA_PERU);
        YearMonth mesACerrar = YearMonth.from(hoy).minusMonths(1);
        String periodo = mesACerrar.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        // Si ya existe al menos una nómina para el período, RRHH ya lo calculó
        // (aunque sea parcialmente o con ajustes manuales) — no se toca.
        if (!nominaRepository.buscarPorPeriodo(periodo).isEmpty()) {
            return;
        }

        CalcularNominaRequestDTO request = new CalcularNominaRequestDTO(
                mesACerrar.atDay(1),
                mesACerrar.atEndOfMonth(),
                null,
                null
        );

        try {
            nominaService.calcularNomina(request, ID_USUARIO_SISTEMA);

            Historial log = new Historial(ID_USUARIO_SISTEMA, "CALCULO_NOMINA_AUTOMATICO", "SISTEMA");
            log.setTablaAfectada("NOMINA");
            log.setValorNuevo("Periodo: " + periodo
                    + " | RRHH no calculo la nomina antes del cierre de la ventana; se calculo automaticamente.");
            historialRepository.save(log);
        } catch (Exception e) {
            Historial log = new Historial(ID_USUARIO_SISTEMA, "ERROR_CALCULO_NOMINA_AUTOMATICO", "SISTEMA");
            log.setTablaAfectada("NOMINA");
            log.setValorNuevo("Periodo: " + periodo + " | Error: " + e.getMessage());
            historialRepository.save(log);
        }
    }
}
