package be.ordina.analytics;

import be.ordina.analytics.service.AnalyticsCollector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Slf4j
@SpringBootApplication
public class AnalyticsCounterApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnalyticsCounterApplication.class, args);
	}

	@Bean
	public RouterFunction<ServerResponse> routingFunction(AnalyticsCollector analyticsCollector) {
		return route(path("/collect"),
			req -> analyticsCollector.processRequest(req)
				.doOnError(a -> log.warn("[{}]", a.getMessage()))
				.flatMap(clientResponse -> ServerResponse.ok()
					.body(BodyInserters.fromPublisher(clientResponse.bodyToMono(String.class), String.class))));
	}
}
