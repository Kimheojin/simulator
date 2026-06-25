package heojin.simulator.simulation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import heojin.simulator.simulation.dto.SimulationStartResponse;
import heojin.simulator.simulation.dto.SimulationStatusResponse;
import heojin.simulator.simulation.service.SimulationService;

@RestController
@RequestMapping("/v2/simulations")
public class SimulationController {

	private final SimulationService simulationService;

	public SimulationController(SimulationService simulationService) {
		this.simulationService = simulationService;
	}

	@PostMapping("/start")
	public ResponseEntity<SimulationStartResponse> start() {
		return ResponseEntity
			.status(HttpStatus.ACCEPTED)
			.body(simulationService.start());
	}

	@GetMapping("/current")
	public SimulationStatusResponse current() {
		return simulationService.currentStatus();
	}
}
