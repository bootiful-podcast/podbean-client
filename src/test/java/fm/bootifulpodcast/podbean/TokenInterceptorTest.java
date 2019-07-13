package fm.bootifulpodcast.podbean;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;

class TokenInterceptorTest {

	@Test
	void vendNewToken() {
		var expiry = Long.toString(10 * 1000);
		var token = "1234";
		var rt = new RestTemplate();
		var interceptor = new TokenInterceptor(null, rt);
		var server = MockRestServiceServer.bindTo(rt).build();
		server.expect(ExpectedCount.once(), requestTo(interceptor.getTokenUri()))
				.andExpect(method(HttpMethod.POST)).andRespond(
						MockRestResponseCreators.withSuccess(
								"{\"access_token\": \"" + token
										+ "\" , \"expires_in\": \"" + expiry + "\"}",
								MediaType.APPLICATION_JSON));
		var tokenObj = interceptor.ensureToken();
		Assert.assertTrue(
				tokenObj.getExpiration() > System.currentTimeMillis() + (8 * 1000));
		Assert.assertEquals(tokenObj.getToken(), token);
		server.verify();
	}

}