package fm.bootifulpodcast.podbean.token;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Token {

	private final String token;

	private final long expiration;

}
