package com.rrhh.Modulos.CU5_Reporte.Domain.entities;

import java.time.LocalDateTime;

import com.rrhh.Shared.prototype.Prototype;

import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteGenerado implements Prototype<ReporteGenerado> {

    private Integer idReporte;
    private String tipoReporte;
    private String filtrosAplicados;
    private Integer totalRegistros;
    private String formatoExportacion;
    private Long generadoPor;
    private LocalDateTime creadoEl;

    public Integer getIdReporte() { return idReporte; }
    public void setIdReporte(Integer idReporte) { this.idReporte = idReporte; }

    public String getTipoReporte() { return tipoReporte; }
    public void setTipoReporte(String tipoReporte) { this.tipoReporte = tipoReporte; }

    public String getFiltrosAplicados() { return filtrosAplicados; }
    public void setFiltrosAplicados(String filtrosAplicados) { this.filtrosAplicados = filtrosAplicados; }

    public Integer getTotalRegistros() { return totalRegistros; }
    public void setTotalRegistros(Integer totalRegistros) { this.totalRegistros = totalRegistros; }

    public String getFormatoExportacion() { return formatoExportacion; }
    public void setFormatoExportacion(String formatoExportacion) { this.formatoExportacion = formatoExportacion; }

    public Long getGeneradoPor() { return generadoPor; }
    public void setGeneradoPor(Long generadoPor) { this.generadoPor = generadoPor; }

    public LocalDateTime getCreadoEl() { return creadoEl; }
    public void setCreadoEl(LocalDateTime creadoEl) { this.creadoEl = creadoEl; }

    @Override
    public ReporteGenerado clonePrototype() {
        ReporteGenerado clon = new ReporteGenerado();
        clon.setTipoReporte(this.tipoReporte);
        clon.setFormatoExportacion(this.formatoExportacion);
        return clon;
    }
}