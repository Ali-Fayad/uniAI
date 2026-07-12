package com.uniai.chat.infrastructure.ai;

import com.uniai.chat.application.provider.AiProviderFailureCategory;
import com.uniai.chat.application.provider.AiProviderRuntimeStatus;
import com.uniai.chat.application.provider.AiProviderStatusSnapshot;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiProviderStatusRegistryTest {

    @Test
    void registryShouldStartUnknownAndRecordLatestSnapshot() throws Exception {
        InMemoryAiProviderStatusRegistry registry = new InMemoryAiProviderStatusRegistry();

        AiProviderStatusSnapshot initial = registry.getStatus("gemini");
        assertEquals(AiProviderRuntimeStatus.UNKNOWN, initial.status());
        assertEquals(AiProviderFailureCategory.UNKNOWN, initial.lastFailureCategory());

        registry.recordSuccess("gemini", "gemini-2.5-flash", 42L);
        AiProviderStatusSnapshot afterSuccess = registry.getStatus("gemini");
        assertEquals(AiProviderRuntimeStatus.AVAILABLE, afterSuccess.status());
        assertEquals(AiProviderFailureCategory.NONE, afterSuccess.lastFailureCategory());
        assertEquals("gemini-2.5-flash", afterSuccess.model());
        assertEquals(42L, afterSuccess.lastLatencyMs());
        assertNotNull(afterSuccess.lastSuccessAt());

        registry.recordFailure("gemini", "gemini-2.5-flash", AiProviderFailureCategory.RATE_LIMITED, 99L);
        AiProviderStatusSnapshot afterFailure = registry.getStatus("gemini");
        assertEquals(AiProviderRuntimeStatus.UNAVAILABLE, afterFailure.status());
        assertEquals(AiProviderFailureCategory.RATE_LIMITED, afterFailure.lastFailureCategory());
        assertEquals(99L, afterFailure.lastLatencyMs());
        assertNotNull(afterFailure.lastFailureAt());
    }

    @Test
    void registryShouldHandleConcurrentUpdates() throws Exception {
        InMemoryAiProviderStatusRegistry registry = new InMemoryAiProviderStatusRegistry();
        var executor = Executors.newFixedThreadPool(4);
        CountDownLatch latch = new CountDownLatch(2);
        List<Runnable> tasks = new ArrayList<>();
        tasks.add(() -> {
            registry.recordSuccess("groq", "llama-3.3-70b-versatile", 11L);
            latch.countDown();
        });
        tasks.add(() -> {
            registry.recordFailure("groq", "llama-3.3-70b-versatile", AiProviderFailureCategory.TIMEOUT, 22L);
            latch.countDown();
        });

        for (Runnable task : tasks) {
            executor.submit(task);
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        executor.shutdownNow();

        AiProviderStatusSnapshot snapshot = registry.getStatus("groq");
        assertEquals("groq", snapshot.provider());
        assertTrue(snapshot.status() == AiProviderRuntimeStatus.AVAILABLE
                || snapshot.status() == AiProviderRuntimeStatus.UNAVAILABLE);
        assertNotNull(snapshot.lastLatencyMs());
        assertEquals(1, registry.getAllStatuses().size());
    }
}
