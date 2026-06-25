package heojin.simulator.stop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import heojin.simulator.stop.entity.Stop;

public interface StopRepository extends JpaRepository<Stop, Long> {
}
