package com.rrhh.Modulos.CU5_Reporte.Infrastructure.interfaces;

import com.rrhh.Modulos.CU5_Reporte.Domain.entities.ReporteGenerado;
import com.rrhh.Modulos.CU5_Reporte.Domain.repository.IReporteGeneradoRepository;
import com.rrhh.Shared.persistence.ReporteGeneradoModel;
import com.rrhh.Shared.persistence.UsuarioModel;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
public class ReporteGeneradoRepositoryImpl implements IReporteGeneradoRepository {

    private final JpaReporteGeneradoRepository jpaReporteGeneradoRepository;

    public ReporteGeneradoRepositoryImpl(JpaReporteGeneradoRepository jpaReporteGeneradoRepository) {
        this.jpaReporteGeneradoRepository = jpaReporteGeneradoRepository;
    }

    @Override
    public ReporteGenerado guardar(ReporteGenerado reporte) {
        ReporteGeneradoModel model = mapearAModel(reporte);
        ReporteGeneradoModel guardado = jpaReporteGeneradoRepository.save(Objects.requireNonNull(model));
        reporte.setIdReporte(guardado.getIdReporte());
        return reporte;
    }

    @Override
    public List<ReporteGenerado> buscarTodos() {
        return jpaReporteGeneradoRepository.findAll()
            .stream().map(this::mapearAEntidad).collect(Collectors.toList());
    }

    @Override
    public List<ReporteGenerado> buscarPorTipo(String tipoReporte) {
        return jpaReporteGeneradoRepository.findByTipoReporte(tipoReporte)
            .stream().map(this::mapearAEntidad).collect(Collectors.toList());
    }

    private ReporteGeneradoModel mapearAModel(ReporteGenerado reporte) {
        ReporteGeneradoModel model = new ReporteGeneradoModel();
        model.setTipoReporte(reporte.getTipoReporte());
        model.setFiltrosAplicados(reporte.getFiltrosAplicados());
        model.setTotalRegistros(reporte.getTotalRegistros());
        model.setFormatoExportacion(reporte.getFormatoExportacion());
        model.setCreadoEl(reporte.getCreadoEl());
        if (reporte.getGeneradoPor() != null) {
            UsuarioModel usuario = new UsuarioModel();
            usuario.setIdUsuario(reporte.getGeneradoPor());
            model.setGeneradoPor(usuario);
        }
        return model;
    }

    private ReporteGenerado mapearAEntidad(ReporteGeneradoModel model) {
        ReporteGenerado reporte = new ReporteGenerado();
        reporte.setIdReporte(model.getIdReporte());
        reporte.setTipoReporte(model.getTipoReporte());
        reporte.setFiltrosAplicados(model.getFiltrosAplicados());
        reporte.setTotalRegistros(model.getTotalRegistros());
        reporte.setFormatoExportacion(model.getFormatoExportacion());
        reporte.setCreadoEl(model.getCreadoEl());
        if (model.getGeneradoPor() != null) {
            reporte.setGeneradoPor(model.getGeneradoPor().getIdUsuario());
        }
        return reporte;
    }
}