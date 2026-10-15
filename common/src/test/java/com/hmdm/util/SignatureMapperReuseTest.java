package com.hmdm.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;

class SignatureMapperReuseTest {
    @Test
    void concurrentSignaturesPreserveLegacySerialization() throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("text", "a b\t更新\n");
        payload.put("items", List.of(1, 2, 3));
        payload.put("empty", null);
        String legacyJson = new ObjectMapper().writeValueAsString(payload).replaceAll("\\s", "");
        String expected = CryptoUtil.getSHA1String("secret" + legacyJson);
        try (var executor = Executors.newFixedThreadPool(4)) {
            List<Callable<String>> tasks = java.util.stream.IntStream.range(0, 100)
                    .mapToObj(i -> (Callable<String>) () -> CryptoUtil.getDataSignature("secret", payload))
                    .toList();
            for (var result : executor.invokeAll(tasks)) {
                assertEquals(expected, result.get());
            }
        }
    }
}
