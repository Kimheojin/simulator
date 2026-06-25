package heojin.simulator.simulation.service;

import java.util.List;
import java.util.Map;

record SimulationSeedData(
	List<BusPlan> buses,
	Map<Long, RoutePlan> routePlans
) {
}

record BusPlan(
	Long busId,
	Long routeId
) {
}

record RoutePlan(
	Long routeId,
	List<Coordinate> stops
) {
}

record Coordinate(
	double latitude,
	double longitude
) {

	Coordinate interpolate(Coordinate target, double ratio) {
		double nextLatitude = latitude + ((target.latitude - latitude) * ratio);
		double nextLongitude = longitude + ((target.longitude - longitude) * ratio);
		return new Coordinate(nextLatitude, nextLongitude);
	}
}
