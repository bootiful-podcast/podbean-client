package fm.bootifulpodcast.podbean;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

import java.util.Collection;

/**
 * A Java client for the <a href=
 * "https://help.podbean.com/support/solutions/articles/25000008051-publishing-a-new-podcast-episode-via-podbean-api">
 * Podbean publication API</a>.
 *
 * @author Josh Long
 */
public interface PodbeanClient {

	Collection<Podcast> getAllPodcasts();

	void uploadFile(MediaType mediaType, Resource resource, long filesize);

}
