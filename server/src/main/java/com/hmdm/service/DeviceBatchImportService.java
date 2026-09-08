package com.hmdm.service;

import com.google.inject.Inject;
import com.hmdm.persistence.DeviceDAO;
import com.hmdm.persistence.domain.Device;
import com.hmdm.rest.json.BatchDevicePreviewRow;
import com.hmdm.rest.json.LookupItem;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class DeviceBatchImportService {

    private final DeviceDAO deviceDAO;

    @Inject
    public DeviceBatchImportService(DeviceDAO deviceDAO) {
        this.deviceDAO = deviceDAO;
    }

    public BatchImportResult importDevices(List<BatchDevicePreviewRow> rows) {

        BatchImportResult result = new BatchImportResult();

        result.setTotalRows(rows.size());

        for (BatchDevicePreviewRow row : rows) {

            if (!row.isValid()) {
                result.setFailedCount(result.getFailedCount() + 1);
                result.getErrors().add(
                        "Row " + row.getRowNumber() + " is invalid.");
                continue;
            }

            try {

                Device existing = deviceDAO.getDeviceByNumber(row.getDeviceName());

                if (existing != null) {

                    result.setFailedCount(
                            result.getFailedCount() + 1);

                    String message = "Device already exists ("
                            + row.getDeviceName()
                            + ")";

                    result.getErrors().add(
                            "Row "
                                    + row.getRowNumber()
                                    + ": "
                                    + message);

                    result.getRows().add(
                            new BatchImportRowResult(
                                    row.getRowNumber(),
                                    false,
                                    message));

                    continue;
                }

                Device device = new Device();

                // Required fields
                device.setNumber(row.getDeviceName());

                device.setConfigurationId(
                        row.getConfigurationId());

                List<LookupItem> groups = new ArrayList<>();

                LookupItem group = new LookupItem();
                group.setId(row.getGroupId());

                groups.add(group);

                device.setGroups(groups);

                // Optional fields
                device.setDescription(row.getDescription());

                device.setLastUpdate(0L);

                // Persist
                deviceDAO.insertDevice(device);

                result.setSuccessCount(
                        result.getSuccessCount() + 1);

                // Add successful row
                result.getRows().add(
                        new BatchImportRowResult(
                                row.getRowNumber(),
                                true,
                                "Imported successfully"));

            } catch (Exception ex) {

                result.setFailedCount(
                        result.getFailedCount() + 1);

                String message = ex.getMessage();

                result.getErrors().add(
                        "Row "
                                + row.getRowNumber()
                                + ": "
                                + message);

                // Add failed row
                result.getRows().add(
                        new BatchImportRowResult(
                                row.getRowNumber(),
                                false,
                                message));
            }

        }

        return result;

    }
}