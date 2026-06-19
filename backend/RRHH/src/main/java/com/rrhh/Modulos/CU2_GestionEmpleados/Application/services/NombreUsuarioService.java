package com.rrhh.Modulos.CU2_GestionEmpleados.Application.services;

import org.springframework.stereotype.Service;

import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IUsuarioGestionRepository;

@Service
public class NombreUsuarioService {

    private final IUsuarioGestionRepository usuarioRepository;

    public NombreUsuarioService(IUsuarioGestionRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    private static final String DOMINIO_HOSPITAL = "@hospitalsangabriel.com";

    public String generarNombreUsuarioUnico(String nombres, String apellidos) {

        String primerNombre = obtenerPalabra(nombres, 0);
        String segundoNombre = obtenerPalabra(nombres, 1);

        String primerApellido = obtenerPalabra(apellidos, 0);
        String segundoApellido = obtenerPalabra(apellidos, 1);

        String usernameBase1 =
                limpiarEspacios(primerNombre)
                        + "."
                        + limpiarEspacios(primerApellido);

        if (!usuarioRepository.existsByNombreUsuario(usernameBase1)) {
            return usernameBase1;
        }

        String usernameBase2 =
                limpiarEspacios(primerNombre)
                        + "."
                        + limpiarEspacios(primerApellido)
                        + limpiarEspacios(segundoApellido);

        if (!segundoApellido.isBlank()
                && !usuarioRepository.existsByNombreUsuario(usernameBase2)) {
            return usernameBase2;
        }

        String usernameBase3 =
                limpiarEspacios(primerNombre)
                        + limpiarEspacios(segundoNombre)
                        + "."
                        + limpiarEspacios(primerApellido)
                        + limpiarEspacios(segundoApellido);

        if (!segundoNombre.isBlank()
                && !segundoApellido.isBlank()
                && !usuarioRepository.existsByNombreUsuario(usernameBase3)) {
            return usernameBase3;
        }

        String usernameFinal =
                !segundoNombre.isBlank() && !segundoApellido.isBlank()
                        ? usernameBase3
                        : usernameBase2;

        int contador = 1;

        while (usuarioRepository.existsByNombreUsuario(usernameFinal + contador)) {
            contador++;
        }

        return usernameFinal + contador;
    }

    public String generarCorreoInstitucional(String nombreUsuario) {

        String correoBase =
                normalizarParaCorreo(nombreUsuario)
                        + DOMINIO_HOSPITAL;

        String correoFinal = correoBase;

        int contador = 1;

        while (usuarioRepository.existsByCorreoInst(correoFinal)) {
            correoFinal =
                    normalizarParaCorreo(nombreUsuario)
                            + contador
                            + DOMINIO_HOSPITAL;

            contador++;
        }

        return correoFinal;
    }

    private String obtenerPalabra(String texto, int posicion) {

        if (texto == null || texto.isBlank()) {
            return "";
        }

        String[] partes = texto.trim().split("\\s+");

        if (posicion >= partes.length) {
            return "";
        }

        return partes[posicion].toLowerCase();
    }

    private String limpiarEspacios(String texto) {

        if (texto == null) {
            return "";
        }

        return texto
                .trim()
                .toLowerCase()
                .replaceAll("\\s+", "")
                .replaceAll("[^a-zA-ZáéíóúÁÉÍÓÚñÑ0-9]", "");
    }

    private String normalizarParaCorreo(String texto) {

        if (texto == null) {
            return "";
        }

        String normalizado = java.text.Normalizer.normalize(
                texto,
                java.text.Normalizer.Form.NFD
        );

        normalizado = normalizado.replaceAll("\\p{M}", "");

        return normalizado
                .toLowerCase()
                .replace("ñ", "n")
                .replaceAll("[^a-z0-9.]", "");
    }
}