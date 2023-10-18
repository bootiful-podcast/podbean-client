package fm.bootifulpodcast.podbean;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;

import java.io.File;
import java.util.Collection;

@Log4j2
@SpringBootTest
public class SimplePodbeanClientTest {

	@Autowired
	private SimplePodbeanClient client;

	private File mp3;

	private File jpg;

	@BeforeEach
	public void before() throws Exception {
		this.jpg = new ClassPathResource("/test-profile-image.jpg").getFile();
		this.mp3 = new ClassPathResource("/super-compressed-file-for-tests.mp3").getFile();
	}

	@Test
	void getEpisodeRange() throws Exception {
		var max = 100;
		var er = this.client.getEpisodeRange(0, max);
		Assertions.assertTrue(er.hasMore());
		Assertions.assertEquals(er.offset(), 0);
		Assertions.assertEquals(er.limit(), max);
		Assertions.assertEquals(er.episodes().size(), max);
		Assertions.assertTrue(er.count() > 0);
	}

	@Test
	void getAllEpisodes() throws Exception {
		var episodes = this.client.getAllEpisodes();
		Assertions.assertTrue(episodes.size() > 200);
	}

	@Test
	public void getEpisodes() {
		Assertions.assertNotNull(this.client);
		Collection<Episode> episodes = this.client.getEpisodes();
		Assertions.assertTrue(!episodes.isEmpty(), "there should be more than one episode");
		Episode next = episodes.iterator().next();
		Assertions.assertNotNull(next.getId());
		Assertions.assertNotNull(next.getPodcastId());
		Assertions.assertNotNull(next.getContent());
		Assertions.assertNotNull(next.getMediaUrl());
		episodes.forEach(log::info);
	}

	@Test
	public void createEpisode() {
		// {"episode":{"id":"IUUETB885A2","podcast_id":"o6DLxaF0purw","title":"t1563611568056","content":"c1563611568056","logo":null,"media_url":"https:\/\/starbuxman.podbean.com\/mf\/play\/jj6eg5\/intro.mp3","player_url":"https:\/\/www.podbean.com\/media\/player\/iuuet-b885a2","permalink_url":null,"publish_time":1563611568,"status":"draft","type":"public","duration":0,"object":"Episode"}}
		var currentTimeMillis = System.currentTimeMillis();
		var uploadMp3 = this.client.upload(MediaType.parseMediaType("audio/mpeg"), this.mp3, this.mp3.length());
		var uploadJpg = this.client.upload(MediaType.IMAGE_JPEG, this.jpg, this.jpg.length());
		var episode = this.client.publishEpisode("t" + currentTimeMillis, "c" + currentTimeMillis, EpisodeStatus.DRAFT,
				EpisodeType.PUBLIC, uploadMp3.getFileKey(), uploadJpg.getFileKey());
		Assertions.assertNotNull(episode.getId());
	}

	@Test
	public void upload() {
		Assertions.assertTrue(this.mp3.exists());
		var mediaType = MediaType.parseMediaType("audio/mpeg");
		var upload = client.upload(mediaType, this.mp3, this.mp3.length());
		log.info(upload);
	}

	@Test
	public void getPodcasts() throws Exception {
		this.client.getAllPodcasts().forEach(log::info);
	}

}

@SpringBootApplication
class SimplePodbeanClientApplication {

}