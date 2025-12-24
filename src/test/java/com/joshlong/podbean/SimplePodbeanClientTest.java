package com.joshlong.podbean;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;

import java.io.File;
import java.util.Collection;

@SpringBootTest
public class SimplePodbeanClientTest {

	private final Logger log = LoggerFactory.getLogger(getClass());

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
		var episodes = this.client.getEpisodes();
		Assertions.assertTrue(!episodes.isEmpty(), "there should be more than one episode");
		Episode next = episodes.iterator().next();
		Assertions.assertNotNull(next.getId());
		Assertions.assertNotNull(next.getPodcastId());
		Assertions.assertNotNull(next.getContent());
		Assertions.assertNotNull(next.getMediaUrl());
		episodes.forEach(it -> log.info("{}", it));
	}

	@Test
	public void createEpisode() {
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
		log.info("{}", upload);
	}

	@Test
	public void getPodcasts() throws Exception {
		this.client.getAllPodcasts().forEach(it -> log.info("{}", it));
	}

}

@SpringBootApplication
class SimplePodbeanClientApplication {

}