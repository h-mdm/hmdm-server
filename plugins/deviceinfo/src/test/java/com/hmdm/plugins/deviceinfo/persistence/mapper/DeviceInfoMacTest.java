package com.hmdm.plugins.deviceinfo.persistence.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmdm.plugins.deviceinfo.rest.json.DeviceInfo;
import java.util.Map;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.junit.jupiter.api.Test;

class DeviceInfoMacTest {
    @Test
    void detailedInfoProjectsMacFromExistingJsonColumn() {
        Configuration configuration = new Configuration();
        configuration.addMapper(DeviceInfoMapper.class);
        var statement = configuration.getMappedStatement(DeviceInfoMapper.class.getName() + ".getDetailedDeviceInfo");
        var sql = statement.getBoundSql(Map.of("id", 42));
        assertTrue(sql.getSql().contains("devices.infojson ->> 'mac' AS mac"));
        assertTrue(sql.getSql().contains("WHERE devices.id = ?"));
        assertEquals(DeviceInfo.class, statement.getResultMaps().getFirst().getType());
    }

    @Test
    void myBatisPropertyIsExposedInDeviceInfoResponse() throws Exception {
        DeviceInfo info = new DeviceInfo();
        SystemMetaObject.forObject(info).setValue("mac", "02:11:22:33:44:55");
        var mapper = new ObjectMapper();
        assertEquals("02:11:22:33:44:55", mapper.readTree(mapper.writeValueAsString(info)).path("mac").asText());
    }
}
