package com.hmdm.rest.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.hmdm.notification.PushService;
import com.hmdm.persistence.ApplicationDAO;
import com.hmdm.persistence.ConfigurationDAO;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.domain.Application;
import com.hmdm.persistence.domain.Configuration;
import com.hmdm.persistence.domain.User;
import com.hmdm.persistence.domain.UserRole;
import com.hmdm.persistence.domain.UserRolePermission;
import com.hmdm.rest.json.ApplicationConfigurationLink;
import com.hmdm.rest.json.ApplicationVersionConfigurationLink;
import com.hmdm.rest.json.LinkConfigurationsToAppRequest;
import com.hmdm.rest.json.LinkConfigurationsToAppVersionRequest;
import com.hmdm.rest.json.LookupItem;
import com.hmdm.rest.json.Response;
import com.hmdm.security.SecurityContext;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ApplicationConfigurationAccessTest {
    private final ApplicationDAO applications = mock(ApplicationDAO.class);
    private final ConfigurationDAO configurations = mock(ConfigurationDAO.class);
    private final UnsecureDAO unsecure = mock(UnsecureDAO.class);
    private final PushService push = mock(PushService.class);
    private final User user = new User();
    private ApplicationResource resource;

    @BeforeEach
    void setUp(@TempDir Path directory) {
        UserRolePermission editApps = new UserRolePermission();
        editApps.setName("edit_applications");
        UserRolePermission editVersions = new UserRolePermission();
        editVersions.setName("edit_application_versions");
        UserRole role = new UserRole();
        role.setPermissions(List.of(editApps, editVersions));
        user.setUserRole(role);
        user.setCustomerId(42);
        user.setAllConfigAvailable(true);
        SecurityContext.init(user);
        when(unsecure.getMasterCustomerId()).thenReturn(1);
        resource = new ApplicationResource(applications, configurations, unsecure, push, directory.toString());
    }

    @AfterEach
    void releaseContext() {
        SecurityContext.release();
    }

    @Test
    void allowsMasterOwnedApplicationInOwnConfiguration() {
        assertLinksRetained(1, 42, true);
    }

    @Test
    void allowsOwnApplicationInOwnConfiguration() {
        assertLinksRetained(42, 42, true);
    }

    @Test
    void rejectsOtherTenantsApplication() {
        assertLinksRetained(99, 42, false);
    }

    @Test
    void masterApplicationDoesNotAllowForeignConfiguration() {
        // Even if the DAO returns it, the resource must not link to another tenant.
        assertLinksRetained(1, 99, false);
    }

    @Test
    void absentMasterStillAllowsOwnApplication() {
        when(unsecure.getMasterCustomerId()).thenReturn(null);
        assertLinksRetained(42, 42, true);
    }

    @Test
    void absentMasterDoesNotTreatZeroAsSharedCustomer() {
        when(unsecure.getMasterCustomerId()).thenReturn(null);
        assertLinksRetained(0, 42, false);
    }

    @Test
    void restrictedUserOnlyLinksAssignedConfigurations() {
        user.setAllConfigAvailable(false);
        LookupItem allowed = new LookupItem();
        allowed.setId(100);
        user.setConfigurations(List.of(allowed));
        var allowedLink = link(100, 1, 42);
        var forbiddenLink = link(101, 1, 42);
        var request = new LinkConfigurationsToAppRequest();
        request.setApplicationId(7);
        request.setConfigurations(new ArrayList<>(List.of(allowedLink, forbiddenLink)));

        assertEquals(Response.ResponseStatus.OK, resource.updateApplicationConfigurations(request).getStatus());
        assertEquals(List.of(allowedLink), request.getConfigurations());
        verify(configurations, never()).getConfigurationById(101);
        verify(push).notifyDevicesOnUpdate(100);
        verify(push, never()).notifyDevicesOnUpdate(101);
    }

    @Test
    void restrictedUserWithNoAssignedConfigurationsCannotLinkAny() {
        user.setAllConfigAvailable(false);
        user.setConfigurations(List.of());
        assertLinksRetained(1, 42, false);
        verifyNoInteractions(configurations);
    }

    @Test
    void versionLinkingAlsoFiltersUnassignedConfigurations() {
        link(100, 42, 42);
        user.setAllConfigAvailable(false);
        LookupItem allowed = new LookupItem();
        allowed.setId(100);
        user.setConfigurations(List.of(allowed));
        var allowedLink = new ApplicationVersionConfigurationLink();
        allowedLink.setConfigurationId(100);
        allowedLink.setNotify(true);
        var forbiddenLink = new ApplicationVersionConfigurationLink();
        forbiddenLink.setConfigurationId(101);
        forbiddenLink.setNotify(true);
        var request = new LinkConfigurationsToAppVersionRequest();
        request.setApplicationVersionId(70);
        request.setConfigurations(new ArrayList<>(List.of(allowedLink, forbiddenLink)));

        assertEquals(Response.ResponseStatus.OK, resource.updateApplicationVersionConfigurations(request).getStatus());
        assertEquals(List.of(allowedLink), request.getConfigurations());
        verify(applications).updateApplicationVersionConfigurations(request, user);
        verify(push).notifyDevicesOnUpdate(100);
        verify(push, never()).notifyDevicesOnUpdate(101);
    }

    @Test
    void administratorVersionLinksOnlyExistingOwnConfigurations() {
        link(100, 42, 42);
        link(101, 42, 99);
        var own = versionLink(100);
        var foreign = versionLink(101);
        var missing = versionLink(102);
        var request = new LinkConfigurationsToAppVersionRequest();
        request.setApplicationVersionId(70);
        request.setConfigurations(new ArrayList<>(List.of(own, foreign, missing)));

        assertEquals(Response.ResponseStatus.OK, resource.updateApplicationVersionConfigurations(request).getStatus());
        assertEquals(List.of(own), request.getConfigurations());
        verify(applications).updateApplicationVersionConfigurations(request, user);
        verify(push).notifyDevicesOnUpdate(100);
        verify(push, never()).notifyDevicesOnUpdate(101);
        verify(push, never()).notifyDevicesOnUpdate(102);
    }

    @Test
    void ownershipExceptionDeniesVersionRequestBeforeWritesOrPush() {
        when(configurations.getConfigurationById(101)).thenThrow(
                com.hmdm.security.SecurityException.onConfigurationAccessViolation(101));
        var request = new LinkConfigurationsToAppVersionRequest();
        request.setApplicationVersionId(70);
        request.setConfigurations(new ArrayList<>(List.of(versionLink(101))));

        assertEquals("error.permission.denied", resource.updateApplicationVersionConfigurations(request).getMessage());
        verifyNoInteractions(applications, push);
    }

    @Test
    void missingApplicationIsFilteredWithoutInternalError() {
        assertMissingLinkFiltered(true);
    }

    @Test
    void missingConfigurationIsFilteredWithoutInternalError() {
        assertMissingLinkFiltered(false);
    }

    private void assertMissingLinkFiltered(boolean missingApplication) {
        var link = link(100, 42, 42);
        if (missingApplication) {
            when(applications.findById(7)).thenReturn(null);
        } else {
            when(configurations.getConfigurationById(100)).thenReturn(null);
        }
        var request = new LinkConfigurationsToAppRequest();
        request.setApplicationId(7);
        request.setConfigurations(new ArrayList<>(List.of(link)));
        assertEquals(Response.ResponseStatus.OK, resource.updateApplicationConfigurations(request).getStatus());
        assertEquals(List.of(), request.getConfigurations());
        verifyNoInteractions(push);
    }

    private ApplicationVersionConfigurationLink versionLink(int id) {
        var link = new ApplicationVersionConfigurationLink();
        link.setConfigurationId(id);
        link.setAction(1);
        link.setNotify(true);
        return link;
    }

    @Test
    void sharedApplicationDoesNotBypassEditPermission() {
        user.getUserRole().setPermissions(List.of());
        resource.updateApplicationConfigurations(new LinkConfigurationsToAppRequest());
        verifyNoInteractions(applications, configurations, unsecure, push);
    }

    private ApplicationConfigurationLink link(int configurationId, int appCustomer, int configurationCustomer) {
        Application application = new Application();
        application.setId(7);
        application.setCustomerId(appCustomer);
        when(applications.findById(7)).thenReturn(application);
        Configuration configuration = new Configuration();
        configuration.setId(configurationId);
        configuration.setCustomerId(configurationCustomer);
        when(configurations.getConfigurationById(configurationId)).thenReturn(configuration);
        ApplicationConfigurationLink link = new ApplicationConfigurationLink();
        link.setApplicationId(7);
        link.setConfigurationId(configurationId);
        link.setAction(1);
        link.setNotify(true);
        return link;
    }

    private void assertLinksRetained(int appCustomer, int configurationCustomer, boolean retained) {
        var link = link(100, appCustomer, configurationCustomer);
        var request = new LinkConfigurationsToAppRequest();
        request.setApplicationId(7);
        request.setConfigurations(new ArrayList<>(List.of(link)));

        assertEquals(Response.ResponseStatus.OK, resource.updateApplicationConfigurations(request).getStatus());
        assertEquals(retained ? List.of(link) : List.of(), request.getConfigurations());
        verify(applications).updateApplicationConfigurations(request);
        verify(push, times(retained ? 1 : 0)).notifyDevicesOnUpdate(100);
    }
}
