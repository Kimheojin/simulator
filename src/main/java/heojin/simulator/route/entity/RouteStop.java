package heojin.simulator.route.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "route_stops")
public class RouteStop {

	@Id
	private Long id;

	@Column(name = "route_id", nullable = false)
	private Long routeId;

	@Column(name = "stop_id", nullable = false)
	private Long stopId;

	@Column(name = "stop_order", nullable = false)
	private int stopOrder;

	protected RouteStop() {
	}

	public Long getId() {
		return id;
	}

	public Long getRouteId() {
		return routeId;
	}

	public Long getStopId() {
		return stopId;
	}

	public int getStopOrder() {
		return stopOrder;
	}
}
