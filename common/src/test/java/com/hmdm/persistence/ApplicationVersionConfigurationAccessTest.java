package com.hmdm.persistence;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.hmdm.persistence.domain.Application;
import com.hmdm.persistence.domain.ApplicationVersion;
import com.hmdm.persistence.domain.User;
import com.hmdm.persistence.mapper.ApplicationMapper;
import com.hmdm.rest.json.ApplicationVersionConfigurationLink;
import com.hmdm.rest.json.LinkConfigurationsToAppVersionRequest;
import com.hmdm.security.SecurityContext;
import com.hmdm.security.SecurityException;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ApplicationVersionConfigurationAccessTest {
    private final ApplicationMapper mapper = mock(ApplicationMapper.class);
    private final ApplicationDAO dao = new ApplicationDAO(mapper, null, "/tmp", "", "", null);
    private final User user = new User();
    private final Application application = new Application();

    @BeforeEach
    void setUp() {
        user.setCustomerId(42);
        user.setAllConfigAvailable(true);
        SecurityContext.init(user);
        application.setId(7);
        application.setCustomerId(42);
        var version = new ApplicationVersion();
        version.setId(70);
        version.setApplicationId(7);
        when(mapper.findVersionById(70)).thenReturn(version);
        when(mapper.findById(7)).thenReturn(application);
        when(mapper.isConfigurationOwnedByCustomer(100, 42)).thenReturn(true);
    }

    @AfterEach
    void release() {
        SecurityContext.release();
    }

    @Test
    void rejectsMixedTargetsBeforeAnyMutation() {
        var request = request(100, 101);
        assertThrows(SecurityException.class, () -> dao.updateApplicationVersionConfigurations(request, user));
        verify(mapper, never()).removeApplicationVersionConfigurationsById(anyInt(), anyInt());
        verify(mapper, never()).uninstallOtherVersions(anyInt(), anyInt());
        verify(mapper, never()).insertApplicationVersionConfigurations(anyInt(), anyInt(), anyList());
    }

    @Test
    void directInsertCannotBypassConfigurationOwnershipEvenForCommonApps() {
        application.setCommonApplication(true);
        application.setCustomerId(1);
        assertThrows(SecurityException.class,
                () -> dao.insertApplicationVersionConfigurations(70, request(101).getConfigurations()));
        verify(mapper, never()).insertApplicationVersionConfigurations(anyInt(), anyInt(), anyList());
    }

    @Test
    void allowsOwnApplicationInOwnConfiguration() {
        assertOwnConfigurationUpdated();
    }

    @Test
    void allowsCommonApplicationInOwnConfiguration() {
        application.setCommonApplication(true);
        application.setCustomerId(1);
        assertOwnConfigurationUpdated();
    }

    private void assertOwnConfigurationUpdated() {
        var request = request(100);
        dao.updateApplicationVersionConfigurations(request, user);
        verify(mapper).removeApplicationVersionConfigurationsById(42, 70);
        verify(mapper).uninstallOtherVersions(70, 100);
        verify(mapper).insertApplicationVersionConfigurations(7, 70, request.getConfigurations());
    }

    private LinkConfigurationsToAppVersionRequest request(Integer... ids) {
        var request = new LinkConfigurationsToAppVersionRequest();
        request.setApplicationVersionId(70);
        request.setConfigurations(List.of(ids).stream().map(id -> {
            var link = new ApplicationVersionConfigurationLink();
            link.setConfigurationId(id);
            link.setAction(1);
            return link;
        }).toList());
        return request;
    }
}
