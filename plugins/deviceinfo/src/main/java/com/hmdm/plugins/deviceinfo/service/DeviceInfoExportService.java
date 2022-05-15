/*
 *
 * Headwind MDM: Open Source Android MDM Software
 * https://h-mdm.com
 *
 * Copyright (C) 2019 Headwind Solutions LLC (http://h-sms.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.hmdm.plugins.deviceinfo.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hmdm.plugins.deviceinfo.persistence.mapper.DeviceInfoMapper;
import com.hmdm.plugins.deviceinfo.rest.json.DeviceDynamicInfoRecord;
import com.hmdm.plugins.deviceinfo.rest.json.DynamicInfoExportFilter;
import com.hmdm.security.SecurityContext;
import com.hmdm.util.ResourceBundleUTF8Control;
import com.opencsv.CSVWriter;
import org.apache.ibatis.cursor.Cursor;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * <p>A service used for supporting device info export process.</p>
 *
 * @author isv
 */
@Singleton
public class DeviceInfoExportService {

    private static final Logger logger = LoggerFactory.getLogger(DeviceInfoExportService.class);

    /**
     * <p>An interface to device info management services.</p>
     */
    private final DeviceInfoMapper mapper;

    /**
     * <p>Constructs new <code>DeviceInfoExportService</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public DeviceInfoExportService(DeviceInfoMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * <p>Exports the device dynamic info records matching the specified parameters into CSV file which is written to
     * specified stream.</p>
     *
     * @param request the parameters for export process.
     * @param output  a stream to write the generated content to.
     * @throws DeviceInfoExportServiceException if an I/O error occurs.
     */
    @Transactional
    public void exportDeviceDynamicInfo(DynamicInfoExportFilter request, OutputStream output) {
        SecurityContext.get().getCurrentUser().ifPresent(user -> {
            logger.debug("Starting device dynamic info export for request: {} ...", request);
            try (Cursor<DeviceDynamicInfoRecord> records = this.mapper.searchDynamicDataForExport(request);
                 CSVWriter writer = new CSVWriter(new OutputStreamWriter(output));) {
                if (records != null) {
                    String locale = request.getLocale() == null ? Locale.ENGLISH.getLanguage() : request.getLocale();
                    if (locale.contains("_")) {
                        locale = locale.substring(0, locale.indexOf("_"));
                    }
                    ResourceBundle translations = ResourceBundle.getBundle(
                            "plugin_deviceinfo_translations", new Locale(locale), new ResourceBundleUTF8Control()
                    );

                    final String[] fieldNames = request.getFields();
                    final int fieldsCount = fieldNames.length;

                    // adding header to csv
                    final String[] headers = new String[fieldsCount + 1];
                    headers[0] = translations.getString("plugin.deviceinfo.title.time");
                    for (int i = 0; i < fieldsCount; i++) {
                        String fieldName = fieldNames[i];
                        try {
                            headers[i + 1] = translations.getString("plugin.deviceinfo.title.group." + fieldName);
                        } catch (Exception e) {
                            headers[i + 1] = fieldName;
                        }
                    }
                    writer.writeNext(headers);

                    // add data to csv
                    DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                    records.forEach(record -> {
                        final String[] recordLine = new String[fieldsCount + 1];
                        recordLine[0] = dateFormat.format(new Date(record.getLatestUpdateTime()));
                        for (int i = 0; i < fieldsCount; i++) {
                            String fieldName = fieldNames[i];
                            final BiFunction<DeviceDynamicInfoRecord, ResourceBundle, String> valueExtractor = valueExtractors.get(fieldName);
                            if (valueExtractor != null) {
                                recordLine[i + 1] = valueExtractor.apply(record, translations);
                            } else {
                                logger.warn("No value extractor for field: {}", fieldName);
                            }
                        }

                        writer.writeNext(recordLine, false);
                    });
                }
            } catch (IOException e) {
                throw new DeviceInfoExportServiceException("Failed to export device info records due to I/O error", e);
            } finally {
                logger.debug("Finished device info export for request: {}", request);
            }
        });
    }

    private static final Function<Function<DeviceDynamicInfoRecord, Integer>, BiFunction<DeviceDynamicInfoRecord, ResourceBundle, String>> integerValueExtractorFactory
            = valueSupplier -> (record, resourceBundle) -> {
        final Integer value = valueSupplier.apply(record);
        if (value != null) {
            return value.toString();
        } else {
            return null;
        }
    };

    private static final Function<Function<DeviceDynamicInfoRecord, String>, BiFunction<DeviceDynamicInfoRecord, ResourceBundle, String>> stringValueExtractorFactory
            = valueSupplier -> (record, resourceBundle) -> valueSupplier.apply(record);

    private static final BiFunction<String, Function<DeviceDynamicInfoRecord, String>, BiFunction<DeviceDynamicInfoRecord, ResourceBundle, String>> enumeratedValueExtractorFactory
            = (fieldName, valueSupplier) -> (record, resourceBundle) -> {
        final String value = valueSupplier.apply(record);
        if (value != null) {
            try {
                return resourceBundle.getString("plugin.deviceinfo.state." + fieldName + "." + value);
            } catch (Exception e) {
                return value;
            }
        } else {
            return null;
        }
    };

    private static final Function<Function<DeviceDynamicInfoRecord, Long>, BiFunction<DeviceDynamicInfoRecord, ResourceBundle, String>> longValueExtractorFactory
            = valueSupplier -> (record, resourceBundle) -> {
        final Long value = valueSupplier.apply(record);
        if (value != null) {
            return value.toString();
        } else {
            return null;
        }
    };

    private static final Function<Function<DeviceDynamicInfoRecord, Double>, BiFunction<DeviceDynamicInfoRecord, ResourceBundle, String>> doubleValueExtractorFactory
            = valueSupplier -> (record, resourceBundle) -> {
        final Double value = valueSupplier.apply(record);
        if (value != null) {
            return value.toString();
        } else {
            return null;
        }
    };

    private static final Function<Function<DeviceDynamicInfoRecord, Boolean>, BiFunction<DeviceDynamicInfoRecord, ResourceBundle, String>> booleanValueExtractorFactory
            = valueSupplier -> (record, resourceBundle) -> {
        final Boolean value = valueSupplier.apply(record);
        if (value != null) {
            try {
                return resourceBundle.getString("plugin.deviceinfo.boolean." + value);
            } catch (Exception e) {
                return value.toString();
            }
        } else {
            return null;
        }
    };

    /**
     * <p>A mapping from record field name to value extractor for the field.</p>
     */
    private static final Map<String, BiFunction<DeviceDynamicInfoRecord, ResourceBundle, String>> valueExtractors = new HashMap<>();

    static {
        valueExtractors.put("deviceBatteryLevel", integerValueExtractorFactory.apply(DeviceDynamicInfoRecord::getDeviceBatteryLevel));
        valueExtractors.put("deviceBatteryCharging", stringValueExtractorFactory.apply(DeviceDynamicInfoRecord::getDeviceBatteryCharging));
        valueExtractors.put("deviceIpAddress", stringValueExtractorFactory.apply(DeviceDynamicInfoRecord::getDeviceIpAddress));
        valueExtractors.put("deviceKeyguard", booleanValueExtractorFactory.apply(DeviceDynamicInfoRecord::getDeviceKeyguard));
        valueExtractors.put("deviceRingVolume", integerValueExtractorFactory.apply(DeviceDynamicInfoRecord::getDeviceRingVolume));
        valueExtractors.put("deviceWifiEnabled", booleanValueExtractorFactory.apply(DeviceDynamicInfoRecord::getDeviceWifiEnabled));
        valueExtractors.put("deviceMobileDataEnabled", booleanValueExtractorFactory.apply(DeviceDynamicInfoRecord::getDeviceMobileDataEnabled));
        valueExtractors.put("deviceGpsEnabled", booleanValueExtractorFactory.apply(DeviceDynamicInfoRecord::getDeviceGpsEnabled));
        valueExtractors.put("deviceBluetoothEnabled", booleanValueExtractorFactory.apply(DeviceDynamicInfoRecord::getDeviceBluetoothEnabled));
        valueExtractors.put("deviceUsbEnabled", booleanValueExtractorFactory.apply(DeviceDynamicInfoRecord::getDeviceUsbEnabled));
        valueExtractors.put("deviceMemoryTotal", integerValueExtractorFactory.apply(DeviceDynamicInfoRecord::getDeviceMemoryTotal));
        valueExtractors.put("deviceMemoryAvailable", integerValueExtractorFactory.apply(DeviceDynamicInfoRecord::getDeviceMemoryAvailable));

        valueExtractors.put("wifiRssi", integerValueExtractorFactory.apply(DeviceDynamicInfoRecord::getWifiRssi));
        valueExtractors.put("wifiSsid", stringValueExtractorFactory.apply(DeviceDynamicInfoRecord::getWifiSsid));
        valueExtractors.put("wifiSecurity", stringValueExtractorFactory.apply(DeviceDynamicInfoRecord::getWifiSecurity));
        valueExtractors.put("wifiState", enumeratedValueExtractorFactory.apply("wifiState", DeviceDynamicInfoRecord::getWifiState));
        valueExtractors.put("wifiIpAddress", stringValueExtractorFactory.apply(DeviceDynamicInfoRecord::getWifiIpAddress));
        valueExtractors.put("wifiTx", longValueExtractorFactory.apply(DeviceDynamicInfoRecord::getWifiTx));
        valueExtractors.put("wifiRx", longValueExtractorFactory.apply(DeviceDynamicInfoRecord::getWifiRx));

        valueExtractors.put("gpsState", enumeratedValueExtractorFactory.apply("gpsState", DeviceDynamicInfoRecord::getGpsState));
        valueExtractors.put("gpsLat", doubleValueExtractorFactory.apply(DeviceDynamicInfoRecord::getGpsLat));
        valueExtractors.put("gpsLon", doubleValueExtractorFactory.apply(DeviceDynamicInfoRecord::getGpsLon));
        valueExtractors.put("gpsAlt", doubleValueExtractorFactory.apply(DeviceDynamicInfoRecord::getGpsAlt));
        valueExtractors.put("gpsSpeed", doubleValueExtractorFactory.apply(DeviceDynamicInfoRecord::getGpsSpeed));
        valueExtractors.put("gpsCourse", doubleValueExtractorFactory.apply(DeviceDynamicInfoRecord::getGpsCourse));
        
        valueExtractors.put("mobile1Rssi", integerValueExtractorFactory.apply(DeviceDynamicInfoRecord::getMobile1Rssi));
        valueExtractors.put("mobile1Carrier", stringValueExtractorFactory.apply(DeviceDynamicInfoRecord::getMobile1Carrier));
        valueExtractors.put("mobile1DataEnabled", booleanValueExtractorFactory.apply(DeviceDynamicInfoRecord::getMobile1DataEnabled));
        valueExtractors.put("mobile1IpAddress", stringValueExtractorFactory.apply(DeviceDynamicInfoRecord::getMobile1IpAddress));
        valueExtractors.put("mobile1State", enumeratedValueExtractorFactory.apply("mobile1State", DeviceDynamicInfoRecord::getMobile1State));
        valueExtractors.put("mobile1SimState", enumeratedValueExtractorFactory.apply("mobile1SimState", DeviceDynamicInfoRecord::getMobile1SimState));
        valueExtractors.put("mobile1Tx", longValueExtractorFactory.apply(DeviceDynamicInfoRecord::getMobile1Tx));
        valueExtractors.put("mobile1Rx", longValueExtractorFactory.apply(DeviceDynamicInfoRecord::getMobile1Rx));

        valueExtractors.put("mobile2Rssi", integerValueExtractorFactory.apply(DeviceDynamicInfoRecord::getMobile2Rssi));
        valueExtractors.put("mobile2Carrier", stringValueExtractorFactory.apply(DeviceDynamicInfoRecord::getMobile2Carrier));
        valueExtractors.put("mobile2DataEnabled", booleanValueExtractorFactory.apply(DeviceDynamicInfoRecord::getMobile2DataEnabled));
        valueExtractors.put("mobile2IpAddress", stringValueExtractorFactory.apply(DeviceDynamicInfoRecord::getMobile2IpAddress));
        valueExtractors.put("mobile2State", enumeratedValueExtractorFactory.apply("mobile2State", DeviceDynamicInfoRecord::getMobile2State));
        valueExtractors.put("mobile2SimState", enumeratedValueExtractorFactory.apply("mobile2SimState", DeviceDynamicInfoRecord::getMobile2SimState));
        valueExtractors.put("mobile2Tx", longValueExtractorFactory.apply(DeviceDynamicInfoRecord::getMobile2Tx));
        valueExtractors.put("mobile2Rx", longValueExtractorFactory.apply(DeviceDynamicInfoRecord::getMobile2Rx));

    }
}
