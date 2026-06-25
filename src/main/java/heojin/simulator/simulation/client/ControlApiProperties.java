package heojin.simulator.simulation.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "control-api")
public record ControlApiProperties(
	String baseUrl,
	String apiKey
) {
}
