package fm.bootifulpodcast.podbeanintegration;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@SpringBootApplication
public class PodbeanIntegrationApplication {

	public static void main(String[] args) {
		SpringApplication.run(PodbeanIntegrationApplication.class, args);
	}

	@Bean
	RestTemplate restTemplate(RestTemplateBuilder builder, TokenInterceptor interceptor) {
		return builder.interceptors(interceptor).build();
	}
}

@Component
@RequiredArgsConstructor
@Log4j2
class Listener {

	private final RestTemplate template;

	@EventListener(ApplicationReadyEvent.class)
	public void go() {
		var forEntity = this.template.getForEntity("https://api.podbean.com/v1/podcasts", String.class);
		var body = forEntity.getBody();
		log.info("body: " + body);
	}
}

@Log4j2
@Component
class TokenInterceptor implements ClientHttpRequestInterceptor {

	private final AtomicReference<Token> token = new AtomicReference<>();
	private final RestTemplate template;

	TokenInterceptor(@Value("${podbean.client-id}") String clientId,
																		@Value("${podbean.client-secret}") String clientSecret) {
		this.template = new RestTemplateBuilder()
			.basicAuthentication(clientId, clientSecret)
			.build();
	}

	@Data
	@RequiredArgsConstructor
	private static class Token {
		private final String token;
		private final long expiration;
	}

/*

	private HttpHeaders createAuthorizationHeaders(String username, String password) {
		return new HttpHeaders() {{
			var auth = username + ":" + password;
			var encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
			var authHeader = "Basic " + new String(encodedAuth);
			set("Authorization", authHeader);
		}};
	}

	*/

	@SneakyThrows
	private Token ensureToken() {
		var minute = 1000 * 60;
		var currentToken = this.token.get();
		var shouldEvaluate = currentToken == null || (currentToken.getExpiration() - minute) < System.currentTimeMillis();
		if (shouldEvaluate) {
			var request = Map.of("grant_type", "client_credentials");
			var url = URI.create("https://api.podbean.com/v1/oauth/token");
			var type = new ParameterizedTypeReference<Map<String, String>>() {
			};
			var responseEntity = this.template.exchange(url, HttpMethod.POST, new HttpEntity<>(request), type);
			if (responseEntity.getStatusCode().is2xxSuccessful()) {
				var map = Objects.requireNonNull(responseEntity.getBody());
				var accessToken = map.get("access_token");
				var expiry = Long.parseLong(map.get("expires_in"));
				var newToken = new Token(accessToken, System.currentTimeMillis() + expiry);
				this.token.set(newToken);
			}
		}
		var token = this.token.get();
		Assert.notNull(token, "the token must be non-null");
		return token;
	}


	@Override
	public ClientHttpResponse intercept(
		HttpRequest request, byte[] body,
		ClientHttpRequestExecution execution) throws IOException {

		var token = this.ensureToken();
		var tokenString = token.getToken();
//		log.info("token: " + token.getToken());
//		log.info("expiration: " + token.getExpiration());

		request.getHeaders().setBearerAuth(tokenString);

		return execution.execute(request, body);
	}
}