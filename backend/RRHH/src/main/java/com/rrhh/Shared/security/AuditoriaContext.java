package com.rrhh.Shared.security;

import com.rrhh.config.security.AuthenticatedUser;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class AuditoriaContext {

    public Long obtenerIdUsuarioActual() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return 1L;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof AuthenticatedUser user) {
            return user.getIdUsuario();
        }

        return 1L;
    }

    public String obtenerIpActual() {

        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            return "SISTEMA";
        }

        HttpServletRequest request = attributes.getRequest();

        String ip = request.getHeader("X-Forwarded-For");

        if (ip != null && !ip.isBlank()) {
            return ip.split(",")[0].trim();
        }

        ip = request.getHeader("X-Real-IP");

        if (ip != null && !ip.isBlank()) {
            return ip.trim();
        }

        return request.getRemoteAddr();
    }
}