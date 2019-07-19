package fm.bootifulpodcast.podbean;

import org.springframework.http.MediaType;

import java.io.File;
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

	UploadAuthorization upload(MediaType mediaType, File resource, long filesize);

	Episode publishEpisode(String title, String content, EpisodeStatus status,
			EpisodeType type, String mediaKey, String logoKey);

	Collection<Episode> getEpisodes(int offset, int limit);

	Collection<Episode> getEpisodes();

}
