package com.rrhh.Shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "rrhh")
public class RRHHProperties {

    private BigDecimal salarioMinimo;

    private BigDecimal tasaHoraExtra1_5;

    private BigDecimal tasaHoraExtra2_0;

    private int umbralHorasDobles;

    private BigDecimal bonifFamiliar;
    private BigDecimal rmv;

    private BigDecimal bonifNocturno;

    private BigDecimal bonifGuardia;

    private BigDecimal bonifRiesgo;

    private BigDecimal bonifCargo;

    private BigDecimal tasaDescuentoLey;

    private BigDecimal tasaDescuentoAfp;

    public BigDecimal getSalarioMinimo() {
        return salarioMinimo;
    }

    public void setSalarioMinimo(BigDecimal salarioMinimo) {
        this.salarioMinimo = salarioMinimo;
    }

    public BigDecimal getTasaHoraExtra1_5() {
        return tasaHoraExtra1_5;
    }

    public void setTasaHoraExtra1_5(BigDecimal tasaHoraExtra1_5) {
        this.tasaHoraExtra1_5 = tasaHoraExtra1_5;
    }

    public BigDecimal getTasaHoraExtra2_0() {
        return tasaHoraExtra2_0;
    }

    public void setTasaHoraExtra2_0(BigDecimal tasaHoraExtra2_0) {
        this.tasaHoraExtra2_0 = tasaHoraExtra2_0;
    }

    public int getUmbralHorasDobles() {
        return umbralHorasDobles;
    }

    public void setUmbralHorasDobles(int umbralHorasDobles) {
        this.umbralHorasDobles = umbralHorasDobles;
    }

    public BigDecimal getBonifFamiliar() {
        return bonifFamiliar;
    }

    public void setBonifFamiliar(BigDecimal bonifFamiliar) {
        this.bonifFamiliar = bonifFamiliar;
    }

    public BigDecimal getRmv() { return rmv; }
    public void setRmv(BigDecimal rmv) { this.rmv = rmv; }

    public BigDecimal getBonifNocturno() {
        return bonifNocturno;
    }

    public void setBonifNocturno(BigDecimal bonifNocturno) {
        this.bonifNocturno = bonifNocturno;
    }

    public BigDecimal getBonifGuardia() {
        return bonifGuardia;
    }

    public void setBonifGuardia(BigDecimal bonifGuardia) {
        this.bonifGuardia = bonifGuardia;
    }

    public BigDecimal getBonifRiesgo() {
        return bonifRiesgo;
    }

    public void setBonifRiesgo(BigDecimal bonifRiesgo) {
        this.bonifRiesgo = bonifRiesgo;
    }

    public BigDecimal getBonifCargo() {
        return bonifCargo;
    }

    public void setBonifCargo(BigDecimal bonifCargo) {
        this.bonifCargo = bonifCargo;
    }

    public BigDecimal getTasaDescuentoLey() {
        return tasaDescuentoLey;
    }

    public void setTasaDescuentoLey(BigDecimal tasaDescuentoLey) {
        this.tasaDescuentoLey = tasaDescuentoLey;
    }

    public BigDecimal getTasaDescuentoAfp() {
        return tasaDescuentoAfp;
    }

    public void setTasaDescuentoAfp(BigDecimal tasaDescuentoAfp) {
        this.tasaDescuentoAfp = tasaDescuentoAfp;
    }
}
