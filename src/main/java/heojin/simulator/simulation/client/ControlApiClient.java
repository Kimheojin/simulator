package heojin.simulator.simulation.client;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import heojin.simulator.simulation.dto.TelemetryRequest;

@Component
public class ControlApiClient {

	private static final String API_KEY_HEADER = "X-API-KEY";
	private static final String TELEMETRY_PATH = "/v1/internal/telemetry";

	private final RestClient restClient;
	private final ControlApiProperties properties;

	public ControlApiClient(RestClient.Builder restClientBuilder, ControlApiProperties properties) {
		this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
		this.properties = properties;
	}

	public void sendTelemetry(TelemetryRequest request) {
		RestClient.RequestBodySpec requestSpec = restClient
			.post()
			.uri(TELEMETRY_PATH)
			.contentType(MediaType.APPLICATION_JSON);

		if (StringUtils.hasText(properties.apiKey())) {
			requestSpec.header(API_KEY_HEADER, properties.apiKey());
		}

		requestSpec
			.body(request)
			.retrieve()
			.toBodilessEntity();
	}
}
