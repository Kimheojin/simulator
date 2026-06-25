package heojin.simulator.simulation.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.random.RandomGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import heojin.simulator.global.exception.SimulatorException;
import heojin.simulator.simulation.client.ControlApiClient;
import heojin.simulator.simulation.dto.SimulationStartResponse;
import heojin.simulator.simulation.dto.SimulationStatusResponse;
import heojin.simulator.simulation.dto.TelemetryRequest;

@Service
public class SimulationService {

	private static final Logger log = LoggerFactory.getLogger(SimulationService.class);

	private final Object lock = new Object();
	private final SimulatorProperties properties;
	private final SimulationSeedDataLoader seedDataLoader;
	private final ControlApiClient controlApiClient;
	private final TaskScheduler taskScheduler;
	private final Clock clock;
	private final RandomGenerator random = new Random();
	private SimulationRun currentRun;

	public SimulationService(
		SimulatorProperties properties,
		SimulationSeedDataLoader seedDataLoader,
		ControlApiClient controlApiClient,
		TaskScheduler taskScheduler,
		Clock clock
	) {
		this.properties = properties;
		this.seedDataLoader = seedDataLoader;
		this.controlApiClient = controlApiClient;
		this.taskScheduler = taskScheduler;
		this.clock = clock;
	}

	public SimulationStartResponse start() {
		synchronized (lock) {
			if (currentRun != null && currentRun.isRunning()) {
				throw new SimulatorException(
					HttpStatus.CONFLICT,
					"SIMULATION_ALREADY_RUNNING",
					"이미 실행 중인 시뮬레이션이 있습니다."
				);
			}

			SimulationSeedData seedData = seedDataLoader.load();
			List<BusSimulationState> activeBuses = createActiveBusStates(seedData);
			Instant startedAt = clock.instant();
			Instant endsAt = startedAt.plus(properties.duration());
			SimulationRun run = new SimulationRun(startedAt, endsAt, properties.intervalMs(), activeBuses);
			currentRun = run;

			ScheduledFuture<?> tickFuture = taskScheduler.scheduleAtFixedRate(
				() -> publishTick(run),
				startedAt,
				Duration.ofMillis(properties.intervalMs())
			);
			ScheduledFuture<?> endFuture = taskScheduler.schedule(() -> complete(run), endsAt);
			run.setFutures(tickFuture, endFuture);

			return new SimulationStartResponse(
				run.isRunning(),
				run.startedAt(),
				run.endsAt(),
				run.intervalMs(),
				properties.duration().toSeconds()
			);
		}
	}

	public SimulationStatusResponse currentStatus() {
		synchronized (lock) {
			if (currentRun == null) {
				return new SimulationStatusResponse(false, null, null, properties.intervalMs(), 0, 0, 0);
			}
			return toStatusResponse(currentRun);
		}
	}

	private List<BusSimulationState> createActiveBusStates(SimulationSeedData seedData) {
		Set<Long> offlineBusIds = chooseOfflineBusIds(seedData.buses());
		List<BusSimulationState> activeBuses = new ArrayList<>();
		for (BusPlan bus : seedData.buses()) {
			if (offlineBusIds.contains(bus.busId())) {
				continue;
			}
			RoutePlan routePlan = seedData.routePlans().get(bus.routeId());
			activeBuses.add(new BusSimulationState(bus.busId(), routePlan, random.nextDouble()));
		}
		return activeBuses;
	}

	private Set<Long> chooseOfflineBusIds(List<BusPlan> buses) {
		if (buses.size() <= 1 || properties.offlineRatio() <= 0) {
			return Set.of();
		}
		int offlineCount = (int)Math.round(buses.size() * properties.offlineRatio());
		offlineCount = Math.min(Math.max(offlineCount, 0), buses.size() - 1);
		List<BusPlan> candidates = new ArrayList<>(buses);
		java.util.Collections.shuffle(candidates, new Random());

		Set<Long> offlineBusIds = new HashSet<>();
		for (int i = 0; i < offlineCount; i++) {
			offlineBusIds.add(candidates.get(i).busId());
		}
		return offlineBusIds;
	}

	private void publishTick(SimulationRun run) {
		if (!run.isRunning()) {
			return;
		}
		if (!clock.instant().isBefore(run.endsAt())) {
			complete(run);
			return;
		}

		run.incrementTickCount();
		Instant recordedAt = clock.instant();
		for (BusSimulationState bus : run.buses()) {
			TelemetryRequest request = bus.nextTelemetry(recordedAt, random);
			try {
				controlApiClient.sendTelemetry(request);
				run.incrementSentCount();
			}
			catch (Exception exception) {
				run.incrementFailureCount();
				log.warn("Failed to send telemetry. busId={}", request.busId(), exception);
			}
		}
	}

	private void complete(SimulationRun run) {
		synchronized (lock) {
			if (currentRun == run) {
				run.complete();
			}
		}
	}

	private SimulationStatusResponse toStatusResponse(SimulationRun run) {
		return new SimulationStatusResponse(
			run.isRunning(),
			run.startedAt(),
			run.endsAt(),
			run.intervalMs(),
			run.tickCount(),
			run.sentCount(),
			run.failureCount()
		);
	}
}
