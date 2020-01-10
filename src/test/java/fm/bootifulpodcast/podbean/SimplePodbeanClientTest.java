package fm.bootifulpodcast.podbean;

import lombok.extern.log4j.Log4j2;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.util.Collection;

@Log4j2
@SpringBootTest
@RunWith(SpringRunner.class)
public class SimplePodbeanClientTest {

	@Autowired
	private SimplePodbeanClient client;

	private File file;

	@Before
	public void before() throws Exception {
		this.file = new ClassPathResource("/super-compressed-file-for-tests.mp3")
				.getFile();
	}

	@Test
	public void getEpisodes() {
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
	public void createEpisode() {
		// {"episode":{"id":"IUUETB885A2","podcast_id":"o6DLxaF0purw","title":"t1563611568056","content":"c1563611568056","logo":null,"media_url":"https:\/\/starbuxman.podbean.com\/mf\/play\/jj6eg5\/intro.mp3","player_url":"https:\/\/www.podbean.com\/media\/player\/iuuet-b885a2","permalink_url":null,"publish_time":1563611568,"status":"draft","type":"public","duration":0,"object":"Episode"}}
		var mediaType = MediaType.parseMediaType("audio/mpeg");
		var upload = client.upload(mediaType, file, file.length());
		log.info("file key: " + upload.toString());
		long currentTimeMillis = System.currentTimeMillis();
		var episode = client.publishEpisode("t" + currentTimeMillis,
				"c" + currentTimeMillis, EpisodeStatus.DRAFT, EpisodeType.PUBLIC,
				upload.getFileKey(), null);
		Assert.assertNotNull(episode.getId());
	}

	@Test
	public void upload() {
		Assert.assertTrue(file.exists());
		var mediaType = MediaType.parseMediaType("audio/mpeg");
		UploadAuthorization upload = client.upload(mediaType, file, file.length());
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