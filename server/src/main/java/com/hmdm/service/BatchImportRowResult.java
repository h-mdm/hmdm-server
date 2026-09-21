package com.hmdm.service;

public class BatchImportRowResult {

    private Integer rowNumber;
    private boolean success;
    private String message;

    public BatchImportRowResult() {
    }

    public BatchImportRowResult(
            Integer rowNumber,
            boolean success,
            String message) {

        this.rowNumber = rowNumber;
        this.success = success;
        this.message = message;
    }

    public Integer getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(Integer rowNumber) {
        this.rowNumber = rowNumber;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}