package fm.bootifulpodcast.podbean;

import com.fasterxml.jackson.databind.ObjectMapper;
import fm.bootifulpodcast.podbean.token.TokenInterceptor;
import fm.bootifulpodcast.podbean.token.TokenProvider;
import lombok.extern.log4j.Log4j2;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;

import java.io.File;
import java.util.Collection;

@Log4j2
class SimplePodbeanClientTest {

	private SimplePodbeanClient client;

	@BeforeEach
	void before() throws Exception {
		log.info("setUp()");
		var tp = new TokenProvider(//
				System.getenv("PODBEAN_CLIENT_ID"), //
				System.getenv("PODBEAN_CLIENT_SECRET")//
		);
		var ti = new TokenInterceptor(tp);
		var rt = new RestTemplateBuilder().interceptors(ti).build();
		this.client = new SimplePodbeanClient(rt, new ObjectMapper());
	}

	@Test
	void getEpisodes() throws Exception {
		Assert.assertNotNull(this.client);
		Collection<Episode> episodes = this.client.getEpisodes();
		episodes.forEach(log::info);
	}

	@Test
	void createEpisode() throws Exception {
		var mediaType = MediaType.parseMediaType("audio/mpeg");
		var filePath = new File("/Users/joshlong/code/bootiful-podcast/assets/intro.mp3");
		Episode episode = client.createEpisode("this is the title",
				"<p> in this episoe we talk to <b>Sarah</b> about <EM> stuff</EM>",
				"publish", "public", "intro.mp3", null);
		Assert.assertNotNull(episode.getId());
	}

	@Test
	void upload() {
		var mediaType = MediaType.parseMediaType("audio/mpeg");
		var filePath = new File("/Users/joshlong/code/bootiful-podcast/assets/intro.mp3");
		UploadAuthorization upload = client.upload(mediaType, filePath,
				filePath.length());
		log.info(upload);
	}

}