package fm.bootifulpodcast.podbean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;



// {
// "podcasts":[{"id":"o6DLxaF0purw","title":"The starbuxman's Podcast","desc":"New podcast weblog",
// 		"logo":"https:\/\/pbcdn1.podbean.com\/imglogo\/image-logo\/5518947\/photo.jpg","website":"https:\/\/starbuxman.podbean.com",
// 		"category_name":"","allow_episode_type":["public"],"object":"Podcast"}]}


@Data
@RequiredArgsConstructor
public class Podcast {

	private final String id, title, desc, logo, website;

	@JsonProperty("category_name")
	private final String categoryName;

	@JsonProperty("allow_episode_type")
	private final Collection<String> allowEpisodeType = new ArrayList<>();

}
