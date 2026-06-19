package com.rrhh.Shared.export;

import com.rrhh.Shared.export.pdf.interfaces.PDFExporter;
import com.rrhh.Shared.export.excel.interfaces.ExcelExporter;
import com.rrhh.Shared.export.csv.interfaces.CSVExporter;

public interface ExportAbstractFactory {
    PDFExporter createPDFExporter();
    ExcelExporter createExcelExporter();
    CSVExporter createCSVExporter();
    String[] getHeaders();
    String[] getKeys();
}
