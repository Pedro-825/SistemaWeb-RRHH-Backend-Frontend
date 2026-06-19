package com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services;

import com.rrhh.Modulos.CU4_RegistroAsistencia.Infrastructure.interfaces.JpaRegistroAsistenciaNominaRepository;
import com.rrhh.Shared.persistence.RegistroAsistenciaModel;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CierreJornadaService {

    private final JpaRegistroAsistenciaNominaRepository repository;

    public CierreJornadaService(JpaRegistroAsistenciaNominaRepository repository) {
        this.repository = repository;
    }

    @Scheduled(cron = "0 0 22 * * *")
    public void cerrarJornadasAbiertas() {
        LocalDate hoy = LocalDate.now();
        LocalTime horaCierre = LocalTime.of(22, 0);

        List<RegistroAsistenciaModel> abiertos =
                repository.findByFechaAndHoraSalidaIsNullAndTipoRegistro(hoy, "ORIGINAL");

        List<RegistroAsistenciaModel> cerrados = new ArrayList<>();

        for (RegistroAsistenciaModel r : abiertos) {
            if (r.getHoraEntrada() != null) {
                long minutos = Duration.between(r.getHoraEntrada(), horaCierre).toMinutes();
                if (minutos > 0) {
                    r.setHoraSalida(horaCierre);
                    BigDecimal horas = BigDecimal.valueOf(minutos)
                            .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
                    r.setHorasTrabajadas(horas);
                    r.setTipoUltimoRegistro("CIERRE_AUTOMATICO");
                    r.setObservacion("Jornada cerrada automaticamente. Salida no registrada.");
                    cerrados.add(r);
                }
            }
        }

        if (!cerrados.isEmpty()) {
            repository.saveAll(cerrados);
        }
    }
}
