package com.joshlong.podbean;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.io.File;
import java.net.URI;
import java.util.*;

/**
 * default implementation of the {@link PodbeanClient}
 *
 * @author Josh Long
 */
@Slf4j
public class SimplePodbeanClient implements PodbeanClient {

	private final RestTemplate authenticatedRestTemplate;

	private final RestTemplate restTemplate = new RestTemplateBuilder().build();

	private final JsonMapper objectMapper;

	private final String episodeUri = "https://api.podbean.com/v1/episodes".trim();

	private final ParameterizedTypeReference<Map<String, Collection<Podcast>>> getAllPodcastsTypeReference = new ParameterizedTypeReference<>() {
	};

	/**
	 * construct a valid {@link PodbeanClient }
	 * @param authenticatedRestTemplate a correctly configured {@link RestTemplate}
	 * @param jsonMapper an {@link JsonMapper} from Jackson
	 */

	public SimplePodbeanClient(RestTemplate authenticatedRestTemplate, JsonMapper jsonMapper) {
		this.authenticatedRestTemplate = authenticatedRestTemplate;
		this.objectMapper = jsonMapper;
	}

	@Override
	public Collection<Podcast> getAllPodcasts() {
		var responseEntity = this.authenticatedRestTemplate.exchange("https://api.podbean.com/v1/podcasts",
				HttpMethod.GET, null, this.getAllPodcastsTypeReference);
		Assert.isTrue(responseEntity.getStatusCode().is2xxSuccessful(), "the result must be an HTTP 200-series");
		var entityBody = Objects.requireNonNull(responseEntity.getBody());
		return entityBody.getOrDefault("podcasts", Collections.emptyList());
	}

	@Override
	public UploadAuthorization upload(MediaType mt, File f) {
		return this.upload(mt, f, f.length());
	}

	@Override
	public UploadAuthorization upload(MediaType mediaType, File resource, long filesize) {
		log.debug("the resource is " + resource.getAbsolutePath() + " and the file size is " + filesize);
		var results = new ParameterizedTypeReference<UploadAuthorization>() {
		};
		var filename = Objects.requireNonNull(resource.getName());
		var uriString = UriComponentsBuilder.fromUri(URI.create("https://api.podbean.com/v1/files/uploadAuthorize\""))
			.queryParam("content_type", mediaType.toString())//
			.queryParam("filename", filename)//
			.queryParam("filesize", resource.length())//
			.build()//
			.toUriString();
		Assert.isTrue(resource.exists(), "the resource must point to a valid file");
		var responseEntity = this.authenticatedRestTemplate.exchange(uriString, HttpMethod.GET, null, results);
		var uploadAuthorization = responseEntity.getBody();
		Assert.notNull(uploadAuthorization, "the uploadAuthorization must not be null");
		if (log.isDebugEnabled()) {
			log.debug(uploadAuthorization.toString());
		}
		var presignedUrl = uploadAuthorization.getPresignedUrl();
		var result = this.doUploadToS3(presignedUrl, mediaType, resource);
		Assert.isTrue(result, "the result should be " + HttpStatus.OK.value());
		return uploadAuthorization;
	}

	@Override
	public Episode updateEpisode(String episodeId, String title, String content, EpisodeStatus status, EpisodeType type,
			String media, String logo) {
		return this.mutateEpisode(URI.create(this.episodeUri + "/" + episodeId), title, content, status, type, media,
				logo);
	}

	@Override
	public Episode publishEpisode(String title, String content, EpisodeStatus status, EpisodeType type, String mediaKey,
			String logoKey) {
		return this.mutateEpisode(URI.create(this.episodeUri), title, content, status, type, mediaKey, logoKey);
	}

	@Override
	public Collection<Episode> getAllEpisodes() {
		var episodes = new LinkedHashSet<Episode>();
		var offset = 0;
		var step = 100;
		while (true) {
			var er = getEpisodeRange(offset, step);
			episodes.addAll(er.episodes());
			offset += step;
			if (!er.hasMore())
				break;
		}
		return episodes;
	}

	@Override
	public EpisodeRange getEpisodeRange(int offset) {
		return this.getEpisodeRange(offset, 100);
	}

	// todo
	@Override
	@SneakyThrows
	public EpisodeRange getEpisodeRange(int offset, int limit) {

		var uriComponentsBuilder = UriComponentsBuilder.fromUriString("https://api.podbean.com/v1/episodes");
		if (offset > 0)
			uriComponentsBuilder.queryParam("offset", offset);
		if (limit > 0)
			uriComponentsBuilder.queryParam("limit", limit);
		var url = uriComponentsBuilder.build().toUriString();
		var responseEntity = this.authenticatedRestTemplate.exchange(url, HttpMethod.GET, null, String.class);
		var json = responseEntity.getBody();
		var jsonNode = this.objectMapper.readTree(json);
		var episodesNode = jsonNode.get("episodes");
		var episodes = objectMapper.readValue(episodesNode.toString(), new TypeReference<Collection<Episode>>() {
		});
		var countField = jsonNode.get("count").asInt();
		var offsetField = jsonNode.get("offset").asInt();
		var hasMoreField = jsonNode.get("has_more").asBoolean();
		var limitField = jsonNode.get("limit").asInt();
		return new EpisodeRange(episodes, offsetField, hasMoreField, countField, limitField);

	}

	private Episode mutateEpisode(URI uri, String title, String content, EpisodeStatus status, EpisodeType type,
			String mediaKey, String logoKey) {

		var bodyMap = new LinkedMultiValueMap<String, String>();
		if (StringUtils.hasText(logoKey)) {
			bodyMap.add("logo_key", logoKey);
		}
		if (StringUtils.hasText(mediaKey)) {
			bodyMap.add("media_key", mediaKey);
		}
		Map.of(//
				"title", title, //
				"content", content, //
				"status", status.name().toLowerCase(), //
				"type", type.name().toLowerCase() //
		)//
			.forEach(bodyMap::add);
		try {
			var result = this.authenticatedRestTemplate.postForObject(uri, bodyMap, String.class);
			log.info(result);
			Map<String, Episode> readValue = this.objectMapper.readValue(result, new TypeReference<>() {
			});
			return readValue.get("episode");
		}
		catch (Exception e) {
			if (e instanceof HttpClientErrorException.BadRequest) {
				var msg = ((HttpClientErrorException.BadRequest) e).getResponseBodyAsString();
				log.error(msg);
			}
			ReflectionUtils.rethrowRuntimeException(e);
		}
		return null;
	}

	@Override
	@Deprecated
	@SneakyThrows
	public Collection<Episode> getEpisodes(int offset, int limit) {
		var uriComponentsBuilder = UriComponentsBuilder.fromUriString("https://api.podbean.com/v1/episodes");
		if (offset > 0)
			uriComponentsBuilder.queryParam("offset", offset);
		if (limit > 0)
			uriComponentsBuilder.queryParam("limit", limit);
		var url = uriComponentsBuilder.build().toUriString();
		var responseEntity = this.authenticatedRestTemplate.exchange(url, HttpMethod.GET, null, String.class);
		var json = responseEntity.getBody();
		var jsonNode = this.objectMapper.readTree(json);
		var episodes = jsonNode.get("episodes");
		return objectMapper.readValue(episodes.toString(), new TypeReference<>() {
		});
	}

	@Override
	@Deprecated
	public Collection<Episode> getEpisodes() {
		return getEpisodes(0, 0);
	}

	@SneakyThrows
	private boolean doUploadToS3(String presignedUrl, MediaType mt, File file) {
		var url = URI.create(presignedUrl);
		var request = RequestEntity.put(url).contentType(mt).body(new FileSystemResource(file));
		return this.restTemplate.exchange(url, HttpMethod.PUT, request, String.class).getStatusCode().is2xxSuccessful();
	}

}
