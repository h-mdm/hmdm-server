package com.hmdm.service;

import com.hmdm.rest.json.BatchDeviceUploadRow;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.inject.Singleton;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class DeviceBatchExcelParser {

    private static final String SHEET_NAME = "Device Upload";

    public List<BatchDeviceUploadRow> parse(InputStream inputStream) throws Exception {
        List<BatchDeviceUploadRow> rows = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheet(SHEET_NAME);
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet '" + SHEET_NAME + "' not found");
            }

            int lastRowNum = sheet.getLastRowNum();

            for (int i = 1; i <= lastRowNum; i++) { // skip header row 0
                Row row = sheet.getRow(i);
                if (isRowEmpty(row)) {
                    continue;
                }

                BatchDeviceUploadRow dto = new BatchDeviceUploadRow();
                dto.setRowNumber(i + 1); // Excel row number
                dto.setBusNo(getCellString(row, 0));
                dto.setDeviceName(getCellString(row, 1));
                dto.setConfigurationValue(getCellString(row, 2));
                dto.setGroupValue(getCellString(row, 3));
                dto.setDescription(getCellString(row, 4));

                rows.add(dto);
            }
        }

        return rows;
    }

    private String getCellString(Row row, int cellIndex) {
        if (row == null) {
            return null;
        }

        Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return null;
        }

        DataFormatter formatter = new DataFormatter();
        String value = formatter.formatCellValue(cell);
        if (value != null) {
            value = value.trim();
        }

        return value == null || value.isEmpty() ? null : value;
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }

        for (int i = 0; i <= 4; i++) {
            String value = getCellString(row, i);
            if (value != null && !value.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}