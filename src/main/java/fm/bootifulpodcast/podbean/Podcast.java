package fm.bootifulpodcast.podbean;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;

@Data
public class Podcast {

	private final String id, title, desc, logo, website, categoryName;

	@JsonProperty("allow_episode_type")
	private final Collection<String> allowEpisodeType = new ArrayList<>();

	@JsonCreator
	public Podcast(@JsonProperty("id") String id, //
			@JsonProperty("title") String title, //
			@JsonProperty("desc") String desc, //
			@JsonProperty("logo") String logo, //
			@JsonProperty("website") String website, //
			@JsonProperty("category_name") String categoryName//
	) {
		this.id = id;
		this.title = title;
		this.desc = desc;
		this.logo = logo;
		this.website = website;
		this.categoryName = categoryName;
	}

}
