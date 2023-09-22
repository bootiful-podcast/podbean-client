package fm.bootifulpodcast.podbean;

import org.springframework.http.MediaType;

import java.io.File;
import java.util.Collection;

/**
 * A Java client for the <a href=
 * "https://help.podbean.com/support/solutions/articles/25000008051-publishing-a-new-podcast-episode-via-podbean-api">
 * Podbean podcast API</a>. You can use the Podbean API to publish podcast episodes, to
 * retrieve information about the episodes, and to edit the episodes.
 *
 * @author Josh Long
 */
public interface PodbeanClient {

	/**
	 * get all the podcasts
	 * @return a collection of {@link Podcast}
	 */
	Collection<Podcast> getAllPodcasts();

	/**
	 * Upload new media and get a {@link UploadAuthorization} which you can then use to
	 * reference when publishing a {@link Podcast}
	 * @param mediaType the media type
	 * @param file the file
	 * @return a {@link UploadAuthorization}
	 */
	UploadAuthorization upload(MediaType mediaType, File file);

	/**
	 * Upload new media and get a {@link UploadAuthorization} which you can then use to
	 * reference when publishing a {@link Podcast}
	 * @param mediaType the media type
	 * @param resource the file
	 * @param filesize the file size
	 * @return a {@link UploadAuthorization}
	 */
	UploadAuthorization upload(MediaType mediaType, File resource, long filesize);

	/**
	 * Updates a given {@link Episode}
	 * @param episodeId the episode ID
	 * @param title the title
	 * @param content the content
	 * @param status the status
	 * @param type the type
	 * @param media the media
	 * @param logo the logo
	 * @return a new {@link Episode}
	 */
	Episode updateEpisode(String episodeId, String title, String content, EpisodeStatus status, EpisodeType type,
			String media, String logo);

	/**
	 * publish an episode
	 * @param title the title
	 * @param content the content
	 * @param status the status
	 * @param type the type
	 * @param mediaKey the media key
	 * @param logoKey the logo key
	 * @return a {@link Episode}
	 */
	Episode publishEpisode(String title, String content, EpisodeStatus status, EpisodeType type, String mediaKey,
			String logoKey);

	/**
	 * returns a collection of {@link Episode } given paging dimensions
	 * @param offset the offset
	 * @param limit the limit
	 * @return a collection of satisfactory {@link Episode}
	 */
	Collection<Episode> getEpisodes(int offset, int limit);

	/**
	 * get all episodes
	 * @return returns all episodes
	 */
	Collection<Episode> getEpisodes();

}
