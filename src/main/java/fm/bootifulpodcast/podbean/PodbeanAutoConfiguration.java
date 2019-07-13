package fm.bootifulpodcast.podbean;

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

	@Bean
	@ConditionalOnMissingBean
	RestTemplate restTemplate(RestTemplateBuilder builder, TokenInterceptor tokenInterceptor) {
		return builder.interceptors(tokenInterceptor).build();
	}

	@Bean
	@ConditionalOnMissingBean
	TokenInterceptor tokenInterceptor(@Value("${podbean.client-id}") String clientId, @Value("${podbean.client-secret}") String clientSecret) {
		return new TokenInterceptor(clientId, clientSecret);
	}

	@Bean
	@ConditionalOnMissingBean
	SimplePodbeanClient podbeanClient(RestTemplate template) {
		return new SimplePodbeanClient(template);
	}
}