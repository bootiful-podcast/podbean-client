package fm.bootifulpodcast.integration;

import fm.bootifulpodcast.podbean.PodbeanClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
class Listener {

	private final PodbeanClient client;

	@EventListener(ApplicationReadyEvent.class)
	public void go() {
		log.info("going..");
		this.client.getAllPodcasts().forEach(log::info);
	}

}
