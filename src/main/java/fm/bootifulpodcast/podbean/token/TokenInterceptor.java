package fm.bootifulpodcast.podbean.token;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * Automatically stuffs the authentication token in the HTTP client request
 *
 * @author Josh Long
 */
public class TokenInterceptor implements ClientHttpRequestInterceptor {

	private final TokenProvider tokenProvider;

	/**
	 * Configure a new {@link TokenInterceptor }
	 * @param tokenProvider a token provider
	 */
	public TokenInterceptor(TokenProvider tokenProvider) {
		this.tokenProvider = tokenProvider;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		var token = this.tokenProvider.getToken();
		request.getHeaders().setBearerAuth(token.token());
		return execution.execute(request, body);
	}

}
