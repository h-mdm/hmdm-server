package com.hmdm.guice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class InitializerShutdownTest {
    @Test
    @SuppressWarnings("unchecked")
    void stopsRetainedTasksInReverseOrderEvenWhenOneFails() throws Exception {
        Initializer initializer = new Initializer();
        var field = Initializer.class.getDeclaredField("taskShutdowns");
        field.setAccessible(true);
        List<Runnable> shutdowns = (List<Runnable>) field.get(initializer);
        List<Integer> calls = new ArrayList<>();
        shutdowns.add(() -> calls.add(1));
        shutdowns.add(() -> {
            calls.add(2);
            throw new IllegalStateException("test failure");
        });
        shutdowns.add(() -> calls.add(3));
        var event = new ServletContextEvent(mock(ServletContext.class));
        initializer.contextDestroyed(event);
        initializer.contextDestroyed(event);
        assertEquals(List.of(3, 2, 1), calls);
    }
}
