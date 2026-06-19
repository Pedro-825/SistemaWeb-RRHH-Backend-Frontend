package com.rrhh.Shared.export.pdf.interfaces;

import java.util.List;
import java.util.Map;

public interface PDFExporter {
    byte[] export(String titulo, List<Map<String, Object>> datos, String[] headers, String[] keys);
}
