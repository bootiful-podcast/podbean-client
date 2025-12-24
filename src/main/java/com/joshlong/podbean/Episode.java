package com.joshlong.podbean;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.util.Date;

/**
 *
 * Represents a given episode
 *
 * @author Josh Long
 */
public class Episode {

	private final String podcastId, id, title, content, status, type;

	private final URI mediaUrl, permalinkUrl, playerUrl, logoUrl;

	private final Date publishTime;

	private final int duration;

	@JsonProperty
	private String object;

	@Override
	public String toString() {
		return "Episode{" + "podcastId='" + podcastId + '\'' + ", id='" + id + '\'' + ", title='" + title + '\''
				+ ", content='" + content + '\'' + ", status='" + status + '\'' + ", type='" + type + '\''
				+ ", mediaUrl=" + mediaUrl + ", permalinkUrl=" + permalinkUrl + ", playerUrl=" + playerUrl
				+ ", logoUrl=" + logoUrl + ", publishTime=" + publishTime + ", duration=" + duration + ", object='"
				+ object + '\'' + '}';
	}

	public String getPodcastId() {
		return podcastId;
	}

	public String getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getContent() {
		return content;
	}

	public String getStatus() {
		return status;
	}

	public String getType() {
		return type;
	}

	public URI getMediaUrl() {
		return mediaUrl;
	}

	public URI getPermalinkUrl() {
		return permalinkUrl;
	}

	public URI getPlayerUrl() {
		return playerUrl;
	}

	public URI getLogoUrl() {
		return logoUrl;
	}

	public Date getPublishTime() {
		return publishTime;
	}

	public int getDuration() {
		return duration;
	}

	public String getObject() {
		return object;
	}

	public void setObject(String object) {
		this.object = object;
	}

	/**
	 * constructs a new {@link Episode} from attributes gleemed in the JSON
	 * @param podcastId the podcast ID
	 * @param id an arbitrary ID
	 * @param title the title
	 * @param content the content
	 * @param logo the logo
	 * @param status the status
	 * @param type the type
	 * @param mediaUrl the URL for the media
	 * @param permalinkUrl the URL for the permalink
	 * @param playerUrl the player URL
	 * @param publishTime the time of publication
	 * @param duration the duration
	 */
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
			@JsonProperty("publish_time") Date publishTime, @JsonProperty("duration") int duration) {
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
