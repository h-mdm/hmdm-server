package com.hmdm.rest.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class DeviceInfoMacTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void deviceReportPreservesMacWhenStoredAsJson() throws Exception {
        DeviceInfo info = mapper.readValue("{\"mac\":\"02:11:22:33:44:55\",\"serial\":\"abc\"}", DeviceInfo.class);
        var stored = mapper.readTree(mapper.writeValueAsString(info));
        assertEquals("02:11:22:33:44:55", stored.path("mac").asText());
        assertEquals("abc", stored.path("serial").asText());
    }

    @Test
    void olderDeviceReportsNeedNotContainMac() throws Exception {
        DeviceInfo info = mapper.readValue("{\"serial\":\"abc\"}", DeviceInfo.class);
        assertFalse(mapper.readTree(mapper.writeValueAsString(info)).has("mac"));
    }
}
