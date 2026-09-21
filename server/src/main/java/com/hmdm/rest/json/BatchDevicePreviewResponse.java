package com.hmdm.rest.json;

import java.util.ArrayList;
import java.util.List;

public class BatchDevicePreviewResponse {
    private int totalRows;
    private int validRows;
    private int invalidRows;
    private List<BatchDevicePreviewRow> rows = new ArrayList<>();

    public BatchDevicePreviewResponse() {
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getValidRows() {
        return validRows;
    }

    public void setValidRows(int validRows) {
        this.validRows = validRows;
    }

    public int getInvalidRows() {
        return invalidRows;
    }

    public void setInvalidRows(int invalidRows) {
        this.invalidRows = invalidRows;
    }

    public List<BatchDevicePreviewRow> getRows() {
        return rows;
    }

    public void setRows(List<BatchDevicePreviewRow> rows) {
        this.rows = rows;
    }
}