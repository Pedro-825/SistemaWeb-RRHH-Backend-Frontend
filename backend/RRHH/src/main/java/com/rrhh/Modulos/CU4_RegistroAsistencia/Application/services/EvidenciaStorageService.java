package com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class EvidenciaStorageService {

    @Value("${azure.storage.connection-string:}")
    private String connectionString;

    @Value("${azure.storage.container-name:evidencias}")
    private String containerName;

    @Value("${app.upload-dir:uploads/evidencias}")
    private String uploadDir;

    private BlobContainerClient containerClient;

    @PostConstruct
    public void init() {
        if (connectionString != null && !connectionString.isBlank()) {
            BlobServiceClient serviceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();
            containerClient = serviceClient.getBlobContainerClient(containerName);
            if (!containerClient.exists()) {
                containerClient.create();
            }
        }
    }

    /**
     * Elimina un archivo por su URL. En local borra del disco; en Azure borra el blob.
     */
    public void eliminar(String url) throws IOException {
        if (url == null || url.isBlank()) return;
        String filename = url.substring(url.lastIndexOf('/') + 1);
        if (containerClient != null) {
            containerClient.getBlobClient(filename).deleteIfExists();
        } else {
            Path filePath = Paths.get(uploadDir).toAbsolutePath().resolve(filename);
            Files.deleteIfExists(filePath);
        }
    }

    /**
     * Sube el archivo y devuelve la URL pública de acceso.
     * En local → disco. En Azure → Blob Storage.
     */
    public String guardar(MultipartFile file) throws IOException {
        String ext = "";
        String original = file.getOriginalFilename();
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf("."));
        }
        String filename = UUID.randomUUID() + ext;

        if (containerClient != null) {
            // Azure Blob Storage
            BlobClient blob = containerClient.getBlobClient(filename);
            blob.upload(file.getInputStream(), file.getSize(), true);
            return blob.getBlobUrl();
        } else {
            // Disco local (desarrollo)
            Path dir = Paths.get(uploadDir).toAbsolutePath();
            Files.createDirectories(dir);
            Files.copy(file.getInputStream(), dir.resolve(filename));
            return "/uploads/evidencias/" + filename;
        }
    }
}
