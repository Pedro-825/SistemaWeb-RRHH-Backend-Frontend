package com.rrhh.Shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sistema")
public class ConfiguracionSistema {

    private String nombreEmpresa;

    private int maxIntentosLogin;

    private String zonaHoraria;

    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    public void setNombreEmpresa(String nombreEmpresa) {
        this.nombreEmpresa = nombreEmpresa;
    }

    public int getMaxIntentosLogin() {
        return maxIntentosLogin;
    }

    public void setMaxIntentosLogin(int maxIntentosLogin) {
        this.maxIntentosLogin = maxIntentosLogin;
    }

    public String getZonaHoraria() {
        return zonaHoraria;
    }

    public void setZonaHoraria(String zonaHoraria) {
        this.zonaHoraria = zonaHoraria;
    }
}

