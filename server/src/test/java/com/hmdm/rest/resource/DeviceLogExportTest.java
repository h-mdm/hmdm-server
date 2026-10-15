package com.hmdm.rest.resource;

import static org.mockito.Mockito.*;

import com.hmdm.persistence.domain.User;
import com.hmdm.persistence.domain.UserRole;
import com.hmdm.persistence.domain.UserRolePermission;
import com.hmdm.plugins.devicelog.model.DeviceLogRecord;
import com.hmdm.plugins.devicelog.persistence.DeviceLogDAO;
import com.hmdm.plugins.devicelog.rest.json.DeviceLogFilter;
import com.hmdm.plugins.devicelog.rest.resource.DeviceLogResource;
import com.hmdm.security.SecurityContext;
import jakarta.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeviceLogExportTest {
    private final DeviceLogDAO dao = mock(DeviceLogDAO.class);
    private final DeviceLogFilter filter = new DeviceLogFilter();
    private final DeviceLogResource resource = new DeviceLogResource(dao, null, null);

    @BeforeEach
    void setUp() {
        UserRolePermission permission = new UserRolePermission();
        permission.setName("plugin_devicelog_access");
        UserRole role = new UserRole();
        role.setPermissions(List.of(permission));
        User user = new User();
        user.setUserRole(role);
        SecurityContext.init(user);

        DeviceLogRecord record = mock(DeviceLogRecord.class);
        when(record.getCreateTime()).thenReturn(1L);
        when(dao.findAll(filter)).thenReturn(List.of(record, record), List.of());
    }

    @AfterEach
    void releaseContext() {
        SecurityContext.release();
    }

    @Test
    void flushesOncePerPageRatherThanPerRecord() throws Exception {
        OutputStream output = mock(OutputStream.class);
        try (var response = resource.exportLogs(filter)) {
            ((StreamingOutput) response.getEntity()).write(output);
        }
        verify(output, times(2)).write(any(byte[].class));
        verify(output).flush();
        verify(dao, times(2)).findAll(filter);
    }

    @Test
    void stopsWritingAndFetchingWhenClientDisconnects() throws Exception {
        OutputStream output = mock(OutputStream.class);
        doThrow(new IOException("client disconnected")).when(output).write(any(byte[].class));
        try (var response = resource.exportLogs(filter)) {
            ((StreamingOutput) response.getEntity()).write(output);
        }
        verify(output).write(any(byte[].class));
        verify(output, never()).flush();
        verify(dao).findAll(filter);
    }
}
