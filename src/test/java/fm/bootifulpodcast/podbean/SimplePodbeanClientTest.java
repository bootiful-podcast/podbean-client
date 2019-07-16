package fm.bootifulpodcast.podbean;

import fm.bootifulpodcast.podbean.token.TokenInterceptor;
import fm.bootifulpodcast.podbean.token.TokenProvider;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.Collection;

import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class SimplePodbeanClientTest {

	@Test
	void getAllPodcasts() {
		var restTemplate = new RestTemplate();
		var tp = Mockito.mock(TokenProvider.class);
		var client = new SimplePodbeanClient(tp, restTemplate);
		var logo = "https://pbcdn1.podbean.com/imglogo/image-logo/5518947/photo.jpg";
		var title = "The starbuxman's Podcast";
		var website = "https://starbuxman.podbean.com";
		var server = MockRestServiceServer.bindTo(restTemplate).build();
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
		Collection<Podcast> podcasts = client.getAllPodcasts();
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
	void getUploadAuthorization() {
		var tokenProvider = new TokenProvider(//
				System.getenv("PODBEAN_CLIENT_ID"), //
				System.getenv("PODBEAN_CLIENT_SECRET")//
		);
		var tokenInterceptor = new TokenInterceptor(tokenProvider);
		var restTemplate = new RestTemplateBuilder().interceptors(tokenInterceptor)
				.build();
		var client = new SimplePodbeanClient(tokenProvider, restTemplate);
		var mediaType = MediaType.parseMediaType("audio/mpeg");
		var filePath = new File("/Users/joshlong/code/bootiful-podcast/assets/intro.mp3");
		var resource = new FileSystemResource(filePath);
		UploadAuthorization authorization = client.getUploadAuthorization(mediaType,
				resource, filePath.length());
		Assert.assertEquals(authorization.getExpireAt(), 600);
		Assert.assertTrue(authorization.getPresignedUrl().contains("s3"));
		Assert.assertTrue(authorization.getFileKey().contains(filePath.getName()));
	}

}