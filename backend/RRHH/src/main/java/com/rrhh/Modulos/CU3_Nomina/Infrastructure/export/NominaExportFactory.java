package com.rrhh.Modulos.CU3_Nomina.Infrastructure.export;

import com.rrhh.Shared.export.ExportAbstractFactory;
import com.rrhh.Shared.export.pdf.interfaces.PDFExporter;
import com.rrhh.Shared.export.excel.interfaces.ExcelExporter;
import com.rrhh.Shared.export.csv.interfaces.CSVExporter;
import com.rrhh.Shared.export.pdf.implementations.GenericPDFExporter;
import com.rrhh.Shared.export.excel.implementations.GenericExcelExporter;
import org.springframework.stereotype.Component;

@Component("nominaExportFactory")
public class NominaExportFactory implements ExportAbstractFactory {

    @Override
    public String[] getHeaders() {
        return new String[]{"Empleado", "Periodo", "Sueldo Base", "Horas Extra", "Bonificaciones", "Descuentos", "Sueldo Bruto", "Sueldo Neto", "Estado"};
    }

    @Override
    public String[] getKeys() {
        return new String[]{"empleado", "periodo", "sueldoBase", "horasExtra", "bonificaciones", "descuentos", "sueldoBruto", "sueldoNeto", "estado"};
    }

    @Override
    public PDFExporter createPDFExporter() {
        return new GenericPDFExporter();
    }

    @Override
    public ExcelExporter createExcelExporter() {
        return new GenericExcelExporter();
    }

    @Override
    public CSVExporter createCSVExporter() {
        return new com.rrhh.Shared.export.csv.implementations.GenericCSVExporter();
    }
}
