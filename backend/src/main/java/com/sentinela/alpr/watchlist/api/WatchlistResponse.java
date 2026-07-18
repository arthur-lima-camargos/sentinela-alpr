package com.sentinela.alpr.watchlist.api;

import java.time.Instant;

import com.sentinela.alpr.watchlist.domain.WatchReason;

public record WatchlistResponse(
		Long id,
		String plate,
		WatchReason reason,
		boolean active,
		Instant createdAt) {
}
