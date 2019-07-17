package fm.bootifulpodcast.podbean;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Log4j2
public class SimplePodbeanClient implements PodbeanClient {

	private final RestTemplate restTemplate;

	private final RestTemplate nonAuthenticatedRestTemplate = new RestTemplateBuilder()
			.build();

	private final ParameterizedTypeReference<Map<String, Collection<Podcast>>> podcastsParameterizedTypeReference = new ParameterizedTypeReference<>() {
	};

	SimplePodbeanClient(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
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
	 * http://developers.podbean.com/podbean-api-docs/#api-File_upload
	 * http://developers.podbean.com/podbean-api-docs/#api-appendix-episode-publishing-process
	 * <p>
	 * This is a two-step process:
	 * <p>
	 * 1. permission is first saught from Podbean to upload files to an AWS S3 bucket 2.
	 * once its granted, you're expected to upload files to the relevant AWS S3 bucket
	 */
	@Override
	public UploadAuthorization upload(MediaType mediaType, File resource, long filesize) {
		var results = new ParameterizedTypeReference<UploadAuthorization>() {
		};
		var filename = Objects.requireNonNull(resource.getName());
		var uriString = UriComponentsBuilder
				.fromHttpUrl("https://api.podbean.com/v1/files/uploadAuthorize")
				.queryParam("content_type", mediaType.toString())//
				.queryParam("filename", filename)//
				.queryParam("filesize", filesize)//
				.build().toUriString();
		Assert.isTrue(resource.exists(), "the resource must point to a valid file");
		var responseEntity = this.restTemplate.exchange(uriString, HttpMethod.GET, null,
				results);
		var uploadAuthorization = responseEntity.getBody();
		log.info(uploadAuthorization);
		var presignedUrl = Objects.requireNonNull(uploadAuthorization).getPresignedUrl();
		var result = upload(presignedUrl, resource);
		Assert.isTrue(result, "the result should be " + HttpStatus.OK.value());
		return uploadAuthorization;
	}

	@SneakyThrows
	private boolean upload(String presignedUrl, File file) {
		var url = URI.create(presignedUrl);
		var request = RequestEntity.put(url)
				.contentType(MediaType.parseMediaType("audio/mpeg"))
				.body(new FileSystemResource(file));
		return this.nonAuthenticatedRestTemplate
				.exchange(url, HttpMethod.PUT, request, String.class).getStatusCode()
				.is2xxSuccessful();
	}

}
