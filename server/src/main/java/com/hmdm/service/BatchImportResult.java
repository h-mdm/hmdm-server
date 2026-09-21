package com.hmdm.service;

import java.util.ArrayList;
import java.util.List;

/**
 * BatchImportResult
 */
public class BatchImportResult {

    private int totalRows;
    private int successCount;
    private int failedCount;

    private List<String> errors = new ArrayList<>();
    private List<BatchImportRowResult> rows = new ArrayList<>();

    // getters/setters
    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }

    public List<String> getErrors() {
        return errors;
    }

    public List<BatchImportRowResult> getRows() {
        return rows;
    }

    public void setRows(List<BatchImportRowResult> rows) {
        this.rows = rows;
    }
}