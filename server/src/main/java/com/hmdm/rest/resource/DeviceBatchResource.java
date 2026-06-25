package com.hmdm.rest.resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.MediaType;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.hmdm.persistence.ConfigurationDAO;
import com.hmdm.persistence.GroupDAO;
import com.hmdm.rest.json.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import com.hmdm.service.DeviceBatchExcelParser;
import com.hmdm.service.DeviceBatchValidationService;
import com.hmdm.rest.json.BatchDevicePreviewResponse;
import com.hmdm.rest.json.BatchDeviceUploadRow;
import java.util.List;

@Api(tags = { "Batch Device Upload" }, authorizations = { @Authorization("Bearer Token") })
@Singleton
@Path("/private/devices/batch")
public class DeviceBatchResource {

    private GroupDAO groupDAO;
    private ConfigurationDAO configurationDAO;
    private DeviceBatchExcelParser excelParser;
    private DeviceBatchValidationService validationService;

    /**
     * Constructor required by Swagger/Jersey
     */
    public DeviceBatchResource() {
    }

    @Inject
    public DeviceBatchResource(GroupDAO groupDAO,
            ConfigurationDAO configurationDAO,
            DeviceBatchExcelParser excelParser,
            DeviceBatchValidationService validationService) {
        this.groupDAO = groupDAO;
        this.configurationDAO = configurationDAO;
        this.excelParser = excelParser;
        this.validationService = validationService;
    }

    @ApiOperation(value = "Preview batch device upload Excel", notes = "Parses uploaded Excel and validates each device row before import")
    @POST
    @Path("/preview")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response previewBatchUpload(@FormDataParam("file") InputStream fileInputStream) {
        if (fileInputStream == null) {
            return Response.ERROR("error.file.required");
        }

        try {
            BatchDevicePreviewResponse previewResponse = this.validationService
                    .validate(this.excelParser.parse(fileInputStream));

            return Response.OK(previewResponse);
        } catch (IllegalArgumentException e) {
            return Response.ERROR(e.getMessage());
        } catch (Exception e) {
            return Response.ERROR("Failed to parse uploaded Excel file");
        }
    }

    @ApiOperation(value = "Download batch device upload Excel template", notes = "Downloads an Excel template containing Device Upload sheet and reference sheets for groups and configurations")
    @GET
    @Path("/template")
    @Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public javax.ws.rs.core.Response downloadTemplate() throws IOException {
        byte[] fileBytes = buildTemplate();

        String fileName = "device-batch-template-" +
                LocalDate.now().format(DateTimeFormatter.ISO_DATE) + ".xlsx";

        return javax.ws.rs.core.Response.ok(fileBytes)
                .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                .header("Content-Length", fileBytes.length)
                .build();
    }

    private byte[] buildTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            createDeviceUploadSheet(workbook);
            createConfigurationsSheet(workbook);
            createGroupsSheet(workbook);
            createDropdownSheet(workbook);
            addDropdownsToUploadSheet(workbook);
            createInstructionsSheet(workbook);
            autoSizeColumns(workbook);

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private void createDropdownSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("_Dropdowns");

        int rowIndex = 0;

        // Column A = configuration names
        for (com.hmdm.persistence.domain.Configuration configuration : this.configurationDAO.getAllConfigurations()) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                row = sheet.createRow(rowIndex);
            }
            row.createCell(0).setCellValue(configuration.getName() != null ? configuration.getName() : "");
            rowIndex++;
        }

        int configCount = rowIndex;

        // Column B = group names
        rowIndex = 0;
        for (com.hmdm.persistence.domain.Group group : this.groupDAO.getAllGroups()) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                row = sheet.createRow(rowIndex);
            }
            row.createCell(1).setCellValue(group.getName() != null ? group.getName() : "");
            rowIndex++;
        }

        int groupCount = rowIndex;

        // Named range for configurations
        if (configCount > 0) {
            Name configRange = workbook.createName();
            configRange.setNameName("CONFIGURATION_OPTIONS");
            configRange.setRefersToFormula("_Dropdowns!$A$1:$A$" + configCount);
        }

        // Named range for groups
        if (groupCount > 0) {
            Name groupRange = workbook.createName();
            groupRange.setNameName("GROUP_OPTIONS");
            groupRange.setRefersToFormula("_Dropdowns!$B$1:$B$" + groupCount);
        }

        // Hide helper sheet from admin
        workbook.setSheetHidden(workbook.getSheetIndex(sheet), true);
    }

    private void addDropdownsToUploadSheet(Workbook workbook) {
        Sheet uploadSheet = workbook.getSheet("Device Upload");
        if (uploadSheet == null) {
            return;
        }

        DataValidationHelper validationHelper = uploadSheet.getDataValidationHelper();

        // Apply dropdown to rows 2..1000 (Excel row numbers)
        // POI uses zero-based indexes, so row 1 = second Excel row
        int firstRow = 1;
        int lastRow = 1000;

        // Column C = Configuration (index 2)
        addNamedRangeDropdown(validationHelper, uploadSheet,
                "CONFIGURATION_OPTIONS", firstRow, lastRow, 2, 2);

        // Column D = Group (index 3)
        addNamedRangeDropdown(validationHelper, uploadSheet,
                "GROUP_OPTIONS", firstRow, lastRow, 3, 3);
    }

    private void addNamedRangeDropdown(DataValidationHelper validationHelper,
            Sheet sheet,
            String formula,
            int firstRow,
            int lastRow,
            int firstCol,
            int lastCol) {

        DataValidationConstraint constraint = validationHelper.createFormulaListConstraint(formula);

        CellRangeAddressList addressList = new CellRangeAddressList(firstRow, lastRow, firstCol, lastCol);

        DataValidation validation = validationHelper.createValidation(constraint, addressList);

        validation.setSuppressDropDownArrow(false);
        validation.setShowErrorBox(true);
        validation.createErrorBox("Invalid value", "Please select a value from the dropdown list.");

        // Excel compatibility quirk
        if (validation instanceof org.apache.poi.xssf.usermodel.XSSFDataValidation) {
            validation.setSuppressDropDownArrow(true);
            validation.setShowErrorBox(true);
        }

        sheet.addValidationData(validation);
    }

    private void createDeviceUploadSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Device Upload");
        CellStyle headerStyle = createHeaderStyle(workbook);

        Row header = sheet.createRow(0);
        createCell(header, 0, "Bus No", headerStyle);
        createCell(header, 1, "Device Name", headerStyle);
        createCell(header, 2, "Configuration", headerStyle);
        createCell(header, 3, "Group", headerStyle);
        createCell(header, 4, "Description", headerStyle);

        for (int i = 1; i <= 20; i++) {
            sheet.createRow(i);
        }

        sheet.createFreezePane(0, 1);
    }

    private void createConfigurationsSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Configurations");
        CellStyle headerStyle = createHeaderStyle(workbook);

        Row header = sheet.createRow(0);
        createCell(header, 0, "Configuration ID", headerStyle);
        createCell(header, 1, "Configuration Name", headerStyle);

        int rowIndex = 1;
        for (com.hmdm.persistence.domain.Configuration configuration : this.configurationDAO.getAllConfigurations()) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(configuration.getId() != null ? configuration.getId() : 0);
            row.createCell(1).setCellValue(configuration.getName() != null ? configuration.getName() : "");
        }
    }

    private void createGroupsSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Groups");
        CellStyle headerStyle = createHeaderStyle(workbook);

        Row header = sheet.createRow(0);
        createCell(header, 0, "Group ID", headerStyle);
        createCell(header, 1, "Group Name", headerStyle);

        int rowIndex = 1;
        for (com.hmdm.persistence.domain.Group group : this.groupDAO.getAllGroups()) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(group.getId() != null ? group.getId() : 0);
            row.createCell(1).setCellValue(group.getName() != null ? group.getName() : "");
        }
    }

    private void createInstructionsSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Instructions");

        Row row0 = sheet.createRow(0);
        row0.createCell(0).setCellValue("Batch Device Upload Template");

        Row row2 = sheet.createRow(2);
        row2.createCell(0).setCellValue("Instructions:");

        Row row3 = sheet.createRow(3);
        row3.createCell(0).setCellValue("1. Fill only the 'Device Upload' sheet.");

        Row row4 = sheet.createRow(4);
        row4.createCell(0).setCellValue("2. Do not rename the column headers.");

        Row row5 = sheet.createRow(5);
        row5.createCell(0).setCellValue("3. Use values from 'Configurations' and 'Groups' sheets.");

        Row row6 = sheet.createRow(6);
        row6.createCell(0).setCellValue("4. Device Name should be unique.");

        Row row7 = sheet.createRow(7);
        row7.createCell(0).setCellValue("5. Configuration and Group dropdowns can be added in the next step.");

        row7.createCell(0)
                .setCellValue("5. Use the dropdowns in Configuration and Group columns to select valid values.");
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    private void createCell(Row row, int columnIndex, String value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void autoSizeColumns(Workbook workbook) {
        Sheet uploadSheet = workbook.getSheet("Device Upload");
        for (int i = 0; i < 5; i++) {
            uploadSheet.autoSizeColumn(i);
            uploadSheet.setColumnWidth(i, Math.max(uploadSheet.getColumnWidth(i), 4500));
        }

        Sheet configSheet = workbook.getSheet("Configurations");
        for (int i = 0; i < 2; i++) {
            configSheet.autoSizeColumn(i);
            configSheet.setColumnWidth(i, Math.max(configSheet.getColumnWidth(i), 4500));
        }

        Sheet groupsSheet = workbook.getSheet("Groups");
        for (int i = 0; i < 2; i++) {
            groupsSheet.autoSizeColumn(i);
            groupsSheet.setColumnWidth(i, Math.max(groupsSheet.getColumnWidth(i), 4500));
        }

        Sheet instructionsSheet = workbook.getSheet("Instructions");
        instructionsSheet.setColumnWidth(0, 18000);
    }
}