package fm.bootifulpodcast.podbean;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
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

	private final RestTemplate authenticatedRestTemplate;

	private final RestTemplate restTemplate = new RestTemplateBuilder().build();

	private final ParameterizedTypeReference<Map<String, Collection<Podcast>>> getAllPodcastsTypeReference = new ParameterizedTypeReference<>() {
	};

	SimplePodbeanClient(RestTemplate authenticatedRestTemplate) {
		this.authenticatedRestTemplate = authenticatedRestTemplate;
	}

	@Override
	public Collection<Podcast> getAllPodcasts() {
		var responseEntity = this.authenticatedRestTemplate.exchange(
				"https://api.podbean.com/v1/podcasts", HttpMethod.GET, null,
				this.getAllPodcastsTypeReference);
		Assert.isTrue(responseEntity.getStatusCode().is2xxSuccessful(),
				"the result must be an HTTP 200-series");
		var entityBody = Objects.requireNonNull(responseEntity.getBody());
		return entityBody.getOrDefault("podcasts", Collections.emptyList());
	}

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
		var responseEntity = this.authenticatedRestTemplate.exchange(uriString,
				HttpMethod.GET, null, results);
		var uploadAuthorization = responseEntity.getBody();
		log.info(uploadAuthorization);
		var presignedUrl = Objects.requireNonNull(uploadAuthorization).getPresignedUrl();
		var result = this.doUploadToS3(presignedUrl, resource);
		Assert.isTrue(result, "the result should be " + HttpStatus.OK.value());
		return uploadAuthorization;
	}

	@SneakyThrows
	private boolean doUploadToS3(String presignedUrl, File file) {
		var url = URI.create(presignedUrl);
		var request = RequestEntity.put(url)
				.contentType(MediaType.parseMediaType("audio/mpeg"))
				.body(new FileSystemResource(file));
		return this.restTemplate.exchange(url, HttpMethod.PUT, request, String.class)
				.getStatusCode().is2xxSuccessful();
	}

}
