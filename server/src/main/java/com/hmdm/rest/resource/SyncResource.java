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

package com.hmdm.rest.resource;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.hmdm.event.DeviceBatteryLevelUpdatedEvent;
import com.hmdm.event.DeviceInfoUpdatedEvent;
import com.hmdm.event.DeviceLocationUpdatedEvent;
import com.hmdm.event.EventService;
import com.hmdm.persistence.CustomerDAO;
import com.hmdm.persistence.DeviceDAO;
import com.hmdm.persistence.domain.ApplicationSetting;
import com.hmdm.persistence.domain.ApplicationSettingType;
import com.hmdm.persistence.domain.ApplicationVersion;
import com.hmdm.persistence.domain.ConfigurationFile;
import com.hmdm.persistence.domain.Customer;
import com.hmdm.rest.filter.BaseIPFilter;
import com.hmdm.rest.json.*;
import com.hmdm.security.SecurityContext;
import com.hmdm.util.CryptoUtil;
import com.hmdm.util.FileUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.domain.Application;
import com.hmdm.persistence.domain.Configuration;
import com.hmdm.persistence.domain.Device;
import com.hmdm.persistence.domain.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * A resource used for synchronizing the data with devices.
 * </p>
 */
@Singleton
@Path("/public/sync")
@Api(tags = { "Device data synchronization" })
public class SyncResource {

    private static final Logger logger = LoggerFactory.getLogger(SyncResource.class);

    /**
     * <p>
     * DAO objects
     * </p>
     */
    private UnsecureDAO unsecureDAO;

    private DeviceDAO deviceDAO;

    private CustomerDAO customerDAO;

    /**
     * <p>
     * A service used for sending notifications on battery level update for device
     * </p>
     */
    private EventService eventService;

    /**
     * <p>
     * A list of hooks to be executed against the response to device confoguration
     * synchronization request.
     * </p>
     */
    private Set<SyncResponseHook> syncResponseHooks;

    private String baseUrl;

    private BaseIPFilter remoteAddrResolver;

    private boolean secureEnrollment;
    private String hashSecret;

    private static final String HEADER_IP_ADDRESS = "X-IP-Address";
    private static final String HEADER_CPU_ARCH = "X-CPU-Arch";
    private static final String HEADER_ENROLLMENT_SIGNATURE = "X-Request-Signature";
    private static final String HEADER_RESPONSE_SIGNATURE = "X-Response-Signature";

    private String mobileAppName;
    private String vendor;

    /**
     * <p>
     * A constructor required by Swagger.
     * </p>
     */
    public SyncResource() {
    }

    /**
     * <p>
     * Constructs new <code>SyncResource</code> instance. This implementation does
     * nothing.
     * </p>
     */
    @Inject
    public SyncResource(UnsecureDAO unsecureDAO,
            EventService eventService,
            Injector injector,
            CustomerDAO customerDAO,
            DeviceDAO deviceDAO,
            @Named("base.url") String baseUrl,
            @Named("secure.enrollment") boolean secureEnrollment,
            @Named("hash.secret") String hashSecret,
            @Named("rebranding.mobile.name") String mobileAppName,
            @Named("rebranding.vendor.name") String vendor,
            @Named("proxy.addresses") String proxyIps,
            @Named("proxy.ip.header") String ipHeader) {
        this.unsecureDAO = unsecureDAO;
        this.eventService = eventService;
        this.customerDAO = customerDAO;
        this.deviceDAO = deviceDAO;
        this.baseUrl = baseUrl;
        this.secureEnrollment = secureEnrollment;
        this.hashSecret = hashSecret;
        this.mobileAppName = mobileAppName;
        this.vendor = vendor;
        this.remoteAddrResolver = new BaseIPFilter("", proxyIps, ipHeader);

        Set<SyncResponseHook> allYourInterfaces = new HashSet<>();
        for (Key<?> key : injector.getAllBindings().keySet()) {
            if (SyncResponseHook.class.isAssignableFrom(key.getTypeLiteral().getRawType())) {
                SyncResponseHook yourInterface = (SyncResponseHook) injector.getInstance(key);
                allYourInterfaces.add(yourInterface);
            }
        }
        this.syncResponseHooks = allYourInterfaces;
    }

    // =================================================================================================================
    @ApiOperation(value = "Get device settings", notes = "Gets the device info and settings from the MDM server.", response = SyncResponse.class)
    @POST
    @Path("/configuration/{deviceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDeviceSettingExtended(DeviceCreateOptions createOptions,
            @PathParam("deviceId") @ApiParam("An identifier of device within MDM server") String number,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response) {
        logger.debug("/public/sync/configuration/{}", number);

        if (secureEnrollment) {
            if (!CryptoUtil.checkRequestSignature(request.getHeader(HEADER_ENROLLMENT_SIGNATURE),
                    hashSecret + number)) {
                logger.warn("Failed to setup device {}: signature mismatch", number);
                return Response.PERMISSION_DENIED();
            }
        }

        try {
            Device dbDevice = this.unsecureDAO.getDeviceByNumber(number);
            boolean migration = false;
            boolean foundByImeiOrSerial = false;

            if (dbDevice == null) {
                dbDevice = this.unsecureDAO.getDeviceByOldNumber(number);
                migration = dbDevice != null;
            }

            if (dbDevice == null) {
                dbDevice = this.unsecureDAO.getDeviceByImeiOrSerial(number);
                foundByImeiOrSerial = dbDevice != null;
            }

            // Device creation on demand
            if (dbDevice == null) {
                logger.info("Creating device {} with options {}", number, createOptions.toString());
                dbDevice = unsecureDAO.createNewDeviceOnDemand(number, createOptions);
            }

            if (dbDevice != null) {
                return getDeviceSettingInternal(dbDevice, migration, foundByImeiOrSerial, request, response);
            } else {
                logger.warn("Requested device {} was not found", number);
                return Response.DEVICE_NOT_FOUND_ERROR();
            }
        } catch (Exception e) {
            logger.error("Unexpected error when getting device settings", e);
            e.printStackTrace();
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(value = "Get device settings", notes = "Gets the device info and settings from the MDM server.", response = SyncResponse.class)
    @GET
    @Path("/configuration/{deviceId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDeviceSetting(
            @PathParam("deviceId") @ApiParam("An identifier of device within MDM server") String number,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response) {
        logger.debug("/public/sync/configuration/{}", number);

        if (secureEnrollment) {
            if (!CryptoUtil.checkRequestSignature(request.getHeader(HEADER_ENROLLMENT_SIGNATURE),
                    hashSecret + number)) {
                logger.warn("Failed to setup device {}: signature mismatch", number);
                return Response.PERMISSION_DENIED();
            }
        }

        try {
            Device dbDevice = this.unsecureDAO.getDeviceByNumber(number);
            boolean migration = false;
            boolean foundByImeiOrSerial = false;

            if (dbDevice == null) {
                dbDevice = this.unsecureDAO.getDeviceByOldNumber(number);
                migration = dbDevice != null;
            }

            if (dbDevice == null) {
                dbDevice = this.unsecureDAO.getDeviceByImeiOrSerial(number);
                foundByImeiOrSerial = dbDevice != null;
            }

            // Device creation on demand
            if (dbDevice == null) {
                if (unsecureDAO.isSingleCustomer()) {
                    dbDevice = unsecureDAO.createNewDeviceOnDemand(number);
                } else {
                    logger.warn("Not allowed to create devices in the multi-tenant setup");
                }
            }

            if (dbDevice != null) {
                return getDeviceSettingInternal(dbDevice, migration, foundByImeiOrSerial, request, response);
            } else {
                logger.warn("Requested device {} was not found", number);
                return Response.DEVICE_NOT_FOUND_ERROR();
            }
        } catch (Exception e) {
            logger.error("Unexpected error when getting device settings", e);
            e.printStackTrace();
            return Response.INTERNAL_ERROR();
        }
    }

    private Response getDeviceSettingInternal(Device dbDevice, boolean migration, boolean foundByImeiOrSerial,
            HttpServletRequest request,
            HttpServletResponse response) throws UnsupportedEncodingException {

        if (!migration && dbDevice.getOldNumber() != null) {
            // If a device requested the configuration by new device ID, the migration is
            // completed
            unsecureDAO.completeDeviceMigration(dbDevice.getId());
            dbDevice.setOldNumber(null);
        }

        final Customer customer = this.customerDAO.findById(dbDevice.getCustomerId());

        String cpuArch = request.getHeader(HEADER_CPU_ARCH);
        if (cpuArch == null) {
            // Default
            cpuArch = Application.ARCH_ARM64;
        } else {
            // Remove version: armeabi-v7a -> armeabi
            int i = cpuArch.indexOf('-');
            if (i != -1) {
                cpuArch = cpuArch.substring(0, i);
            }
        }

        Settings settings = this.unsecureDAO.getSettings(dbDevice.getCustomerId());
        final List<Application> applications = this.unsecureDAO.getPlainConfigurationApplications(
                dbDevice.getCustomerId(), dbDevice.getConfigurationId());

        for (Application app : applications) {
            final String icon = app.getIcon();
            if (icon != null) {
                if (!icon.trim().isEmpty()) {
                    String iconUrl = FileUtil.createFileUrl(this.baseUrl,
                            URLEncoder.encode(customer.getFilesDir(), "UTF8"),
                            URLEncoder.encode(icon, "UTF8"));
                    app.setIcon(iconUrl);
                }
            }
            if (app.isSplit()) {
                if (cpuArch.equals(Application.ARCH_ARM64)) {
                    app.setUrl(app.getUrlArm64());
                } else if (cpuArch.equals(Application.ARCH_ARMEABI)) {
                    app.setUrl(app.getUrlArmeabi());
                }
            }
        }

        Configuration configuration = this.unsecureDAO
                .getConfigurationByIdWithAppSettings(dbDevice.getConfigurationId());

        SyncResponse data;
        if (configuration.isUseDefaultDesignSettings()) {
            data = new SyncResponse(settings, configuration.getPassword(), applications, dbDevice);
        } else {
            data = new SyncResponse(configuration, applications, dbDevice);
        }

        data.setGps(configuration.getGps());
        data.setBluetooth(configuration.getBluetooth());
        data.setWifi(configuration.getWifi());
        data.setMobileData(configuration.getMobileData());
        data.setUsbStorage(configuration.getUsbStorage());
        data.setLockStatusBar(configuration.isBlockStatusBar());
        data.setSystemUpdateType(configuration.getSystemUpdateType());
        data.setRequestUpdates(configuration.getRequestUpdates().getTransmittedValue());
        data.setDisableLocation(configuration.getDisableLocation());
        data.setAppPermissions(configuration.getAppPermissions().getTransmittedValue());
        data.setPushOptions(configuration.getPushOptions());
        data.setKeepaliveTime(configuration.getKeepaliveTime());
        data.setAutoBrightness(configuration.getAutoBrightness());
        if (data.getAutoBrightness() != null && !data.getAutoBrightness()) {
            // Set only if autoBrightness == false
            data.setBrightness(configuration.getBrightness());
        }
        data.setManageTimeout(
                configuration.getManageTimeout() == null || !configuration.getManageTimeout() ? null : true);
        if (data.getManageTimeout() != null && data.getManageTimeout()) {
            data.setTimeout(configuration.getTimeout());
        }
        data.setLockVolume(configuration.getLockVolume());
        data.setManageVolume(configuration.getManageVolume() == null || !configuration.getManageVolume() ? null : true);
        if (data.getManageVolume() != null && data.getManageVolume()) {
            data.setVolume(configuration.getVolume());
        }
        if (configuration.getSystemUpdateType() == 2) {
            data.setSystemUpdateFrom(configuration.getSystemUpdateFrom());
            data.setSystemUpdateTo(configuration.getSystemUpdateTo());
        }
        if (configuration.isScheduleAppUpdate()) {
            data.setScheduleAppUpdate(true);
            data.setAppUpdateFrom(configuration.getAppUpdateFrom());
            data.setAppUpdateTo(configuration.getAppUpdateTo());
        }
        data.setDownloadUpdates(configuration.getDownloadUpdates().getTransmittedValue());
        data.setPasswordMode(configuration.getPasswordMode());
        data.setOrientation(configuration.getOrientation());
        data.setDisplayStatus(configuration.isDisplayStatus() ? true : null);
        data.setRunDefaultLauncher(configuration.getRunDefaultLauncher());
        data.setDisableScreenshots(configuration.getDisableScreenshots());
        data.setAutostartForeground(configuration.getAutostartForeground());
        data.setTimeZone(configuration.getTimeZone());
        data.setAllowedClasses(configuration.getAllowedClasses());
        data.setNewServerUrl(configuration.getNewServerUrl());
        data.setLockSafeSettings(configuration.getLockSafeSettings());
        data.setPermissive(configuration.getPermissive());
        data.setKioskExit(configuration.getKioskExit());
        data.setShowWifi(configuration.getShowWifi());

        data.setKioskMode(configuration.isKioskMode());
        if (data.isKioskMode()) {
            data.setLockStatusBar(true); // Esta línea bloquea la barra de estado

            // Bloquear capturas de pantalla
            data.setDisableScreenshots(true); // Bloquea las capturas de pantalla

            // Bloquear el volumen y ajustar el brillo
            data.setLockVolume(true); // Bloquea el control del volumen
            data.setAutoBrightness(false); // Desactiva el ajuste automático del brillo
            data.setBrightness(50); // Fija el nivel de brillo al 50%

            Integer contentAppId = configuration.getContentAppId();
            if (contentAppId != null) {
                ApplicationVersion applicationVersion = this.unsecureDAO.findApplicationVersionById(contentAppId);
                if (applicationVersion != null) {
                    Application application = this.unsecureDAO
                            .findApplicationById(applicationVersion.getApplicationId());
                    data.setMainApp(application.getPkg());
                }
            }
        }
        
        //data.setWifi(false);  // Bloquea el acceso a la configuración de Wi-Fi
        //data.setBluetooth(false);  // Bloquea el acceso a la configuración de Bluetooth

        data.setKioskHome(configuration.getKioskHome() != null && configuration.getKioskHome() ? true : null);
        data.setKioskRecents(configuration.getKioskRecents() != null && configuration.getKioskRecents() ? true : null);
        data.setKioskNotifications(
                configuration.getKioskNotifications() != null && configuration.getKioskNotifications() ? true : null);
        data.setKioskSystemInfo(
                configuration.getKioskSystemInfo() != null && configuration.getKioskSystemInfo() ? true : null);
        data.setKioskKeyguard(
                configuration.getKioskKeyguard() != null && configuration.getKioskKeyguard() ? true : null);
        data.setKioskLockButtons(
                configuration.getKioskLockButtons() != null && configuration.getKioskLockButtons() ? true : null);
        data.setRestrictions(configuration.getRestrictions());

        if (settings != null) {
            if (settings.isCustomSend1()) {
                data.setCustom1(dbDevice.getCustom1());
            }

            if (settings.isCustomSend2()) {
                data.setCustom2(dbDevice.getCustom2());
            }

            if (settings.isCustomSend3()) {
                data.setCustom3(dbDevice.getCustom3());
            }

            if (settings.isSendDescription()) {
                data.setDescription(dbDevice.getDescription());
            }
        }

        // Evaluate the application settings
        final List<ApplicationSetting> deviceAppSettings = this.unsecureDAO.getDeviceAppSettings(dbDevice.getId());
        final List<ApplicationSetting> configApplicationSettings = configuration.getApplicationSettings();
        final List<ApplicationSetting> applicationSettings = combineDeviceLogRules(configApplicationSettings,
                deviceAppSettings);

        final Device dbDevice1 = dbDevice;
        data.setApplicationSettings(applicationSettings.stream().map(s -> {
            SyncApplicationSetting syncSetting = new SyncApplicationSetting();
            syncSetting.setPackageId(s.getApplicationPkg());
            syncSetting.setName(s.getName());
            syncSetting.setType(s.getType().getId());
            syncSetting.setReadonly(s.isReadonly());
            syncSetting.setValue(s.getValueForDevice(dbDevice1));
            syncSetting.setLastUpdate(s.getLastUpdate());

            return syncSetting;
        }).collect(Collectors.toList()));

        final List<ConfigurationFile> configurationFiles = this.unsecureDAO.getConfigurationFiles(dbDevice);
        configurationFiles.forEach(
                file -> {
                    if (file.getExternalUrl() != null) {
                        file.setUrl(file.getExternalUrl());
                    } else if (file.getFilePath() != null) {
                        final String url = FileUtil.createFileUrl(this.baseUrl, customer.getFilesDir(),
                                file.getFilePath());
                        file.setUrl(url);
                    }
                });

        data.setFiles(configurationFiles.stream().map(SyncConfigurationFile::new).collect(Collectors.toList()));

        // Rebranding data
        if (!mobileAppName.equals("")) {
            data.setAppName(mobileAppName);
        }
        if (!vendor.equals("")) {
            data.setVendor(vendor);
        }

        if (foundByImeiOrSerial) {
            data.setNewNumber(dbDevice.getNumber());
        }

        SyncResponseInt syncResponse = data;

        SecurityContext.init(dbDevice.getCustomerId());
        try {
            if (this.syncResponseHooks != null && !this.syncResponseHooks.isEmpty()) {
                for (SyncResponseHook hook : this.syncResponseHooks) {
                    syncResponse = hook.handle(dbDevice.getId(), syncResponse);
                }
            }
        } finally {
            SecurityContext.release();
        }

        response.setHeader(HEADER_IP_ADDRESS, remoteAddrResolver.getRemoteAddr(request));

        // Always add signature to enable "soft" security implementation
        // if (secureEnrollment) {
        // Add a signature to avoid MITM attack
        response.setHeader(HEADER_RESPONSE_SIGNATURE, CryptoUtil.getDataSignature(hashSecret, syncResponse));
        // }

        return Response.OK(syncResponse);

    }

    // =================================================================================================================
    @ApiOperation(value = "Update device info", notes = "Updates the device info on the MDM server.", response = Response.class)
    @POST
    @Path("/info")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateDeviceInfo(DeviceInfo deviceInfo,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response) {
        logger.debug("/public/sync/info --> {}", deviceInfo);

        try {
            Device dbDevice = this.unsecureDAO.getDeviceByNumber(deviceInfo.getDeviceId());
            boolean migration = false;

            if (dbDevice == null) {
                dbDevice = this.unsecureDAO.getDeviceByOldNumber(deviceInfo.getDeviceId());
                migration = dbDevice != null;
            }

            // Device creation on demand
            if (dbDevice == null) {
                if (unsecureDAO.isSingleCustomer()) {
                    dbDevice = unsecureDAO.createNewDeviceOnDemand(deviceInfo.getDeviceId());
                } else {
                    logger.warn("Not allowed to create devices in the multi-tenant setup");
                }
            }

            if (dbDevice != null) {
                if (!migration && dbDevice.getOldNumber() != null) {
                    // If a device sends data using a new device ID, the migration is completed
                    this.unsecureDAO.completeDeviceMigration(dbDevice.getId());
                    dbDevice.setOldNumber(null);
                }

                ObjectMapper objectMapper = new ObjectMapper();
                DeviceInfo prevInfo = null;
                try {
                    prevInfo = objectMapper.readValue(dbDevice.getInfo(), DeviceInfo.class);
                } catch (Exception e) {
                }
                if (prevInfo != null && prevInfo.getImei() != null && deviceInfo.getImei() != null &&
                        !prevInfo.getImei().equals(deviceInfo.getImei())) {
                    dbDevice.setImeiUpdateTs(System.currentTimeMillis());
                }
                this.unsecureDAO.updateDeviceInfo(dbDevice.getId(),
                        objectMapper.writeValueAsString(deviceInfo),
                        dbDevice.getImeiUpdateTs(),
                        remoteAddrResolver.getRemoteAddr(request));

                boolean needUpdate = false;
                if (deviceInfo.getCustom1() != null) {
                    dbDevice.setCustom1(deviceInfo.getCustom1());
                    needUpdate = true;
                }
                if (deviceInfo.getCustom2() != null) {
                    dbDevice.setCustom2(deviceInfo.getCustom2());
                    needUpdate = true;
                }
                if (deviceInfo.getCustom3() != null) {
                    dbDevice.setCustom3(deviceInfo.getCustom3());
                    needUpdate = true;
                }
                if (needUpdate) {
                    this.unsecureDAO.updateDeviceCustomProperties(dbDevice.getId(), dbDevice);
                }

                if (deviceInfo.getBatteryLevel() != null) {
                    this.eventService.fireEvent(
                            new DeviceBatteryLevelUpdatedEvent(dbDevice.getId(), deviceInfo.getBatteryLevel()));
                }

                final DeviceLocation location = deviceInfo.getLocation();
                if (location != null) {
                    List<DeviceLocation> locations = new LinkedList<>();
                    locations.add(deviceInfo.getLocation());
                    this.eventService.fireEvent(
                            new DeviceLocationUpdatedEvent(dbDevice.getId(), locations, false));
                }

                this.eventService.fireEvent(new DeviceInfoUpdatedEvent(dbDevice.getId()));

                response.setHeader(HEADER_IP_ADDRESS, remoteAddrResolver.getRemoteAddr(request));
                return Response.OK();
            } else {
                logger.warn("Requested device {} was not found", deviceInfo.getDeviceId());
                return Response.DEVICE_NOT_FOUND_ERROR();
            }
        } catch (Exception e) {
            logger.error("Unexpected error when processing info submitted by device", e);
            return Response.INTERNAL_ERROR();
        }
    }

    // =================================================================================================================
    @ApiOperation(value = "Save application settings", notes = "Saves the application settings for the device on the MDM server.", response = Response.class)
    @POST
    @Path("/applicationSettings/{deviceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveApplicationSettings(
            @PathParam("deviceId") @ApiParam("An identifier of device within MDM server") String deviceNumber,
            List<SyncApplicationSetting> applicationSettings) {
        logger.debug("/public/sync/applicationSettings/{} --> {}", deviceNumber, applicationSettings);

        try {
            Device dbDevice = this.unsecureDAO.getDeviceByNumber(deviceNumber);
            if (dbDevice != null) {
                this.unsecureDAO.saveDeviceApplicationSettings(dbDevice, applicationSettings.stream().map(s -> {
                    ApplicationSetting applicationSetting = new ApplicationSetting();
                    applicationSetting.setApplicationPkg(s.getPackageId());
                    applicationSetting.setName(s.getName());
                    applicationSetting
                            .setType(ApplicationSettingType.byId(s.getType()).orElse(ApplicationSettingType.STRING));
                    applicationSetting.setReadonly(s.isReadonly());
                    applicationSetting.setValue(s.getValue());
                    applicationSetting.setLastUpdate(s.getLastUpdate());

                    return applicationSetting;
                }).collect(Collectors.toList()));

                return Response.OK();
            } else {
                logger.warn("Requested device {} was not found", deviceNumber);
                return Response.DEVICE_NOT_FOUND_ERROR();
            }
        } catch (Exception e) {
            logger.error("Unexpected error when saving device application settings for device {}", deviceNumber, e);
            return Response.INTERNAL_ERROR();
        }
    }

    /**
     * <p>
     * A function producing the key for referencing the specified application
     * setting.
     * </p>
     */
    private static final Function<ApplicationSetting, String> appSettingMapKeyGenerator = (s) -> s.getApplicationId()
            + "," + s.getName();

    /**
     * <p>
     * Combines the specified list of application settings into a single list.
     * </p>
     *
     * @param lessPreferred a list of less preferred settings.
     * @param morePreferred a list of more preferred settings.
     * @return a resulting list of application settings.
     */
    private static List<ApplicationSetting> combineDeviceLogRules(List<ApplicationSetting> lessPreferred,
            List<ApplicationSetting> morePreferred) {

        lessPreferred = lessPreferred.stream()
                .filter(s -> s.getValue() != null && !s.getValue().trim().isEmpty())
                .collect(Collectors.toList());
        morePreferred = morePreferred.stream()
                .filter(s -> s.getValue() != null && !s.getValue().trim().isEmpty())
                .collect(Collectors.toList());

        final Map<String, ApplicationSetting> moreMapping = morePreferred.stream()
                .collect(Collectors.toMap(appSettingMapKeyGenerator, r -> r));

        List<ApplicationSetting> result = new ArrayList<>();

        lessPreferred.stream()
                .filter(less -> !moreMapping.containsKey(appSettingMapKeyGenerator.apply(less)))
                .forEach(result::add);

        result.addAll(morePreferred);

        return result;
    }

}