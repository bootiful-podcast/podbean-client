package fm.bootifulpodcast.podbean;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

class TokenInterceptorTest {

	private final TokenInterceptor tokenInterceptor;

	TokenInterceptorTest() {
		this.tokenInterceptor = new TokenInterceptor("clientId", "clientSecret");
	}

	@Test
	public void vendANewToken() throws Exception {

		var restTemplate = Mockito.mock(RestTemplate.class);

		var uri = Mockito.any(URI.class);
		var httpMethod = Mockito.any(HttpMethod.class);
		var httpRequest = Mockito.any(HttpEntity.class);
		var ptr = new ParameterizedTypeReference<Map<String, Collection<Podcast>>>() {
		};

		var map = Map.of("podcasts", List.of());
		var responseEntity = ResponseEntity.ok(map).;
		Mockito
			.when(restTemplate.exchange(uri, httpMethod, httpRequest, Mockito.any(ptr.getClass())))
			.thenReturn(responseEntity)
/*		var responseEntity = this.template.exchange(url, HttpMethod.POST,
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
	}*/
		this.tokenInterceptor.ensureToken();

	}

}