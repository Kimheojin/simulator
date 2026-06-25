package heojin.simulator.route.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import heojin.simulator.route.entity.RouteStop;

public interface RouteStopRepository extends JpaRepository<RouteStop, Long> {

	List<RouteStop> findByRouteIdInOrderByRouteIdAscStopOrderAsc(Collection<Long> routeIds);
}
