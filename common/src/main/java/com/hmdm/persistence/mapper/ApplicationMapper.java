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

package com.hmdm.persistence.mapper;

import java.util.Collection;
import java.util.List;

import com.hmdm.persistence.domain.ApplicationVersion;
import com.hmdm.rest.json.ApplicationConfigurationLink;
import com.hmdm.rest.json.ApplicationVersionConfigurationLink;
import com.hmdm.rest.json.LookupItem;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectKey;
import org.apache.ibatis.annotations.Update;
import com.hmdm.persistence.domain.Application;

public interface ApplicationMapper {

    String SELECT_BASE =
            "SELECT applications.*, customers.name AS customerName, customers.master AS commonApplication, " +
                    "applicationVersions.version, applicationVersions.versionCode, applicationVersions.url," +
                    "applicationVersions.split, applicationVersions.urlArmeabi, applicationVersions.urlArm64," +
                    "applications.latestVersion AS usedVersionId, " +
                    "(usageData.usageCount > 0) AS deletionProhibited " +
            "FROM applications " +
            "INNER JOIN customers ON customers.id = applications.customerId " +
            "INNER JOIN applicationVersions ON applicationVersions.id = applications.latestVersion " +
            "LEFT JOIN (SELECT applicationVersions.applicationId AS id, COUNT(*) AS usageCount " +
            "            FROM applicationVersions " +
            "            INNER JOIN configurationApplications c ON applicationVersions.id = c.applicationVersionId" +
            "            GROUP BY 1) usageData ON usageData.id=applications.id ";

    String SELECT_BY_VERSION_BASE =
            "SELECT applications.*, customers.name AS customerName, customers.master AS commonApplication, " +
                    "applicationVersions.version, applicationVersions.versionCode, applicationVersions.url, " +
                    "applicationVersions.split, applicationVersions.urlArmeabi, applicationVersions.urlArm64," +
                    "applications.latestVersion AS usedVersionId, " +
                    "(usageData.usageCount > 0) AS deletionProhibited " +
                    "FROM applicationVersions " +
                    "INNER JOIN applications ON applicationVersions.id = applications.latestVersion " +
                    "INNER JOIN customers ON customers.id = applications.customerId " +
                    "LEFT JOIN (SELECT applicationVersions.applicationId AS id, COUNT(*) AS usageCount " +
                    "           FROM applicationVersions " +
                    "           INNER JOIN configurationApplications c ON applicationVersions.id = c.applicationVersionId" +
                    "           GROUP BY 1) usageData ON usageData.id=applications.id ";

    String SELECT_VERSION_BASE =
            "SELECT "+
                    "applicationVersions.id, " +
                    "applicationVersions.applicationId, " +
                    "applicationVersions.version, " +
                    "applicationVersions.versionCode, " +
                    "applicationVersions.url, " +
                    "applicationVersions.split, " +
                    "applicationVersions.urlArmeabi, " +
                    "applicationVersions.urlArm64, " +
                    "applicationVersions.apkHash, " +
                    "(usageData.usageCount > 0 OR versionsData.appVersionsCount = 1) AS deletionProhibited, " +
                    "customers.master AS commonApplication," +
                    "applications.system, " +
                    "applications.type " +
                    "FROM applicationVersions " +
                    "INNER JOIN applications ON applicationVersions.applicationId = applications.id " +
                    "INNER JOIN customers ON applications.customerId = customers.id " +
                    "LEFT JOIN (SELECT applicationId, COUNT(*) AS appVersionsCount " +
                    "           FROM applicationVersions " +
                    "           GROUP BY 1) versionsData ON versionsData.applicationId=applications.id " +
                    "LEFT JOIN (SELECT applicationVersions.id AS id, COUNT(*) AS usageCount " +
                    "           FROM applicationVersions " +
                    "           INNER JOIN configurationApplications c ON applicationVersions.id = c.applicationVersionId" +
                    "           GROUP BY 1) usageData ON usageData.id=applicationVersions.id ";
    ;

    @Select({SELECT_BASE +
            "WHERE customerId = #{customerId} " +
            "OR customers.master = TRUE " +
            "   AND NOT EXISTS" +
            "    (" +
            "     SELECT 1 " +
            "     FROM applications apps2 " +
            "     INNER JOIN applicationVersions ver2 ON ver2.applicationId=apps2.id" +
            "     WHERE apps2.customerid = #{customerId} " +
            "     AND apps2.pkg=applications.pkg " +
            "     AND ver2.version=applicationVersions.version" +
            "    )" +
            "ORDER BY name"})
    List<Application> getAllApplications(@Param("customerId") int customerId);

    @Select({SELECT_BASE +
            "WHERE (customerId = #{customerId} " +
            "OR customers.master = TRUE" +
            "   AND NOT EXISTS" +
            "    (" +
            "     SELECT 1 " +
            "     FROM applications apps2 " +
            "     INNER JOIN applicationVersions ver2 ON ver2.applicationId=apps2.id" +
            "     WHERE apps2.customerid = #{customerId} " +
            "     AND apps2.pkg=applications.pkg " +
            "     AND ver2.version=applicationVersions.version" +
            "    )" +
            ")" +
            "AND (applications.name ILIKE #{value} OR pkg ILIKE #{value}) " +
            "ORDER BY applications.name"})
    List<Application> getAllApplicationsByValue(@Param("customerId") int customerId, @Param("value") String value);

    @Select({SELECT_BASE +
            "WHERE (customerId = #{customerId})" +
            "AND (applicationVersions.url=#{url} OR applicationVersions.urlarmeabi=#{url} OR applicationVersions.urlarm64=#{url}) " +
            "ORDER BY applications.name"})
    List<Application> getAllApplicationsByUrl(@Param("customerId") int customerId, @Param("url") String url);

    @Insert({"INSERT INTO applications (name, pkg, showIcon, useKiosk, system, customerId, runAfterInstall, runAtBoot, type, iconText, iconId) " +
            "VALUES (#{name}, #{pkg}, #{showIcon}, #{useKiosk}, #{system}, #{customerId}, #{runAfterInstall}, #{runAtBoot}, #{type}, #{iconText}, #{iconId})"})
    @SelectKey( statement = "SELECT currval('applications_id_seq')", keyColumn = "id", keyProperty = "id", before = false, resultType = int.class )
    void insertApplication(Application application);

    @Insert({"INSERT INTO applicationVersions (applicationId, version, versionCode, url, apkHash, split, urlArmeabi, urlArm64) " +
            "VALUES (#{applicationId}, #{version}, #{versionCode}, #{url}, #{apkHash}, #{split}, #{urlArmeabi}, #{urlArm64})"})
    @SelectKey( statement = "SELECT currval('applicationVersions_id_seq')", keyColumn = "id", keyProperty = "id", before = false, resultType = int.class )
    int insertApplicationVersion(ApplicationVersion version);

    @Update({"UPDATE applications SET name=#{name}, pkg=#{pkg}, " +
            "showIcon=#{showIcon}, useKiosk=#{useKiosk}, system=#{system}, customerId=#{customerId}, " +
            "runAfterInstall = #{runAfterInstall}, runAtBoot = #{runAtBoot}, " +
            "type = #{type}, iconText = #{iconText}, iconId = #{iconId} " +
            "WHERE id=#{id}"})
    void updateApplication(Application application);

    @Update({"UPDATE applicationVersions SET version = #{version}, versionCode=#{versionCode}, url = #{url}, apkHash = #{apkHash}, " +
            "split = #{split}, urlArmeabi = #{urlArmeabi}, urlArm64 = #{urlArm64} " +
            "WHERE id=#{id}"})
    void updateApplicationVersion(ApplicationVersion applicationVersion);

    @Delete({"DELETE FROM applications WHERE id=#{id}"})
    void removeApplicationById(@Param("id") Integer id);

    @Select({"SELECT configurationApplications.id       AS id, " +
            "       configurations.id                  AS configurationId, " +
            "       configurations.name                AS configurationName, " +
            "       configurations.customerId          AS customerId, " +
            "       applications.id                    AS applicationId, " +
            "       applications.name                  AS applicationName, " +
            "       COALESCE(configurationApplications.showIcon, applications.showIcon) AS showIcon, " +
            "       applications.useKiosk              AS useKiosk, " +
            "       configurationApplications.remove   AS remove, " +
            "       latestAppVersion.version   AS latestVersionText, " +
            "       currentAppVersion.version   AS currentVersionText, " +
            "       (configurationApplications.configurationId IS NOT NULL AND applications.latestVersion <> configurationApplications.applicationVersionId) AS outdated, " +
            "       configurationApplications.action AS action " +
            "FROM configurations " +
            "         LEFT JOIN applications ON applications.id = #{id} " +
            "         INNER JOIN applicationVersions AS latestAppVersion ON latestAppVersion.applicationId = applications.id AND latestAppVersion.id=applications.latestversion " +
            "         LEFT JOIN configurationApplications ON configurations.id = configurationApplications.configurationId AND " +
            "                                                applications.id = configurationApplications.applicationId " +
            "         LEFT JOIN applicationVersions AS currentAppVersion ON currentAppVersion.applicationId = applications.id AND currentAppVersion.id=configurationApplications.applicationVersionId " +
            "WHERE configurations.customerId = #{customerId} " +
            "ORDER BY LOWER(configurations.name)"})
    List<ApplicationConfigurationLink> getApplicationConfigurations(@Param("customerId") Integer customerId,
                                                                    @Param("id") Integer applicationId);

    @Select({"SELECT configurationApplications.id       AS id, " +
            "       configurations.id                  AS configurationId, " +
            "       configurations.name                AS configurationName, " +
            "       configurations.customerId          AS customerId, " +
            "       applications.id                    AS applicationId, " +
            "       applications.name                  AS applicationName, " +
            "       COALESCE(configurationApplications.showIcon, caPrev.showIcon, applications.showIcon) AS showIcon, " +
            "       applications.useKiosk              AS useKiosk, " +
            "       COALESCE(configurationApplications.screenOrder, caPrev.screenOrder) AS screenOrder, " +
            "       COALESCE(configurationApplications.keyCode, caPrev.keyCode) AS keyCode, " +
            "       COALESCE(configurationApplications.bottom, caPrev.bottom) AS bottom, " +
            "       applications.id                    AS applicationVersionId, " +
            "       applications.id                    AS versionText, " +
            "       configurationApplications.remove   AS remove, " +
            "       configurationApplications.action   AS action " +
            "FROM configurations " +
            "INNER JOIN applicationVersions ON applicationVersions.id = #{id} " +
            "INNER JOIN applications ON applications.id = applicationVersions.applicationId " +
            "LEFT JOIN configurationApplications ON configurations.id = configurationApplications.configurationId AND configurationApplications.applicationVersionId = applicationVersions.id " +
            "LEFT JOIN configurationApplications caPrev ON configurations.id = caPrev.configurationId AND caPrev.applicationId = #{appId} AND caPrev.action=1 " +
            "WHERE configurations.customerId = #{customerId} " +
            "ORDER BY LOWER(configurations.name)"})
    List<ApplicationVersionConfigurationLink> getApplicationVersionConfigurationsWithCandidates(
            @Param("customerId") Integer customerId, @Param("appId") Integer applicationId, @Param("id") Integer applicationVersionId
    );


//    @Delete({"DELETE FROM configurationApplications " +
//            "WHERE applicationId=#{id} " +
//            "AND configurationId IN (SELECT configurations.id " +
//            "                        FROM configurations " +
//            "                        WHERE configurations.customerId=#{customerId})"})
//    void removeApplicationConfigurationsById(@Param("customerId") int customerId, @Param("id") Integer applicationId);

    @Delete({"DELETE FROM configurationApplications " +
            "WHERE applicationVersionId=#{id} " +
            "AND configurationId IN (SELECT configurations.id " +
            "                        FROM configurations " +
            "                        WHERE configurations.customerId=#{customerId})"})
    void removeApplicationVersionConfigurationsById(@Param("customerId") int customerId,
                                                    @Param("id") Integer applicationVersionId);

    void insertApplicationConfigurations(@Param("applicationId") Integer applicationId,
                                         @Param("versionId") Integer applicationVersionId,
                                         @Param("confs") List<ApplicationConfigurationLink> configurations);

    void insertApplicationVersionConfigurations(@Param("applicationId") Integer applicationId,
                                                @Param("versionId") Integer applicationVersionId,
                                                @Param("confs") List<ApplicationVersionConfigurationLink> configurations);

    @Select({SELECT_BY_VERSION_BASE +
            "WHERE (customerId = #{customerId} OR customers.master = TRUE )" +
            "AND pkg = #{pkg} " +
            "AND applicationVersions.version=#{version}"})
    List<Application> findByPackageIdAndVersion(@Param("customerId") int customerId,
                                                @Param("pkg") String pkg,
                                                @Param("version") String version);

    @Select({SELECT_BY_VERSION_BASE +
            "WHERE (customerId = #{customerId} OR customers.master = TRUE )" +
            "AND pkg = #{pkg} " +
            "AND mdm_app_version_comparison_index(applicationVersions.version) > mdm_app_version_comparison_index(#{version})"})
    List<Application> findByPackageIdAndNewerVersion(@Param("customerId") int customerId,
                                                     @Param("pkg") String pkg,
                                                     @Param("version") String version);

    @Select({SELECT_BY_VERSION_BASE +
            "WHERE (customerId = #{customerId} OR customers.master = TRUE )" +
            "AND pkg = #{pkg}"})
    List<Application> findByPackageId(@Param("customerId") int customerId,
                                      @Param("pkg") String pkg);

    @Select({"SELECT COUNT(*) " +
            "FROM applications " +
            "INNER JOIN customers ON applications.customerId = customers.id " +
            "WHERE (applications.customerId = #{customerId} OR customers.master = TRUE)" +
            "AND applications.pkg = #{pkg}"})
    Long countByPackageId(@Param("customerId") int customerId, @Param("pkg") String pkg);

    @Select({SELECT_BY_VERSION_BASE +
            "WHERE pkg = #{pkg}"})
    List<Application> findAllByPackageId(@Param("pkg") String pkg);

    @Select({SELECT_BASE +
            "WHERE applications.id = #{id}"})
    Application findById(@Param("id") int id);

    @Select({SELECT_BASE +
            "ORDER BY name"})
    List<Application> getAllAdminApplications();

    @Select({SELECT_BASE +
            "WHERE (applications.name ILIKE #{value} OR pkg ILIKE #{value}) " +
            "ORDER BY applications.name"})
    List<Application> getAllAdminApplicationsByValue(String value);

    @Update("UPDATE configurationApplications " +
            "SET applicationId=#{newAppId}, applicationVersionId=#{newAppVerId} " +
            "WHERE applicationId=#{appId} AND applicationVersionId=#{appVerId}")
    void changeConfigurationsApplication(@Param("appId") Integer oldAppId,
                                         @Param("appVerId") Integer oldAppVerId,
                                         @Param("newAppId") Integer newAppId,
                                         @Param("newAppVerId") Integer newAppVerId);

    @Update("UPDATE configurations SET mainAppId=#{newId} WHERE mainAppId=#{id}")
    void changeConfigurationsMainApplication(@Param("id") Integer id, @Param("newId") Integer newId);

    @Update("UPDATE configurations SET contentAppId=#{newId} WHERE contentAppId=#{id}")
    void changeConfigurationsContentApplication(@Param("id") Integer id, @Param("newId") Integer newId);

    @Select("SELECT COUNT(*) > 0 " +
            "FROM configurationApplications " +
            "WHERE configurationApplications.applicationId=#{id}")
    boolean isApplicationUsedInConfigurations(@Param("id") Integer applicationId);

    @Select(SELECT_VERSION_BASE +
            "WHERE applicationVersions.applicationId=#{id} " +
            "ORDER BY mdm_app_version_comparison_index(applicationVersions.version) DESC")
    List<ApplicationVersion> getApplicationVersions(@Param("id") Integer applicationId);

    @Select(SELECT_VERSION_BASE +
            "WHERE (customerId = #{customerId} OR customers.master = TRUE) " +
            "AND applications.pkg=#{pkg} AND applicationVersions.version=#{version} LIMIT 1")
    ApplicationVersion findApplicationVersion(@Param("customerId") int customerId,
                                              @Param("pkg") String pkg,
                                              @Param("version") String version);

    @Select(SELECT_VERSION_BASE +
            "WHERE (customerId = #{customerId} OR customers.master = TRUE) " +
            "AND applications.pkg=#{pkg} AND applicationVersions.versionCode=#{versionCode} LIMIT 1")
    ApplicationVersion findApplicationVersionByCode(@Param("customerId") int customerId,
                                              @Param("pkg") String pkg,
                                              @Param("versionCode") int versionCode);

    @Select("SELECT COUNT(*) > 0 " +
            "FROM configurationApplications " +
            "WHERE configurationApplications.applicationVersionId=#{id}")
    boolean isApplicationVersionUsedInConfigurations(@Param("id") Integer applicationVersionId);

    @Select({SELECT_VERSION_BASE +
            "WHERE applicationVersions.id = #{id}"})
    ApplicationVersion findVersionById(@Param("id") int id);

    @Delete({"DELETE FROM applicationVersions WHERE id=#{id}"})
    void removeApplicationVersionById(@Param("id") Integer id);

    @Update("UPDATE applications " +
            "SET latestVersion = (" +
            "            SELECT id " +
            "            FROM applicationVersions apv1 " +
            "            WHERE apv1.applicationId = applications.id " +
            "            AND mdm_app_version_comparison_index(apv1.version) = " +
            "                (SELECT MAX(mdm_app_version_comparison_index(apv2.version)) " +
            "                 FROM applicationVersions apv2 " +
            "                 WHERE apv2.applicationId = applications.id) " +
            "            LIMIT 1) " +
            "WHERE id = #{id}")
    void recalculateLatestVersion(@Param("id") Integer applicationId);

    @Update("UPDATE configurationApplications " +
            "SET applicationVersionId = #{newId} " +
            "WHERE applicationId = #{appId} " +
            "AND action <> 2 " +
            "AND EXISTS (SELECT 1 " +
            "            FROM configurations " +
            "            WHERE configurations.id = configurationApplications.configurationId " +
            "            AND configurations.autoUpdate IS TRUE)")
    int autoUpdateConfigurationsApplication(@Param("appId") Integer applicationId,
                                            @Param("newId") Integer newAppVersionId);

    @Update("UPDATE configurations " +
            "SET mainAppId = #{newId} " +
            "WHERE configurations.autoUpdate IS TRUE " +
            "AND EXISTS (SELECT 1 FROM applicationVersions " +
            "            WHERE applicationVersions.id = configurations.mainAppId" +
            "            AND applicationVersions.applicationId = #{appId})")
    int autoUpdateConfigurationsMainApplication(@Param("appId") Integer applicationId,
                                                @Param("newId") Integer newAppVersionId);

    // TODO : ISV : Re-check methods must be updated to find the application by package ID but not applicationId
    @Update("UPDATE configurations " +
            "SET mainAppId = (" +
            "                 SELECT ca.applicationVersionId " +
            "                 FROM configurationApplications ca " +
            "                 INNER JOIN applications apps ON apps.id = ca.applicationId " +
            "                 WHERE ca.configurationId = configurations.id " +
            "                 AND ca.action = 1 " +
            "                 AND apps.pkg = (SELECT apps2.pkg " +
            "                                 FROM applicationVersions av " +
            "                                 INNER JOIN applications apps2 ON apps2.id = av.applicationId " +
            "                                 WHERE av.id = configurations.mainAppId)" +
            ") " +
            "WHERE configurations.customerId = #{customerId} " +
            "AND NOT configurations.mainAppId IS NULL")
    int recheckConfigurationMainApplications(@Param("customerId") Integer customerId);

    @Update("UPDATE configurations " +
            "SET mainAppId = (" +
            "                 SELECT ca.applicationVersionId " +
            "                 FROM configurationApplications ca " +
            "                 INNER JOIN applications apps ON apps.id = ca.applicationId " +
            "                 WHERE ca.configurationId = configurations.id " +
            "                 AND ca.action = 1 " +
            "                 AND apps.pkg = (SELECT apps2.pkg " +
            "                                 FROM applicationVersions av " +
            "                                 INNER JOIN applications apps2 ON apps2.id = av.applicationId " +
            "                                 WHERE av.id = configurations.mainAppId)" +
            ") " +
            "WHERE configurations.id = #{configurationId} " +
            "AND NOT configurations.mainAppId IS NULL")
    int recheckConfigurationMainApplication(@Param("configurationId") Integer configurationId);

    @Update("UPDATE configurations " +
            "SET contentAppId = (" +
            "                 SELECT ca.applicationVersionId " +
            "                 FROM configurationApplications ca " +
            "                 INNER JOIN applications apps ON apps.id = ca.applicationId " +
            "                 WHERE ca.configurationId = configurations.id " +
            "                 AND ca.action = 1 " +
            "                 AND apps.pkg = (SELECT apps2.pkg " +
            "                                 FROM applicationVersions av " +
            "                                 INNER JOIN applications apps2 ON apps2.id = av.applicationId " +
            "                                 WHERE av.id = configurations.contentAppId)" +
            ") " +
            "WHERE configurations.customerId = #{customerId} " +
            "AND NOT configurations.contentAppId IS NULL")
    int recheckConfigurationContentApplications(@Param("customerId") Integer customerId);

    @Update("UPDATE configurations " +
            "SET contentAppId = (" +
            "                 SELECT ca.applicationVersionId " +
            "                 FROM configurationApplications ca " +
            "                 INNER JOIN applications apps ON apps.id = ca.applicationId " +
            "                 WHERE ca.configurationId = configurations.id " +
            "                 AND ca.action = 1 " +
            "                 AND apps.pkg = (SELECT apps2.pkg " +
            "                                 FROM applicationVersions av " +
            "                                 INNER JOIN applications apps2 ON apps2.id = av.applicationId " +
            "                                 WHERE av.id = configurations.contentAppId)" +
            ") " +
            "WHERE configurations.id = #{configurationId} " +
            "AND NOT configurations.contentAppId IS NULL")
    int recheckConfigurationContentApplication(@Param("configurationId") Integer configurationId);

    @Update("UPDATE configurations " +
            "SET kioskMode = false " +
            "WHERE configurations.customerId = #{customerId} AND configurations.contentAppId IS NULL")
    int recheckConfigurationKioskModes(@Param("customerId") Integer customerId);

    @Update("UPDATE configurations " +
            "SET contentAppId = #{newId} " +
            "WHERE configurations.autoUpdate IS TRUE " +
            "AND EXISTS (SELECT 1 FROM applicationVersions " +
            "            WHERE applicationVersions.id = configurations.contentAppId" +
            "            AND applicationVersions.applicationId = #{appId})")
    int autoUpdateConfigurationsContentApplication(@Param("appId") Integer applicationId,
                                                   @Param("newId") Integer newAppVersionId);

    @Select("SELECT id " +
            "FROM applicationVersions apv1 " +
            "WHERE apv1.applicationId = #{id} " +
            "AND (SELECT COUNT(*) " +
            "     FROM applicationVersions apv2 " +
            "     WHERE mdm_app_version_comparison_index(apv2.version) > mdm_app_version_comparison_index(apv1.version)) = 1 " +
            "LIMIT 1")
    Integer getPrecedingVersion(@Param("id") Integer applicationId);

    @Update("UPDATE configurationApplications SET showIcon = #{showIcon}, remove = #{remove}, action = #{action} WHERE id = #{id}")
    void updateApplicationConfigurationLink(ApplicationConfigurationLink link);

    @Delete("DELETE FROM configurationApplications WHERE id = #{id}")
    void deleteApplicationConfigurationLink(@Param("id") Integer linkId);

    @Delete("DELETE FROM configurationApplications " +
            "WHERE configurationId = #{configurationId} " +
            "AND action <> 2 " +
            "AND applicationId = (SELECT applicationVersions.applicationId " +
            "                     FROM applicationVersions " +
            "                     WHERE applicationVersions.id = #{versionId})")
    int uninstallOtherVersions(@Param("versionId") int applicationVersionId,
                               @Param("configurationId") int configurationId);

    /**
     * <p>Resolves the application IDs for the specified application package IDs for specified customer account.</p>
     *
     * @param customerId an ID of a customer account to resolve applications for.
     * @param appPackages a list of package IDs to resolve.
     * @return a list of items mapping the package ID (as name) to application ID.
     */
    List<LookupItem> resolveAppsByPackageId(@Param("customerId") Integer customerId,
                                            @Param("apps") Collection<String> appPackages);

    @Select("SELECT applications.id AS id, " +
            "       applications.pkg AS name " +
            "FROM applications " +
            "INNER JOIN customers ON customers.id = applications.customerid " +
            "WHERE (applications.customerId = #{customerId} OR customers.master IS TRUE) " +
            "AND applications.pkg ILIKE #{filter} " +
            "LIMIT #{pageSize}")
    List<LookupItem> findMatchingApplicationPackages(@Param("customerId") Integer customerId,
                                                     @Param("filter") String filter,
                                                     @Param("pageSize") int pageSize);

    @Update("UPDATE applicationVersions SET apkHash = #{hash} WHERE id = #{id}")
    int saveApkFileHash(@Param("id") Integer appVersionId, @Param("hash") String hashValue);

    @Select("SELECT COALESCE( " +
            "(SELECT id FROM applicationVersions " +
            "WHERE applicationId = #{appId} " +
            "AND version = #{versionNumber} AND id <> #{appVersionId}), 0)")
    int getDuplicateVersionForApp(@Param("appId") Integer applicationId,
                                  @Param("appVersionId") Integer appVersionId,
                                  @Param("versionNumber") String version);

    @Delete("DELETE FROM configurationApplications " +
            "WHERE configurationId = #{configurationId} " +
            "AND applicationId IN ( " +
            "            SELECT id FROM applications " +
            "            WHERE pkg = (" +
            "                SELECT pkg " +
            "                FROM applications " +
            "                WHERE id=#{applicationId} " +
            "            ) AND id != #{applicationId} " +
            ")")
    int deleteApplicationConfigurationLinksForSamePkg(@Param("applicationId") int applicationId,
                                                      @Param("configurationId") int configurationId);


    @Select({"SELECT COUNT(*) AS cnt " +
            "FROM applications " +
            "INNER JOIN customers ON customers.id = applications.customerId " +
            "INNER JOIN applicationVersions ON applicationVersions.id = applications.latestVersion " +
            "WHERE (applications.customerId = #{customerId})" +
            "AND (applicationVersions.url=#{url}) "})
    long countAllApplicationsByUrl(@Param("customerId") int customerId, @Param("url") String url);

    @Select({"SELECT applications.name || ' ' || applicationVersions.version AS name " +
            "FROM applications " +
            "INNER JOIN customers ON customers.id = applications.customerId " +
            "INNER JOIN applicationVersions ON applicationVersions.id = applications.latestVersion " +
            "WHERE (applications.customerId = #{customerId})" +
            "AND (applicationVersions.url=#{url}) "})
    List<String> getUsingApps(@Param("customerId") int customerId, @Param("url") String url);

    @Select({"SELECT applicationVersions.id FROM applicationVersions " +
            "INNER JOIN configurations ON configurations.mainAppId=applicationVersions.id " +
            "WHERE applicationVersions.url=#{url} " +
            "LIMIT 1 "})
    List<Long> getMainAppWithUrl(@Param("url") String url);
}
