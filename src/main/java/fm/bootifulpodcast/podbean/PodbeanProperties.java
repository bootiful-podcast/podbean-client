package fm.bootifulpodcast.podbean;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Josh Long
 */
@ConfigurationProperties(prefix = "podbean")
record PodbeanProperties(String clientId, String clientSecret) {
}
