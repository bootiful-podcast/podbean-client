package fm.bootifulpodcast.podbean;

import lombok.extern.log4j.Log4j2;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

@Log4j2
public class SimplePodbeanClient implements PodbeanClient {

	private final RestTemplate restTemplate;

	private final ParameterizedTypeReference<Map<String, Collection<Podcast>>> podcastsParameterizedTypeReference =
		new ParameterizedTypeReference<>() { };

	public SimplePodbeanClient(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@Override
	public Collection<Podcast> getAllPodcasts() {
		var responseEntity = this.restTemplate
			.exchange("https://api.podbean.com/v1/podcasts", HttpMethod.GET,
				null, this.podcastsParameterizedTypeReference);
		Assert.isTrue(responseEntity.getStatusCode().is2xxSuccessful(), "the result must be an HTTP 200-series");
		var body = Objects.requireNonNull(responseEntity.getBody());
		return body.get("podcasts");
	}
}
