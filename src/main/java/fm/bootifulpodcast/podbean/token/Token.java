package fm.bootifulpodcast.podbean.token;

import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 *
 * Representation of the token value
 *
 * @author Josh Long
 */
@Data
public class Token {

	private final String token;

	private final long expiration;

	/**
	 * Configure an instance of a token given a token and expiration
	 * @param token the token
	 * @param expiration the expiration
	 */
	public Token(String token, long expiration) {
		this.token = token;
		this.expiration = expiration;
	}

}
