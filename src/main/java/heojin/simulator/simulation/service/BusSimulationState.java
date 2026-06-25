package heojin.simulator.simulation.service;

import java.time.Instant;
import java.util.List;
import java.util.random.RandomGenerator;

import heojin.simulator.simulation.dto.TelemetryRequest;

class BusSimulationState {

	private final Long busId;
	private final RoutePlan routePlan;
	private int segmentIndex;
	private double progress;
	private int direction = 1;
	private int lastSpeedKph;

	BusSimulationState(Long busId, RoutePlan routePlan, double initialProgress) {
		this.busId = busId;
		this.routePlan = routePlan;
		this.progress = initialProgress;
	}

	TelemetryRequest nextTelemetry(Instant recordedAt, RandomGenerator random) {
		int speedKph = nextSpeed(random);
		progress += nextProgress(speedKph);
		normalizePosition();

		List<Coordinate> stops = routePlan.stops();
		Coordinate current = stops.get(segmentIndex);
		Coordinate next = stops.get(segmentIndex + direction);
		Coordinate location = current.interpolate(next, progress);
		TelemetryRequest.EventRequest event = nextEvent(speedKph, random);
		lastSpeedKph = speedKph;

		return new TelemetryRequest(
			busId,
			round(location.latitude()),
			round(location.longitude()),
			speedKph,
			recordedAt,
			event
		);
	}

	private int nextSpeed(RandomGenerator random) {
		boolean nearStop = progress < 0.12 || progress > 0.88;
		if (nearStop && random.nextDouble() < 0.45) {
			return random.nextInt(0, 16);
		}
		return random.nextInt(20, 61);
	}

	private double nextProgress(int speedKph) {
		return Math.max(0.03, Math.min(0.35, speedKph / 180.0));
	}

	private void normalizePosition() {
		while (progress >= 1.0) {
			progress -= 1.0;
			segmentIndex += direction;

			if (segmentIndex >= routePlan.stops().size() - 1) {
				segmentIndex = routePlan.stops().size() - 1;
				direction = -1;
			}
			if (segmentIndex <= 0) {
				segmentIndex = 0;
				direction = 1;
			}
		}
	}

	private TelemetryRequest.EventRequest nextEvent(int speedKph, RandomGenerator random) {
		if (random.nextDouble() < 0.005) {
			return new TelemetryRequest.EventRequest("IMPACT", "충격 감지");
		}
		if (lastSpeedKph - speedKph >= 25 && random.nextDouble() < 0.35) {
			return new TelemetryRequest.EventRequest("SUDDEN_BRAKE", "급정거 감지");
		}
		if (speedKph - lastSpeedKph >= 25 && random.nextDouble() < 0.35) {
			return new TelemetryRequest.EventRequest("RAPID_ACCELERATION", "급가속 감지");
		}
		return null;
	}

	private double round(double value) {
		return Math.round(value * 10_000_000.0) / 10_000_000.0;
	}
}
