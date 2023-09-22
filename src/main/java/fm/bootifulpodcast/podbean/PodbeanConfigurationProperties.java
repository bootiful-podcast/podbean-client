package fm.bootifulpodcast.podbean;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Josh Long
 */
@ConfigurationProperties(prefix = "podbean")
record PodbeanConfigurationProperties(String clientId, String clientSecret) {
}
