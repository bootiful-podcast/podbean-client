package fm.bootifulpodcast.podbean.token;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Log4j2
public class TokenProvider {

	private final URI uri;

	private final AtomicReference<Token> token = new AtomicReference<>();

	private final RestTemplate template;

	public URI getTokenUri() {
		return this.uri;
	}

	public TokenProvider(String clientId, String clientSecret) {
		this(new RestTemplateBuilder().basicAuthentication(clientId, clientSecret)
				.build());
	}

	public TokenProvider(RestTemplate restTemplate) {
		this.template = restTemplate;
		this.uri = URI.create("https://api.podbean.com/v1/oauth/token");
	}

	// testing
	@SneakyThrows
	public Token getToken() {
		var minute = 1000 * 60;
		var currentToken = this.token.get();
		var shouldEvaluate = currentToken == null
				|| (currentToken.getExpiration() - minute) < System.currentTimeMillis();
		if (shouldEvaluate) {
			if (log.isDebugEnabled()) {
				log.debug("We need to obtain a fresh token, the old one expired.");
			}
			var request = Map.of("grant_type", "client_credentials");
			var type = new ParameterizedTypeReference<Map<String, String>>() {
			};
			var responseEntity = this.template.exchange(this.uri, HttpMethod.POST,
					new HttpEntity<>(request), type);
			Assert.notNull(responseEntity, "the response should not be null");
			if (responseEntity.getStatusCode().is2xxSuccessful()) {
				var map = Objects.requireNonNull(responseEntity.getBody());
				var accessToken = map.get("access_token");
				var expiry = Long.parseLong(map.get("expires_in"));
				var newToken = new Token(accessToken,
						System.currentTimeMillis() + expiry);
				this.token.set(newToken);
				log.info("the new token: " + this.token.get());
			}
		}
		var token = this.token.get();
		Assert.notNull(token, "the token must be non-null");
		return token;
	}

}
