package fm.bootifulpodcast.podbean;

import com.fasterxml.jackson.databind.ObjectMapper;
import fm.bootifulpodcast.podbean.token.TokenInterceptor;
import fm.bootifulpodcast.podbean.token.TokenProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@AutoConfigureAfter(RestTemplateAutoConfiguration.class)
public class PodbeanAutoConfiguration {

	private static final String AUTHENTICATED = "authenticated";

	@Bean
	RestTemplate restTemplate() {
		return new RestTemplateBuilder().build();
	}

	@Bean
	@Qualifier(AUTHENTICATED)
	RestTemplate restTemplate(RestTemplateBuilder builder,
			TokenInterceptor tokenInterceptor) {
		return builder.interceptors(tokenInterceptor).build();
	}

	@Bean
	@ConditionalOnMissingBean
	TokenInterceptor tokenInterceptor(TokenProvider tokenProvider) {
		return new TokenInterceptor(tokenProvider);
	}

	@Bean
	@ConditionalOnMissingBean
	TokenProvider tokenProvider(@Value("${podbean.client-id}") String clientId,
			@Value("${podbean.client-secret}") String clientSecret) {
		return new TokenProvider(clientId, clientSecret);
	}

	@Bean
	@ConditionalOnMissingBean
	PodbeanClient podbeanClient(@Qualifier(AUTHENTICATED) RestTemplate template,
			ObjectMapper om) {
		return new SimplePodbeanClient(template, om);
	}

}
