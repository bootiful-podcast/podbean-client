package com.joshlong.podbean;

import com.joshlong.podbean.token.ClientCredentialsTokenProvider;
import com.joshlong.podbean.token.TokenInterceptor;
import com.joshlong.podbean.token.TokenProvider;
import org.junit.jupiter.api.Assertions;
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

public class TokenInterceptorTest {

	private String expiry = Long.toString(10 * 1000);

	private String token = "1234";

	private MockRestServiceServer server;

	private RestTemplate restTemplate;

	private TokenInterceptor interceptor;

	private TokenProvider tokenProvider;

	@BeforeEach
	public void start() {
		this.restTemplate = new RestTemplate();
		this.tokenProvider = new ClientCredentialsTokenProvider(this.restTemplate);
		this.interceptor = new TokenInterceptor(this.tokenProvider);
		this.server = this.init();
	}

	@Test
	public void vendNewToken() {
		var tokenObj = this.tokenProvider.getToken();
		Assertions.assertTrue(tokenObj.expiration() > System.currentTimeMillis() + (8 * 1000));
		Assertions.assertEquals(tokenObj.token(), this.token);
		server.verify();
	}

	@Test
	public void intercept() throws Exception {
		var execution = Mockito.mock(ClientHttpRequestExecution.class);
		var httpRequest = Mockito.mock(HttpRequest.class);
		HttpHeaders httpHeaders = Mockito.mock(HttpHeaders.class);
		Mockito.when(httpRequest.getHeaders()).thenReturn(httpHeaders);
		this.interceptor.intercept(httpRequest, new byte[0], execution);
		Mockito.verify(httpHeaders, times(1)).setBearerAuth(Mockito.anyString());
	}

	private MockRestServiceServer init() {
		var server = MockRestServiceServer.bindTo(this.restTemplate).build();

		server.expect(ExpectedCount.once(),
				requestTo(((ClientCredentialsTokenProvider) this.tokenProvider).getTokenUri()))
				.andExpect(method(HttpMethod.POST))
				.andRespond(MockRestResponseCreators.withSuccess(
						"{\"access_token\": \"" + token + "\" , \"expires_in\": \"" + expiry + "\"}",
						MediaType.APPLICATION_JSON));
		return server;
	}

}