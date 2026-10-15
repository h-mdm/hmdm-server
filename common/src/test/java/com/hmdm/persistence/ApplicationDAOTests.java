/*
 * Headwind MDM: Open Source Android MDM Software https://h-mdm.com
 *
 * Copyright (C) 2019 Headwind Solutions LLC (https://h-mdm.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package com.hmdm.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hmdm.persistence.domain.Application;
import com.hmdm.persistence.domain.ApplicationVersion;
import com.hmdm.persistence.domain.Customer;
import com.hmdm.persistence.domain.User;
import com.hmdm.persistence.mapper.ApplicationMapper;
import com.hmdm.security.SecurityContext;
import com.hmdm.util.APKFileAnalyzer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;

/**
 * Tests for {@link ApplicationDAO#removeApplicationVersionById(Integer)} and {@link ApplicationDAO#removeApplicationVersionByIdWithAPKFile(Integer)}:
 * 9 tests covering the main/content app rebind fix (remap and delete, the refusal branches, and the latest-version recalculation) and the
 * shared-APK-file guard.
 */
public class ApplicationDAOTests {

    private static final Integer APP_ID = 7;
    private static final Integer VERSION_ID = 100;
    private static final Integer REPLACEMENT_ID = 101;
    private static final Integer CUSTOMER_ID = 42;
    private static final String URL = "http://example.com/old.apk";

    private ApplicationMapper mapper;
    private CustomerDAO customerDAO;
    private APKFileAnalyzer apkFileAnalyzer;
    private ApplicationDAO dao;

    @BeforeEach
    public void setUp() {
        mapper = mock(ApplicationMapper.class);
        customerDAO = mock(CustomerDAO.class);
        apkFileAnalyzer = mock(APKFileAnalyzer.class);
        dao = new ApplicationDAO(mapper, customerDAO, "/tmp", "http://test", "http://trusted", apkFileAnalyzer);
    }

    @AfterEach
    public void tearDown() {
        // removeApplicationVersionByIdWithAPKFile reads the thread-local SecurityContext; clear it
        // so it never leaks into another test.
        SecurityContext.release();
    }

    private ApplicationVersion stubVersion() {
        ApplicationVersion v = new ApplicationVersion();
        v.setId(VERSION_ID);
        v.setApplicationId(APP_ID);
        v.setDeletionProhibited(false);
        v.setCommonApplication(false);
        v.setUrl(URL);
        return v;
    }

    private Application stubApp() {
        Application a = new Application();
        a.setId(APP_ID);
        a.setCustomerId(CUSTOMER_ID);
        // latestVersion intentionally != VERSION_ID so the recalc/auto-update branch
        // at ApplicationDAO line ~865 is not exercised.
        a.setLatestVersion(REPLACEMENT_ID);
        return a;
    }

    private User stubUser() {
        User u = new User();
        u.setCustomerId(CUSTOMER_ID);
        return u;
    }

    @Test
    public void removeVersion_usedAsMainOnly_remapsMainAndDeletes() {
        when(mapper.findVersionById(VERSION_ID)).thenReturn(stubVersion());
        when(mapper.findById(APP_ID)).thenReturn(stubApp());
        when(mapper.isApplicationVersionUsedInConfigurationApplications(VERSION_ID)).thenReturn(false);
        // In use at detection, then cleared by the scoped rebind (normal case): the post-rebind
        // guard must see it no longer referenced so the delete proceeds.
        when(mapper.isApplicationVersionUsedAsMainApp(VERSION_ID)).thenReturn(true, false);
        when(mapper.isApplicationVersionUsedAsContentApp(VERSION_ID)).thenReturn(false);
        when(mapper.findReplacementVersionId(APP_ID, VERSION_ID, CUSTOMER_ID)).thenReturn(REPLACEMENT_ID);

        String returnedUrl = dao.removeApplicationVersionById(VERSION_ID);

        assertEquals(URL, returnedUrl);

        // A per-customer (non-common) app rebinds via the customer-scoped variant only.
        ArgumentCaptor<Integer> oldId = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> newId = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> custId = ArgumentCaptor.forClass(Integer.class);
        verify(mapper).changeConfigurationsMainApplicationForCustomer(
                oldId.capture(), newId.capture(), custId.capture());
        assertEquals(VERSION_ID, oldId.getValue());
        assertEquals(REPLACEMENT_ID, newId.getValue());
        assertEquals(CUSTOMER_ID, custId.getValue());

        verify(mapper, never()).changeConfigurationsContentApplicationForCustomer(any(), any(), any());
        verify(mapper, never()).changeConfigurationsMainApplication(any(), any());

        InOrder inOrder = Mockito.inOrder(mapper);
        inOrder.verify(mapper).changeConfigurationsMainApplicationForCustomer(VERSION_ID, REPLACEMENT_ID, CUSTOMER_ID);
        inOrder.verify(mapper).removeApplicationVersionById(VERSION_ID);
    }

    @Test
    public void removeVersion_usedAsContentOnly_remapsContentAndDeletes() {
        when(mapper.findVersionById(VERSION_ID)).thenReturn(stubVersion());
        when(mapper.findById(APP_ID)).thenReturn(stubApp());
        when(mapper.isApplicationVersionUsedInConfigurationApplications(VERSION_ID)).thenReturn(false);
        when(mapper.isApplicationVersionUsedAsMainApp(VERSION_ID)).thenReturn(false);
        // In use at detection, then cleared by the scoped rebind (normal case).
        when(mapper.isApplicationVersionUsedAsContentApp(VERSION_ID)).thenReturn(true, false);
        when(mapper.findReplacementVersionId(APP_ID, VERSION_ID, CUSTOMER_ID)).thenReturn(REPLACEMENT_ID);

        dao.removeApplicationVersionById(VERSION_ID);

        ArgumentCaptor<Integer> oldId = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> newId = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> custId = ArgumentCaptor.forClass(Integer.class);
        verify(mapper).changeConfigurationsContentApplicationForCustomer(
                oldId.capture(), newId.capture(), custId.capture());
        assertEquals(VERSION_ID, oldId.getValue());
        assertEquals(REPLACEMENT_ID, newId.getValue());
        assertEquals(CUSTOMER_ID, custId.getValue());

        verify(mapper, never()).changeConfigurationsMainApplicationForCustomer(any(), any(), any());

        InOrder inOrder = Mockito.inOrder(mapper);
        inOrder.verify(mapper).changeConfigurationsContentApplicationForCustomer(VERSION_ID, REPLACEMENT_ID, CUSTOMER_ID);
        inOrder.verify(mapper).removeApplicationVersionById(VERSION_ID);
    }

    @Test
    public void removeVersion_usedAsBoth_remapsBothWithSameReplacementAndDeletes() {
        when(mapper.findVersionById(VERSION_ID)).thenReturn(stubVersion());
        when(mapper.findById(APP_ID)).thenReturn(stubApp());
        when(mapper.isApplicationVersionUsedInConfigurationApplications(VERSION_ID)).thenReturn(false);
        // Both in use at detection, then cleared by the scoped rebind (normal case).
        when(mapper.isApplicationVersionUsedAsMainApp(VERSION_ID)).thenReturn(true, false);
        when(mapper.isApplicationVersionUsedAsContentApp(VERSION_ID)).thenReturn(true, false);
        when(mapper.findReplacementVersionId(APP_ID, VERSION_ID, CUSTOMER_ID)).thenReturn(REPLACEMENT_ID);

        dao.removeApplicationVersionById(VERSION_ID);

        // findReplacementVersionId must be called exactly once with (appId, excludeId, customerId)
        verify(mapper).findReplacementVersionId(eq(APP_ID), eq(VERSION_ID), eq(CUSTOMER_ID));

        ArgumentCaptor<Integer> mainNewId = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> contentNewId = ArgumentCaptor.forClass(Integer.class);
        verify(mapper).changeConfigurationsMainApplicationForCustomer(eq(VERSION_ID), mainNewId.capture(), eq(CUSTOMER_ID));
        verify(mapper).changeConfigurationsContentApplicationForCustomer(eq(VERSION_ID), contentNewId.capture(), eq(CUSTOMER_ID));

        // Both fields must be remapped to the same replacement id.
        assertEquals(REPLACEMENT_ID, mainNewId.getValue());
        assertEquals(REPLACEMENT_ID, contentNewId.getValue());
        assertEquals(mainNewId.getValue(), contentNewId.getValue());

        verify(mapper).removeApplicationVersionById(VERSION_ID);
    }

    @Test
    public void removeVersion_usedInConfigurationApplications_throwsAndDoesNotRemapOrDelete() {
        when(mapper.findVersionById(VERSION_ID)).thenReturn(stubVersion());
        when(mapper.findById(APP_ID)).thenReturn(stubApp());
        when(mapper.isApplicationVersionUsedInConfigurationApplications(VERSION_ID)).thenReturn(true);

        assertThrows(
                ApplicationReferenceExistsException.class,
                () -> dao.removeApplicationVersionById(VERSION_ID));

        verify(mapper, never()).changeConfigurationsMainApplicationForCustomer(any(), any(), any());
        verify(mapper, never()).changeConfigurationsContentApplicationForCustomer(any(), any(), any());
        verify(mapper, never()).findReplacementVersionId(any(), any(), any());
        verify(mapper, never()).removeApplicationVersionById(any());
    }

    @Test
    public void removeVersion_noReplacementAvailable_throwsAndDoesNotRemapOrDelete() {
        when(mapper.findVersionById(VERSION_ID)).thenReturn(stubVersion());
        when(mapper.findById(APP_ID)).thenReturn(stubApp());
        when(mapper.isApplicationVersionUsedInConfigurationApplications(VERSION_ID)).thenReturn(false);
        when(mapper.isApplicationVersionUsedAsMainApp(VERSION_ID)).thenReturn(true);
        when(mapper.isApplicationVersionUsedAsContentApp(VERSION_ID)).thenReturn(false);
        when(mapper.findReplacementVersionId(APP_ID, VERSION_ID, CUSTOMER_ID)).thenReturn(null);

        assertThrows(
                ApplicationReferenceExistsException.class,
                () -> dao.removeApplicationVersionById(VERSION_ID));

        // Explicit assertion: lookup happened with the right arguments, but the result
        // (null) must prevent any UPDATE or DELETE.
        verify(mapper).findReplacementVersionId(eq(APP_ID), eq(VERSION_ID), eq(CUSTOMER_ID));
        verify(mapper, never()).changeConfigurationsMainApplicationForCustomer(any(), any(), any());
        verify(mapper, never()).changeConfigurationsContentApplicationForCustomer(any(), any(), any());
        verify(mapper, never()).removeApplicationVersionById(any());
    }

    @Test
    public void removeVersion_stillReferencedByAnotherTenantAfterRebind_throwsAndDoesNotDelete() {
        // Pre-existing cross-tenant data: another customer's configuration still references this
        // per-customer version. The scoped rebind fixes the owner's configs, but the global re-check
        // still finds the stray reference, so the delete aborts with the domain exception instead of
        // hitting a raw FK violation. (The app cannot create this state via normal use.)
        when(mapper.findVersionById(VERSION_ID)).thenReturn(stubVersion());
        when(mapper.findById(APP_ID)).thenReturn(stubApp());
        when(mapper.isApplicationVersionUsedInConfigurationApplications(VERSION_ID)).thenReturn(false);
        // Still reported in use AFTER the scoped rebind (constant true) = stray cross-tenant reference.
        when(mapper.isApplicationVersionUsedAsMainApp(VERSION_ID)).thenReturn(true);
        when(mapper.isApplicationVersionUsedAsContentApp(VERSION_ID)).thenReturn(false);
        when(mapper.findReplacementVersionId(APP_ID, VERSION_ID, CUSTOMER_ID)).thenReturn(REPLACEMENT_ID);

        assertThrows(
                ApplicationReferenceExistsException.class,
                () -> dao.removeApplicationVersionById(VERSION_ID));

        // The owner's configs were rebound (detection passed), but the row was NOT deleted —
        // the global post-rebind guard blocked it.
        verify(mapper).changeConfigurationsMainApplicationForCustomer(VERSION_ID, REPLACEMENT_ID, CUSTOMER_ID);
        verify(mapper, never()).removeApplicationVersionById(VERSION_ID);
    }

    @Test
    public void removeVersion_deletingLatest_recalculatesAndAutoUpdates() {
        // Deleting the version that IS the application's current latestVersion exercises
        // the recalc + doAutoUpdateToApplicationVersion branch at ApplicationDAO.java:864.
        Application appBefore = new Application();
        appBefore.setId(APP_ID);
        appBefore.setCustomerId(CUSTOMER_ID);
        appBefore.setLatestVersion(VERSION_ID); // deleting the latest

        Application appAfter = new Application();
        appAfter.setId(APP_ID);
        appAfter.setCustomerId(CUSTOMER_ID);
        appAfter.setLatestVersion(REPLACEMENT_ID); // after recalc the new latest

        ApplicationVersion newLatestVersion = new ApplicationVersion();
        newLatestVersion.setId(REPLACEMENT_ID);
        newLatestVersion.setApplicationId(APP_ID);

        when(mapper.findVersionById(VERSION_ID)).thenReturn(stubVersion());
        when(mapper.findVersionById(REPLACEMENT_ID)).thenReturn(newLatestVersion);
        when(mapper.findById(APP_ID)).thenReturn(appBefore, appAfter);
        when(mapper.isApplicationVersionUsedInConfigurationApplications(VERSION_ID)).thenReturn(false);
        // In use at detection, then cleared by the scoped rebind (normal case).
        when(mapper.isApplicationVersionUsedAsMainApp(VERSION_ID)).thenReturn(true, false);
        when(mapper.isApplicationVersionUsedAsContentApp(VERSION_ID)).thenReturn(false);
        when(mapper.findReplacementVersionId(APP_ID, VERSION_ID, CUSTOMER_ID)).thenReturn(REPLACEMENT_ID);

        dao.removeApplicationVersionById(VERSION_ID);

        InOrder inOrder = Mockito.inOrder(mapper);
        inOrder.verify(mapper).changeConfigurationsMainApplicationForCustomer(VERSION_ID, REPLACEMENT_ID, CUSTOMER_ID);
        inOrder.verify(mapper).removeApplicationVersionById(VERSION_ID);
        inOrder.verify(mapper).recalculateLatestVersion(APP_ID);

        // doAutoUpdateToApplicationVersion fires for the new latest.
        verify(mapper).autoUpdateConfigurationsApplication(eq(APP_ID), eq(REPLACEMENT_ID));
        verify(mapper).autoUpdateConfigurationsMainApplication(eq(APP_ID), eq(REPLACEMENT_ID));
        verify(mapper).autoUpdateConfigurationsContentApplication(eq(APP_ID), eq(REPLACEMENT_ID));
    }

    @Test
    public void removeVersionWithApk_sharedFile_guardsEachArchUrlAndKeepsFile() {
        // The APK-delete path must consult countOtherVersionsByUrl for every populated
        // URL column (url, urlArmeabi, urlArm64), excluding the version being deleted, and must do so
        // only after the version row itself has been removed.
        final String urlArmeabi = "http://example.com/old-armeabi.apk";
        final String urlArm64 = "http://example.com/old-arm64.apk";

        SecurityContext.init(stubUser());
        when(customerDAO.findById(CUSTOMER_ID)).thenReturn(new Customer());

        ApplicationVersion version = stubVersion();
        version.setUrlArmeabi(urlArmeabi);
        version.setUrlArm64(urlArm64);
        when(mapper.findVersionById(VERSION_ID)).thenReturn(version);
        when(mapper.findById(APP_ID)).thenReturn(stubApp());
        // Another version still references each file (count > 0), so the guard returns before any
        // filesystem call and the file is kept.
        when(mapper.countOtherVersionsByUrl(anyString(), eq(VERSION_ID))).thenReturn(1);

        dao.removeApplicationVersionByIdWithAPKFile(VERSION_ID);

        // Every populated arch URL column is guarded, with the deleted version excluded from the count.
        verify(mapper).countOtherVersionsByUrl(URL, VERSION_ID);
        verify(mapper).countOtherVersionsByUrl(urlArmeabi, VERSION_ID);
        verify(mapper).countOtherVersionsByUrl(urlArm64, VERSION_ID);

        // The version row is deleted before the file-cleanup guards run.
        InOrder inOrder = Mockito.inOrder(mapper);
        inOrder.verify(mapper).removeApplicationVersionById(VERSION_ID);
        inOrder.verify(mapper).countOtherVersionsByUrl(URL, VERSION_ID);
    }

    @Test
    public void removeVersionWithApk_nullArchUrls_onlyGuardsPopulatedUrl() {
        // Null/empty arch URL columns must be skipped entirely: no count query, no file access.
        SecurityContext.init(stubUser());
        when(customerDAO.findById(CUSTOMER_ID)).thenReturn(new Customer());

        ApplicationVersion version = stubVersion(); // url set; urlArmeabi / urlArm64 left null
        when(mapper.findVersionById(VERSION_ID)).thenReturn(version);
        when(mapper.findById(APP_ID)).thenReturn(stubApp());
        when(mapper.countOtherVersionsByUrl(URL, VERSION_ID)).thenReturn(1);

        dao.removeApplicationVersionByIdWithAPKFile(VERSION_ID);

        verify(mapper, times(1)).countOtherVersionsByUrl(URL, VERSION_ID);
        verify(mapper, never()).countOtherVersionsByUrl(isNull(), any());
    }
}
