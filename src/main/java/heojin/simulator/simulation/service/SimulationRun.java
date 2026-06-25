package heojin.simulator.simulation.service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

class SimulationRun {

	private final Instant startedAt;
	private final Instant endsAt;
	private final long intervalMs;
	private final List<BusSimulationState> buses;
	private final AtomicBoolean running = new AtomicBoolean(true);
	private final AtomicLong tickCount = new AtomicLong();
	private final AtomicLong sentCount = new AtomicLong();
	private final AtomicLong failureCount = new AtomicLong();
	private ScheduledFuture<?> tickFuture;
	private ScheduledFuture<?> endFuture;

	SimulationRun(Instant startedAt, Instant endsAt, long intervalMs, List<BusSimulationState> buses) {
		this.startedAt = startedAt;
		this.endsAt = endsAt;
		this.intervalMs = intervalMs;
		this.buses = buses;
	}

	Instant startedAt() {
		return startedAt;
	}

	Instant endsAt() {
		return endsAt;
	}

	long intervalMs() {
		return intervalMs;
	}

	List<BusSimulationState> buses() {
		return buses;
	}

	boolean isRunning() {
		return running.get();
	}

	void setFutures(ScheduledFuture<?> tickFuture, ScheduledFuture<?> endFuture) {
		this.tickFuture = tickFuture;
		this.endFuture = endFuture;
	}

	void incrementTickCount() {
		tickCount.incrementAndGet();
	}

	void incrementSentCount() {
		sentCount.incrementAndGet();
	}

	void incrementFailureCount() {
		failureCount.incrementAndGet();
	}

	long tickCount() {
		return tickCount.get();
	}

	long sentCount() {
		return sentCount.get();
	}

	long failureCount() {
		return failureCount.get();
	}

	void complete() {
		if (!running.compareAndSet(true, false)) {
			return;
		}
		if (tickFuture != null) {
			tickFuture.cancel(false);
		}
		if (endFuture != null) {
			endFuture.cancel(false);
		}
	}
}
