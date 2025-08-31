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

package com.hmdm.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hmdm.event.CustomerCreatedEvent;
import com.hmdm.event.DeviceInfoUpdatedEvent;
import com.hmdm.event.EventService;
import com.hmdm.persistence.domain.*;
import com.hmdm.persistence.mapper.*;
import com.hmdm.rest.json.DeviceCreateOptions;
import com.hmdm.rest.json.DeviceListHook;
import com.hmdm.rest.json.LookupItem;
import com.hmdm.rest.json.PaginatedData;
import com.hmdm.security.SecurityContext;
import com.hmdm.security.SecurityException;
import com.hmdm.util.CryptoUtil;
import com.hmdm.util.PasswordUtil;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>A DAO which does not perform any security checks when accessing/updating data. It is intended for processing
 * requests from anonymous clients (for example, devices).</p>
 *
 * @author isv
 */
@Singleton
public class UnsecureDAO {

    private static final Logger logger = LoggerFactory.getLogger(UnsecureDAO.class);

    private final DeviceMapper deviceMapper;
    private final UserMapper userMapper;
    private final ConfigurationMapper configurationMapper;
    private final CommonMapper settingsMapper;
    private final ApplicationMapper applicationMapper;
    private final UploadedFileMapper uploadedFileMapper;
    private final ApplicationDAO applicationDAO;
    private final ApplicationSettingDAO applicationSettingDAO;
    private final UserDAO userDAO;
    private final CommonDAO settingDAO;
    private final CustomerDAO customerDAO;
    private final ConfigurationFileMapper configurationFileMapper;
    private final CustomerMapper customerMapper;
    private final String defaultLauncherPackage;
    private final File filesDirectory;
    private final int orgAdminRoleId;
    private final EventService eventService;

    private static final int DEFAULT_CUSTOMER_ID = 1;

    /**
     * <p>Constructs new <code>UnsecureDAO</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public UnsecureDAO(DeviceMapper deviceMapper,
                       UserMapper userMapper,
                       ConfigurationMapper configurationMapper,
                       CommonMapper settingsMapper,
                       ApplicationMapper applicationMapper,
                       UploadedFileMapper uploadedFileMapper,
                       ApplicationDAO applicationDAO,
                       ApplicationSettingDAO applicationSettingDAO,
                       UserDAO userDAO,
                       CommonDAO settingDAO,
                       CustomerDAO customerDAO,
                       ConfigurationFileMapper configurationFileMapper,
                       CustomerMapper customerMapper,
                       EventService eventService,
                       @Named("files.directory") String filesDirectory,
                       @Named("role.orgadmin.id") int orgAdminRoleId,
                       @Named("launcher.package") String defaultLauncherPackage) {
        this.deviceMapper = deviceMapper;
        this.userMapper = userMapper;
        this.configurationMapper = configurationMapper;
        this.settingsMapper = settingsMapper;
        this.applicationMapper = applicationMapper;
        this.uploadedFileMapper = uploadedFileMapper;
        this.applicationDAO = applicationDAO;
        this.applicationSettingDAO = applicationSettingDAO;
        this.userDAO = userDAO;
        this.settingDAO = settingDAO;
        this.customerDAO = customerDAO;
        this.configurationFileMapper = configurationFileMapper;
        this.customerMapper = customerMapper;
        this.eventService = eventService;
        this.filesDirectory = new File(filesDirectory);
        this.orgAdminRoleId = orgAdminRoleId;
        this.defaultLauncherPackage = defaultLauncherPackage;
    }

    public User findByLoginOrEmail(String login) {
        User user = userMapper.findByLogin(login);
        if (user == null) {
            user = userMapper.findByEmail(login);
        }
        return user;
    }

    public User findByLogin(String login) {
        return userMapper.findByLogin(login);
    }

    public User findByEmail(String email) {
        return userMapper.findByEmail(email);
    }

    public User findByPasswordResetToken( String token ) {
        return userMapper.findByPasswordResetToken(token);
    }

    public List<User> findAllWithOldPassword() {
        return userMapper.findAllWithOldPassword();
    }

    public void updateUserUnsecure(User user) {
        if (user.getId() == null) {
            userMapper.insert(user);
        } else {
            userMapper.updateUserMainDetails(user);
        }
    }

    public void setUserNewPasswordUnsecure(User user ) {
        userMapper.setNewPassword(user);
    }

    public void setUserLoginFailTime(User user, long ts) {
        user.setLastLoginFail(ts);
        userMapper.setLoginFailTime(user);
    }

    public void resetUserLoginFailTime() {
        userMapper.resetLoginFailTime();
    }

    public Device getDeviceByNumber(String number) {
        return this.deviceMapper.getDeviceByNumber(number);
    }

    public Device getDeviceByOldNumber(String number) {
        return this.deviceMapper.getDeviceByOldNumber(number);
    }

    public Device getDeviceByImeiOrSerial(String number) {
        return this.deviceMapper.getDeviceByImeiOrSerial(number);
    }

    public List<ApplicationSetting> getDeviceAppSettings(int deviceId) {
        final List<ApplicationSetting> appSettings
                = this.applicationSettingDAO.getApplicationSettingsByDeviceId(deviceId);
        return appSettings;
    }

    public void updateDeviceInfo(Integer id, String info, Long imeiUpdateTs, String publicIp) {
        this.deviceMapper.updateDeviceInfo(id, info, imeiUpdateTs, publicIp);
    }

    public void updateDeviceCustomProperties(Integer id, Device device) {
        this.deviceMapper.updateDeviceCustomProperties(id, device.getCustom1(), device.getCustom2(), device.getCustom3());
    }

    public void completeDeviceMigration(Integer id) {
        this.deviceMapper.clearOldNumber(id);
    }

    // This method should be called in a single-tenant mode only
    // and the device customer ID should be set
    @Transactional
    public void insertDevice(Device device) {
        this.deviceMapper.insertDevice(device);
        if (device.getGroups() != null && !device.getGroups().isEmpty()) {
            this.deviceMapper.insertDeviceGroups(
                    device.getId(), device.getGroups().stream().map(LookupItem::getId).collect(Collectors.toList())
            );
        }
    }

    @Transactional
    public List<Application> getPlainConfigurationApplications(Integer customerId, Integer id) {
        String tblName = "ca" + CryptoUtil.randomHexString(8);
        configurationMapper.createTempConfigAppTable(tblName, id);
        return this.configurationMapper.getPlainConfigurationApplications(customerId, tblName, id);
    }

    public Configuration getConfigurationById(Integer id) {
        return this.configurationMapper.getConfigurationById(id);
    }

    @Transactional
    public Configuration getConfigurationByIdWithAppSettings(Integer id) {
        final Configuration dbConfiguration = this.configurationMapper.getConfigurationById(id);
        if (dbConfiguration != null) {
            final List<ApplicationSetting> appSettings = this.applicationSettingDAO.getApplicationSettingsByConfigurationId(dbConfiguration.getId());
            dbConfiguration.setApplicationSettings(appSettings);
        }

        return dbConfiguration;
    }

    public Settings getSettings(int customerId) {
        return this.settingsMapper.getSettings(customerId);
    }

    public Settings getSingleCustomerSettings() {
        return this.settingsMapper.getSettings(DEFAULT_CUSTOMER_ID);
    }

    public List<Application> findByPackageIdAndVersion(Integer customerId, String pkg, String version) {
        return this.applicationMapper.findByPackageIdAndVersion(customerId, pkg, version);
    }

    /**
     * <p>Builds the lookup map from application package ID to application ID for specified packages and customer
     * account.</p>
     *
     * @param customerId an ID of a customer record.
     * @param appPackages a collection of application package IDs to build mapping for.
     * @return a mapping from application package ID to application ID.
     */
    public Map<String, Integer> buildPackageIdMapping(Integer customerId, Collection<String> appPackages) {
        if (appPackages == null || appPackages.isEmpty()) {
            return new HashMap<>();
        }

        List<LookupItem> ddd = this.applicationMapper.resolveAppsByPackageId(customerId, appPackages);
        return ddd.stream().collect(Collectors.toMap(LookupItem::getName, LookupItem::getId, (r1, r2) -> r1));
    }

    public void insertApplication(Application application) {
        final List<User> users = this.userMapper.findAll(application.getCustomerId());
        if (!users.isEmpty()) {
            final User user = users.stream().filter(u -> !u.getUserRole().isSuperAdmin()).findAny().orElse(users.get(0));
            logger.info("Using user account '{}' for setting up the security context when uploading application {} " +
                    "from mobile device", user.getLogin(), application);

            SecurityContext.init(user);
            try {
                this.applicationDAO.insertApplication(application);
            } finally {
                SecurityContext.release();
            }
        } else {
            throw new DAOException("No user accounts have been found mapped to requested customer account: #"
                    + application.getCustomerId());
        }
    }

    public Application findApplicationById(Integer appId) {
        Application app = this.applicationMapper.findById(appId);
        return app;
    }

    public ApplicationVersion findApplicationVersionById(Integer appId) {
        ApplicationVersion app = this.applicationMapper.findVersionById(appId);
        return app;
    }


    public Configuration getConfigurationByQRCodeKey(String id) {
        return this.configurationMapper.getConfigurationByQRCodeKey(id);
    }

    private static final Function<ApplicationSetting, String> appSettingMapKeyGenerator = (s) -> s.getApplicationPkg() + "," + s.getName();


    @Transactional
    public void saveDeviceApplicationSettings(Device dbDevice,
                                              List<ApplicationSetting> applicationSettings) {

        final Map<String, ApplicationSetting> dbDeviceAppSettingsMapping
                = this.applicationSettingDAO.getApplicationSettingsByDeviceId(dbDevice.getId())
                .stream()
                .collect(Collectors.toMap(appSettingMapKeyGenerator, s -> s, (r1, r2) -> r1));

        final Map<String, ApplicationSetting> appSettingsMapping
                = applicationSettings
                .stream()
                .filter(s -> s.getValue() != null && !s.getValue().trim().isEmpty())
                .collect(Collectors.toMap(appSettingMapKeyGenerator, s -> s, (r1, r2) -> r1));

        List<ApplicationSetting> mergedApplicationSettings = new ArrayList<>();

        dbDeviceAppSettingsMapping.values().forEach(dbSetting -> {
            final String dbSettingKey = appSettingMapKeyGenerator.apply(dbSetting);
            if (appSettingsMapping.containsKey(dbSettingKey)) {
                final ApplicationSetting appSetting = appSettingsMapping.get(dbSettingKey);
                if (appSetting.getLastUpdate() < dbSetting.getLastUpdate()) {
                    mergedApplicationSettings.add(dbSetting);
                } else {
                    mergedApplicationSettings.add(appSetting);
                }
            } else {
                mergedApplicationSettings.add(dbSetting);
            }
        });

        final Map<String, Application> appsMapping = this.applicationMapper.getAllApplications(dbDevice.getCustomerId())
                .stream()
                .collect(Collectors.toMap(Application::getPkg, a -> a, (r1, r2) -> r1));

        appSettingsMapping.values().forEach(appSetting -> {
            final String appSettingKey = appSettingMapKeyGenerator.apply(appSetting);
            if (!dbDeviceAppSettingsMapping.containsKey(appSettingKey)) {
                mergedApplicationSettings.add(appSetting);
            }
        });

        mergedApplicationSettings.forEach(appSetting -> {
            if (appSetting.getApplicationId() == null) {
                if (appsMapping.containsKey(appSetting.getApplicationPkg())) {
                    appSetting.setApplicationId(appsMapping.get(appSetting.getApplicationPkg()).getId());
                } else {
                    // TODO : Log a warning on unknown package ID
                }
            }
        });

        this.deviceMapper.deleteDeviceApplicationSettings(dbDevice.getId());
        if (!mergedApplicationSettings.isEmpty()) {
            final List<ApplicationSetting> validSettings = mergedApplicationSettings
                    .stream()
                    .filter(appSetting -> appSetting.getApplicationId() != null)
                    .collect(Collectors.toList());
            this.deviceMapper.insertDeviceApplicationSettings(dbDevice.getId(), validSettings);
        }
    }

    /**
     * <p>Saves the hash value for APK-file associated with the specified aplication version.</p>
     *
     * @param appVersionId an application version ID to save the hash value for APK file for.
     * @param hashValue a hash-value to be saved.
     */
    public void saveApkFileHash(Integer appVersionId, String hashValue) {
        this.applicationMapper.saveApkFileHash(appVersionId, hashValue);
    }

    /**
     * To use in impersonated background services only
     */
    public List<Device> getAllGroupDevices(int groupId, int customerId) {
        return this.deviceMapper.getAllGroupDevices(groupId, customerId);
    }

    public List<Device> getAllConfigurationDevices(int configurationId, int customerId) {
        return this.deviceMapper.getAllConfigurationDevices(configurationId, customerId);
    }

    public List<Device> getAllCustomerDevices(int customerId) {
        return this.deviceMapper.getAllCustomerDevices(customerId);
    }

    /**
     * <p>Gets the device referenced by the specified ID.</p>
     *
     * @param id an ID of a device.
     * @return a device referenced by the specified ID or <code>null</code> if there is no such device found.
     */
    public Device getDeviceById(Integer id) {
        return this.deviceMapper.getDeviceById(id);
    }

    /**
     * <p>Gets the list of configuration files to be used on device.</p>
     *
     * @param device a device to get the configuration files for.
     * @return a list of configuration files to be used on device.
     */
    public List<ConfigurationFile> getConfigurationFiles(Device device) {
        return this.configurationFileMapper.getConfigurationFiles(device.getConfigurationId());
    }

//    /**
//     * <p>Gets the settings for the customer account mapped to specified device.</p>
//     *
//     * @param deviceId a device number identifying the device.
//     * @return the settings for related customer account.
//     */
//    public Settings getSettingsByDeviceId(String deviceId) {
//        return this.settingsMapper.getSettingsByDeviceId(deviceId);
//    }

    /**
     * <p>Tests if the current installation is single-customer</p>
     *
     * @return true if single-customer, false otherwise
     */
    public boolean isSingleCustomer() {
        return !customerMapper.isMultiTenant();
    }

    // This should only be called from the tasks, not from the web resource methods
    public List<Customer> getAllCustomersUnsecure() {
        return customerMapper.findAll();
    }

    public List<Customer> getFollowUpCustomersUnsecure() {
        return customerMapper.findFollowedUp();
    }

    public void updateCustomerUnsecure(Customer customer) {
        customerMapper.update(customer);
    }

    public Customer getCustomerByEmailUnsecure(String email) {
        return customerMapper.findCustomerByEmail(email);
    }

    public Customer getCustomerByNameUnsecure(String name) {
        return customerMapper.findCustomerByName(name);
    }

    public Customer getCustomerByIdUnsecure(Integer id) {
        return customerMapper.findCustomerById(id);
    }

    public void signupCustomerUnsecure(Customer customer, String passwordMD5, boolean copySettings) {

        logger.debug("Registering customer account: {}", customer);

        File customerFilesDir;
        do {
            customerFilesDir = new File(this.filesDirectory, UUID.randomUUID().toString());
        } while (customerFilesDir.exists());
        if (customerFilesDir.mkdirs()) {
            customer.setRegistrationTime(System.currentTimeMillis());
            customer.setFilesDir(customerFilesDir.getName());
            generatePrefix(customer);
            customerMapper.insert(customer);

            // Here we assume that nobody will delete the default customer!
            final Settings masterSettings = getSingleCustomerSettings();

            // Create a customer admin record
            UserRole orgAdminRole = new UserRole();
            orgAdminRole.setId(this.orgAdminRoleId);

            User user = new User();
            user.setCustomerId(customer.getId());
            user.setPassword(PasswordUtil.getHashFromMd5(passwordMD5));
            user.setAuthToken(PasswordUtil.generateToken());
            user.setLogin(customer.getName());
            user.setName(customer.getName());
            user.setEmail(customer.getEmail());
            user.setUserRole(orgAdminRole);

            // Works here without a security context, see notice to UserDAO.insert()
            userDAO.insert(user);
            logger.info("Registered customer account: {}", customer);
            logger.info("Registered customer admin account: {}", user.getLogin());

            // Copy design settings if required
            Settings customerSettings = new Settings();

            if (copySettings) {
                customerSettings.setBackgroundColor(masterSettings.getBackgroundColor());
                customerSettings.setBackgroundImageUrl(masterSettings.getBackgroundImageUrl());
                customerSettings.setDesktopHeader(masterSettings.getDesktopHeader());
                customerSettings.setDesktopHeaderTemplate(masterSettings.getDesktopHeaderTemplate());
                customerSettings.setIconSize(masterSettings.getIconSize());
                customerSettings.setTextColor(masterSettings.getTextColor());
                customerSettings.setCustomerId(customer.getId());

                settingDAO.saveDefaultDesignSettingsBySuperAdmin(customerSettings);
            }

            // Copy configurations if required
            Map<Integer, Integer> configIdsMapping = new HashMap<>();
            if (customer.getConfigurationIds() != null && customer.getConfigurationIds().length > 0) {
                for (Integer configurationId: customer.getConfigurationIds()) {
                    final Integer copyId = customerDAO.copyConfigurationForCustomer(customer, DEFAULT_CUSTOMER_ID, configurationId);
                    configIdsMapping.put(configurationId, copyId);
                }
            }
            logger.debug("Mapping for original and copied configurations: {}", configIdsMapping);

            // Notify plugins about new customer so they could set up default settings for this customer
            eventService.fireEvent(new CustomerCreatedEvent(customer));

            // Generate three default devices
            for (int i = 0; i < CustomerDAO.DEFAULT_DEVICE_SUFFIXES.length; i++) {
                String deviceNumber = customer.getPrefix() + CustomerDAO.DEFAULT_DEVICE_SUFFIXES[i];
                Device newDevice = new Device();
                newDevice.setLastUpdate(0L);
                newDevice.setNumber(deviceNumber);
                newDevice.setConfigurationId(configIdsMapping.get(customer.getDeviceConfigurationId()));
                newDevice.setCustomerId(customer.getId());

                deviceMapper.insertDevice(newDevice);

                eventService.fireEvent(new DeviceInfoUpdatedEvent(newDevice.getId()));

                logger.info("Created default device '{}' for new customer account", deviceNumber);
            }
        } else {
            logger.error("Could not create files directory when creating customer {}", customer);
            throw new IllegalArgumentException("Could not create directory for customer's file.");
        }
    }

    private void generatePrefix(Customer customer) {
        String prefix;

        for (int n = 2; n < customer.getName().length(); n++) {
            prefix = customer.getName().substring(0, n).toLowerCase();
            if (!customerMapper.isPrefixUsed(prefix)) {
                customer.setPrefix(prefix);
                return;
            }
        }
        // The customer username is unique, so it could always be used as a prefix
        // So we shouldn't be here, but set the prefix anyway
        customer.setPrefix(customer.getName().toLowerCase());
    }

    public Application getDefaultLauncher() {
        List<Application> apps = this.applicationMapper.findByPackageId(DEFAULT_CUSTOMER_ID, defaultLauncherPackage);
        if (apps.size() == 0) {
            return null;
        }
        return apps.get(0);
    }


    public Device createNewDeviceOnDemand(String deviceId) {

        Settings settings = getSingleCustomerSettings();
        if (settings.isCreateNewDevices()) {
            Device newDevice = new Device();
            newDevice.setCustomerId(settings.getCustomerId());
            newDevice.setConfigurationId(settings.getNewDeviceConfigurationId());
            Integer groupId = settings.getNewDeviceGroupId();
            if (groupId != null) {
                List<LookupItem> groups = new LinkedList<>();
                groups.add(new LookupItem(groupId, ""));
                newDevice.setGroups(groups);
            }
            newDevice.setNumber(deviceId);
            newDevice.setLastUpdate(0L);
            insertDevice(newDevice);
            logger.info("New device {} added", deviceId);

            return getDeviceByNumber(deviceId);
        } else {
            logger.warn("Creating new devices disabled by settings");
            return null;
        }
    }

    public Device createNewDeviceOnDemand(String deviceId, DeviceCreateOptions createOptions) {
        int customerId = DEFAULT_CUSTOMER_ID;
        int deviceLimit = 0;
        int deviceCount = 0;
        if (!isSingleCustomer()) {
            if (createOptions.getCustomer() == null) {
                logger.warn("Customer is not set, device not created");
                return null;
            }
            Customer customer = customerMapper.findCustomerByName(createOptions.getCustomer());
            if (customer != null) {
                customerId = customer.getId();
            } else {
                logger.warn("Failed to get a customer by name '" + createOptions.getCustomer() + "', device not created");
                return null;
            }
            deviceLimit = customer.getDeviceLimit();
            Long deviceCountLong = deviceMapper.countAllDevicesForCustomer(customerId);
            if (deviceCountLong != null) {
                deviceCount = deviceCountLong.intValue();
            }
        }

        if (deviceLimit != 0 && deviceCount >= deviceLimit) {
            logger.warn("New device " + deviceId + " not created by customer " + customerId + ": license limit");
            return null;
        }

        Settings settings = getSettings(customerId);
        Device newDevice = new Device();
        newDevice.setCustomerId(customerId);

        // If the configuration is specified, we want to create a new device, so don't check the legacy setting
        if (createOptions.getConfiguration() != null) {
            int configId = 0;
            try {
                configId = Integer.parseInt(createOptions.getConfiguration());
            } catch (NumberFormatException e) {
                logger.warn("Configuration id must be integer: '" + createOptions.getConfiguration() + "', device not created");
                return null;
            }
            Configuration configuration = configurationMapper.getConfigurationById(configId);
            if (configuration == null) {
                logger.warn("Failed to get a configuration by id " + createOptions.getConfiguration() + ", device not created");
                return null;
            } else if (configuration.getCustomerId() != customerId) {
                logger.warn("Configuration id " + createOptions.getConfiguration() + " doesn't belong to customer " +
                        customerId + ", device not created");
                return null;
            } else {
                newDevice.setConfigurationId(configuration.getId());
            }
        } else if (settings != null && settings.isCreateNewDevices()) {
            // Configuration not specified, we will add a device only if a legacy setting "Create new devices" is set
            newDevice.setConfigurationId(settings.getNewDeviceConfigurationId());
        } else {
            logger.warn("Creating new devices disabled by settings, configuration ID not set");
            return null;
        }

        if (createOptions.getGroups() != null) {
            List<LookupItem> groups = new LinkedList<>();
            for (String s : createOptions.getGroups()) {
                int groupId = 0;
                try {
                    groupId = Integer.parseInt(s);
                } catch (Exception e) {
                    logger.warn("Group id must be integer: '" + s + "', group ignored");
                    continue;
                }
                Group group = deviceMapper.getGroupById(groupId);
                if (group == null) {
                    logger.warn("Failed to find group by id: " + s + ", group ignored");
                } else if (group.getCustomerId() != customerId) {
                    logger.warn("Group id " + s + " doesn't belong to customer " + customerId + ", group ignored");
                } else {
                    groups.add(new LookupItem(group.getId(), ""));
                }
            }
            if (groups.size() > 0) {
                newDevice.setGroups(groups);
            }
        } else if (settings != null) {
            Integer groupId = settings.getNewDeviceGroupId();
            if (groupId != null) {
                List<LookupItem> groups = new LinkedList<>();
                groups.add(new LookupItem(groupId, ""));
                newDevice.setGroups(groups);
            }
        }

        newDevice.setNumber(deviceId);
        newDevice.setLastUpdate(0L);
        insertDevice(newDevice);
        logger.info("New device {} added", deviceId);

        return getDeviceByNumber(deviceId);
    }

    public void updateDeviceFastSearch(int fastSearchChars) {
        deviceMapper.updateFastSearch(fastSearchChars);
    }

    public UserRole findRoleByNameUnsecure(String name) {
        return userMapper.findUserRoleByName(name);
    }

    public UploadedFile getUploadedFileByPath(Integer customerId, String path) {
        return uploadedFileMapper.findByPath(customerId, path);
    }

    public int insertUploadedFile(UploadedFile uploadedFile) {
        return uploadedFileMapper.insert(uploadedFile);
    }

    public void updateUploadedFile(UploadedFile uploadedFile) {
        uploadedFileMapper.update(uploadedFile);
    }

}
