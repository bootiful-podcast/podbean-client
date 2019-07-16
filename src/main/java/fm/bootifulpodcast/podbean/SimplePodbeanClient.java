package fm.bootifulpodcast.podbean;

import fm.bootifulpodcast.podbean.token.TokenProvider;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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
				.queryParam("content_type", mediaType.toString())
				.queryParam("filename", filename).queryParam("filesize", filesize).build()
				.toUriString();
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

	private final OkHttpClient client = new OkHttpClient.Builder().build();

	@SneakyThrows
	private boolean upload(String presignedUrl, File file) {
		// todo figure out how to make this work with the RestTemplate and not just OkHttp
		var fileBody = RequestBody.create(null, file);
		var request = new Request.Builder().url(presignedUrl).method("PUT", fileBody)
				.addHeader("Content-Type", "audio/mpeg") // use your Content-Type
				.build();

		Response response = client.newCall(request).execute();
		return response.code() == 200;

	}

	@Deprecated
	private boolean uploadWithRestTemplate(String url, Resource resource) {
		log.info("the presigned_url is " + url);
		MultiValueMap<String, String> params = UriComponentsBuilder.fromUriString(url)
				.build().getQueryParams();
		URI uri = URI.create(url);
		log.info(uri.toASCIIString());
		log.info(params.toSingleValueMap());

		var headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		var body = new LinkedMultiValueMap<String, Object>();
		body.add("file", resource);
		var requestEntity = new HttpEntity<MultiValueMap<String, Object>>(body, headers);
		var response = this.nonAuthenticatedRestTemplate.exchange(url, HttpMethod.PUT,
				requestEntity, String.class);

		return response.getStatusCode().is2xxSuccessful();
	}

}
