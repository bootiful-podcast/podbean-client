package com.joshlong.podbean;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Represents an upload authorization
 *
 * @author Josh Long
 */
@Data
public class UploadAuthorization {

	private final int expireAt;

	private final String fileKey;

	private final String presignedUrl;

	/**
	 * the main constructor
	 * @param expireAt when the authorization expires
	 * @param fileKey the pre-agreed key of the thing we're uploading
	 * @param presignedUrl the pre-signed URL for the upload authorization
	 */
	@JsonCreator
	public UploadAuthorization(@JsonProperty("expire_at") int expireAt, @JsonProperty("file_key") String fileKey,
			@JsonProperty("presigned_url") String presignedUrl) {
		this.expireAt = expireAt;
		this.fileKey = fileKey;
		this.presignedUrl = presignedUrl;
	}

}
