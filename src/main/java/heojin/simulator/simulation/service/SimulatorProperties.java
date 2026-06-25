package heojin.simulator.simulation.service;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "simulator")
public record SimulatorProperties(
	long intervalMs,
	Duration duration,
	double offlineRatio
) {
}
