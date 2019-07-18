package fm.bootifulpodcast.podbean;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.util.Assert;

import java.net.URI;
import java.util.Date;
import java.util.Set;

@Data
public class Episode {

	@JsonIgnore
	private final Set<String> validTypes = Set.of("publish", "draft");

	private final String podcastId, id, title, content, logo, status, type;

	private final URI mediaUrl, permalinkUrl, playerUrl;

	private final Date publishTime;

	private final int duration;

	@JsonProperty
	private String object;

	@JsonCreator
	public Episode(@JsonProperty("podcast_id") String podcastId, //
			@JsonProperty("id") String id, //
			@JsonProperty("title") String title, //
			@JsonProperty("content") String content, //
			@JsonProperty("logo") String logo, //
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
		this.logo = logo;
		this.status = status;
		this.type = type;
		this.playerUrl = playerUrl;
		this.mediaUrl = mediaUrl;
		this.permalinkUrl = permalinkUrl;
		this.duration = duration;

		Assert.notNull(this.status, "the status must not be null");
		Assert.isTrue(this.status.contains(this.status.toLowerCase()), "");

	}

}
