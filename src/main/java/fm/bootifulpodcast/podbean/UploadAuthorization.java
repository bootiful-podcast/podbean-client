package fm.bootifulpodcast.podbean;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UploadAuthorization {

	private final int expireAt;

	private final String fileKey;

	private final String presignedUrl;

	@JsonCreator
	public UploadAuthorization(@JsonProperty("expire_at") int expireAt,
			@JsonProperty("file_key") String fileKey,
			@JsonProperty("presigned_url") String presignedUrl) {
		this.expireAt = expireAt;
		this.fileKey = fileKey;
		this.presignedUrl = presignedUrl;
	}

}
