package com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.export;

import com.rrhh.Shared.export.ExportAbstractFactory;
import com.rrhh.Shared.export.pdf.interfaces.PDFExporter;
import com.rrhh.Shared.export.excel.interfaces.ExcelExporter;
import com.rrhh.Shared.export.csv.interfaces.CSVExporter;
import com.rrhh.Shared.export.pdf.implementations.GenericPDFExporter;
import com.rrhh.Shared.export.excel.implementations.GenericExcelExporter;
import org.springframework.stereotype.Component;

@Component("autenticacionYRolExportFactory")
public class AutenticacionYRolExportFactory implements ExportAbstractFactory {

    @Override
    public String[] getHeaders() {
        return new String[]{"ID Usuario", "Nombre Usuario", "Correo Inst.", "Rol", "Activo", "2FA", "Intentos Fallidos"};
    }

    @Override
    public String[] getKeys() {
        return new String[]{"idUsuario", "nombreUsuario", "correoInst", "nombreRol", "activo", "dosFA", "intentosFallidos"};
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
