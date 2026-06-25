package heojin.simulator.simulation.service;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import heojin.simulator.bus.entity.Bus;
import heojin.simulator.bus.repository.BusRepository;
import heojin.simulator.global.exception.SimulatorException;
import heojin.simulator.route.entity.RouteStop;
import heojin.simulator.route.repository.RouteStopRepository;
import heojin.simulator.stop.entity.Stop;
import heojin.simulator.stop.repository.StopRepository;

@Component
public class SimulationSeedDataLoader {

	private final BusRepository busRepository;
	private final RouteStopRepository routeStopRepository;
	private final StopRepository stopRepository;

	public SimulationSeedDataLoader(
		BusRepository busRepository,
		RouteStopRepository routeStopRepository,
		StopRepository stopRepository
	) {
		this.busRepository = busRepository;
		this.routeStopRepository = routeStopRepository;
		this.stopRepository = stopRepository;
	}

	@Transactional(readOnly = true)
	public SimulationSeedData load() {
		List<Bus> buses = busRepository.findAll();
		if (buses.isEmpty()) {
			throw dataNotReady("시뮬레이션에 사용할 버스 기준 데이터가 없습니다.");
		}

		List<Long> routeIds = buses.stream()
			.map(Bus::getRouteId)
			.distinct()
			.toList();
		List<RouteStop> routeStops = routeStopRepository.findByRouteIdInOrderByRouteIdAscStopOrderAsc(routeIds);
		if (routeStops.isEmpty()) {
			throw dataNotReady("시뮬레이션에 사용할 노선 정류장 기준 데이터가 없습니다.");
		}

		Map<Long, Stop> stopsById = stopRepository.findAllById(stopIds(routeStops))
			.stream()
			.collect(Collectors.toMap(Stop::getId, Function.identity()));
		Map<Long, RoutePlan> routePlans = buildRoutePlans(routeStops, stopsById);

		for (Long routeId : routeIds) {
			RoutePlan routePlan = routePlans.get(routeId);
			if (routePlan == null || routePlan.stops().size() < 2) {
				throw dataNotReady("노선별 정류장 기준 데이터가 부족합니다. routeId=" + routeId);
			}
		}

		List<BusPlan> busPlans = buses.stream()
			.sorted(Comparator.comparing(Bus::getId))
			.map(bus -> new BusPlan(bus.getId(), bus.getRouteId()))
			.toList();

		return new SimulationSeedData(busPlans, routePlans);
	}

	private Collection<Long> stopIds(List<RouteStop> routeStops) {
		return routeStops.stream()
			.map(RouteStop::getStopId)
			.distinct()
			.toList();
	}

	private Map<Long, RoutePlan> buildRoutePlans(List<RouteStop> routeStops, Map<Long, Stop> stopsById) {
		Map<Long, List<RouteStop>> routeStopsByRouteId = routeStops.stream()
			.collect(Collectors.groupingBy(RouteStop::getRouteId, LinkedHashMap::new, Collectors.toList()));

		Map<Long, RoutePlan> routePlans = new LinkedHashMap<>();
		for (Map.Entry<Long, List<RouteStop>> entry : routeStopsByRouteId.entrySet()) {
			List<Coordinate> coordinates = entry.getValue()
				.stream()
				.sorted(Comparator.comparingInt(RouteStop::getStopOrder))
				.map(routeStop -> toCoordinate(routeStop, stopsById))
				.toList();
			routePlans.put(entry.getKey(), new RoutePlan(entry.getKey(), coordinates));
		}
		return routePlans;
	}

	private Coordinate toCoordinate(RouteStop routeStop, Map<Long, Stop> stopsById) {
		Stop stop = stopsById.get(routeStop.getStopId());
		if (stop == null) {
			throw dataNotReady("노선 정류장에 연결된 정류장 기준 데이터가 없습니다. stopId=" + routeStop.getStopId());
		}
		return new Coordinate(stop.getLatitude().doubleValue(), stop.getLongitude().doubleValue());
	}

	private SimulatorException dataNotReady(String message) {
		return new SimulatorException(HttpStatus.CONFLICT, "SIMULATION_DATA_NOT_READY", message);
	}
}
