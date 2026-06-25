package heojin.simulator.route.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import heojin.simulator.route.entity.Route;

public interface RouteRepository extends JpaRepository<Route, Long> {
}
