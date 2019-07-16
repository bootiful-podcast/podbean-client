package fm.bootifulpodcast.podbean;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import fm.bootifulpodcast.podbean.token.TokenProvider;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Log4j2
public class SimplePodbeanClient implements PodbeanClient {

	private final RestTemplate restTemplate;

	private final TokenProvider tokenProvider;

	private final ParameterizedTypeReference<Map<String, Collection<Podcast>>> podcastsParameterizedTypeReference = new ParameterizedTypeReference<>() {
	};

	SimplePodbeanClient(TokenProvider tokenProvider, RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
		this.tokenProvider = tokenProvider;
	}

	@Override
	public Collection<Podcast> getAllPodcasts() {
		var responseEntity = this.restTemplate.exchange(
				"https://api.podbean.com/v1/podcasts", HttpMethod.GET, null,
				this.podcastsParameterizedTypeReference);
		Assert.isTrue(responseEntity.getStatusCode().is2xxSuccessful(),
				"the result must be an HTTP 200-series");
		var entityBody = Objects.requireNonNull(responseEntity.getBody());
		return entityBody.getOrDefault("podcasts", Collections.emptyList());
	}

	/**
	 * //http://developers.podbean.com/podbean-api-docs/#api-File_upload
	 * <p>
	 * This is a two-step process:
	 * <p>
	 * 1. permission is first saught from Podbean to upload files to an AWS S3 bucket 2.
	 * once its granted, you're expected to upload files to the relevant AWS S3 bucket
	 */
	@Override
	public UploadAuthorization getUploadAuthorization(MediaType mediaType,
			Resource resource, long filesize) {
		var results = new ParameterizedTypeReference<UploadAuthorization>() {
		};
		var filename = Objects.requireNonNull(resource.getFilename());
		// var url = "https://api.podbean.com/v1/files/uploadAuthorize?content_type=" +
		// mediaType.toString() + "&filename=" + filename + "&filesize=" + filesize;
		var uriString = UriComponentsBuilder
				.fromHttpUrl("https://api.podbean.com/v1/files/uploadAuthorize")
				.queryParam("content_type", mediaType.toString())
				.queryParam("filename", filename).queryParam("filesize", filesize).build()
				.toUriString();
		Assert.isTrue(resource.exists(), "the resource must point to a valid file");
		var responseEntity = this.restTemplate.exchange(uriString, HttpMethod.GET, null,
				results);
		return responseEntity.getBody();
	}

}
