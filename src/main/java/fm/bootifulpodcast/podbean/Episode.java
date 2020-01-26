package fm.bootifulpodcast.podbean;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.net.URI;
import java.util.Date;

@Data
public class Episode {

	private final String podcastId, id, title, content, status, type;

	private final URI mediaUrl, permalinkUrl, playerUrl, logoUrl;

	private final Date publishTime;

	private final int duration;

	@JsonProperty
	private String object;

	@JsonCreator
	public Episode(@JsonProperty("podcast_id") String podcastId, //
			@JsonProperty("id") String id, //
			@JsonProperty("title") String title, //
			@JsonProperty("content") String content, //
			@JsonProperty("logo") URI logo, //
			@JsonProperty("status") String status, //
			@JsonProperty("type") String type, //
			@JsonProperty("media_url") URI mediaUrl, //
			@JsonProperty("permalink_url") URI permalinkUrl, //
			@JsonProperty("player_url") URI playerUrl, //
			@JsonProperty("publish_time") Date publishTime,
			@JsonProperty("duration") int duration) {
		this.id = id;
		this.podcastId = podcastId;
		this.title = title;
		this.content = content;
		this.publishTime = publishTime;
		this.logoUrl = logo;
		this.playerUrl = playerUrl;
		this.mediaUrl = mediaUrl;
		this.permalinkUrl = permalinkUrl;
		this.duration = duration;
		this.type = this.resolveTypeGiven(type);
		this.status = this.resolveStatusGiven(status);
	}

	private String resolveTypeGiven(String type) {
		String resolvedType;
		try {
			resolvedType = EpisodeType.valueOf(type.toUpperCase()).name();
		}
		catch (IllegalArgumentException | NullPointerException e) {
			resolvedType = EpisodeType.PUBLIC.name();
		}
		return resolvedType;
	}

	private String resolveStatusGiven(String status) {
		String resolvedStatus;
		try {
			resolvedStatus = EpisodeStatus.valueOf(status.toUpperCase()).name();
		}
		catch (IllegalArgumentException | NullPointerException e) {
			resolvedStatus = EpisodeStatus.DRAFT.name();
		}
		return resolvedStatus;
	}

}
