package heojin.simulator.simulation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import heojin.simulator.simulation.dto.SimulationStartResponse;
import heojin.simulator.simulation.dto.SimulationStatusResponse;
import heojin.simulator.simulation.service.SimulationService;

class SimulationControllerTest {

	private final SimulationService simulationService = mock(SimulationService.class);
	private final SimulationController controller = new SimulationController(simulationService);

	@Test
	void startReturnsAcceptedWithStartResponse() {
		SimulationStartResponse startResponse = new SimulationStartResponse(
			true,
			Instant.parse("2026-06-25T00:00:00Z"),
			Instant.parse("2026-06-25T00:05:00Z"),
			5000,
			300
		);
		when(simulationService.start()).thenReturn(startResponse);

		ResponseEntity<SimulationStartResponse> response = controller.start();

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
		assertThat(response.getBody()).isEqualTo(startResponse);
	}

	@Test
	void currentReturnsSimulationStatus() {
		SimulationStatusResponse statusResponse = new SimulationStatusResponse(
			true,
			Instant.parse("2026-06-25T00:00:00Z"),
			Instant.parse("2026-06-25T00:05:00Z"),
			5000,
			1,
			8,
			0
		);
		when(simulationService.currentStatus()).thenReturn(statusResponse);

		SimulationStatusResponse response = controller.current();

		assertThat(response).isEqualTo(statusResponse);
	}
}
