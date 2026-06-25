package heojin.simulator.simulation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Delayed;
import java.util.concurrent.ScheduledFuture;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;

import heojin.simulator.global.exception.SimulatorException;
import heojin.simulator.simulation.client.ControlApiClient;
import heojin.simulator.simulation.dto.SimulationStartResponse;
import heojin.simulator.simulation.dto.SimulationStatusResponse;
import heojin.simulator.simulation.dto.TelemetryRequest;

class SimulationServiceTest {

	private final SimulatorProperties properties = new SimulatorProperties(5000, Duration.ofMinutes(5), 0);
	private final SimulationSeedDataLoader seedDataLoader = mock(SimulationSeedDataLoader.class);
	private final ControlApiClient controlApiClient = mock(ControlApiClient.class);
	private final CapturingTaskScheduler taskScheduler = new CapturingTaskScheduler();
	private final Clock clock = Clock.fixed(Instant.parse("2026-06-25T00:00:00Z"), ZoneOffset.UTC);
	private final SimulationService simulationService = new SimulationService(
		properties,
		seedDataLoader,
		controlApiClient,
		taskScheduler,
		clock
	);

	@Test
	void startCreatesRunningSimulationAndSchedulesTicks() {
		org.mockito.Mockito.when(seedDataLoader.load()).thenReturn(seedData());

		SimulationStartResponse response = simulationService.start();

		assertThat(response.running()).isTrue();
		assertThat(response.startedAt()).isEqualTo(Instant.parse("2026-06-25T00:00:00Z"));
		assertThat(response.endsAt()).isEqualTo(Instant.parse("2026-06-25T00:05:00Z"));
		assertThat(response.intervalMs()).isEqualTo(5000);
		assertThat(response.durationSeconds()).isEqualTo(300);
		assertThat(taskScheduler.fixedRateTask).isNotNull();
		assertThat(taskScheduler.endTask).isNotNull();
	}

	@Test
	void startRejectsDuplicateRun() {
		org.mockito.Mockito.when(seedDataLoader.load()).thenReturn(seedData());

		simulationService.start();

		assertThatThrownBy(simulationService::start)
			.isInstanceOf(SimulatorException.class)
			.extracting("code")
			.isEqualTo("SIMULATION_ALREADY_RUNNING");
	}

	@Test
	void tickContinuesWhenTelemetrySendFails() {
		org.mockito.Mockito.when(seedDataLoader.load()).thenReturn(seedData());
		doThrow(new RuntimeException("connection refused"))
			.when(controlApiClient)
			.sendTelemetry(any(TelemetryRequest.class));

		simulationService.start();
		taskScheduler.fixedRateTask.run();

		SimulationStatusResponse status = simulationService.currentStatus();
		assertThat(status.tickCount()).isEqualTo(1);
		assertThat(status.sentCount()).isZero();
		assertThat(status.failureCount()).isEqualTo(1);
	}

	@Test
	void tickSendsTelemetryForActiveBuses() {
		org.mockito.Mockito.when(seedDataLoader.load()).thenReturn(seedData());

		simulationService.start();
		taskScheduler.fixedRateTask.run();

		verify(controlApiClient).sendTelemetry(any(TelemetryRequest.class));
		SimulationStatusResponse status = simulationService.currentStatus();
		assertThat(status.tickCount()).isEqualTo(1);
		assertThat(status.sentCount()).isEqualTo(1);
		assertThat(status.failureCount()).isZero();
	}

	private SimulationSeedData seedData() {
		return new SimulationSeedData(
			List.of(new BusPlan(1L, 10L)),
			Map.of(10L, new RoutePlan(10L, List.of(
				new Coordinate(37.5775000, 126.8903000),
				new Coordinate(37.5683000, 126.8972000)
			)))
		);
	}

	private static class CapturingTaskScheduler implements TaskScheduler {

		private Runnable fixedRateTask;
		private Runnable endTask;

		@Override
		public ScheduledFuture<?> schedule(Runnable task, Trigger trigger) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ScheduledFuture<?> schedule(Runnable task, Instant startTime) {
			this.endTask = task;
			return new CompletedScheduledFuture();
		}

		@Override
		public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Instant startTime, Duration period) {
			this.fixedRateTask = task;
			return new CompletedScheduledFuture();
		}

		@Override
		public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Duration period) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Instant startTime, Duration delay) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Duration delay) {
			throw new UnsupportedOperationException();
		}
	}

	private static class CompletedScheduledFuture implements ScheduledFuture<Object> {

		@Override
		public long getDelay(java.util.concurrent.TimeUnit unit) {
			return 0;
		}

		@Override
		public int compareTo(Delayed other) {
			return 0;
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			return true;
		}

		@Override
		public boolean isCancelled() {
			return false;
		}

		@Override
		public boolean isDone() {
			return true;
		}

		@Override
		public Object get() {
			return null;
		}

		@Override
		public Object get(long timeout, java.util.concurrent.TimeUnit unit) {
			return null;
		}
	}
}
