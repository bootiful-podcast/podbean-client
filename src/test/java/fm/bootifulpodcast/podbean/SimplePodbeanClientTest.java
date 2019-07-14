package fm.bootifulpodcast.podbean;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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
		var logo = "https://pbcdn1.podbean.com/imglogo/image-logo/5518947/photo.jpg";
		var title = "The starbuxman's Podcast";
		var website = "https://starbuxman.podbean.com";
		var server = MockRestServiceServer.bindTo(this.restTemplate).build();
		var allowEpisodeType = "public";
		var id = "o6DLxaF0purw";
		var desc = "New podcast weblog";
		server.expect(once(), requestTo("https://api.podbean.com/v1/podcasts"))//
				.andExpect(method(HttpMethod.GET)) //
				.andRespond(withSuccess("{\"podcasts\":[{\"id\":\"" + id + "\",\"title\":"
						+ "\"" + title + "\",\"desc\":\"" + desc + "\"," + "\"logo\":\""
						+ logo + "\"," + "\"website\":\"" + website
						+ "\",\"category_name\":\"\",\"allow_episode_type\":" + "[\""
						+ allowEpisodeType + "\"],\"object\":\"Podcast\"}]}",
						MediaType.APPLICATION_JSON));
		Collection<Podcast> podcasts = this.client.getAllPodcasts();
		Assert.assertFalse(podcasts.isEmpty());
		Assert.assertEquals(podcasts.size(), 1);
		Podcast next = podcasts.iterator().next();
		Assert.assertEquals(next.getTitle(), title);
		Assert.assertEquals(next.getId(), id);
		Assert.assertEquals(next.getLogo(), logo);
		Assert.assertEquals(next.getDesc(), desc);
		Assert.assertTrue(next.getAllowEpisodeType().contains(allowEpisodeType));

		server.verify();
	}

	@Test
	void uploadFile() {
	}

}