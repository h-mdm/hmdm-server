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

package com.hmdm.security;


import com.hmdm.persistence.domain.CustomerData;
import com.hmdm.persistence.domain.Icon;
import com.hmdm.persistence.domain.UploadedFile;

/**
 * <p>An unchecked exception to be thrown to indicate that there is security issue encountered.</p>
 *
 * @author isv
 */
public class SecurityException extends RuntimeException {

    /**
     * <p>An error code identifying the type of error.</p>
     */
    private final int errorCode;

    /**
     * <p>Constructs new <code>SecurityException</code> instance. This implementation does nothing.</p>
     */
    protected SecurityException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * <p>Constructs an exception to be thrown in case an unauthorized access to specified customerData is detected.</p>
     *
     * @param customerData a source of the exception.
     * @return a security exception to be thrown.
     */
    public static SecurityException onCustomerDataAccessViolation(CustomerData customerData) {
        return onCustomerDataAccessViolation(customerData.getCustomerId(), customerData.getId(), "generic");
    }

    /**
     * <p>Constructs an exception to be thrown in case an unauthorized access to specified application is detected. </p>
     *
     * @param application a source of the exception.
     * @return a security exception to be thrown.
     */
    public static SecurityException onApplicationAccessViolation(CustomerData application) {
        return onCustomerDataAccessViolation(application.getCustomerId(), application.getId(), "application");
    }

    /**
     * <p>Constructs an exception to be thrown in case an unauthorized access to specified application is detected. </p>
     *
     * @param id an ID of an application.
     * @return a security exception to be thrown.
     */
    public static SecurityException onApplicationAccessViolation(Integer id) {
        return onCustomerDataAccessViolation(id, "application");
    }

    /**
     * <p>Constructs an exception to be thrown in case an unauthorized access to specified application version is
     * detected. </p>
     *
     * @param id an ID of an application version.
     * @return a security exception to be thrown.
     */
    public static SecurityException onApplicationVersionAccessViolation(Integer id) {
        return onCustomerDataAccessViolation(id, "applicationVersion");
    }

    /**
     * <p>Constructs an exception to be thrown in case an unauthorized access to specified user account is detected.</p>
     *
     * @param user a source of the exception.
     * @return a security exception to be thrown.
     */
    public static SecurityException onUserAccessViolation(CustomerData user) {
        return onCustomerDataAccessViolation(user.getCustomerId(), user.getId(), "user");
    }

    /**
     * <p>Constructs an exception to be thrown in case an unauthorized access to specified settings is detected.</p>
     *
     * @param settings a source of the exception.
     * @return a security exception to be thrown.
     */
    public static SecurityException onSettingsAccessViolation(CustomerData settings) {
        return onCustomerDataAccessViolation(settings.getCustomerId(), settings.getId(), "settings");
    }

    /**
     * <p>Constructs an exception to be thrown in case an unauthorized access to specified configuration is detected.</p>
     *
     * @param configuration a source of the exception.
     * @return a security exception to be thrown.
     */
    public static SecurityException onConfigurationAccessViolation(CustomerData configuration) {
        return onCustomerDataAccessViolation(configuration.getCustomerId(), configuration.getId(), "configuration");
    }

    /**
     * <p>Constructs an exception to be thrown in case an unauthorized access to specified configuration is detected.</p>
     *
     * @return a security exception to be thrown.
     */
    public static SecurityException onAdminDataAccessViolation(String message) {
        return SecurityContext.get()
                .getCurrentUser()
                .map(u -> new SecurityException(String.format("Unauthorized attempt to %1$s by user %2$s", message, u.getLogin()), 403))
                .orElse(new SecurityException(String.format("Unauthorized attempt to %1$s by user anonymous user", message), 403));

    }

    /**
     * <p>Constructs an exception to be thrown in case an unauthorized access to specified configuration is detected.</p>
     *
     * @param id an ID of an configuration.
     * @return a security exception to be thrown.
     */
    public static SecurityException onConfigurationAccessViolation(Integer id) {
        return onCustomerDataAccessViolation(id, "configuration");
    }

    /**
     * <p>Constructs an exception to be thrown in case an unauthorized access to specified device is detected.</p>
     *
     * @param device a source of the exception.
     * @return a security exception to be thrown.
     */
    public static SecurityException onDeviceAccessViolation(CustomerData device) {
        return onCustomerDataAccessViolation(device.getCustomerId(), device.getId(), "device");
    }

    /**
     * <p>Constructs an exception to be thrown in case an unauthorized access to specified group is detected.</p>
     *
     * @param group a source of the exception.
     * @return a security exception to be thrown.
     */
    public static SecurityException onGroupAccessViolation(CustomerData group) {
        return onCustomerDataAccessViolation(group.getCustomerId(), group.getId(), "group");
    }

    /**
     * <p>Constructs an exception to be thrown in case an unauthorized access to specified icon is detected.</p>
     *
     * @param icon a source of the exception.
     * @return a security exception to be thrown.
     */
    public static SecurityException onIconAccessViolation(Icon icon) {
        return onCustomerDataAccessViolation(icon.getCustomerId(), icon.getId(), "icon");
    }

    /**
     * <p>Constructs an exception to be thrown in case an unauthorized access to specified uploaded file is detected.</p>
     *
     * @param file a source of the exception.
     * @return a security exception to be thrown.
     */
    public static SecurityException onUploadedFileAccessViolation(UploadedFile file) {
        return onCustomerDataAccessViolation(file.getCustomerId(), file.getId(), "file");
    }

    /**
     * <p>Constructs an exception to be thrown in case an unauthorized anonymous access to data is detected.</p>
     *
     * @return a security exception to be thrown.
     */
    public static SecurityException onAnonymousAccess() {
        return new SecurityException("Anonymous access prohibited", 403);
    }

    public static SecurityException onCustomerDataAccessViolation(int customerId, int entityId, String entityType) {
        return SecurityContext.get()
                .getCurrentUser()
                .map(u -> new SecurityException(String.format("Unauthorized attempt to access %3$s record #%2$s for customer #%1$s by user %4$s", customerId, entityId, entityType, u.getLogin()), 403))
                .orElse(new SecurityException(String.format("Unauthorized attempt to access %3$s record #%2$s for customer #%1$s by anonymous user", customerId, entityId, entityType), 403));
    }

    public static SecurityException onCustomerDataAccessViolation(int entityId, String entityType) {
        return SecurityContext.get()
                .getCurrentUser()
                .map(u -> new SecurityException(String.format("Unauthorized attempt to access %2$s record #%1$s by user %3$s", entityId, entityType, u.getLogin()), 403))
                .orElse(new SecurityException(String.format("Unauthorized attempt to access %2$s record #%1$s by anonymous user", entityId, entityType), 403));
    }
}
