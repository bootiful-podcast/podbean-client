package fm.bootifulpodcast.podbean;

import org.springframework.web.client.RestTemplate;

class PodbeanClientTest {

	private final RestTemplate template;

	private final PodbeanClient client;

	PodbeanClientTest() {
		this.template = new RestTemplate();
		this.client = new SimplePodbeanClient(this.template);
	}

	void getAllPodcasts() {

	}

}