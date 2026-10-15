package com.hmdm.plugins;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class TenantIsolationImageTest {
    @Test
    void runtimeOverrideWorks() {
        String image = "postgres:16.4-alpine3.20@sha256:" + "a".repeat(64);
        String previous = System.getProperty("hmdm.it.pgImage");
        try {
            System.setProperty("hmdm.it.pgImage", image);
            assertEquals(image, TenantIsolationSupport.resolveImage().asCanonicalNameString());
        } finally {
            if (previous == null) {
                System.clearProperty("hmdm.it.pgImage");
            } else {
                System.setProperty("hmdm.it.pgImage", previous);
            }
        }
    }

    @Test
    void committedImageResolvesWithoutOverride() {
        String previous = System.getProperty("hmdm.it.pgImage");
        try {
            System.clearProperty("hmdm.it.pgImage");
            assertEquals(TenantIsolationSupport.PINNED_IMAGE,
                    TenantIsolationSupport.resolveImage().asCanonicalNameString());
        } finally {
            if (previous != null) {
                System.setProperty("hmdm.it.pgImage", previous);
            }
        }
    }

    @Test
    void rejectsFloatingTagsAndPlaceholder() {
        assertThrows(IllegalStateException.class, () -> TenantIsolationSupport.resolveImage("postgres:16-alpine"));
        assertThrows(IllegalStateException.class, () -> TenantIsolationSupport.resolveImage("postgres:16.4-alpine3.20@sha256:UNRESOLVED"));
    }
}
