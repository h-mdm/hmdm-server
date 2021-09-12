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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.google.inject.Singleton;
import javax.inject.Named;
import com.hmdm.persistence.domain.ApplicationVersion;
import com.hmdm.rest.json.APKFileDetails;
import com.hmdm.rest.json.ApplicationConfigurationLink;
import com.hmdm.rest.json.ApplicationVersionConfigurationLink;
import com.hmdm.rest.json.LinkConfigurationsToAppRequest;
import com.hmdm.rest.json.LinkConfigurationsToAppVersionRequest;
import com.hmdm.rest.json.LookupItem;
import com.hmdm.util.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.glassfish.jersey.jaxb.internal.XmlJaxbElementProvider;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hmdm.persistence.domain.Application;
import com.hmdm.persistence.domain.Customer;
import com.hmdm.persistence.mapper.ApplicationMapper;
import com.hmdm.security.SecurityContext;
import com.hmdm.security.SecurityException;

import javax.validation.constraints.NotNull;

@Singleton
public class ApplicationDAO extends AbstractLinkedDAO<Application, ApplicationConfigurationLink> {

    private static final Logger log = LoggerFactory.getLogger(ApplicationDAO.class);

    private final ApplicationMapper mapper;
    private final CustomerDAO customerDAO;
    private final String filesDirectory;
    private final String baseUrl;
    private APKFileAnalyzer apkFileAnalyzer;

    @Inject
    public ApplicationDAO(ApplicationMapper mapper, CustomerDAO customerDAO,
                          @Named("files.directory") String filesDirectory,
                          @Named("base.url") String baseUrl,
                          APKFileAnalyzer apkFileAnalyzer) {
        this.mapper = mapper;
        this.customerDAO = customerDAO;
        this.filesDirectory = filesDirectory;
        this.baseUrl = baseUrl;
        this.apkFileAnalyzer = apkFileAnalyzer;
    }

    public List<Application> getAllApplications() {
        return getList(this.mapper::getAllApplications);
    }

    public List<Application> getAllApplicationsByValue(String value) {
        return getList(customerId -> this.mapper.getAllApplicationsByValue(customerId, "%" + value + "%"));
    }

    public List<Application> getAllApplicationsByUrl(String url) {
        List<Application> appList = getList(customerId -> this.mapper.getAllApplicationsByUrl(customerId, url));
        if (appList == null || appList.size() == 0) {
            // There's an issue with the last slash, it's sometimes duplicated!
            int i = url.lastIndexOf('/');
            if (i != -1) {
                String url1 = url.substring(0, i) + '/' + url.substring(i);
                return getList(customerId -> this.mapper.getAllApplicationsByUrl(customerId, url1));
            }
        }
        return appList;
    }

    /**
     * <p>Creates new application record in DB.</p>
     *
     * @param application an application record to be created.
     * @throws DuplicateApplicationException if another application record with same package ID and version already
     *         exists either for current or master customer account.
     */
    @Transactional
    public int insertApplication(Application application) {
        log.debug("Entering #insertApplication: application = {}", application);

        // If an APK-file was set for new app then make the file available in Files area and parse the app parameters
        // from it (package ID, version)
        final String filePath = application.getFilePath();
        if (filePath != null && !filePath.trim().isEmpty()) {
            final int customerId = SecurityContext.get().getCurrentUser().get().getCustomerId();
            Customer customer = customerDAO.findById(customerId);

            File movedFile = null;
            try {
                movedFile = FileUtil.moveFile(customer, filesDirectory, null, filePath);
            } catch (FileExistsException e) {
                FileUtil.deleteFile(filesDirectory, FileUtil.getNameFromTmpPath(filePath));
                movedFile = FileUtil.moveFile(customer, filesDirectory, null, filePath);
            }
            if (movedFile != null) {
                final String fileName = movedFile.getAbsolutePath();
                final APKFileDetails apkFileDetails = this.apkFileAnalyzer.analyzeFile(fileName);

                // If URL is not specified explicitly for new app then set the application URL to reference to that
                // file
                if ((application.getUrl() == null || application.getUrl().trim().isEmpty())) {
                    application.setUrl(this.baseUrl + "/files/" + customer.getFilesDir() + "/" + movedFile.getName());
                }

                application.setPkg(apkFileDetails.getPkg());
                application.setVersion(apkFileDetails.getVersion());
                application.setArch(apkFileDetails.getArch());
            } else {
                log.error("Could not move the uploaded .apk-file {}", filePath);
                throw new DAOException("Could not move the uploaded .apk-file");
            }
        }

        insertRecord(application, this.mapper::insertApplication);
        final ApplicationVersion applicationVersion = new ApplicationVersion(application);

        this.mapper.insertApplicationVersion(applicationVersion);
        this.mapper.recalculateLatestVersion(application.getId());

        return application.getId();
    }

    /**
     * <p>Creates new application record in DB.</p>
     *
     * @param application an application record to be created.
     */
    @Transactional
    public int insertWebApplication(Application application) {
        log.debug("Entering #insertWebApplication: application = {}", application);

        final Optional<Integer> currentCustomerId = SecurityContext.get().getCurrentCustomerId();
        if (currentCustomerId.isPresent()) {
            String pkg;
            Long count;
            do {
                pkg = DigestUtils.sha1Hex(application.getUrl() + System.currentTimeMillis());
                count = this.mapper.countByPackageId(currentCustomerId.get(), pkg);
            } while (count > 0);

            application.setPkg(pkg);
            application.setVersion("0");

            insertRecord(application, this.mapper::insertApplication);

            final ApplicationVersion applicationVersion = new ApplicationVersion(application);

            this.mapper.insertApplicationVersion(applicationVersion);
            this.mapper.recalculateLatestVersion(application.getId());

            return application.getId();
        } else {
            throw SecurityException.onAnonymousAccess();
        }
    }

    /**
     * <p>Checks if another application with same package ID and version already exists or not.</p>
     *
     * @param application an application to check against duplicates.
     * @throws DuplicateApplicationException if a duplicated application is found.
     */
    private void guardDuplicateApp(Application application) {
        if (application.getPkg() != null && application.getVersion() != null) {
            final List<Application> dbApps = findByPackageIdAndVersion(application.getPkg(), application.getVersion());
            if (!dbApps.isEmpty()) {
                throw new DuplicateApplicationException(application.getPkg(), application.getVersion(), dbApps.get(0).getCustomerId());
            }
        }
    }

    /**
     * <p>Checks if another application version with same version number already exists for specified application or
     * not.</p>
     *
     * @param application an application to check against duplicates.
     * @param version an application version to check against duplicates.
     * @throws DuplicateApplicationException if a duplicated application is found.
     */
    private void guardDuplicateAppVersion(Application application, ApplicationVersion version) {
        if (getDuplicateAppVersion(application, version) != 0) {
            throw new DuplicateApplicationException(application.getPkg(), version.getVersion(), application.getCustomerId());
        }
    }

    /**
     * <p>Checks if another application version with same version number already exists for specified application or
     * not.</p>
     *
     * @param application an application to check against duplicates.
     * @param version an application version to check against duplicates.
     * @return id of a duplicated application if found, otherwise 0
     */
    private int getDuplicateAppVersion(Application application, ApplicationVersion version) {
        return this.mapper.getDuplicateVersionForApp(
                application.getId(), version.getId() == null ? -1 : version.getId(), version.getVersion()
        );
    }

    /**
     * <p>Checks if another application with same package ID and version already exists or not.</p>
     *
     * @param application an application to check against duplicates.
     * @throws DuplicateApplicationException if a duplicated application is found.
     */
//    private void guardDowngradeAppVersion(Application application) {
//        if (application.getPkg() != null && application.getVersion() != null) {
//            final List<Application> dbApps = findByPackageIdAndNewerVersion(application.getPkg(), application.getVersion());
//            if (!dbApps.isEmpty()) {
//                throw new RecentApplicationVersionExistsException(application.getPkg(), application.getVersion(), dbApps.get(0).getCustomerId());
//            }
//        }
//    }

    /**
     * <p>Checks if another application with same package ID and version already exists or not.</p>
     *
     * @param application an application to check against duplicates.
     * @throws DuplicateApplicationException if a duplicated application is found.
     */
//    private void guardDowngradeAppVersion(Application application, ApplicationVersion version) {
//        if (application.getPkg() != null && version.getVersion() != null) {
//            final List<Application> dbApps = findByPackageIdAndNewerVersion(application.getPkg(), version.getVersion());
//            if (!dbApps.isEmpty()) {
//                throw new RecentApplicationVersionExistsException(application.getPkg(), version.getVersion(), dbApps.get(0).getCustomerId());
//            }
//        }
//    }

    /**
     * <p>Updates existing application record in DB.</p>
     *
     * @param application an application record to be updated.
     * @throws DuplicateApplicationException if another application record with same package ID and version already
     *         exists either for current or master customer account.
     */
    @Transactional
    public void updateApplication(Application application) {
        updateRecord(application, this.mapper::updateApplication, SecurityException::onApplicationAccessViolation);
    }

    /**
     * <p>Updates existing web application record in DB.</p>
     *
     * @param application an application record to be updated.
     * @throws DuplicateApplicationException if another application record with same package ID and version already
     *         exists either for current or master customer account.
     */
    @Transactional
    public void updateWebApplication(Application application) {
        final Optional<Integer> currentCustomerId = SecurityContext.get().getCurrentCustomerId();
        if (currentCustomerId.isPresent()) {
            final Application dbDevice = this.mapper.findById(application.getId());
            application.setPkg(dbDevice.getPkg());
            application.setVersion("0");

            updateRecord(application, this.mapper::updateApplication, SecurityException::onApplicationAccessViolation);
        } else {
            throw SecurityException.onAnonymousAccess();
        }
    }

    /**
     * <p>Updates existing application version record in DB.</p>
     *
     * @param applicationVersion an application version record to be updated.
     * @throws DuplicateApplicationException if another application record with same package ID and version already
     *         exists either for current or master customer account.
     */
    @Transactional
    public void updateApplicationVersion(ApplicationVersion applicationVersion) {
        final Application application = getSingleRecord(
                () -> this.mapper.findById(applicationVersion.getApplicationId()),
                SecurityException::onApplicationAccessViolation
        );
        
        guardDuplicateAppVersion(application, applicationVersion);

        final ApplicationVersion dbApplicationVersion = this.mapper.findVersionById(applicationVersion.getId());
        String existingUrl = dbApplicationVersion.getUrl();
        if (existingUrl != null && existingUrl.trim().isEmpty()) {
            existingUrl = null;
        }

        if (applicationVersion.getUrl() != null && applicationVersion.getUrl().trim().isEmpty()) {
            applicationVersion.setUrl(null);
        }
        final String newUrl = applicationVersion.getUrl();
        
        boolean urlChanged = newUrl == null && existingUrl != null ||
                newUrl != null && existingUrl == null ||
                newUrl != null && !newUrl.equals(existingUrl);
        if (urlChanged) {
            applicationVersion.setApkHash(null);
        } else {
            applicationVersion.setApkHash(dbApplicationVersion.getApkHash());
        }

        final Integer currentLatestVersionId = application.getLatestVersion();

        this.mapper.updateApplicationVersion(applicationVersion);
        this.mapper.recalculateLatestVersion(application.getId());

        final Integer newLatestVersionId = this.mapper.findById(applicationVersion.getApplicationId()).getLatestVersion();
        if (!currentLatestVersionId.equals(newLatestVersionId)) {
            final ApplicationVersion newLatestVersion = this.mapper.findVersionById(newLatestVersionId);
            doAutoUpdateToApplicationVersion(newLatestVersion);
        }
    }

    /**
     * <p>Removes the application referenced by the specified ID. The associated application versions are removed as
     * well.</p>
     *
     * @param id an ID of an application to delete.
     * @throws SecurityException if current user is not granted a permission to delete the specified application.
     */
    @Transactional
    public void removeApplicationById(Integer id) {
        Application dbApplication = this.mapper.findById(id);
        if (dbApplication != null && dbApplication.isCommonApplication()) {
            if (!SecurityContext.get().isSuperAdmin()) {
                throw SecurityException.onAdminDataAccessViolation("delete common application");
            }
        }

        boolean used = this.mapper.isApplicationUsedInConfigurations(id);
        if (used) {
            throw new ApplicationReferenceExistsException(id, "configurations");
        }

        updateById(
                id,
                this::findById,
                (record) -> this.mapper.removeApplicationById(record.getId()),
                SecurityException::onApplicationAccessViolation
        );
    }

    public List<ApplicationConfigurationLink> getApplicationConfigurations(Integer id) {
        return getLinkedList(
                id,
                this::findById,
                customerId -> this.mapper.getApplicationConfigurations(customerId, id),
                SecurityException::onApplicationAccessViolation
        );
    }

    public List<ApplicationVersionConfigurationLink> getApplicationVersionConfigurations(Integer versionId) {
        final ApplicationVersion applicationVersion = findApplicationVersionById(versionId);
        final Application application = this.mapper.findById(applicationVersion.getApplicationId());
        final int userCustomerId = SecurityContext.get().getCurrentUser().get().getCustomerId();

        if (application.isCommon() || application.getCustomerId() == userCustomerId) {
            return this.mapper.getApplicationVersionConfigurationsWithCandidates(userCustomerId, application.getId(), versionId);
        } else {
            throw SecurityException.onApplicationAccessViolation(application);
        }
    }

    @Transactional
    public void updateApplicationConfigurations(LinkConfigurationsToAppRequest request) {
        final List<ApplicationConfigurationLink> activeLinks = request.getConfigurations()
                .stream()
                .filter(c -> c.getAction() == 1)
                .collect(Collectors.toList());
        if (!activeLinks.isEmpty()) {
            activeLinks.forEach(link -> {
                final int deletedCount = this.mapper.deleteApplicationConfigurationLinksForSamePkg(
                        link.getApplicationId(), link.getConfigurationId()
                );
                log.debug("Deleted {} links to applications with same package as for application #{} for configuration #{}",
                        deletedCount, link.getApplicationId(), link.getConfigurationId());
            });
        }

        final List<ApplicationConfigurationLink> deletedLinks = request.getConfigurations()
                .stream()
                .filter(c -> c.getId() != null && c.getAction() == 0)
                .collect(Collectors.toList());
        deletedLinks.forEach(link -> {
            this.mapper.deleteApplicationConfigurationLink(link.getId());
        });

        final List<ApplicationConfigurationLink> updatedLinks = request.getConfigurations()
                .stream()
                .filter(c -> c.getId() != null && c.getAction() > 0)
                .collect(Collectors.toList());
        updatedLinks.forEach(this.mapper::updateApplicationConfigurationLink);

        final List<ApplicationConfigurationLink> newLinks = request.getConfigurations()
                .stream()
                .filter(c -> c.getId() == null && c.getAction() > 0)
                .collect(Collectors.toList());
        this.insertApplicationConfigurations(request.getApplicationId(), newLinks);

        SecurityContext.get().getCurrentUser().ifPresent(user -> {
            this.mapper.recheckConfigurationMainApplications(user.getCustomerId());
            this.mapper.recheckConfigurationContentApplications(user.getCustomerId());
            this.mapper.recheckConfigurationKioskModes(user.getCustomerId());
        });

    }

    @Transactional
    public void updateApplicationVersionConfigurations(LinkConfigurationsToAppVersionRequest request) {
        final int applicationVersionId = request.getApplicationVersionId();
        this.removeApplicationConfigurationsByVersionId(applicationVersionId);

        // If this version is set for installation, then other versions of same app must be set for de-installation
        final List<ApplicationVersionConfigurationLink> configurations = request.getConfigurations();
        configurations.forEach(link -> {
            if (link.getAction() == 1) {
                final int uninstalledCount = this.mapper.uninstallOtherVersions(applicationVersionId, link.getConfigurationId());
                log.debug("Uninstalled {} application versions of application #{} ({}) for configuration #{} ({})",
                        uninstalledCount, link.getApplicationId(), link.getApplicationName(),
                        link.getConfigurationId(), link.getConfigurationName());

                // Update the Main App and Content App references to refer to new application version (if necessary)
//                final int mainAppUpdateCount
//                        = this.mapper.syncConfigurationMainApplication(link.getConfigurationId(), applicationVersionId);
//                log.debug("Synchronized {} main application versions of application #{} ({}) for configuration #{} ({})",
//                        mainAppUpdateCount, link.getApplicationId(), link.getApplicationName(),
//                        link.getConfigurationId(), link.getConfigurationName());
//                final int contentAppUpdateCount
//                        = this.mapper.syncConfigurationContentApplication(link.getConfigurationId(), applicationVersionId);
//                log.debug("Synchronized {} content application versions of application #{} ({}) for configuration #{} ({})",
//                        contentAppUpdateCount, link.getApplicationId(), link.getApplicationName(),
//                        link.getConfigurationId(), link.getConfigurationName());
            }
        });

        this.insertApplicationVersionConfigurations(applicationVersionId, configurations);

        SecurityContext.get().getCurrentUser().ifPresent(user -> {
            this.mapper.recheckConfigurationMainApplications(user.getCustomerId());
            this.mapper.recheckConfigurationContentApplications(user.getCustomerId());
            this.mapper.recheckConfigurationKioskModes(user.getCustomerId());
        });
    }

    public void removeApplicationConfigurationsByVersionId(Integer applicationVersionId) {
        final ApplicationVersion applicationVersion = findApplicationVersionById(applicationVersionId);
        final Application application = this.mapper.findById(applicationVersion.getApplicationId());
        final int userCustomerId = SecurityContext.get().getCurrentUser().get().getCustomerId();

        if (application.isCommon() || application.getCustomerId() == userCustomerId) {
            this.mapper.removeApplicationVersionConfigurationsById(userCustomerId, applicationVersionId);
        } else {
            throw SecurityException.onApplicationAccessViolation(application);
        }
    }

    public void insertApplicationVersionConfigurations(Integer applicationVersionId, List<ApplicationVersionConfigurationLink> configurations) {
        if (configurations != null && !configurations.isEmpty()) {
            final ApplicationVersion applicationVersion = findApplicationVersionById(applicationVersionId);
            final Application application = this.mapper.findById(applicationVersion.getApplicationId());
            final int userCustomerId = SecurityContext.get().getCurrentUser().get().getCustomerId();

            if (application.isCommon() || application.getCustomerId() == userCustomerId) {
                this.mapper.insertApplicationVersionConfigurations(application.getId(), applicationVersionId, configurations);
            } else {
                throw SecurityException.onApplicationAccessViolation(application);
            }

        }
    }

//    public void removeApplicationConfigurationsById(Integer applicationId) {
//        updateLinkedData(
//                applicationId,
//                this::findById,
//                app -> this.mapper.removeApplicationConfigurationsById(
//                        SecurityContext.get().getCurrentUser().get().getCustomerId(), app.getId()
//                ),
//                SecurityException::onApplicationAccessViolation
//        );
//    }

    public void insertApplicationConfigurations(Integer applicationId, List<ApplicationConfigurationLink> configurations) {
        if (configurations != null && !configurations.isEmpty()) {
            updateLinkedData(
                    applicationId,
                    this::findById,
                    app -> this.mapper.insertApplicationConfigurations(app.getId(), app.getLatestVersion(), configurations),
                    SecurityException::onApplicationAccessViolation
            );
        }
    }

    public List<Application> findByPackageIdAndVersion(String pkg, String version) {
        return getList(customerId -> this.mapper.findByPackageIdAndVersion(customerId, pkg, version));
    }

    public List<Application> findByPackageIdAndNewerVersion(String pkg, String version) {
        return getList(customerId -> this.mapper.findByPackageIdAndNewerVersion(customerId, pkg, version));
    }

    public List<Application> findByPackageId(String pkg) {
        return getList(customerId -> this.mapper.findByPackageId(customerId, pkg));
    }

    public Application findById(int id) {
        return this.mapper.findById(id);
    }

    public ApplicationVersion findApplicationVersionById(int id) {
        return this.mapper.findVersionById(id);
    }

    public ApplicationVersion findApplicationVersion(String pkg, String version) {
        return SecurityContext.get()
                .getCurrentUser()
                .map(u -> this.mapper.findApplicationVersion(u.getCustomerId(), pkg, version))
                .orElse(null);
    }

    public List<Application> getAllAdminApplications() {
        if (SecurityContext.get().isSuperAdmin()) {
            return this.mapper.getAllAdminApplications();
        } else {
            throw SecurityException.onAdminDataAccessViolation("get all applications");
        }
    }

    public List<Application> getAllAdminApplicationsByValue(String value) {
        if (SecurityContext.get().isSuperAdmin()) {
            return getList(customerId -> this.mapper.getAllAdminApplicationsByValue("%" + value + "%"));
        } else {
            throw SecurityException.onAdminDataAccessViolation("get all applications");
        }
    }

    @Transactional
    public void turnApplicationIntoCommon_Transaction(Integer id, Map<File, File> filesToCopyCollector) {
        Application application = this.mapper.findById(id);
        if (application != null) {
            if (!application.isCommonApplication()) {
                final int currentUserCustomerId = SecurityContext.get().getCurrentUser().get().getCustomerId();
                final Customer newAppCustomer = customerDAO.findById(currentUserCustomerId);

                // Create new common application record
                // Notice: all applications belonging to a super-admin are considered as common (or shared)
                final Application newCommonApplication = new Application();
                newCommonApplication.setPkg(application.getPkg());
                newCommonApplication.setName(application.getName());
                newCommonApplication.setType(application.getType());
                newCommonApplication.setShowIcon(application.getShowIcon());
                newCommonApplication.setUseKiosk(application.getUseKiosk());
                newCommonApplication.setSystem(application.isSystem());
                newCommonApplication.setCustomerId(newAppCustomer.getId());
                newCommonApplication.setLatestVersion(null);

                this.mapper.insertApplication(newCommonApplication);
                final Integer newAppId = newCommonApplication.getId();

                // Find all applications among all customers which have the same package ID and build the list of
                // all possible version for target application
                final List<Application> candidateApplications = mapper.findAllByPackageId(application.getPkg());
                final List<Application> affectedApplications = new ArrayList<>();
                final Map<Integer, Customer> affectedCustomers = new HashMap<>();
                final List<ApplicationVersion> affectedAppVersions = new ArrayList<>();
                Map<String, ApplicationVersion> candidateApplicationVersions = new HashMap<>();
                candidateApplications.forEach(app -> {
                    final List<ApplicationVersion> applicationVersions = this.mapper.getApplicationVersions(app.getId());
                    applicationVersions.forEach(ver -> {
                        final String normalizedVersion = ApplicationUtil.normalizeVersion(ver.getVersion());
                        if (!candidateApplicationVersions.containsKey(normalizedVersion)) {
                            candidateApplicationVersions.put(normalizedVersion, ver);
                        } else {
                            log.debug("Will use following substitution for application versions when turning application {} to common: {} -> {}",
                                    application.getPkg(), ver, candidateApplicationVersions.get(normalizedVersion));
                        }

                        affectedAppVersions.add(ver);
                    });

                    affectedApplications.add(app);
                    affectedCustomers.put(app.getId(), customerDAO.findById(app.getCustomerId()));
                });

                // Re-create the collected application versions and link them to new application. At the same time
                // collect the files to copy to master-customer account
                final Map<String, Integer> versionIdMapping = new HashMap<>();
                candidateApplicationVersions.forEach((normalizedVersionText, appVersionObject) -> {
                    final String newUrl = translateAppVersionUrl(
                            appVersionObject,
                            affectedCustomers.get(appVersionObject.getApplicationId()),
                            newAppCustomer,
                            filesToCopyCollector
                    );

                    ApplicationVersion newAppVersion = new ApplicationVersion();
                    newAppVersion.setApplicationId(newAppId);
                    newAppVersion.setVersion(appVersionObject.getVersion());
                    newAppVersion.setUrl(newUrl);

                    this.mapper.insertApplicationVersion(newAppVersion);

                    versionIdMapping.put(normalizedVersionText, newAppVersion.getId());
                });

                // Replace the references to existing applications and application versions to new one
                affectedAppVersions.forEach(appVer -> {
                    final String normalizedVersionText = ApplicationUtil.normalizeVersion(appVer.getVersion());
                    final Integer newAppVersionId = versionIdMapping.get(normalizedVersionText);

                    mapper.changeConfigurationsApplication(appVer.getApplicationId(), appVer.getId(), newAppId, newAppVersionId);
                    mapper.changeConfigurationsMainApplication(appVer.getId(), newAppVersionId);
                    mapper.changeConfigurationsContentApplication(appVer.getId(), newAppVersionId);

                });

                // Remove migrated applications
                affectedApplications.forEach(app -> {
                    mapper.removeApplicationById(app.getId());
                });

                // Evaluate the most recent version for new common app
                this.mapper.recalculateLatestVersion(newCommonApplication.getId());
            }
        }
    }
    
    public void turnApplicationIntoCommon(Integer id) {
        if (SecurityContext.get().isSuperAdmin()) {
            final Map<File, File> filesToCopy = new HashMap<>();

            turnApplicationIntoCommon_Transaction(id, filesToCopy);

            // Move the files from affected versions
            filesToCopy.forEach((currentAppFile, newAppFile) -> {
                if (newAppFile.exists()) {
                    log.warn("Skip copying file: {} -> {} since the target file already exists",
                            currentAppFile.getAbsolutePath(), newAppFile.getAbsolutePath());
                } else if (!currentAppFile.exists()) {
                    log.warn("Skip copying file: {} -> {} since the source file does not exist",
                            currentAppFile.getAbsolutePath(), newAppFile.getAbsolutePath());
                } else if (!currentAppFile.isFile()) {
                    log.warn("Skip copying file: {} -> {} since the source file is not a regular file",
                            currentAppFile.getAbsolutePath(), newAppFile.getAbsolutePath());
                } else {
                    log.debug("Copying file: {} -> {}...", currentAppFile.getAbsolutePath(), newAppFile.getAbsolutePath());
                    try {
                        Path newAppFileDir = newAppFile.toPath().getParent();
                        newAppFileDir = Files.createDirectories(newAppFileDir);
                        if (!Files.exists(newAppFileDir)) {
                            log.error("Couldn't create a directory '{}' in files area for Master-customer account",
                                    newAppFileDir.toAbsolutePath());
                        } else {
                            Files.copy(currentAppFile.toPath(), newAppFile.toPath());
                            deleteAppFile(currentAppFile);
                        }
                    } catch (IOException e) {
                        log.error("Failed to copy file: {} -> {} due to unexpected error. The process continues.",
                                currentAppFile.getAbsolutePath(), newAppFile.getAbsolutePath());
                    }
                }
            });
        } else {
            throw SecurityException.onAdminDataAccessViolation("turn application into common");
        }
    }

    private void deleteAppFile(File appFile) {
        final boolean deleted = appFile.delete();
        if (deleted) {
            log.info("Deleted the file {} when turning application to common",
                    appFile.getAbsolutePath());
        } else {
            log.error("Failed to delete the file {} when turning application to common",
                    appFile.getAbsolutePath());
        }
    }

    /**
     * <p>Gets the list of versions for specified application.</p>
     *
     * @param id an ID of an application to get versions for.
     * @return a list of versions for requested application.
     */
    public List<ApplicationVersion> getApplicationVersions(Integer id) {
        return SecurityContext.get().getCurrentUser()
                .map(currentUser -> {
                    Application dbApplication = this.mapper.findById(id);
                    if (dbApplication != null) {
                        if (dbApplication.getCustomerId() == currentUser.getCustomerId() || dbApplication.isCommonApplication()) {
                            return this.mapper.getApplicationVersions(id);
                        }
                    }

                    throw SecurityException.onApplicationAccessViolation(id);
                })
                .orElseThrow(SecurityException::onAnonymousAccess);


    }

    /**
     * <p>Removes the application version referenced by the specified ID.</p>
     *
     * @param id an ID of an application to delete.
     * @return an URL for the deleted application version.
     * @throws SecurityException if current user is not granted a permission to delete the specified application.
     */
    @Transactional
    public String removeApplicationVersionById(@NotNull Integer id) {
        ApplicationVersion dbApplicationVersion = this.mapper.findVersionById(id);
        if (dbApplicationVersion != null) {
            if (dbApplicationVersion.isDeletionProhibited()) {
                throw SecurityException.onApplicationVersionAccessViolation(id);
            }

            if (dbApplicationVersion.isCommonApplication()) {
                if (!SecurityContext.get().isSuperAdmin()) {
                    throw SecurityException.onAdminDataAccessViolation("delete common application version");
                }
            }

            final Application dbApplication = this.mapper.findById(dbApplicationVersion.getApplicationId());
            boolean used = this.mapper.isApplicationVersionUsedInConfigurations(id);
            if (used) {
                throw new ApplicationReferenceExistsException(id, "configurations");
            }

            this.mapper.removeApplicationVersionById(id);

            // Recalculate latest version for application if necessary
            if (dbApplication.getLatestVersion() != null && dbApplication.getLatestVersion().equals(id)) {
                this.mapper.recalculateLatestVersion(dbApplication.getId());
                final Application application = this.mapper.findById(dbApplication.getId());
                if (application.getLatestVersion() != null) {
                    final ApplicationVersion latestVersion = this.mapper.findVersionById(application.getLatestVersion());
                    doAutoUpdateToApplicationVersion(latestVersion);
                }
            }


            return dbApplicationVersion.getUrl();
        }

        return null;
    }

    /**
     * <p>Removes the application version referenced by the specified ID and deletes the associated APK-file from local
     * file system.</p>
     *
     * @param id an ID of an application to delete.
     * @throws SecurityException if current user is not granted a permission to delete the specified application.
     */
    public void removeApplicationVersionByIdWithAPKFile(@NotNull Integer id) {
        final int customerId = SecurityContext.get().getCurrentUser().get().getCustomerId();
        final Customer customer = customerDAO.findById(customerId);
        final String url = this.removeApplicationVersionById(id);
        if (url != null && !url.trim().isEmpty()) {
            final String apkFile = FileUtil.translateURLToLocalFilePath(customer, url, baseUrl);
            if (apkFile != null) {
                final boolean deleted = FileUtil.deleteFile(filesDirectory, apkFile);
                if (!deleted) {
                    log.warn("Could not delete the APK-file {} related to deleted application version #{}", apkFile, id);
                }
            }
        }
    }

    /**
     * <p>Creates new application version record in DB.</p>
     *
     * @param applicationVersion an application version record to be created.
     * @throws DuplicateApplicationException if another application record with same package ID and version already
     *         exists either for current or master customer account.
     * @throws CommonAppAccessException if target application is common and current user is not a super-admin.
     */
    @Transactional
    public int insertApplicationVersion(ApplicationVersion applicationVersion) {
        log.debug("Entering #insertApplicationVersion: application = {}", applicationVersion);

        // If an APK-file was set for new app then make the file available in Files area and parse the app parameters
        // from it (package ID, version)
        final AtomicReference<String> appPkg = new AtomicReference<>();

        final String filePath = applicationVersion.getFilePath();
        if (filePath != null && !filePath.trim().isEmpty()) {
            final int customerId = SecurityContext.get().getCurrentUser().get().getCustomerId();
            Customer customer = customerDAO.findById(customerId);

            File movedFile = null;
            try {
                movedFile = FileUtil.moveFile(customer, filesDirectory, null, filePath);
            } catch (FileExistsException e) {
                FileUtil.deleteFile(filesDirectory, FileUtil.getNameFromTmpPath(filePath));
                movedFile = FileUtil.moveFile(customer, filesDirectory, null, filePath);
            }
            if (movedFile != null) {
                final String fileName = movedFile.getAbsolutePath();
                final APKFileDetails apkFileDetails = this.apkFileAnalyzer.analyzeFile(fileName);

                // If URL is not specified explicitly for new app then set the application URL to reference to that
                // file
                if ((applicationVersion.getUrl() == null || applicationVersion.getUrl().trim().isEmpty())) {
                    String url = this.baseUrl + "/files/" + customer.getFilesDir() + "/" + movedFile.getName();
                    if (StringUtil.isEmpty(apkFileDetails.getArch())) {
                        applicationVersion.setSplit(false);
                        applicationVersion.setUrl(url);
                    } else if (apkFileDetails.getArch().equals(Application.ARCH_ARMEABI)) {
                        applicationVersion.setSplit(true);
                        applicationVersion.setUrlArmeabi(url);
                    } else if (apkFileDetails.getArch().equals(Application.ARCH_ARM64)) {
                        applicationVersion.setSplit(true);
                        applicationVersion.setUrlArm64(url);
                    }
                }

                applicationVersion.setVersion(apkFileDetails.getVersion());
            } else {
                log.error("Could not move the uploaded .apk-file {}", filePath);
                throw new DAOException("Could not move the uploaded .apk-file");
            }
        }

        final Application existingApplication = findById(applicationVersion.getApplicationId());
        if (existingApplication == null) {
            throw new DAOException("The requested application does not exist: #" + applicationVersion.getApplicationId());
        }

        if (existingApplication.isCommonApplication()) {
            if (!SecurityContext.get().isSuperAdmin()) {
                throw new CommonAppAccessException(
                        existingApplication.getPkg(), SecurityContext.get().getCurrentCustomerId().get()
                );
            }
        }

        // Check the version package id against application's package id - they must be the same
        if (appPkg.get() != null) {
            if (!existingApplication.getPkg().equals(appPkg.get())) {
                throw new ApplicationVersionPackageMismatchException(appPkg.get(), existingApplication.getPkg());
            }
        }

//        guardDowngradeAppVersion(existingApplication, applicationVersion);

        // The user may wish to add the same application and version when he moves
        // the application from h-mdm.com to his own server
        int duplicateVersionId = getDuplicateAppVersion(existingApplication, applicationVersion);
        if (duplicateVersionId > 0) {
            applicationVersion.setId(duplicateVersionId);
            ApplicationVersion existingVersion = this.mapper.findVersionById(duplicateVersionId);
            // If a user added APK for another architecture, keep previous architecture
            if (applicationVersion.isSplit()) {
                if (StringUtil.isEmpty(applicationVersion.getUrlArmeabi())) {
                    applicationVersion.setUrlArmeabi(existingVersion.getUrlArmeabi());
                }
                if (StringUtil.isEmpty(applicationVersion.getUrlArm64())) {
                    applicationVersion.setUrlArm64(existingVersion.getUrlArm64());
                }
            }
            this.mapper.updateApplicationVersion(applicationVersion);
        } else {
            this.mapper.insertApplicationVersion(applicationVersion);
            this.mapper.recalculateLatestVersion(existingApplication.getId());
        }

        // Auto update the configurations if the created application version becomes the latest version for application
        final Application refreshedExistingApplication = findById(applicationVersion.getApplicationId());
        final Integer latestVersionId = refreshedExistingApplication.getLatestVersion();
        if (latestVersionId != null && latestVersionId.equals(applicationVersion.getId())) {
            doAutoUpdateToApplicationVersion(applicationVersion);
        }

        return applicationVersion.getId();
    }

    private String translateAppVersionUrl(ApplicationVersion appVersion,
                                          Customer appCustomer,
                                          Customer newAppCustomer,
                                          Map<File, File> fileToCopyCollector) {
        // Update application URL and link it to new customer and copy application file to master
        // customer
        final String currentApplicationUrl = appVersion.getUrl();
        if (currentApplicationUrl != null) {
            final String currentCustomerFileDirUrlPart = "/" + appCustomer.getFilesDir() + "/";
            int pos = currentApplicationUrl.indexOf(currentCustomerFileDirUrlPart);
            if (pos >= 0) {

                final String relativeFilePath = currentApplicationUrl.substring(pos + 1);
                final File newCustomerFilesBaseDir = new File(this.filesDirectory, newAppCustomer.getFilesDir());

                final File currentAppFile = new File(this.filesDirectory, relativeFilePath);
                final File newAppFile = new File(newCustomerFilesBaseDir, relativeFilePath);

                fileToCopyCollector.put(currentAppFile, newAppFile);

                return this.baseUrl + "/files/" + newAppCustomer.getFilesDir() + "/" + relativeFilePath;
            } else {
                log.warn("Invalid application URL does not contain the base directory for customer files: {}" ,
                        currentApplicationUrl);
            }
        }

        return null;
    }

    /**
     * <p>Updates the configurations which have the AUTO-UPDATE flag set to true to refer to newly added application
     * version.</p>
     *
     * @param newApplicationVersion a new application version to update the configuration references to.
     */
    private void doAutoUpdateToApplicationVersion( ApplicationVersion newApplicationVersion) {
        int autoUpdatedConfigAppsCount  = this.mapper.autoUpdateConfigurationsApplication(
                newApplicationVersion.getApplicationId(), newApplicationVersion.getId()
        );
        int autoUpdatedMainAppsCount = this.mapper.autoUpdateConfigurationsMainApplication(
                newApplicationVersion.getApplicationId(), newApplicationVersion.getId()
        );
        int autoUpdatedContentAppsCount = this.mapper.autoUpdateConfigurationsContentApplication(
                newApplicationVersion.getApplicationId(), newApplicationVersion.getId()
        );

        log.debug("Auto-updated {} application links for configurations", autoUpdatedConfigAppsCount);
        log.debug("Auto-updated main application for {} configurations", autoUpdatedMainAppsCount);
        log.debug("Auto-updated content application for {} configurations", autoUpdatedContentAppsCount);
    }

    /**
     * <p>Gets the lookup list of applications matching the package ID with specified filter.</p>
     *
     * @param filter a filter to be used for filtering the records.
     * @param resultsCount a maximum number of items to be included to list.
     * @return a response with list of applications matching the specified filter.
     */
    public List<LookupItem> getApplicationPkgLookup(String filter, int resultsCount) {
        String searchFilter = '%' + filter.trim() + '%';
        return SecurityContext.get().getCurrentUser()
                .map(u -> this.mapper.findMatchingApplicationPackages(u.getCustomerId(), searchFilter, resultsCount))
                .orElse(new ArrayList<>());
    }

    /**
     * <p>Locates the applications other than specified one which have the same package ID.</p>
     *
     * @param application an application to be validated.
     * @return a list of existing applications with same package ID as set for validated one.
     */
    public List<Application> getApplicationsForPackageID(Application application) {
        if (application.getPkg() != null) {
            final List<Application> dbApps = findByPackageId(application.getPkg())
                    .stream()
                    .filter(dbApp -> !dbApp.getId().equals(application.getId()))
                    .collect(Collectors.toList());
            return dbApps;
        }

        return new ArrayList<>();
    }

    public String ddd(Customer customer, String fileName) {
        return this.baseUrl + "/files/" + customer.getFilesDir() + "/" + fileName;
    }

    public boolean isFileUsed(Customer customer, String fileDirPath, String fileName) {
        final String appFileUrl;
        if (fileDirPath == null || fileDirPath.trim().isEmpty()) {
            appFileUrl = this.baseUrl + "/files/" + customer.getFilesDir() + "/" + fileName;
        } else {
            appFileUrl = this.baseUrl + "/files/" + customer.getFilesDir() + "/" + fileDirPath.replace('\\', '/') + fileName;
        }

        final boolean used = this.mapper.countAllApplicationsByUrl(customer.getId(), appFileUrl) > 0;

        return used;
    }

    public List<String> getUsingApps(Customer customer, String fileDirPath, String fileName) {
        final String appFileUrl;
        if (fileDirPath == null || fileDirPath.trim().isEmpty()) {
            appFileUrl = this.baseUrl + "/files/" + customer.getFilesDir() + "/" + fileName;
        } else {
            appFileUrl = this.baseUrl + "/files/" + customer.getFilesDir() + "/" + fileDirPath.replace('\\', '/') + fileName;
        }

        return this.mapper.getUsingApps(customer.getId(), appFileUrl);
    }
}
