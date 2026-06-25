package com.hmdm.service;

import com.hmdm.persistence.ConfigurationDAO;
import com.hmdm.persistence.GroupDAO;
import com.hmdm.persistence.domain.Configuration;
import com.hmdm.persistence.domain.Group;
import com.hmdm.rest.json.BatchDevicePreviewResponse;
import com.hmdm.rest.json.BatchDevicePreviewRow;
import com.hmdm.rest.json.BatchDeviceUploadRow;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class DeviceBatchValidationService {

    private final GroupDAO groupDAO;
    private final ConfigurationDAO configurationDAO;

    @Inject
    public DeviceBatchValidationService(GroupDAO groupDAO,
            ConfigurationDAO configurationDAO) {
        this.groupDAO = groupDAO;
        this.configurationDAO = configurationDAO;
    }

    public BatchDevicePreviewResponse validate(List<BatchDeviceUploadRow> uploadedRows) {
        BatchDevicePreviewResponse response = new BatchDevicePreviewResponse();

        List<Group> groups = this.groupDAO.getAllGroups();
        List<Configuration> configurations = this.configurationDAO.getAllConfigurations();

        Map<Integer, Group> groupById = groups.stream()
                .collect(Collectors.toMap(Group::getId, g -> g, (a, b) -> a, LinkedHashMap::new));

        Map<String, Group> groupByName = groups.stream()
                .filter(g -> g.getName() != null)
                .collect(Collectors.toMap(g -> normalize(g.getName()), g -> g, (a, b) -> a, LinkedHashMap::new));

        Map<Integer, Configuration> configurationById = configurations.stream()
                .collect(Collectors.toMap(Configuration::getId, c -> c, (a, b) -> a, LinkedHashMap::new));

        Map<String, Configuration> configurationByName = configurations.stream()
                .filter(c -> c.getName() != null)
                .collect(Collectors.toMap(c -> normalize(c.getName()), c -> c, (a, b) -> a, LinkedHashMap::new));

        Map<String, Integer> deviceNameCount = new HashMap<>();
        for (BatchDeviceUploadRow row : uploadedRows) {
            if (row.getDeviceName() != null) {
                String key = normalize(row.getDeviceName());
                deviceNameCount.put(key, deviceNameCount.getOrDefault(key, 0) + 1);
            }
        }

        List<BatchDevicePreviewRow> previewRows = new ArrayList<>();

        for (BatchDeviceUploadRow row : uploadedRows) {
            BatchDevicePreviewRow preview = new BatchDevicePreviewRow();
            preview.setRowNumber(row.getRowNumber());
            preview.setBusNo(row.getBusNo());
            preview.setDeviceName(row.getDeviceName());
            preview.setConfigurationValue(row.getConfigurationValue());
            preview.setGroupValue(row.getGroupValue());
            preview.setDescription(row.getDescription());

            List<String> errors = new ArrayList<>();

            // Required validations
            if (isBlank(row.getDeviceName())) {
                errors.add("Device Name is required");
            }
            if (isBlank(row.getConfigurationValue())) {
                errors.add("Configuration is required");
            }
            if (isBlank(row.getGroupValue())) {
                errors.add("Group is required");
            }

            // Duplicate device name within uploaded file
            if (!isBlank(row.getDeviceName())) {
                String key = normalize(row.getDeviceName());
                if (deviceNameCount.getOrDefault(key, 0) > 1) {
                    errors.add("Duplicate Device Name in uploaded file");
                }
            }

            // Resolve configuration
            if (!isBlank(row.getConfigurationValue())) {
                Integer configurationId = resolveConfigurationId(row.getConfigurationValue(), configurationById,
                        configurationByName);
                if (configurationId == null) {
                    errors.add("Configuration not found: " + row.getConfigurationValue());
                } else {
                    preview.setConfigurationId(configurationId);
                }
            }

            // Resolve group
            if (!isBlank(row.getGroupValue())) {
                Integer groupId = resolveGroupId(row.getGroupValue(), groupById, groupByName);
                if (groupId == null) {
                    errors.add("Group not found: " + row.getGroupValue());
                } else {
                    preview.setGroupId(groupId);
                }
            }

            // TODO: DB duplicate device name check
            // Hook this once you confirm the actual DeviceDAO method:
            // if (deviceDAO.getDeviceByName(row.getDeviceName()) != null) {
            // errors.add("Device already exists: " + row.getDeviceName());
            // }

            preview.setErrors(errors);
            preview.setValid(errors.isEmpty());

            previewRows.add(preview);
        }

        response.setRows(previewRows);
        response.setTotalRows(previewRows.size());
        response.setValidRows((int) previewRows.stream().filter(BatchDevicePreviewRow::isValid).count());
        response.setInvalidRows(response.getTotalRows() - response.getValidRows());

        return response;
    }

    private Integer resolveConfigurationId(String rawValue,
            Map<Integer, Configuration> configurationById,
            Map<String, Configuration> configurationByName) {
        ParsedLookup parsed = parseLookupValue(rawValue);
        if (parsed.id != null && configurationById.containsKey(parsed.id)) {
            return parsed.id;
        }

        if (!isBlank(parsed.name)) {
            Configuration configuration = configurationByName.get(normalize(parsed.name));
            return configuration != null ? configuration.getId() : null;
        }

        return null;
    }

    private Integer resolveGroupId(String rawValue,
            Map<Integer, Group> groupById,
            Map<String, Group> groupByName) {
        ParsedLookup parsed = parseLookupValue(rawValue);
        if (parsed.id != null && groupById.containsKey(parsed.id)) {
            return parsed.id;
        }

        if (!isBlank(parsed.name)) {
            Group group = groupByName.get(normalize(parsed.name));
            return group != null ? group.getId() : null;
        }

        return null;
    }

    private ParsedLookup parseLookupValue(String value) {
        ParsedLookup result = new ParsedLookup();
        if (isBlank(value)) {
            return result;
        }

        String trimmed = value.trim();

        // Supports "22 | Config Name"
        if (trimmed.contains("|")) {
            String[] parts = trimmed.split("\\|", 2);
            String left = parts[0] != null ? parts[0].trim() : null;
            String right = parts.length > 1 && parts[1] != null ? parts[1].trim() : null;

            try {
                result.id = left != null && !left.isEmpty() ? Integer.valueOf(left) : null;
            } catch (NumberFormatException ex) {
                result.id = null;
            }
            result.name = right;
            return result;
        }

        // Supports raw numeric ID
        try {
            result.id = Integer.valueOf(trimmed);
            return result;
        } catch (NumberFormatException ignored) {
        }

        // Supports plain name
        result.name = trimmed;
        return result;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static class ParsedLookup {
        private Integer id;
        private String name;
    }
}