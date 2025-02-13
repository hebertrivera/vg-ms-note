package pe.edu.vallegrande.vgmsnote.application.healthcheck;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class MemoryHealthIndicator implements HealthIndicator {

    private static final double MEMORY_THRESHOLD_PERCENT = 25.0;

    @Override
    public Health health() {
        long freeMemory = Runtime.getRuntime().freeMemory();
        long totalMemory = Runtime.getRuntime().totalMemory();
        double freeMemoryPercent = ((double) freeMemory / (double) totalMemory) * 100;

        return buildHealth(freeMemory, totalMemory, freeMemoryPercent);
    }

    private Health buildHealth(long freeMemory, long totalMemory, double freeMemoryPercent) {
        Health.Builder status = (freeMemoryPercent > MEMORY_THRESHOLD_PERCENT) ? Health.up() : Health.down();
        return status
                .withDetail("free_memory", freeMemory + " bytes")
                .withDetail("total_memory", totalMemory + " bytes")
                .withDetail("free_memory_percent", freeMemoryPercent + "%")
                .build();
    }
}