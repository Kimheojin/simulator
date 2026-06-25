package heojin.simulator.route.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "routes")
public class Route {

	@Id
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column
	private String description;

	protected Route() {
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}
}
