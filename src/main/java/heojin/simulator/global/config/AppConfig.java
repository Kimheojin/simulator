package heojin.simulator.global.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.RestClient;

import heojin.simulator.simulation.client.ControlApiProperties;
import heojin.simulator.simulation.service.SimulatorProperties;

@Configuration
@EnableConfigurationProperties({SimulatorProperties.class, ControlApiProperties.class})
public class AppConfig {

	@Bean
	public Clock clock() {
		return Clock.systemUTC();
	}

	@Bean
	public RestClient.Builder restClientBuilder() {
		return RestClient.builder();
	}

	@Bean
	public ThreadPoolTaskScheduler simulationTaskScheduler() {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(2);
		scheduler.setThreadNamePrefix("simulation-");
		scheduler.setRemoveOnCancelPolicy(true);
		return scheduler;
	}
}
