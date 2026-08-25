package com.rrhh.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Component
public class JwtConfig {

    private final JwtProperties jwtProperties;

    @Value("${cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${cookie.same-site:Lax}")
    private String cookieSameSite;

    private final long expirationTime = 1000 * 60 * 15;

    public long getExpirationTime() {
        return expirationTime;
    }

    public JwtConfig(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    private Key getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generarToken(
            String username,
            String rol,
            Long idUsuario,
            Long idEmpleado,
            List<String> permisos,
            LocalDateTime tokenInvalidoDesde
    ) {

        return Jwts.builder()
                .setSubject(username)
                .claim("rol", rol)
                .claim("idUsuario", idUsuario)
                .claim("idEmpleado", idEmpleado)
                .claim("permisos", permisos)
                .claim("tokenInvalidoDesde", tokenInvalidoDesde != null ? tokenInvalidoDesde.toString() : null)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims obtenerClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String obtenerUsername(String token) {
        return obtenerClaims(token).getSubject();
    }

    public String obtenerRol(String token) {
        return obtenerClaims(token).get("rol", String.class);
    }

    public Long obtenerIdUsuario(String token) {
        Number id = obtenerClaims(token).get("idUsuario", Number.class);

        if (id == null) {
            return null;
        }

        return id.longValue();
    }

    public Long obtenerIdEmpleado(String token) {
        Number id = obtenerClaims(token).get("idEmpleado", Number.class);

        if (id == null) {
            return null;
        }

        return id.longValue();
    }

    public boolean tokenValido(String token) {
        try {
            Claims claims = obtenerClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> obtenerPermisos(String token) {
        return obtenerClaims(token).get("permisos", List.class);
    }

    public LocalDateTime obtenerTokenInvalidoDesde(String token) {
        String valor = obtenerClaims(token).get("tokenInvalidoDesde", String.class);
        return valor != null ? LocalDateTime.parse(valor) : null;
    }

    public long tiempoRestanteMs(String token) {
        try {
            Claims claims = obtenerClaims(token);
            return claims.getExpiration().getTime() - System.currentTimeMillis();
        } catch (Exception e) {
            return 0;
        }
    }

    public String refrescarToken(String token) {
        Claims claims = obtenerClaims(token);
        String username = claims.getSubject();
        String rol = claims.get("rol", String.class);
        Number id = claims.get("idUsuario", Number.class);
        Number idEmp = claims.get("idEmpleado", Number.class);
        List<String> permisos = obtenerPermisos(token);
        String inv = claims.get("tokenInvalidoDesde", String.class);
        LocalDateTime tokenInvalidoDesde = inv != null ? LocalDateTime.parse(inv) : null;

        Long idUsuario = id != null ? id.longValue() : null;
        Long idEmpleado = idEmp != null ? idEmp.longValue() : null;

        return generarToken(username, rol, idUsuario, idEmpleado, permisos, tokenInvalidoDesde);
    }

    public ResponseCookie crearCookieJwt(String token) {
        return ResponseCookie.from("jwt", Objects.requireNonNull(token))
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/")
                .maxAge(expirationTime / 1000)
                .build();
    }

    public ResponseCookie crearCookieJwtVacia() {
        return ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/")
                .maxAge(0)
                .build();
    }
}
