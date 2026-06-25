package heojin.simulator.bus.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "buses")
public class Bus {

	@Id
	private Long id;

	@Column(name = "route_id", nullable = false)
	private Long routeId;

	@Column(name = "bus_number", nullable = false)
	private String busNumber;

	@Column(name = "current_speed_kph", nullable = false)
	private int currentSpeedKph;

	@Column(name = "last_communicated_at")
	private Instant lastCommunicatedAt;

	protected Bus() {
	}

	public Long getId() {
		return id;
	}

	public Long getRouteId() {
		return routeId;
	}

	public String getBusNumber() {
		return busNumber;
	}

	public int getCurrentSpeedKph() {
		return currentSpeedKph;
	}

	public Instant getLastCommunicatedAt() {
		return lastCommunicatedAt;
	}
}
