package heojin.simulator.bus.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import heojin.simulator.bus.entity.Bus;

public interface BusRepository extends JpaRepository<Bus, Long> {
}
