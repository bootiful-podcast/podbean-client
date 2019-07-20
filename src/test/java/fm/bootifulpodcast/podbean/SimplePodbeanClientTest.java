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
import org.springframework.http.converter.FormHttpMessageConverter;

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
		var rt = new RestTemplateBuilder().interceptors(ti).defaultMessageConverters()
				.additionalMessageConverters(new FormHttpMessageConverter()).build();
		this.client = new SimplePodbeanClient(rt, tp, new ObjectMapper());
	}

	@Test
	void getEpisodes() throws Exception {
		Assert.assertNotNull(this.client);
		Collection<Episode> episodes = this.client.getEpisodes();
		Assert.assertTrue("there should be more than one episode", episodes.size() > 0);
		Episode next = episodes.iterator().next();
		Assert.assertNotNull(next.getId());
		Assert.assertNotNull(next.getPodcastId());
		Assert.assertNotNull(next.getContent());
		Assert.assertNotNull(next.getMediaUrl());
		episodes.forEach(log::info);
	}

	@Test
	void createEpisode() {
		// {"episode":{"id":"IUUETB885A2","podcast_id":"o6DLxaF0purw","title":"t1563611568056","content":"c1563611568056","logo":null,"media_url":"https:\/\/starbuxman.podbean.com\/mf\/play\/jj6eg5\/intro.mp3","player_url":"https:\/\/www.podbean.com\/media\/player\/iuuet-b885a2","permalink_url":null,"publish_time":1563611568,"status":"draft","type":"public","duration":0,"object":"Episode"}}
		var mediaType = MediaType.parseMediaType("audio/mpeg");
		var filePath = new File("/Users/joshlong/code/bootiful-podcast/assets/intro.mp3");
		var upload = client.upload(mediaType, filePath, filePath.length());
		log.info("file key: " + upload.toString());
		long currentTimeMillis = System.currentTimeMillis();
		var episode = client.publishEpisode("t" + currentTimeMillis,
				"c" + currentTimeMillis, EpisodeStatus.PUBLISH, EpisodeType.PUBLIC,
				upload.getFileKey(), null);
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

	@Test
	void getPodcasts() throws Exception {
		this.client.getAllPodcasts().forEach(log::info);
	}

}