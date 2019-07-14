package fm.bootifulpodcast.podbean.token;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

@Log4j2
@RequiredArgsConstructor
public class TokenInterceptor implements ClientHttpRequestInterceptor {

	private final TokenProvider tokenProvider;

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body,
			ClientHttpRequestExecution execution) throws IOException {
		var token = this.tokenProvider.getToken();
		request.getHeaders().setBearerAuth(token.getToken());
		return execution.execute(request, body);
	}

}
