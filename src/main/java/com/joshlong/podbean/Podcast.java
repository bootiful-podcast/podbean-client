package com.joshlong.podbean;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A representation of the Podcast, the thing to which we're contributing new
 * {@link Episode episodes}
 *
 * @author Josh Long
 */
public class Podcast {

	public String getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getDesc() {
		return desc;
	}

	public String getLogo() {
		return logo;
	}

	public String getWebsite() {
		return website;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public Collection<String> getAllowEpisodeType() {
		return allowEpisodeType;
	}

	private final String id, title, desc, logo, website, categoryName;

	@JsonProperty("allow_episode_type")
	private final Collection<String> allowEpisodeType = new ArrayList<>();

	/**
	 * create a new {@link Podcast } given attributes from a JSON structure
	 * @param id the Podcast ID
	 * @param title the title
	 * @param desc the description
	 * @param logo the logo
	 * @param website the website
	 * @param categoryName the category name
	 */
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
