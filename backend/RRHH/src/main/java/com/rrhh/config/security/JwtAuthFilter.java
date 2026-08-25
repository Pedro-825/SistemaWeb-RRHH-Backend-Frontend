package com.rrhh.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseCookie;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtConfig jwtConfig;

    public JwtAuthFilter(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String token = null;

        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }

        if (token == null || token.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!jwtConfig.tokenValido(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        String username = jwtConfig.obtenerUsername(token);
        String rol = jwtConfig.obtenerRol(token);
        Long idUsuario = jwtConfig.obtenerIdUsuario(token);
        Long idEmpleado = jwtConfig.obtenerIdEmpleado(token);

        LocalDateTime tokenInvalidoDesde = jwtConfig.obtenerTokenInvalidoDesde(token);
        if (tokenInvalidoDesde != null
                && jwtConfig.obtenerClaims(token).getIssuedAt()
                        .before(java.util.Date.from(
                                tokenInvalidoDesde
                                        .atZone(ZoneId.systemDefault())
                                        .toInstant()
                        ))
        ) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token invalido. El usuario ha sido desactivado.");
            return;
        }

        List<String> permisos = jwtConfig.obtenerPermisos(token);

        if (
                username != null
                && rol != null
                && idUsuario != null
                && SecurityContextHolder.getContext().getAuthentication() == null
        ) {

            AuthenticatedUser authenticatedUser =
                    new AuthenticatedUser(
                            idUsuario,
                            idEmpleado,
                            username,
                            rol
                    );

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            String authorityRole = rol.startsWith("ROLE_") ? rol : "ROLE_" + rol;
            authorities.add(new SimpleGrantedAuthority(authorityRole));

            if (permisos != null) {
                for (String permiso : permisos) {
                    authorities.add(new SimpleGrantedAuthority(permiso));
                }
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            authenticatedUser,
                            null,
                            authorities
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        long tiempoRestante = jwtConfig.tiempoRestanteMs(token);
        if (tiempoRestante > 0 && tiempoRestante < 1000 * 60 * 5) {
            String nuevoToken = jwtConfig.refrescarToken(token);
            ResponseCookie jwtCookie = jwtConfig.crearCookieJwt(nuevoToken);
            response.addHeader("Set-Cookie", jwtCookie.toString());
        }

        filterChain.doFilter(request, response);
    }
}
