package heojin.simulator.simulation.dto;

import java.time.Instant;

public record SimulationStatusResponse(
	boolean running,
	Instant startedAt,
	Instant endsAt,
	long intervalMs,
	long tickCount,
	long sentCount,
	long failureCount
) {
}
