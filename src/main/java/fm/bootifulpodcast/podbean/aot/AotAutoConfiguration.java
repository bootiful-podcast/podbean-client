package fm.bootifulpodcast.podbean.aot;

import fm.bootifulpodcast.podbean.*;
import fm.bootifulpodcast.podbean.token.Token;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

import java.util.Set;

/**
 * Provides GraalVM native image support.
 *
 * @author Josh Long
 */
@Configuration
@ImportRuntimeHints(AotAutoConfiguration.PodbeanRuntimeHintsRegistrar.class)
class AotAutoConfiguration {

	static class PodbeanRuntimeHintsRegistrar implements RuntimeHintsRegistrar {

		@Override
		public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
			Set.of(Episode.class, EpisodeStatus.class, EpisodeType.class, Podcast.class, UploadAuthorization.class,
					Token.class).forEach(clzz -> hints.reflection().registerType(clzz, MemberCategory.values()));
		}

	}

}
