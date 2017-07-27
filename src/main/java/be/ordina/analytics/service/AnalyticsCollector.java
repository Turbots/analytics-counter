package be.ordina.analytics.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.UUID;

@Slf4j
@Service
public class AnalyticsCollector {

	@Value("${analytics.tracking-id}")
	private String trackingId;

	@Value("${analytics.datasource}")
	private String datasource;

	@Value("${analytics.campaign-name}")
	private String campaignName;

	private final WebClient webClient = WebClient.create("https://www.google-analytics.com");

	public Mono<ClientResponse> processRequest(ServerRequest serverRequest) {
		HttpHeaders headers = serverRequest.headers().asHttpHeaders();
		String referer = headers.getFirst("Referer");

		if (referer != null) {
			try {
				URI uri = URI.create(referer);
				log.info("Collecting analytics from [{}]", uri.getPath());

				String body = "v=1"
					+ "&tid=" + trackingId
					+ "&cid=" + UUID.randomUUID().toString()
					+ "&t=pageview"
					+ "&ds=" + datasource
					+ "&dh=" + uri.getHost()
					+ "&dp=" + uri.getPath()
					+ "&cn=" + campaignName
					+ serverRequest.queryParam("title").map(title -> "&dt=" + title).orElse("");

				log.info("Sending Analytics: [{}]", body);

				return webClient.post()
					.uri("/debug/collect")
					.body(Mono.just(body), String.class)
					.exchange();
			} catch (IllegalArgumentException e) {
				return Mono.error(e);
			}
		} else {
			return Mono.empty();
		}
	}
}
