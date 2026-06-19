package com.rrhh.Shared.export.csv.implementations;

import com.rrhh.Shared.export.csv.interfaces.CSVExporter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class GenericCSVExporter implements CSVExporter {

    @Override
    public byte[] export(String titulo, List<Map<String, Object>> datos, String[] headers, String[] keys) {
        if (datos == null || datos.isEmpty()) {
            return new byte[0];
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8)) {
            writer.write('\uFEFF');

            writer.write(String.join(",", headers));
            writer.write("\n");

            for (Map<String, Object> fila : datos) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < keys.length; i++) {
                    if (i > 0) sb.append(",");
                    Object valor = fila.get(keys[i]);
                    String texto = valor != null ? valor.toString().replace("\"", "\"\"") : "";
                    if (texto.contains(",") || texto.contains("\"") || texto.contains("\n")) {
                        sb.append("\"").append(texto).append("\"");
                    } else {
                        sb.append(texto);
                    }
                }
                writer.write(sb.toString());
                writer.write("\n");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al generar CSV: " + e.getMessage(), e);
        }
        return baos.toByteArray();
    }
}
