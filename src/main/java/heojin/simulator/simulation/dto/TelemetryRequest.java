package heojin.simulator.simulation.dto;

import java.time.Instant;

public record TelemetryRequest(
	Long busId,
	double latitude,
	double longitude,
	int speedKph,
	Instant recordedAt,
	EventRequest event
) {

	public record EventRequest(
		String type,
		String description
	) {
	}
}
