package fm.bootifulpodcast.podbean;

import com.fasterxml.jackson.databind.ObjectMapper;
import fm.bootifulpodcast.podbean.token.TokenInterceptor;
import fm.bootifulpodcast.podbean.token.TokenProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configures the default arrangement of objects to, assuming some
 * {@link org.springframework.boot.context.properties.ConfigurationProperties properties},
 * get a working client to talk to the <a href="https://podbean.com">Podbean</a> API.
 *
 * @author Josh Long
 */
@Configuration
@EnableConfigurationProperties(PodbeanConfigurationProperties.class)
@AutoConfigureAfter(RestTemplateAutoConfiguration.class)
class PodbeanAutoConfiguration {

	private static final String AUTHENTICATED = "authenticatedPodbeanRestTemplate";

	@Bean
	RestTemplate defaultRestTemplateWithoutAuthentication() {
		return new RestTemplateBuilder().build();
	}

	@Bean
	@Qualifier(AUTHENTICATED)
	RestTemplate authenticatedPodbeanRestTemplate(RestTemplateBuilder builder, TokenInterceptor tokenInterceptor) {
		return builder.interceptors(tokenInterceptor).build();
	}

	@Bean
	@ConditionalOnMissingBean
	TokenInterceptor tokenInterceptor(TokenProvider tokenProvider) {
		return new TokenInterceptor(tokenProvider);
	}

	@Bean
	@ConditionalOnMissingBean
	TokenProvider tokenProvider(PodbeanConfigurationProperties properties) {
		return new TokenProvider(properties.clientId(), properties.clientSecret());
	}

	@Bean
	@ConditionalOnMissingBean
	PodbeanClient podbeanClient(@Qualifier(AUTHENTICATED) RestTemplate template, ObjectMapper om) {
		return new SimplePodbeanClient(template, om);
	}

}
