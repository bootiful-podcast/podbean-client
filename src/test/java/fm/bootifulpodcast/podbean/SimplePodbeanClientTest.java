package fm.bootifulpodcast.podbean;

import fm.bootifulpodcast.podbean.token.TokenProvider;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
		var uploadAuthorizeUri = "https://api.podbean.com/v1/files/uploadAuthorize";
		var uploadAuthorizationMockResponse = "{\"presigned_url\":\"https://s3.amazonaw"
				+ "s.com/a1.podbean.com/tmp2/5518947/intro.mp3?X-Amz-Content-Sha256=UNSIGNED-PAY"
				+ "LOAD&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=AKIAYBAB55VCNHSFLRBE%2F2"
				+ "0190716%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20190716T021338Z&X-Amz-Signe"
				+ "dHeaders=host&X-Amz-Expires=600&X-Amz-Signature=f012ea05477254ecb90d36e8c53c9b7e5"
				+ "9694b16d976ceef3e28381c8aaa524e\",\"expire_at\":600,\"file_key\":\"tmp2/5518947/in"
				+ "tro.mp3\"}";
		var mBaseMatcher = new BaseMatcher<String>() {

			@Override
			public boolean matches(Object item) {
				return item instanceof String
						&& ((String) item).contains(uploadAuthorizeUri);
			}

			@Override
			public void describeTo(Description description) {
				description
						.appendText("the URL should be to the uploadAuthorize endpoint");
			}
		};
		var restTemplate = new RestTemplate();
		var tokenProvider = Mockito.mock(TokenProvider.class);
		var server = MockRestServiceServer.bindTo(restTemplate).build();
		server.expect(once(), requestTo(mBaseMatcher))//
				.andExpect(method(HttpMethod.GET))//
				.andRespond(withSuccess(uploadAuthorizationMockResponse,
						MediaType.APPLICATION_JSON));
		var client = new SimplePodbeanClient(tokenProvider, restTemplate);
		var mediaType = MediaType.parseMediaType("audio/mpeg");
		var filePath = new File("/Users/joshlong/code/bootiful-podcast/assets/intro.mp3");
		var resource = new FileSystemResource(filePath);
		UploadAuthorization authorization = client.getUploadAuthorization(mediaType,
				resource, filePath.length());
		server.verify();
		Assert.assertEquals(authorization.getExpireAt(), 600);
		Assert.assertTrue(authorization.getPresignedUrl().contains("s3"));
		Assert.assertTrue(authorization.getFileKey().contains(filePath.getName()));
	}

}