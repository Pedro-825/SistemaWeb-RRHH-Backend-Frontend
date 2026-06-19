package com.rrhh.Shared.export.excel.interfaces;

import java.util.List;
import java.util.Map;

public interface ExcelExporter {
    byte[] export(String titulo, List<Map<String, Object>> datos, String[] headers, String[] keys);
}
