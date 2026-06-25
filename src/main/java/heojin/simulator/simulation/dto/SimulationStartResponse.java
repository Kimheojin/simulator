package heojin.simulator.simulation.dto;

import java.time.Instant;

public record SimulationStartResponse(
	boolean running,
	Instant startedAt,
	Instant endsAt,
	long intervalMs,
	long durationSeconds
) {
}
