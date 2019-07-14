package fm.bootifulpodcast.podbean;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;

import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class SimplePodbeanClientTest {

	private final PodbeanClient client;

	private final RestTemplate restTemplate;

	SimplePodbeanClientTest() {
		this.restTemplate = new RestTemplate();
		this.client = new SimplePodbeanClient(this.restTemplate);
	}

	@Test
	void getAllPodcasts() {
		var server = MockRestServiceServer.bindTo(this.restTemplate).build();
		server.expect(once(), requestTo("https://api.podbean.com/v1/podcasts"))//
				.andExpect(method(HttpMethod.GET)) //
				.andRespond(withSuccess(
						"{\"podcasts\":[{\"id\":\"o6DLxaF0purw\",\"title\":"
								+ "\"The starbuxman's Podcast\",\"desc\":\"New podcast weblog\","
								+ "\"logo\":\"https:\\/\\/pbcdn1.podbean.com\\/imglogo\\/image-logo\\/5518947\\/photo.jpg\","
								+ "\"website\":\"https:\\/\\/starbuxman.podbean.com\",\"category_name\":\"\",\"allow_episode_type\":"
								+ "[\"public\"],\"object\":\"Podcast\"}]}",
						MediaType.APPLICATION_JSON));
		Collection<Podcast> podcasts = this.client.getAllPodcasts();
		Assert.assertFalse(podcasts.isEmpty());
		Assert.assertEquals(podcasts.size(), 1);
		server.verify();
	}

	@Test
	void uploadFile() {
	}

}