package fm.bootifulpodcast.podbean.token;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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

/**
 * A thing that can acquire and maintain a token for authentication given client
 * credentials
 *
 * @author Josh Long
 */
@Slf4j
public class TokenProvider {

	private final URI uri;

	private final AtomicReference<Token> token = new AtomicReference<>();

	private final RestTemplate template;

	/**
	 * returns the URI of the token
	 * @return the {@link URI }
	 */
	public URI getTokenUri() {
		return this.uri;
	}

	/**
	 * Configure a new instance with the client credentials
	 * @param clientId the client id
	 * @param clientSecret the client secret
	 */
	public TokenProvider(String clientId, String clientSecret) {
		this(new RestTemplateBuilder().basicAuthentication(clientId, clientSecret).build());
	}

	/**
	 * Provide only the {@link RestTemplate}
	 * @param restTemplate a {@link RestTemplate}
	 */
	public TokenProvider(RestTemplate restTemplate) {
		this.template = restTemplate;
		this.uri = URI.create("https://api.podbean.com/v1/oauth/token");
	}

	/**
	 * Returns a token
	 * @return a {@link Token}
	 */
	@SneakyThrows
	public Token getToken() {
		var minute = 1000 * 60;
		var currentToken = this.token.get();
		var shouldEvaluate = currentToken == null || (currentToken.expiration() - minute) < System.currentTimeMillis();
		if (shouldEvaluate) {
			if (log.isDebugEnabled()) {
				log.debug("We need to obtain a fresh token, the old one expired.");
			}
			var request = Map.of("grant_type", "client_credentials");
			var type = new ParameterizedTypeReference<Map<String, String>>() {
			};
			var responseEntity = this.template.exchange(this.uri, HttpMethod.POST, new HttpEntity<>(request), type);
			Assert.notNull(responseEntity, "the response should not be null");
			if (responseEntity.getStatusCode().is2xxSuccessful()) {
				var map = Objects.requireNonNull(responseEntity.getBody());
				var accessToken = map.get("access_token");
				var expiry = Long.parseLong(map.get("expires_in"));
				var newToken = new Token(accessToken, System.currentTimeMillis() + expiry);
				this.token.set(newToken);
				log.info("returning new token: " + this.token.get());
			}
		}
		else {
			log.info("returning cached token " + this.token.get());
		}
		var token = this.token.get();
		Assert.notNull(token, "the token must be non-null");
		return token;
	}

}
