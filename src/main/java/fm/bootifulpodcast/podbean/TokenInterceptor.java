package fm.bootifulpodcast.podbean;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Log4j2
public class TokenInterceptor implements ClientHttpRequestInterceptor {

	private final AtomicReference<Token> token = new AtomicReference<>();

	private final RestTemplate template;

	public TokenInterceptor(String clientId, String clientSecret) {
		this(new RestTemplateBuilder().basicAuthentication(clientId, clientSecret).build());
	}

	// testing
	TokenInterceptor(RestTemplate restTemplate) {
		this.template = restTemplate;
	}

	// testing
	@Data
	@RequiredArgsConstructor
	static class Token {
		private final String token;
		private final long expiration;
	}

	// testing
	@SneakyThrows
	Token ensureToken() {
		var minute = 1000 * 60;
		var currentToken = this.token.get();
		var shouldEvaluate = currentToken == null
			|| (currentToken.getExpiration() - minute) < System.currentTimeMillis();
		if (shouldEvaluate) {
			log.info("We need to obtain a fresh token, the old one expired.");
			var request = Map.of("grant_type", "client_credentials");
			var url = URI.create("https://api.podbean.com/v1/oauth/token");
			var type = new ParameterizedTypeReference<Map<String, String>>() {
			};
			var responseEntity = this.template.exchange(url, HttpMethod.POST,
				new HttpEntity<>(request), type);
			if (responseEntity.getStatusCode().is2xxSuccessful()) {
				var map = Objects.requireNonNull(responseEntity.getBody());
				var accessToken = map.get("access_token");
				var expiry = Long.parseLong(map.get("expires_in"));
				var newToken = new Token(accessToken,
					System.currentTimeMillis() + expiry);
				this.token.set(newToken);
			}
		}
		var token = this.token.get();
		Assert.notNull(token, "the token must be non-null");
		return token;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body,
																																					ClientHttpRequestExecution execution) throws IOException {
		var token = this.ensureToken();
		request.getHeaders().setBearerAuth(token.getToken());
		return execution.execute(request, body);
	}

}
