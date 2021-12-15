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
import javax.inject.Named;

import com.google.inject.Singleton;
import com.hmdm.event.CustomerCreatedEvent;
import com.hmdm.event.DeviceInfoUpdatedEvent;
import com.hmdm.event.EventService;
import com.hmdm.persistence.domain.Device;
import com.hmdm.persistence.mapper.ApplicationMapper;
import com.hmdm.persistence.mapper.DeviceMapper;
import com.hmdm.rest.json.CustomerSearchRequest;
import com.hmdm.rest.json.PaginatedData;
import com.hmdm.util.PasswordUtil;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hmdm.persistence.domain.Application;
import com.hmdm.persistence.domain.Configuration;
import com.hmdm.persistence.domain.Customer;
import com.hmdm.persistence.domain.Settings;
import com.hmdm.persistence.domain.User;
import com.hmdm.persistence.domain.UserRole;
import com.hmdm.persistence.mapper.ConfigurationMapper;
import com.hmdm.persistence.mapper.CustomerMapper;
import com.hmdm.security.SecurityContext;
import com.hmdm.security.SecurityException;
import com.hmdm.util.CryptoUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>$END$</p>
 *
 * @author isv
 */
@Singleton
public class CustomerDAO {

    private final Logger log = LoggerFactory.getLogger(CustomerDAO.class);

    private final CustomerMapper mapper;
    private final ConfigurationMapper configurationMapper;
    private final ApplicationMapper applicationMapper;
    private final DeviceMapper deviceMapper;
    private final File filesDirectory;
    private final UserDAO userDAO;
    private final CommonDAO settingDAO;
    private final int orgAdminRoleId;
    private final EventService eventService;

    @Inject
    public CustomerDAO(CustomerMapper mapper,
                       ConfigurationMapper configurationMapper,
                       ApplicationMapper applicationMapper,
                       DeviceMapper deviceMapper,
                       UserDAO userDAO,
                       CommonDAO settingDAO,
                       @Named("files.directory") String filesDirectory,
                       @Named("role.orgadmin.id") int orgAdminRoleId,
                       EventService eventService) {
        this.mapper = mapper;
        this.configurationMapper = configurationMapper;
        this.applicationMapper = applicationMapper;
        this.deviceMapper = deviceMapper;
        this.filesDirectory = new File(filesDirectory);
        this.userDAO = userDAO;
        this.settingDAO = settingDAO;
        this.orgAdminRoleId = orgAdminRoleId;
        this.eventService = eventService;
    }

    public void removeCustomerById(Integer id) {
        if (!SecurityContext.get().isSuperAdmin()) {
            throw SecurityException.onAdminDataAccessViolation("delete customer by ID " + id);
        }

        Customer customer = this.mapper.findCustomerById(id);
        if (customer != null && !customer.isMaster()) {
            if (customer.getFilesDir() != null && !customer.getFilesDir().trim().isEmpty()) {
                File customerFilesDir = new File(this.filesDirectory, customer.getFilesDir());
                if (customerFilesDir.exists()) {
                    Path rootPath = Paths.get(customerFilesDir.getAbsolutePath());
                    try (Stream<Path> walk = Files.walk(rootPath)) {
                        walk
                                .sorted(Comparator.reverseOrder())
                                .map(Path::toFile)
                                .peek(System.out::println)
                                .forEach(File::delete);
                    } catch (IOException e) {
                        log.error("Failed to delete the customer's files directory {} due to unexpected error",
                                customer.getFilesDir(), e);
                    }
                }
            } else {
                log.warn("Skipping to delete the customer's files due to invalid files directory name: {}", customer.getFilesDir());
            }
            this.mapper.delete(id);
            log.info("Deleted customer account {}", customer);
        }
    }

    public List<Customer> getAllCustomers() {
        if (!SecurityContext.get().isSuperAdmin()) {
            throw SecurityException.onAdminDataAccessViolation("get the list of customers");
        }
        return this.mapper.findAllExceptMaster();
    }

    public List<Customer> getAllCustomersByValue(String value) {
        if (!SecurityContext.get().isSuperAdmin()) {
            throw SecurityException.onAdminDataAccessViolation("get the list of customers");
        }
        if (value == null) {
            value = "";
        }
        value = "%" + value + "%";

        return this.mapper.findAllByValue(value.toLowerCase());
    }

    public Customer getCustomerByName(String name) {
        if (!SecurityContext.get().isSuperAdmin()) {
            throw SecurityException.onAdminDataAccessViolation("get the customer by name " + name);
        }
        return this.mapper.findCustomerByName(name);
    }

    public Customer getCustomerByEmail(String email) {
        if (!SecurityContext.get().isSuperAdmin()) {
            throw SecurityException.onAdminDataAccessViolation("get the customer by email " + email);
        }
        return this.mapper.findCustomerByEmail(email);
    }

    private static final String[] DEFAULT_DEVICE_SUFFIXES = {"001", "002", "003"};

    public String insertCustomer(Customer customer) {
        if (!SecurityContext.get().isSuperAdmin()) {
            throw SecurityException.onAdminDataAccessViolation("create new customer account");
        }
        
        log.debug("Creating customer account: {}", customer);

        File customerFilesDir;
        do {
            customerFilesDir = new File(this.filesDirectory, UUID.randomUUID().toString());
        } while (customerFilesDir.exists());
        if (customerFilesDir.mkdirs()) {
            customer.setRegistrationTime(System.currentTimeMillis());
            customer.setFilesDir(customerFilesDir.getName());
            this.mapper.insert(customer);

            final Settings masterSettings = this.settingDAO.getSettings();

            // Create a customer admin record
            String password = PasswordUtil.generatePassword(masterSettings.getPasswordLength(), masterSettings.getPasswordStrength());

            UserRole orgAdminRole = new UserRole();
            orgAdminRole.setId(this.orgAdminRoleId);

            User user = new User();
            user.setCustomerId(customer.getId());
            user.setPassword(PasswordUtil.getHashFromRaw(password));
            user.setAuthToken(PasswordUtil.generateToken());
            if (masterSettings.isPasswordReset()) {
                user.setPasswordReset(true);
                user.setPasswordResetToken(PasswordUtil.generateToken());
            }
            user.setLogin(transliterate(customer.getName()));
            user.setName(customer.getName());
            user.setEmail(customer.getEmail());
            user.setUserRole(orgAdminRole);

            userDAO.insert(user);
            log.info("Created customer account: {}", customer);
            log.info("Created customer admin account: {}/{}", user.getLogin(), password);

            // Copy design settings if required
            Settings customerSettings = new Settings();

            if (customer.isCopyDesign()) {
                customerSettings.setBackgroundColor(masterSettings.getBackgroundColor());
                customerSettings.setBackgroundImageUrl(masterSettings.getBackgroundImageUrl());
                customerSettings.setDesktopHeader(masterSettings.getDesktopHeader());
                customerSettings.setDesktopHeaderTemplate(masterSettings.getDesktopHeaderTemplate());
                customerSettings.setIconSize(masterSettings.getIconSize());
                customerSettings.setTextColor(masterSettings.getTextColor());
                customerSettings.setCustomerId(customer.getId());

                this.settingDAO.saveDefaultDesignSettingsBySuperAdmin(customerSettings);
            }

            // Copy configurations if required
            Map<Integer, Integer> configIdsMapping = new HashMap<>();
            if (customer.getConfigurationIds() != null && customer.getConfigurationIds().length > 0) {
                for (Integer configurationId: customer.getConfigurationIds()) {
                    final Integer copyId = copyConfigurationForCustomer(customer, configurationId);
                    configIdsMapping.put(configurationId, copyId);
                }
            }
            log.debug("Mapping for original and copied configurations: {}", configIdsMapping);

            // Notify plugins about new customer so they could set up default settings for this customer
            this.eventService.fireEvent(new CustomerCreatedEvent(customer));

            // Generate three default devices
            for (int i = 0; i < DEFAULT_DEVICE_SUFFIXES.length; i++) {
                String deviceNumber = customer.getPrefix() + DEFAULT_DEVICE_SUFFIXES[i];
                Device newDevice = new Device();
                newDevice.setLastUpdate(0L);
                newDevice.setNumber(deviceNumber);
                newDevice.setConfigurationId(configIdsMapping.get(customer.getDeviceConfigurationId()));
                newDevice.setCustomerId(customer.getId());

                this.deviceMapper.insertDevice(newDevice);

                this.eventService.fireEvent(new DeviceInfoUpdatedEvent(newDevice.getId()));

                log.info("Created default device '{}' for new customer account", deviceNumber);
            }

            return user.getLogin() + "/" + password;
        } else {
            log.error("Could not create files directory when creating customer {}", customer);
            throw new IllegalArgumentException("Could not create directory for customer's file.");
        }
    }

    /**
     * <p>Creates the copy of specified master-customer configuration for specified customer account.</p>
     *
     * @param customer a customer account to create configuration for.
     * @param configurationId an ID of a configuration to copy.
     * @return an ID of configuration copy.
     */
    private Integer copyConfigurationForCustomer(Customer customer, Integer configurationId) {
        Configuration configurationTemplate = this.configurationMapper.getConfigurationById(configurationId);
        List<Application> configApplications = this.configurationMapper.getPlainConfigurationApplications(
                SecurityContext.get().getCurrentUser().get().getCustomerId(), configurationId
        );
        configApplications = configApplications
                .stream()
                .filter(Application::isCommon)
                .collect(Collectors.toList());

        Configuration newConfiguration = configurationTemplate.newCopy();
        newConfiguration.setCustomerId(customer.getId());

        configurationMapper.insertConfiguration(newConfiguration);
        if (!configApplications.isEmpty()) {
            configurationMapper.insertConfigurationApplications(newConfiguration.getId(), configApplications);
            applicationMapper.getPrecedingVersion(newConfiguration.getId());
        }

        return newConfiguration.getId();
    }

    @Transactional
    public void updateCustomer(Customer customer) {
        if (!SecurityContext.get().isSuperAdmin()) {
            throw SecurityException.onAdminDataAccessViolation("update customer account with ID " + customer.getId());
        }
        this.mapper.update(customer);

        log.info("Updated customer account: {}", customer);
    }

    private static String transliterate(String s) {
        s = s.toLowerCase();
        int n = s.length();

        StringBuilder b = new StringBuilder();
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);

            switch (c) {
                case('а'): b.append('a');break;
                case('б'): b.append('b');break;
                case('в'): b.append('v');break;
                case('г'): b.append('g');break;
                case('д'): b.append('d');break;
                case('е'): b.append('e');break;
                case('ё'): b.append('e');break;
                case('ж'): b.append("zh");break;
                case('з'): b.append('z');break;
                case('и'): b.append('i');break;
                case('й'): b.append('i');break;
                case('к'): b.append('k');break;
                case('л'): b.append('l');break;
                case('м'): b.append('m');break;
                case('н'): b.append('n');break;
                case('о'): b.append('o');break;
                case('п'): b.append('p');break;
                case('р'): b.append('r');break;
                case('с'): b.append('s');break;
                case('т'): b.append('t');break;
                case('у'): b.append('u');break;
                case('ф'): b.append('f');break;
                case('х'): b.append("kh");break;
                case('ц'): b.append("ts");break;
                case('ч'): b.append("ch");break;
                case('ш'): b.append("sh");break;
                case('щ'): b.append("shch");break;
                case('ъ'): b.append("ie");break;
                case('ы'): b.append('y');break;
                case('ь'): b.append('-');break;
                case('э'): b.append('e');break;
                case('ю'): b.append("yu");break;
                case('я'): b.append("ya");break;
                case(' '): b.append("_");break;
                default: b.append(c);
            }
        }

        return b.toString();
    }

    public Customer findById(int customerId) {
        return mapper.findCustomerById(customerId);
    }

    public Customer findByIdForUpdate(int customerId) {
        return mapper.findCustomerByIdForUpdate(customerId);
    }

    /**
     * <p>Checks if specified prefix is already used for some customer account.</p>
     *
     * @return <code>true</code> if prefix is already used; <code>false</code> otherwise.
     */
    public boolean isPrefixUsed(String prefix) {
        return mapper.isPrefixUsed(prefix);
    }

    /**
     * <p>Records the specified time of login the user related to specified customer account.</p>
     *
     * @param customerId an ID of a customer account related to authenticated user.
     * @param time a timestamp of successful authentication (in milliseconds since epoch).
     */
    public void recordLastLoginTime(int customerId, long time) {
        this.mapper.recordLastLoginTime(customerId, time);
    }

    /**
     * <p>Searches for the customer accounts matching the specified criteria.</p>
     *
     * @param request the parameters for customers search.
     * @return a list of customer accounts matching the search parameters.
     */
    public PaginatedData<Customer> searchCustomers(CustomerSearchRequest request) {
        final List<Customer> customers = this.mapper.searchCustomers(request);
        final Long totalItemsCount = this.mapper.countAllCustomers(request);
        return new PaginatedData<>(customers, totalItemsCount);
    }

    public Customer getSingleCustomer() {
        return mapper.findCustomerById(1);
    }

}
