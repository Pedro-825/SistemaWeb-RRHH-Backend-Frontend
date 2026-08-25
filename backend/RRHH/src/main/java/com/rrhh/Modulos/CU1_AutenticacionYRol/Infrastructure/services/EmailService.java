package com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import jakarta.mail.internet.MimeMessage;

import java.util.Objects;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final String APP_DOWNLOAD_URL_DEFAULT =
            "https://drive.google.com/uc?export=download&id=1YzmCYq-r6BI3lXNtgYkcqp2wgeY3dIuA";

    private final JavaMailSender mailSender;

    @Value("${app.mobile.download-url:" + APP_DOWNLOAD_URL_DEFAULT + "}")
    private String appDownloadUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarCorreoRecuperacion(
            String destinatario,
            String nombreEmpleado,
            String linkRecuperacion
    ) {

        SimpleMailMessage mensaje = new SimpleMailMessage();

        mensaje.setTo(destinatario);
        mensaje.setSubject("Recuperación de contraseña - Hospital San Gabriel");

        mensaje.setText(
                "Hola" + (nombreEmpleado != null && !nombreEmpleado.isBlank() ? " " + nombreEmpleado : "") + ",\n\n"
                + "Se solicitó recuperar la contraseña de tu cuenta.\n\n"
                + "Ingresa al siguiente enlace para crear una nueva contraseña:\n"
                + linkRecuperacion
                + "\n\nEste enlace vencerá en 15 minutos.\n\n"
                + "Si no solicitaste este cambio, ignora este mensaje."
        );

        mailSender.send(mensaje);
    }

    public void enviarComprobanteNomina(
            String destinatario,
            String nombreEmpleado,
            String periodo,
            byte[] pdf
    ) throws Exception {

        MimeMessage mensaje = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensaje, true);

        helper.setTo(Objects.requireNonNull(destinatario));
        helper.setSubject("Comprobante de Nomina - " + periodo + " - Hospital San Gabriel");

        helper.setText(
                "Estimado/a " + nombreEmpleado + ",\n\n"
                + "Adjunto encontrara su comprobante de nomina correspondiente al periodo " + periodo + ".\n\n"
                + "Saludos,\n"
                + "Departamento de RRHH - Hospital San Gabriel"
        );

        helper.addAttachment(
                "Comprobante_Nomina_" + periodo + "_" + nombreEmpleado.replace(" ", "_") + ".pdf",
                new ByteArrayResource(Objects.requireNonNull(pdf))
        );

        mailSender.send(mensaje);
    }

    public void enviarCodigoOtp(String destinatario, String nombreEmpleado, String codigo) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(destinatario);
        mensaje.setSubject("Código de verificación - App Asistencia - Hospital San Gabriel");
        mensaje.setText(
            "Estimado/a " + nombreEmpleado + ",\n\n"
            + "Has solicitado vincular tu dispositivo móvil para registrar asistencia.\n\n"
            + "Tu código de verificación es:\n\n"
            + codigo + "\n\n"
            + "Este código expira en 5 minutos.\n"
            + "Si no solicitaste este registro, ignora este mensaje.\n\n"
            + "Saludos,\n"
            + "Departamento de RRHH - Hospital San Gabriel"
        );
        mailSender.send(mensaje);
    }

    public void enviarLinkDescargaApp(String destinatario, String nombreEmpleado) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(destinatario);
        mensaje.setSubject("Descarga la App de Asistencia - Hospital San Gabriel");
        mensaje.setText(
            "Estimado/a " + nombreEmpleado + ",\n\n"
            + "Se te ha enviado el enlace para descargar la aplicación móvil del\n"
            + "Hospital San Gabriel para registrar tu asistencia con huella dactilar.\n\n"
            + "Descarga la app aquí:\n"
            + appDownloadUrl + "\n\n"
            + "Pasos:\n"
            + "1. Descarga e instala la app desde el enlace\n"
            + "2. Abre la app e ingresa tu DNI\n"
            + "3. Revisa tu correo personal e ingresa el código de verificación\n"
            + "4. ¡Listo! Podrás registrar tu entrada y salida con huella dactilar.\n\n"
            + "Saludos,\n"
            + "Departamento de RRHH - Hospital San Gabriel"
        );
        mailSender.send(mensaje);
    }
}
