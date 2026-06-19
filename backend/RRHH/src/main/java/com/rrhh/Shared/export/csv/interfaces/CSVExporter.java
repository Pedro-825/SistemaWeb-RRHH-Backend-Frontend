package com.rrhh.Shared.export.csv.interfaces;

import java.util.List;
import java.util.Map;

public interface CSVExporter {
    byte[] export(String titulo, List<Map<String, Object>> datos, String[] headers, String[] keys);
}
