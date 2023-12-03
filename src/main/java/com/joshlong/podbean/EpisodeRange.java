package com.joshlong.podbean;

import java.util.Collection;

/**
 * Provides a ranged batch of episodes
 * @param episodes
 * @param offset
 * @param hasMore
 * @param count
 * @param limit
 * @see PodbeanClient#getAllEpisodes()
 * @see PodbeanClient#getEpisodeRange(int, int)
 * @see PodbeanClient#getEpisodeRange(int)
 */
public record EpisodeRange(Collection<Episode> episodes, int offset, boolean hasMore, int count, int limit) {
}
