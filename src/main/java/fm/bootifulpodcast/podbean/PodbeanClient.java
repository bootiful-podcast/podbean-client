package fm.bootifulpodcast.podbean;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

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

	// curl https://api.podbean.com/v1/files/uploadAuthorize -G -d
	// 'access_token={access_token}' -d 'filename=abc.mp3' -d 'filesize=1291021' -d
	// 'content_type=audio/mpeg'

	void uploadFile(MediaType mediaType, MultipartFile file);

}
