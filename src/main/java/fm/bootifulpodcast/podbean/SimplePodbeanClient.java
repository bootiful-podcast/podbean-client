package fm.bootifulpodcast.podbean;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fm.bootifulpodcast.podbean.token.TokenProvider;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
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

	private final TokenProvider tokenProvider;

	private final ObjectMapper objectMapper;

	private final ParameterizedTypeReference<Map<String, Collection<Podcast>>> getAllPodcastsTypeReference = new ParameterizedTypeReference<>() {
	};

	SimplePodbeanClient(RestTemplate authenticatedRestTemplate, TokenProvider provider,
			ObjectMapper objectMapper) {
		this.authenticatedRestTemplate = authenticatedRestTemplate;
		this.objectMapper = objectMapper;
		this.tokenProvider = provider;
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
				.build()//
				.toUriString();
		Assert.isTrue(resource.exists(), "the resource must point to a valid file");
		var responseEntity = this.authenticatedRestTemplate.exchange(uriString,
				HttpMethod.GET, null, results);
		var uploadAuthorization = responseEntity.getBody();
		log.info(uploadAuthorization);
		var presignedUrl = Objects.requireNonNull(uploadAuthorization).getPresignedUrl();
		var result = this.doUploadToS3(presignedUrl, mediaType, resource);
		Assert.isTrue(result, "the result should be " + HttpStatus.OK.value());
		return uploadAuthorization;
	}

	/*
	 * @Override public Episode publishEpisode(String title, String content, EpisodeStatus
	 * status, EpisodeType type, String mediaKey, String logoKey) { var uri =
	 * URI.create(" https://api.podbean.com/v1/episodes ".trim()); var bodyMap = new
	 * HashMap<String, String>(); if (logoKey != null) { bodyMap.put("logo_key", logoKey);
	 * } bodyMap.putAll(Map.of(// "title", title,// "content", content,// "status",
	 * status.name().toLowerCase(),// "type", type.name().toLowerCase()// // "media_key",
	 * mediaKey )); try { MultiValueMap<String, String> map = new
	 * LinkedMultiValueMap<String, String>(); for (var k : bodyMap.keySet()) { map.add(k,
	 * bodyMap.get(k)); } HttpEntity<MultiValueMap<String, String>> request = new
	 * HttpEntity<>(map); ResponseEntity<String> responseEntity =
	 * this.authenticatedRestTemplate .exchange(uri, HttpMethod.POST, request,
	 * String.class); log.info("json: " + responseEntity.getBody()); } catch (Exception e)
	 * { log.error("", e); } return null; }
	 */

	/**
	 * TODO we need to figure out how to encode th
	 * @param title
	 * @param content
	 * @param status
	 * @param type
	 * @param mediaKey
	 * @param logoKey
	 * @return
	 */
	@Override
	public Episode publishEpisode(String title, String content, EpisodeStatus status,
			EpisodeType type, String mediaKey, String logoKey) {
		var uri = URI.create(" https://api.podbean.com/v1/episodes ".trim());
		var bodyMap = new LinkedMultiValueMap<String, String>();
		if (StringUtils.hasText(logoKey)) {
			bodyMap.add("logo_key", logoKey);
		}
		if (StringUtils.hasText(mediaKey)) {
			bodyMap.add("media_key", mediaKey);
		}
		Map.of(//
				// "access_token", this.tokenProvider.getToken().getToken(),
				"title", title, //
				"content", content, //
				"status", status.name().toLowerCase(), //
				"type", type.name().toLowerCase() //
		).forEach(bodyMap::add);
		try {
			var result = authenticatedRestTemplate.postForObject(uri, bodyMap,
					String.class);
			log.info(result);
		}
		catch (Exception e) {
			if (e instanceof HttpClientErrorException.BadRequest) {
				var msg = ((HttpClientErrorException.BadRequest) e)
						.getResponseBodyAsString();
				log.error(msg);
			}
			log.error("", e);
		}
		return null;
	}

	@Override
	@SneakyThrows
	public Collection<Episode> getEpisodes(int offset, int limit) {
		var uriComponentsBuilder = UriComponentsBuilder
				.fromHttpUrl("https://api.podbean.com/v1/episodes");
		if (offset > 0)
			uriComponentsBuilder.queryParam("offset", offset);
		if (limit > 0)
			uriComponentsBuilder.queryParam("limit", limit);
		var url = uriComponentsBuilder.build().toUriString();
		var responseEntity = this.authenticatedRestTemplate.exchange(url, HttpMethod.GET,
				null, String.class);
		var json = responseEntity.getBody();
		var jsonNode = this.objectMapper.readTree(json);
		JsonNode episodes = jsonNode.get("episodes");
		return objectMapper.readValue(episodes.toString(),
				new TypeReference<Collection<Episode>>() {
				});
	}

	@Override
	public Collection<Episode> getEpisodes() {
		return getEpisodes(0, 0);
	}

	@SneakyThrows
	private boolean doUploadToS3(String presignedUrl, MediaType mt, File file) {
		var url = URI.create(presignedUrl);
		var request = RequestEntity.put(url).contentType(mt)
				.body(new FileSystemResource(file));
		return this.restTemplate.exchange(url, HttpMethod.PUT, request, String.class)
				.getStatusCode().is2xxSuccessful();
	}

}
