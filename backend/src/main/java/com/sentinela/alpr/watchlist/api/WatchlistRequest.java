package com.sentinela.alpr.watchlist.api;

import com.sentinela.alpr.watchlist.domain.WatchReason;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WatchlistRequest(

		@NotBlank String plate,

		@NotNull WatchReason reason,

		Boolean active) {
}
