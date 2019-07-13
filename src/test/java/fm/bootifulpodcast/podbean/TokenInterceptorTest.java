package fm.bootifulpodcast.podbean;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;

import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;

class TokenInterceptorTest {

	String expiry = Long.toString(10 * 1000);

	String token = "1234";

	MockRestServiceServer server;

	RestTemplate restTemplate;

	TokenInterceptor interceptor;

	@BeforeEach
	void start() {
		this.restTemplate = new RestTemplate();
		this.interceptor = new TokenInterceptor(null, this.restTemplate);
		this.server = this.init();
	}

	@Test
	void vendNewToken() {
		MockRestServiceServer server = init();
		var tokenObj = interceptor.ensureToken();
		Assert.assertTrue(
				tokenObj.getExpiration() > System.currentTimeMillis() + (8 * 1000));
		Assert.assertEquals(tokenObj.getToken(), this.token);
		server.verify();
	}

	@Test
	void intercept() throws Exception {
		var execution = Mockito.mock(ClientHttpRequestExecution.class);
		var httpRequest = Mockito.mock(HttpRequest.class);
		HttpHeaders httpHeaders = Mockito.mock(HttpHeaders.class);
		Mockito.when(httpRequest.getHeaders()).thenReturn(httpHeaders);
		interceptor.intercept(httpRequest, new byte[0], execution);
		Mockito.verify(httpHeaders, times(1)).setBearerAuth(Mockito.anyString());
	}

	private MockRestServiceServer init() {
		var server = MockRestServiceServer.bindTo(restTemplate).build();
		server.expect(ExpectedCount.once(), requestTo(interceptor.getTokenUri()))
				.andExpect(method(HttpMethod.POST)).andRespond(
						MockRestResponseCreators.withSuccess(
								"{\"access_token\": \"" + token
										+ "\" , \"expires_in\": \"" + expiry + "\"}",
								MediaType.APPLICATION_JSON));
		return server;
	}

}