package com.sentinela.alpr.watchlist.api;

import com.sentinela.alpr.watchlist.domain.WatchReason;

import jakarta.validation.constraints.NotNull;

public record WatchlistUpdateRequest(

		@NotNull WatchReason reason) {
}
