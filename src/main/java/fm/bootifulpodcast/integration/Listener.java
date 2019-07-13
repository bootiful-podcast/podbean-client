package fm.bootifulpodcast.integration;

import fm.bootifulpodcast.podbean.Podcast;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Log4j2
class Listener {

	private final RestTemplate template;

	@EventListener(ApplicationReadyEvent.class)
	public void go() {

	}

}
